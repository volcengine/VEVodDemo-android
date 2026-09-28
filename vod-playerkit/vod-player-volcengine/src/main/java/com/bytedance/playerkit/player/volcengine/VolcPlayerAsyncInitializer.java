/*
 * Copyright (C) 2026 bytedance
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
 */

package com.bytedance.playerkit.player.volcengine;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 可选的点播 SDK 异步初始化入口。
 *
 * <p>默认同步接入只需要使用 {@link VolcPlayerInit}。选择异步初始化后，播放器创建、预加载、
 * 预渲染等根入口必须等待本类返回成功结果。
 */
public final class VolcPlayerAsyncInitializer {

    public interface InitCallback {
        int RESULT_SUCCESS = 1;
        int RESULT_ERROR = -1;

        void onInitResult(int result, @Nullable Throwable error);
    }

    public interface FailureCallback {
        void onFailure(@Nullable Throwable error);
    }

    public interface InitRequest {
        /**
         * 只取消当前等待请求，不会停止进程级 SDK 初始化，也不会影响其他等待方。
         */
        void cancel();
    }

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final List<PendingInitCallback> INIT_CALLBACKS = new ArrayList<>();

    private VolcPlayerAsyncInitializer() {
    }

    public static void start() {
        start(null);
    }

    public static void start(@Nullable InitCallback initCallback) {
        if (initCallback != null) {
            whenReady(initCallback);
        }

        VolcPlayerInitConfig config = VolcPlayerInit.beginInitialization("initAsync");
        if (config == null) {
            return;
        }
        if (config.workerHandler.getLooper() == Looper.getMainLooper()) {
            VolcPlayerInit.failInitialization(
                    "initAsync",
                    new IllegalArgumentException(
                            "Async init workerHandler must not use main looper."));
            return;
        }

        final long startTime = System.currentTimeMillis();
        final Boolean isMessageQueueIdle = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? config.workerHandler.getLooper().getQueue().isIdle()
                : null;
        L.d(VolcPlayerAsyncInitializer.class, "start",
                config.workerHandler.getLooper().getThread(),
                "isMessageQueueIdle:" + isMessageQueueIdle,
                startTime);

        if (!config.workerHandler.post(() -> {
            L.d(VolcPlayerAsyncInitializer.class, "start", "running",
                    "time:" + (System.currentTimeMillis() - startTime));
            VolcPlayerInit.runInitialization("initAsync", config, false);
        })) {
            VolcPlayerInit.failInitialization(
                    "initAsync",
                    new IllegalStateException(
                            "Async init worker looper is not accepting tasks."));
        }
    }

    /**
     * 订阅初始化结果。结果统一在主线程分发；终态下从主线程调用时会原地回调。
     */
    @NonNull
    public static InitRequest whenReady(@NonNull InitCallback initCallback) {
        Objects.requireNonNull(initCallback, "initCallback");
        PendingInitCallback pendingCallback = new PendingInitCallback(initCallback);
        final int result;
        final Throwable error;
        synchronized (VolcPlayerInit.class) {
            int state = VolcPlayerInit.getInitState();
            if (state == VolcPlayerInit.INIT_STATE_SUCCESS) {
                result = InitCallback.RESULT_SUCCESS;
                error = null;
            } else if (state == VolcPlayerInit.INIT_STATE_ERROR) {
                result = InitCallback.RESULT_ERROR;
                error = VolcPlayerInit.getInitError();
            } else {
                synchronized (INIT_CALLBACKS) {
                    INIT_CALLBACKS.add(pendingCallback);
                }
                return pendingCallback;
            }
        }
        pendingCallback.dispatch(result, error);
        return pendingCallback;
    }

    /**
     * 在初始化成功后执行一次业务动作。
     */
    @NonNull
    public static InitRequest runWhenReady(
            @NonNull Runnable action,
            @Nullable FailureCallback onFailure) {
        Objects.requireNonNull(action, "action");
        return whenReady((result, error) -> {
            if (result == InitCallback.RESULT_SUCCESS) {
                action.run();
            } else if (onFailure != null) {
                onFailure.onFailure(error);
            }
        });
    }

    public static int getState() {
        return VolcPlayerInit.getInitState();
    }

    @Nullable
    public static Throwable getInitError() {
        return VolcPlayerInit.getInitError();
    }

    static void onInitCompleted(int state, @Nullable Throwable error) {
        final List<PendingInitCallback> callbacks;
        synchronized (INIT_CALLBACKS) {
            callbacks = new ArrayList<>(INIT_CALLBACKS);
            INIT_CALLBACKS.clear();
        }
        int result = state == VolcPlayerInit.INIT_STATE_SUCCESS
                ? InitCallback.RESULT_SUCCESS
                : InitCallback.RESULT_ERROR;
        for (PendingInitCallback callback : callbacks) {
            callback.dispatch(result, error);
        }
    }

    private static final class PendingInitCallback implements InitRequest, Runnable {

        @Nullable
        private InitCallback callback;
        private boolean cancelled;
        private int result;
        @Nullable
        private Throwable error;

        private PendingInitCallback(@NonNull InitCallback callback) {
            this.callback = callback;
        }

        private void dispatch(int result, @Nullable Throwable error) {
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                this.result = result;
                this.error = error;
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                run();
            } else if (!MAIN_HANDLER.post(this)) {
                cancel();
            }
        }

        @Override
        public void run() {
            final InitCallback target;
            final Throwable resultError;
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                target = callback;
                callback = null;
                resultError = error;
                error = null;
            }
            if (target != null) {
                target.onInitResult(result, resultError);
            }
        }

        @Override
        public void cancel() {
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                callback = null;
                error = null;
            }
            synchronized (INIT_CALLBACKS) {
                INIT_CALLBACKS.remove(this);
            }
            MAIN_HANDLER.removeCallbacks(this);
        }
    }
}
