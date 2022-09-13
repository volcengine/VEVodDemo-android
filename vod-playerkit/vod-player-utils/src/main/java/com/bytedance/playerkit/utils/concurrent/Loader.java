/*
 * Copyright (C) 2021 bytedance
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.utils.concurrent;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.bytedance.playerkit.utils.Asserts;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;


public final class Loader implements Cancelable {

    private static final int STATE_IDLE = 0;
    private static final int STATE_LOADING = 1;
    private static final int STATE_CANCELED = 3;

    private final List<LoadTask<?>> mEnqueuedTasks = new LinkedList<>();

    private final Looper mLooper;
    private ThreadPoolExecutor mExecutor;

    private int mState;
    private boolean mCanceling;

    public interface Loadable extends Cancelable {
        void load(ProgressNotifier notifier) throws IOException, InterruptedException;
    }

    public interface ProgressNotifier {
        void notifyProgressChanged(float progress);
    }

    public interface Callback<T extends Loadable> {
        void onLoadStart(T loadable);

        void onLoadProgressChanged(T loadable, float progress);

        void onLoadComplete(T loadable);

        void onLoadCanceled(T loadable, String reason);

        void onLoadError(T loadable, IOException e);
    }

    public static class CallbackAdapter<T extends Loadable> implements Callback<T> {

        @Override
        public void onLoadStart(T loadable) {
        }

        @Override
        public void onLoadProgressChanged(T loadable, float progress) {
        }

        @Override
        public void onLoadComplete(T loadable) {
        }

        @Override
        public void onLoadCanceled(T loadable, String reason) {
        }

        @Override
        public void onLoadError(T loadable, IOException e) {
        }
    }

    private interface OnLoadTaskListener<T extends Loadable> {
        void onTaskStart(LoadTask<T> task);

        void onTaskFinish(LoadTask<T> task);
    }

    public Loader(Looper looper, ThreadPoolExecutor executor) {
        this.mLooper = looper;
        this.mExecutor = executor;
    }

    public final <T extends Loadable> void startLoad(final T loadable, final Callback<T> callback) {
        Asserts.checkThread(mLooper);
        Asserts.checkState(mState, STATE_IDLE, STATE_LOADING);
        Asserts.checkState(!mCanceling, "can't enqueue while canceling!");

        new LoadTask<>(mLooper, mExecutor, loadable, callback, new OnLoadTaskListener<T>() {
            @Override
            public void onTaskStart(LoadTask<T> task) {
                Asserts.checkThread(mLooper);
                mEnqueuedTasks.add(task);
                syncState();
            }

            @Override
            public void onTaskFinish(LoadTask<T> task) {
                Asserts.checkThread(mLooper);
                Asserts.checkState(task.isDone());
                mEnqueuedTasks.remove(task);
                syncState();
            }

            private void syncState() {
                if (mEnqueuedTasks.isEmpty()) {
                    if (mCanceling) {
                        mState = STATE_CANCELED;
                        finish();
                    } else {
                        mState = STATE_IDLE;
                    }
                } else if (!mCanceling) {
                    mState = STATE_LOADING;
                }
            }
        }).start();
    }

    public Looper looper() {
        Asserts.checkThread(mLooper);
        return mLooper;
    }

    public final boolean isIDLE() {
        Asserts.checkThread(mLooper);
        return mState == STATE_IDLE;
    }

    public final boolean isLoading() {
        Asserts.checkThread(mLooper);
        return mState == STATE_LOADING;
    }

    public boolean isFree() {
        Asserts.checkThread(mLooper);
        if (mState == STATE_IDLE || mState == STATE_LOADING) {
            return mExecutor.getMaximumPoolSize() > mEnqueuedTasks.size();
        }
        return false;
    }

    public boolean isCanceling() {
        Asserts.checkThread(mLooper);
        return this.mCanceling;
    }

    @Override
    public final void cancel(boolean notify, boolean interrupt, String reason) {
        Asserts.checkThread(mLooper);
        if (mState == STATE_IDLE) {
            mState = STATE_CANCELED;
            finish();
        } else if (mState == STATE_LOADING && !mCanceling) {
            mCanceling = true;
            for (LoadTask<?> loadTask : mEnqueuedTasks) {
                loadTask.cancel(notify, interrupt, reason);
            }
        }
    }

    @Override
    public final boolean isCanceled() {
        return mState == STATE_CANCELED;
    }

    private void finish() {
        mExecutor = null;
    }

    private static class LoadTask<T extends Loadable> extends Handler implements Runnable, Cancelable {
        private static final int MSG_PROGRESS = 0;
        private static final int MSG_COMPLETE = 1;
        private static final int MSG_IO_EXCEPTION = 2;
        private static final int MSG_UNEXPECTED_EXCEPTION = 3;
        private static final int MSG_FATAL_ERROR = 4;
        private static final int MSG_CANCEL = 5;

        private static final int STATE_IDLE = 0;
        private static final int STATE_STARTED = 1;
        private static final int STATE_COMPLETED = 2;
        private static final int STATE_ERROR = 3;
        private static final int STATE_CANCELED = 4;

        private ExecutorService mExecutor;
        private T mLoadable;
        private Loader.Callback<T> mCallback;
        private OnLoadTaskListener<T> mListener;

        private int mState;

        private volatile Thread mExecutorThread;
        private volatile boolean mCanceling;

        private boolean mNotify = true;
        private String mCancelReason;

        /* package */ LoadTask(Looper looper, ExecutorService executor, T loadable, Loader.Callback<T> callback, OnLoadTaskListener<T> listener) {
            super(looper);
            this.mExecutor = executor;
            this.mLoadable = loadable;
            this.mCallback = callback;
            this.mListener = listener;
        }

        private void setState(int state) {
            Asserts.checkThread(getLooper());
            this.mState = state;
        }

        @Override
        public void handleMessage(Message msg) {
            Asserts.checkState(mState, STATE_IDLE, STATE_STARTED);

            if (mCanceling && mState == STATE_STARTED && msg.what != MSG_PROGRESS) {
                fireCanceledEvent();
                return;
            }

            final int what = msg.what;
            switch (what) {
                case MSG_PROGRESS:
                    fireProgressEvent((Float) msg.obj);
                    break;
                case MSG_COMPLETE:
                    fireCompleteEvent();
                    break;
                case MSG_IO_EXCEPTION:
                    fireErrorEvent((IOException) msg.obj);
                    break;
                case MSG_UNEXPECTED_EXCEPTION:
                    fireErrorEvent(new IOException((Throwable) msg.obj));
                    break;
                case MSG_FATAL_ERROR:
                    throw (Error) msg.obj;
                case MSG_CANCEL:
                    fireCanceledEvent();
                    break;
            }
        }

        private void fireStartEvent() {
            Asserts.checkThread(getLooper());
            Asserts.checkState(mState, STATE_IDLE);

            setState(STATE_STARTED);
            if (mListener != null) {
                mListener.onTaskStart(this);
            }
            if (mNotify) {
                if (mCallback != null) {
                    mCallback.onLoadStart(mLoadable);
                }
            }
        }

        private void fireProgressEvent(float progress) {
            Asserts.checkThread(getLooper());
            Asserts.checkState(mState, STATE_STARTED);

            if (mNotify) {
                if (mCallback != null) {
                    mCallback.onLoadProgressChanged(mLoadable, progress);
                }
            }
        }

        private void fireCompleteEvent() {
            Asserts.checkThread(getLooper());
            Asserts.checkState(mState, STATE_STARTED);

            setState(STATE_COMPLETED);
            final Loader.Callback<T> callback = mCallback;
            final T loadable = mLoadable;
            finish();
            if (mNotify) {
                if (callback != null) {
                    callback.onLoadComplete(loadable);
                }
            }
        }

        private void fireErrorEvent(IOException e) {
            Asserts.checkThread(getLooper());
            Asserts.checkState(mState, STATE_STARTED);

            setState(STATE_ERROR);
            final Loader.Callback<T> callback = mCallback;
            final T loadable = mLoadable;
            finish();
            if (mNotify) {
                if (callback != null) {
                    callback.onLoadError(loadable, e);
                }
            }
        }

        private void fireCanceledEvent() {
            Asserts.checkThread(getLooper());
            Asserts.checkState(mState, STATE_STARTED);

            setState(STATE_CANCELED);
            final Loader.Callback<T> callback = mCallback;
            final T loadable = mLoadable;
            finish();
            if (mNotify) {
                if (callback != null) {
                    callback.onLoadCanceled(loadable, mCancelReason);
                }
            }
        }

        private void start() {
            Asserts.checkThread(getLooper());
            Asserts.checkState(mState, STATE_IDLE);
            mExecutor.execute(this);
            fireStartEvent();
        }

        @Override
        public void cancel(boolean notify, boolean interrupt, String reason) {
            Asserts.checkThread(getLooper());
            this.mCanceling = true;
            this.mNotify = notify;
            this.mCancelReason = reason;

            if (mLoadable != null) {
                mLoadable.cancel(notify, interrupt, reason);
            }
            final Thread thread = mExecutorThread;
            if (thread != null && interrupt) {
                thread.interrupt();
            }
        }

        @Override
        public boolean isCanceled() {
            Asserts.checkThread(getLooper());
            return mState == STATE_CANCELED;
        }

        /* package */ boolean isDone() {
            Asserts.checkThread(getLooper());
            return mState == STATE_COMPLETED || mState == STATE_ERROR || mState == STATE_CANCELED;
        }

        @Override
        public void run() {
            if (mCanceling) {
                sendEmptyMessage(MSG_CANCEL);
                return;
            }

            this.mExecutorThread = Thread.currentThread();
            try {
                mLoadable.load(new ProgressNotifier() {
                    @Override
                    public void notifyProgressChanged(float progress) {
                        if (!mCanceling && mState == STATE_STARTED) {
                            removeMessages(MSG_PROGRESS);
                            obtainMessage(MSG_PROGRESS, progress).sendToTarget();
                        }
                    }
                });
                sendEmptyMessage(MSG_COMPLETE);
            } catch (IOException e) {
                if (mLoadable.isCanceled()) {
                    obtainMessage(MSG_CANCEL).sendToTarget();
                } else {
                    obtainMessage(MSG_IO_EXCEPTION, e).sendToTarget();
                }
            } catch (InterruptedException e) {
                Asserts.checkState(mCanceling);
                obtainMessage(MSG_CANCEL).sendToTarget();
            } catch (RuntimeException | OutOfMemoryError e) {
                obtainMessage(MSG_UNEXPECTED_EXCEPTION, e).sendToTarget();
            } catch (Error e) {
                obtainMessage(MSG_FATAL_ERROR, e).sendToTarget();
                throw e;
            }
        }

        private void finish() {
            Asserts.checkThread(getLooper());
            removeCallbacksAndMessages(null);
            mCallback = null;
            if (mListener != null) {
                mListener.onTaskFinish(this);
                mListener = null;
            }
            mExecutor = null;
            mLoadable = null;
            mExecutorThread = null;
        }
    }
}
