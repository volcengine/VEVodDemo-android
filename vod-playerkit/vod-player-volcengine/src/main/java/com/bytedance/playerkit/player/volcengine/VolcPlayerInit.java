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

import java.util.concurrent.FutureTask;

public class VolcPlayerInit {

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
    private static volatile FutureTask<Void> sInitFuture;

    public static void initSync() {
        waitInitAsyncResult();
        final VolcPlayerInitConfig config = config();
        if (isInitState(INIT_STATE_INITING, INIT_STATE_SUCCESS, INIT_STATE_ERROR)) {
            L.d(VolcPlayerInit.class, "initSync", "return", mapState(getInitState()));
            return;
        }
        if (config == null) {
            L.e(VolcPlayerInit.class, "initSync", "return", "config is null! Invoke config first.");
            return;
        }
        setInitState(INIT_STATE_INITING);
        L.d(VolcPlayerInit.class, "initSync", "start");
        final long startTime = System.currentTimeMillis();
        try {
            TTSDKVodInit.initVod(config);
            setInitState(INIT_STATE_SUCCESS);
            L.d(VolcPlayerInit.class, "initSync", "success", "time:" + (System.currentTimeMillis() - startTime));
        } catch (RuntimeException e) {
            setInitState(INIT_STATE_ERROR);
            L.e(VolcPlayerInit.class, "initSync", e, "error", "time:" + (System.currentTimeMillis() - startTime));
        }
    }

    public static synchronized void initAsync(InitCallback initCallback) {
        if (isInitState(INIT_STATE_INITING, INIT_STATE_SUCCESS, INIT_STATE_ERROR)) {
            L.d(VolcPlayerInit.class, "initAsync", "return", mapState(getInitState()));
            return;
        }
        final VolcPlayerInitConfig config = config();
        if (config == null) {
            L.e(VolcPlayerInit.class, "initAsync", "return", "config is null! Invoke config first.");
            return;
        }
        setInitState(INIT_STATE_INITING);
        final Boolean isMessageQueueIdle = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M ?
                config.workerHandler.getLooper().getQueue().isIdle() : null;
        final long startTime = System.currentTimeMillis();
        L.d(VolcPlayerInit.class, "initAsync", "start",
                config.workerHandler.getLooper().getThread(),
                "isMessageQueueIdle:" + isMessageQueueIdle, startTime);
        sInitFuture = new FutureTask<>(() -> {
            try {
                L.d(VolcPlayerInit.class, "initAsync", "running",
                        "time:" + (System.currentTimeMillis() - startTime));
                TTSDKVodInit.initVod(config);
                setInitState(INIT_STATE_SUCCESS);
                L.d(VolcPlayerInit.class, "initAsync", "success",
                        "time:" + (System.currentTimeMillis() - startTime));
                if (initCallback != null) {
                    config.workerHandler.post(() -> initCallback.onInitResult(InitCallback.RESULT_SUCCESS));
                }
            } catch (Exception e) {
                setInitState(INIT_STATE_ERROR);
                L.e(VolcPlayerInit.class, "initAsync", e, "error",
                        "time:" + (System.currentTimeMillis() - startTime));
                if (initCallback != null) {
                    config.workerHandler.post(() -> initCallback.onInitResult(InitCallback.RESULT_ERROR));
                }
                throw e;
            }
            return null;
        });
        config.workerHandler.post(sInitFuture);
    }

    public static void waitInitAsyncResult() {
        if (sInitFuture != null && !sInitFuture.isDone()) {
            final long startTime = System.currentTimeMillis();
            L.d(VolcPlayerInit.class, "waitInitAsyncResult", "wait", Thread.currentThread(), mapState(getInitState()), startTime);
            try {
                sInitFuture.get();
                L.d(VolcPlayerInit.class, "waitInitAsyncResult", "return", mapState(getInitState()),
                        "time:" + (System.currentTimeMillis() - startTime));
            } catch (Exception e) {
                L.e(VolcPlayerInit.class, "waitInitAsyncResult", e, "return", mapState(getInitState()),
                        "time:" + (System.currentTimeMillis() - startTime));
            }
        }
    }

    private static synchronized boolean isInitState(int... states) {
        for (int state : states) {
            if (sInitState == state) {
                return true;
            }
        }
        return false;
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
