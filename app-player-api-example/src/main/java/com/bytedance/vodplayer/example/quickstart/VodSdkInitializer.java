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

package com.bytedance.vodplayer.example.quickstart;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.pandora.common.env.Env;
import com.pandora.common.env.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 在专用后台线程初始化点播 SDK，并为播放器相关操作提供统一的就绪门禁。
 *
 * <p>该类只管理进程级初始化状态，不持有或释放播放器实例。播放器创建、预加载、下载等根操作
 * 通过 {@link #runWhenReady(LifecycleOwner, Runnable, FailureCallback)} 接入；已经创建的播放器实例
 * 不需要为 play、pause、seekTo 等控制操作重复增加初始化门禁。
 */
public final class VodSdkInitializer {

    private static final String TAG = "VodSdkInitializer";

    public interface Callback {
        void onResult(boolean success, @Nullable Throwable error);
    }

    public interface FailureCallback {
        void onFailure(@Nullable Throwable error);
    }

    public interface ReadyRequest {
        /**
         * 只取消本次尚未执行的业务动作，不停止全局初始化，也不影响其他请求。
         * 如果 action 已经开始执行，本方法不会中断 action。
         */
        void cancel();
    }

    public enum State {
        // 尚未提交初始化任务。
        IDLE,
        // 初始化任务正在专用工作线程执行。
        INITIALIZING,
        // Env.start() 已成功返回，进程内保持该终态。
        SUCCESS,
        // 初始化失败，进程内保持该终态且不自动重试。
        FAILED
    }

    // 初始化结果和业务 action 最终都在主线程执行。
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    // 使用进程级单线程串行执行初始化，避免多个入口并发调用 Env.start()。
    private static final HandlerThread WORKER_THREAD = new HandlerThread("vod-sdk-init");
    private static final Handler WORKER_HANDLER;
    private static final Coordinator COORDINATOR;

    static {
        WORKER_THREAD.start();
        WORKER_HANDLER = new Handler(WORKER_THREAD.getLooper());
        COORDINATOR = new Coordinator(
                // 方法引用等价于 runnable -> WORKER_HANDLER.post(runnable)。
                // 它只负责把初始化任务提交到专用工作线程，返回值表示是否成功入队。
                WORKER_HANDLER::post,
                new CallbackDispatcher() {
                    @Override
                    public boolean dispatch(Runnable runnable) {
                        // 已在主线程时直接执行，避免初始化早已完成后仍多等待一次消息循环。
                        if (Looper.myLooper() == Looper.getMainLooper()) {
                            runnable.run();
                            return true;
                        }
                        // 后台线程只负责产生结果，所有业务回调仍统一切回主线程。
                        return MAIN_HANDLER.post(runnable);
                    }

                    @Override
                    public void remove(Runnable runnable) {
                        // cancel() 需要移除已经入队但尚未执行的主线程消息。
                        MAIN_HANDLER.removeCallbacks(runnable);
                    }
                });
    }

    private VodSdkInitializer() {
    }

    public static void start(@NonNull Config config, @Nullable Callback callback) {
        Objects.requireNonNull(config, "config");
        // 对外入口固定执行 Env.start()；下面的重载用于复用同一状态机并支持测试注入。
        start(() -> Env.start(config), callback);
    }

    /**
     * API Example 用完整初始化任务。客户接入时通常直接使用 {@link #start(Config, Callback)}。
     */
    static void start(
            @NonNull InitTask initTask,
            @Nullable Callback callback) {
        Objects.requireNonNull(initTask, "initTask");
        COORDINATOR.start(() -> {
            long startMs = SystemClock.elapsedRealtime();
            try {
                initTask.run();
                Log.i(TAG, "success, costMs=" + (SystemClock.elapsedRealtime() - startMs));
            } catch (Throwable error) {
                Log.e(
                        TAG,
                        "failed, costMs=" + (SystemClock.elapsedRealtime() - startMs),
                        error);
                throw error;
            }
        }, callback);
    }

    @NonNull
    public static ReadyRequest whenReady(@NonNull Callback callback) {
        return COORDINATOR.whenReady(callback);
    }

    /**
     * 在 SDK 初始化完成后执行一次业务操作。
     *
     * <p>传入 LifecycleOwner 时会在 ON_DESTROY 自动取消；传 null 时由调用方持有返回值并按需取消。
     * action 和失败回调始终在主线程执行。若调用方已在主线程且初始化已进入终态，
     * 回调会在本方法返回前原地执行。
     */
    @NonNull
    public static ReadyRequest runWhenReady(
            @Nullable LifecycleOwner owner,
            @NonNull Runnable action,
            @Nullable FailureCallback onFailure) {
        ReadyActionRequest request = new ReadyActionRequest(owner, action, onFailure);
        request.start();
        return request;
    }

    public static State getState() {
        return COORDINATOR.getState();
    }

    @Nullable
    public static Throwable getInitError() {
        return COORDINATOR.getError();
    }

    interface InitTask {
        void run() throws Throwable;
    }

    interface TaskExecutor {
        boolean post(Runnable runnable);
    }

    interface CallbackDispatcher {
        boolean dispatch(Runnable runnable);

        void remove(Runnable runnable);
    }

    /**
     * 与 Android 调度器解耦的状态协调器，生产代码与本地单测共用同一套状态转换。
     */
    static final class Coordinator {

        private final Object lock = new Object();
        private final List<CallbackRequest> callbacks = new ArrayList<>();
        private final TaskExecutor worker;
        private final CallbackDispatcher callbackDispatcher;

        private State state = State.IDLE;
        @Nullable
        private Throwable error;

        Coordinator(TaskExecutor worker, CallbackDispatcher callbackDispatcher) {
            this.worker = Objects.requireNonNull(worker, "worker");
            this.callbackDispatcher =
                    Objects.requireNonNull(callbackDispatcher, "callbackDispatcher");
        }

        void start(@NonNull InitTask initTask, @Nullable Callback callback) {
            Objects.requireNonNull(initTask, "initTask");

            // callbackRequest 同时也是取消句柄，等待期间可从 callbacks 中精确移除。
            CallbackRequest callbackRequest = callback == null
                    ? null
                    : new CallbackRequest(callback);
            State callbackState = null;
            Throwable callbackError = null;
            boolean shouldStart = false;
            synchronized (lock) {
                // 注册回调和抢占初始化权必须在同一把锁内完成，避免初始化恰好完成时漏通知。
                if (callbackRequest != null) {
                    if (isTerminal(state)) {
                        callbackState = state;
                        callbackError = error;
                    } else {
                        callbacks.add(callbackRequest);
                    }
                }
                if (state == State.IDLE) {
                    state = State.INITIALIZING;
                    shouldStart = true;
                }
            }

            // 业务回调禁止在状态锁内执行，避免回调重入初始化器时形成锁竞争。
            if (callbackState != null) {
                callbackRequest.dispatch(
                        callbackState == State.SUCCESS,
                        callbackError);
            }
            if (!shouldStart) {
                return;
            }

            try {
                // post() 返回 false 或直接抛异常都必须进入 FAILED 终态并通知全部等待方。
                if (!worker.post(() -> runInitialization(initTask))) {
                    complete(
                            State.FAILED,
                            new IllegalStateException("init worker is unavailable"));
                }
            } catch (Throwable throwable) {
                complete(State.FAILED, throwable);
            }
        }

        @NonNull
        ReadyRequest whenReady(@NonNull Callback callback) {
            Objects.requireNonNull(callback, "callback");

            CallbackRequest request = new CallbackRequest(callback);
            State snapshot;
            Throwable snapshotError;
            synchronized (lock) {
                // 在锁内获取一致的状态快照；未完成时只登记，不阻塞当前线程。
                snapshot = state;
                snapshotError = error;
                if (!isTerminal(snapshot)) {
                    callbacks.add(request);
                    return request;
                }
            }
            // 终态结果在锁外交付。主线程调用时原地执行，后台线程调用时切回主线程。
            request.dispatch(snapshot == State.SUCCESS, snapshotError);
            return request;
        }

        State getState() {
            synchronized (lock) {
                return state;
            }
        }

        @Nullable
        Throwable getError() {
            synchronized (lock) {
                return error;
            }
        }

        private void runInitialization(InitTask initTask) {
            try {
                initTask.run();
                // 只有完整初始化任务正常返回后才发布 SUCCESS。
                complete(State.SUCCESS, null);
            } catch (Throwable throwable) {
                // 同时捕获 Exception 和 Error，确保所有等待方都能收到失败终态。
                complete(State.FAILED, throwable);
            }
        }

        private void complete(State terminalState, @Nullable Throwable throwable) {
            if (!isTerminal(terminalState)) {
                throw new IllegalArgumentException("terminalState must be terminal");
            }

            List<CallbackRequest> pendingCallbacks;
            synchronized (lock) {
                // 首个终态是唯一权威结果，防止异常调度器重复完成后反转状态。
                if (isTerminal(state)) {
                    return;
                }
                state = terminalState;
                error = throwable;
                pendingCallbacks = new ArrayList<>(callbacks);
                callbacks.clear();
            }

            // 先复制并清空等待列表，再在锁外通知，避免业务回调阻塞新的状态读取。
            boolean success = terminalState == State.SUCCESS;
            for (CallbackRequest callback : pendingCallbacks) {
                callback.dispatch(success, throwable);
            }
        }

        private final class CallbackRequest implements ReadyRequest, Runnable {

            // 同一个对象既是等待列表节点，也是主线程消息，cancel() 可同时覆盖两个阶段。
            @Nullable
            private Callback callback;
            private boolean cancelled;
            private boolean success;
            @Nullable
            private Throwable error;

            private CallbackRequest(@NonNull Callback callback) {
                this.callback = callback;
            }

            private void dispatch(boolean success, @Nullable Throwable error) {
                synchronized (this) {
                    if (cancelled) {
                        return;
                    }
                    this.success = success;
                    this.error = error;
                }
                // 生产分发器采用 main-immediate；测试分发器可选择排队以覆盖取消竞态。
                if (!callbackDispatcher.dispatch(this)) {
                    cancel();
                }
            }

            @Override
            public void run() {
                final Callback target;
                final boolean result;
                final Throwable resultError;
                synchronized (this) {
                    if (cancelled) {
                        return;
                    }
                    // 回调只允许消费一次，执行前先清空强引用，避免页面对象被长期持有。
                    cancelled = true;
                    target = callback;
                    callback = null;
                    result = success;
                    resultError = error;
                    error = null;
                }
                if (target != null) {
                    target.onResult(result, resultError);
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
                synchronized (lock) {
                    // 初始化尚未完成时，请求仍可能位于等待列表中。
                    callbacks.remove(this);
                }
                // 初始化已完成但消息尚未执行时，请求可能位于主线程队列中。
                callbackDispatcher.remove(this);
            }
        }

        private static boolean isTerminal(State state) {
            return state == State.SUCCESS || state == State.FAILED;
        }
    }

    private static final class ReadyActionRequest
            implements ReadyRequest, LifecycleEventObserver {

        // action、失败回调、Lifecycle 销毁和主动取消只能有一个路径取得结束权。
        private final AtomicBoolean finished = new AtomicBoolean();
        private final Runnable subscribeRunnable = this::subscribeOnMainThread;
        @Nullable
        private LifecycleOwner owner;
        @Nullable
        private Runnable action;
        @Nullable
        private FailureCallback onFailure;
        @Nullable
        private volatile ReadyRequest initRequest;

        private ReadyActionRequest(
                @Nullable LifecycleOwner owner,
                @NonNull Runnable action,
                @Nullable FailureCallback onFailure) {
            this.owner = owner;
            this.action = Objects.requireNonNull(action, "action");
            this.onFailure = onFailure;
        }

        private void start() {
            // LifecycleObserver 的注册和移除统一在主线程完成。
            if (Looper.myLooper() == Looper.getMainLooper()) {
                subscribeOnMainThread();
            } else if (!MAIN_HANDLER.post(subscribeRunnable)) {
                cancel();
            }
        }

        private void subscribeOnMainThread() {
            if (finished.get()) {
                return;
            }
            LifecycleOwner currentOwner = owner;
            if (currentOwner != null) {
                // 页面已经销毁时不再订阅，避免执行失效页面的业务动作。
                if (currentOwner.getLifecycle().getCurrentState()
                        == Lifecycle.State.DESTROYED) {
                    cancel();
                    return;
                }
                currentOwner.getLifecycle().addObserver(this);
            }

            ReadyRequest request = COORDINATOR.whenReady(this::deliver);
            initRequest = request;
            // 终态 main-immediate 回调可能在 whenReady() 返回前完成并调用 cancel()。
            // 因此赋值后必须复查 finished，避免把已结束的订阅重新保存在成员变量中。
            if (finished.get()) {
                request.cancel();
                initRequest = null;
            }
        }

        private void deliver(boolean success, @Nullable Throwable error) {
            if (finished.get()) {
                return;
            }
            LifecycleOwner currentOwner = owner;
            if (currentOwner != null
                    && currentOwner.getLifecycle().getCurrentState()
                    == Lifecycle.State.DESTROYED) {
                cancel();
                return;
            }

            Runnable currentAction = action;
            FailureCallback currentFailure = onFailure;
            try {
                // success 只表示 SDK 初始化完成；具体 License 和 Feature 由 SDK 调用链校验。
                if (success) {
                    if (currentAction != null) {
                        currentAction.run();
                    }
                } else if (currentFailure != null) {
                    currentFailure.onFailure(error);
                }
            } finally {
                // 无论业务动作成功、失败或抛异常，本次一次性请求都立即释放引用。
                cancel();
            }
        }

        @Override
        public void onStateChanged(
                @NonNull LifecycleOwner source,
                @NonNull Lifecycle.Event event) {
            if (event == Lifecycle.Event.ON_DESTROY) {
                cancel();
            }
        }

        @Override
        public void cancel() {
            // 幂等抢占结束权，避免页面销毁、主动取消和结果回调并发清理。
            if (!finished.compareAndSet(false, true)) {
                return;
            }

            ReadyRequest currentRequest = initRequest;
            if (currentRequest != null) {
                currentRequest.cancel();
            }
            MAIN_HANDLER.removeCallbacks(subscribeRunnable);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                clearReferences();
            } else {
                MAIN_HANDLER.post(this::clearReferences);
            }
        }

        private void clearReferences() {
            LifecycleOwner currentOwner = owner;
            if (currentOwner != null) {
                currentOwner.getLifecycle().removeObserver(this);
            }
            initRequest = null;
            owner = null;
            action = null;
            onFailure = null;
        }
    }
}
