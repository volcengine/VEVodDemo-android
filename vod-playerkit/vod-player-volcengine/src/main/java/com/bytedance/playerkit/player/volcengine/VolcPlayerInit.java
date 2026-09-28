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

package com.bytedance.playerkit.player.volcengine;

import android.annotation.SuppressLint;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.PlayerKit;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInitConfig.AppInfo;
import com.bytedance.playerkit.utils.L;

public class VolcPlayerInit {

    /**
     * @deprecated 异步初始化请使用 {@link VolcPlayerAsyncInitializer.InitCallback}。
     */
    @Deprecated
    public interface InitCallback {
        int RESULT_SUCCESS = 1;
        int RESULT_ERROR = -1;

        void onInitResult(int result);
    }

    public static final int INIT_STATE_IDLE = 0;
    public static final int INIT_STATE_INITING = 1;
    public static final int INIT_STATE_SUCCESS = 2;
    public static final int INIT_STATE_ERROR = 3;

    public static String mapState(int state) {
        switch (state) {
            case INIT_STATE_IDLE:
                return "IDLE";
            case INIT_STATE_INITING:
                return "INITING";
            case INIT_STATE_SUCCESS:
                return "SUCCESS";
            case INIT_STATE_ERROR:
                return "ERROR";
            default:
                throw new IllegalArgumentException("unsupported state " + state);
        }
    }

    private static int sInitState;
    @Nullable
    private static Throwable sInitError;

    public static void initSync() {
        VolcPlayerInitConfig config = beginInitialization("initSync");
        if (config != null) {
            runInitialization("initSync", config, true);
        }
    }

    /**
     * @deprecated 异步初始化请使用 {@link VolcPlayerAsyncInitializer#start()}。
     */
    @Deprecated
    public static void initAsync(@Nullable InitCallback initCallback) {
        VolcPlayerAsyncInitializer.start(
                initCallback == null
                        ? null
                        : (result, error) -> initCallback.onInitResult(result));
    }

    @Nullable
    static VolcPlayerInitConfig beginInitialization(String method) {
        final VolcPlayerInitConfig config;
        synchronized (VolcPlayerInit.class) {
            if (sInitState != INIT_STATE_IDLE) {
                L.d(VolcPlayerInit.class, method, "return", mapState(sInitState));
                return null;
            }
            config = sConfig;
            setInitState(INIT_STATE_INITING);
        }
        if (config == null) {
            IllegalStateException error = new IllegalStateException("Invoke config first.");
            L.e(VolcPlayerInit.class, method, "return", error.getMessage());
            completeInit(INIT_STATE_ERROR, error);
            return null;
        }
        return config;
    }

    static void runInitialization(
            String method,
            VolcPlayerInitConfig config,
            boolean rethrowError) {
        L.d(VolcPlayerInit.class, method, "start");
        final long startTime = System.currentTimeMillis();
        try {
            TTSDKVodInit.initVod(config);
            L.d(VolcPlayerInit.class, method, "success",
                    "time:" + (System.currentTimeMillis() - startTime));
            completeInit(INIT_STATE_SUCCESS, null);
        } catch (Throwable throwable) {
            L.e(VolcPlayerInit.class, method, throwable, "error",
                    "time:" + (System.currentTimeMillis() - startTime));
            completeInit(INIT_STATE_ERROR, throwable);
            if (rethrowError && throwable instanceof Error) {
                throw (Error) throwable;
            }
        }
    }

    static void failInitialization(String method, Throwable throwable) {
        L.e(VolcPlayerInit.class, method, throwable, "error");
        completeInit(INIT_STATE_ERROR, throwable);
    }

    private static void completeInit(int state, @Nullable Throwable throwable) {
        synchronized (VolcPlayerInit.class) {
            if (sInitState == INIT_STATE_SUCCESS || sInitState == INIT_STATE_ERROR) {
                return;
            }
            setInitState(state);
            sInitError = throwable;
        }
        VolcPlayerAsyncInitializer.onInitCompleted(state, throwable);
    }

    public static synchronized boolean isInitialized() {
        return sInitState == INIT_STATE_SUCCESS;
    }

    @Nullable
    public static synchronized Throwable getInitError() {
        return sInitError;
    }

    static void requireInitialized() {
        if (!isInitialized()) {
            throw new IllegalStateException(
                    "Vod SDK is not initialized. Wait for async initialization success before using player APIs.");
        }
    }

    /**
     * @deprecated 初始化门禁必须通过
     * {@link VolcPlayerAsyncInitializer#runWhenReady(Runnable, VolcPlayerAsyncInitializer.FailureCallback)}
     * 异步处理。
     */
    @Deprecated
    public static void waitInitAsyncResult() {
        requireInitialized();
    }

    private static synchronized void setInitState(int newState) {
        int state = sInitState;
        sInitState = newState;
        L.d(VolcPlayerInit.class, "setInitState", mapState(state), mapState(newState));
    }

    public static synchronized int getInitState() {
        return sInitState;
    }

    @SuppressLint("StaticFieldLeak")
    private static VolcPlayerInitConfig sConfig;

    public static synchronized void config(VolcPlayerInitConfig config) {
        if (config == null) {
            L.e(VolcPlayerInit.class, "config", "return", "config is null");
            return;
        }
        if (sConfig != null) {
            L.w(VolcPlayerInit.class, "config", "return", "already config");
            return;
        }
        L.d(VolcPlayerInit.class, "config", config, AppInfo.dump(config.appInfo));
        sConfig = config;
        PlayerKit.config(config.playerKitConfig);
    }

    public static synchronized VolcPlayerInitConfig config() {
        return sConfig;
    }

    public static void setUserUniqueId(@Nullable String userUniqueId) {
        TTSDKVodInit.setUserUniqueId(userUniqueId);
    }

    public static void initAppLog() {
        TTSDKVodInit.initAppLog();
    }

    public static void startAppLog() {
        TTSDKVodInit.startAppLog();
    }

    public static String getDeviceId() {
        return TTSDKVodInit.getDeviceId();
    }

    public static String getSDKVersion() {
        return TTSDKVodInit.getTTSDKVersion();
    }

    public static void clearDiskCache() {
        TTSDKVodInit.clearDiskCache();
    }
}
