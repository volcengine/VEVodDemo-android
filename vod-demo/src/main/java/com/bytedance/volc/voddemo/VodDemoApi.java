/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.voddemo;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.bytedance.playerkit.player.volcengine.VolcPlayerAsyncInitializer;
import com.bytedance.volc.voddemo.ui.main.MainActivity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class VodDemoApi {

    // 统一接入层只负责线程和生命周期编排，不负责执行 SDK 初始化。
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public interface InitFailureCallback {
        void onFailure(@Nullable Throwable error);
    }

    public interface ReadyRequest {
        /**
         * 只取消当前尚未执行的业务动作，不停止全局 SDK 初始化，也不影响其他等待方。
         * action 已经开始执行后，本方法不会中断 action。
         */
        void cancel();
    }

    public static void initVodSDK(Context context,
                                  String appId,
                                  String appName,
                                  String appChannel,
                                  String appVersion,
                                  String licenseUri) {
        VodSDK.init(context, appId, appName, appChannel, appVersion, licenseUri);
    }

    public static boolean isVodSdkAsyncInitEnabled() {
        return VodSDK.isAsyncInitEnabled();
    }

    /**
     * 在 SDK 初始化完成后执行一次点播业务动作。
     *
     * <p>owner 为 null 时仍在主线程执行，但调用方需要自行取消返回的请求；
     * owner 非 null 时会在 ON_DESTROY 自动取消，避免执行已失效页面的动作。
     * 若调用方已在主线程且初始化已进入终态，业务动作会在本方法返回前原地执行。
     */
    @NonNull
    public static ReadyRequest runWhenVodSdkReady(
            @Nullable LifecycleOwner owner,
            @NonNull Runnable action,
            @Nullable InitFailureCallback onFailure) {
        LifecycleReadyRequest request =
                new LifecycleReadyRequest(
                        owner,
                        Objects.requireNonNull(action, "action"),
                        onFailure);
        request.start();
        return request;
    }

    public static void intentInto(Activity activity, boolean showActionBar) {
        MainActivity.intentInto(activity, showActionBar);
        //SampleVideoActivity.intentInto(activity);
    }

    private static final class LifecycleReadyRequest
            implements ReadyRequest, LifecycleEventObserver {

        private final AtomicBoolean mFinished = new AtomicBoolean();
        // 后台调用入口时先切到主线程，再注册 LifecycleObserver 和初始化订阅。
        private final Runnable mSubscribeRunnable = this::subscribeOnMainThread;
        @Nullable
        private LifecycleOwner mOwner;
        @Nullable
        private Runnable mAction;
        @Nullable
        private InitFailureCallback mOnFailure;
        @Nullable
        private volatile VolcPlayerAsyncInitializer.InitRequest mInitRequest;

        private LifecycleReadyRequest(
                @Nullable LifecycleOwner owner,
                @NonNull Runnable action,
                @Nullable InitFailureCallback onFailure) {
            mOwner = owner;
            mAction = action;
            mOnFailure = onFailure;
        }

        private void start() {
            // 已在主线程时直接注册，避免无意义地多等待一次消息循环。
            if (Looper.myLooper() == Looper.getMainLooper()) {
                subscribeOnMainThread();
            } else if (!MAIN_HANDLER.post(mSubscribeRunnable)) {
                cancel();
            }
        }

        private void subscribeOnMainThread() {
            if (mFinished.get()) {
                return;
            }
            LifecycleOwner owner = mOwner;
            if (owner != null) {
                // 页面已销毁时不再登记初始化回调。
                if (owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                    cancel();
                    return;
                }
                owner.getLifecycle().addObserver(this);
            }

            VolcPlayerAsyncInitializer.InitRequest request =
                    VolcPlayerAsyncInitializer.whenReady(this::onInitResult);
            mInitRequest = request;
            // 终态回调可能在 whenReady() 返回前原地完成并清理当前请求。
            // 赋值后复查状态，避免重新持有已经结束的底层订阅。
            if (mFinished.get()) {
                request.cancel();
                mInitRequest = null;
            }
        }

        private void onInitResult(int result, @Nullable Throwable error) {
            if (mFinished.get()) {
                return;
            }
            LifecycleOwner owner = mOwner;
            if (owner != null
                    && owner.getLifecycle().getCurrentState() == Lifecycle.State.DESTROYED) {
                cancel();
                return;
            }

            Runnable action = mAction;
            InitFailureCallback onFailure = mOnFailure;
            try {
                // 这里只判断 SDK 初始化终态；License 由具体 Feature 调用链自行校验。
                if (result == VolcPlayerAsyncInitializer.InitCallback.RESULT_SUCCESS) {
                    if (action != null) {
                        action.run();
                    }
                } else if (onFailure != null) {
                    onFailure.onFailure(error);
                }
            } finally {
                // 当前门禁是一次性请求，执行完毕后立即解除所有页面引用。
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
            // 页面销毁、主动取消和结果回调可能并发，只有第一个路径负责清理。
            if (!mFinished.compareAndSet(false, true)) {
                return;
            }

            VolcPlayerAsyncInitializer.InitRequest request = mInitRequest;
            if (request != null) {
                request.cancel();
            }
            MAIN_HANDLER.removeCallbacks(mSubscribeRunnable);
            if (Looper.myLooper() == Looper.getMainLooper()) {
                clearReferences();
            } else {
                // LifecycleObserver 必须在主线程移除。
                MAIN_HANDLER.post(this::clearReferences);
            }
        }

        private void clearReferences() {
            LifecycleOwner owner = mOwner;
            if (owner != null) {
                owner.getLifecycle().removeObserver(this);
            }
            mInitRequest = null;
            mOwner = null;
            mAction = null;
            mOnFailure = null;
        }
    }
}
