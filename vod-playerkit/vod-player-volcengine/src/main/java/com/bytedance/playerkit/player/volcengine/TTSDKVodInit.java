/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/2/25
 */

package com.bytedance.playerkit.player.volcengine;

import static com.bytedance.playerkit.player.volcengine.VolcPlayerInit.config;
import static com.ss.ttvideoengine.strategy.StrategyManager.VERSION_2;
import static com.ss.ttvideoengine.utils.TTVideoEngineUtils.SENSITIVE_SCENE_USER_DISAGREE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.applog.IAppLogInstance;
import com.bytedance.playerkit.utils.L;
import com.pandora.common.applog.AppLogWrapper;
import com.pandora.common.env.Env;
import com.pandora.common.env.config.Config;
import com.pandora.common.env.config.VodConfig;
import com.pandora.ttlicense2.C;
import com.pandora.ttlicense2.LicenseManager;
import com.pandora.vod.VodSDK;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.utils.TTVideoEngineUtils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

class TTSDKVodInit {

    private static void doInit(VolcPlayerInitConfig initConfig) {
        if (L.ENABLE_LOG) {
            VodSDK.openAllVodLog();
        }
        try {
            StrategyManager.setVersion(VERSION_2);
        } catch (Exception e) {
            L.e(TTSDKVodInit.class, "doInit", "StrategyManager#setVersion", e.getMessage());
        }

        if (VolcConfigGlobal.ENABLE_HLS_CACHE_MODULE) {
            TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_ENABLE_HLS_PROXY, 1);
        }
        if (VolcConfigGlobal.ENABLE_USE_ORIGINAL_URL) {
            TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_ENABLE_USE_ORIGINAL_URL, 1);
        }
        if (VolcConfigGlobal.ENABLE_USE_BACKUP_URL) {
            TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_INT_ALLOW_TRY_THE_LAST_URL, 1);
        }

        if (!initConfig.playerCacheDir.exists()) initConfig.playerCacheDir.mkdirs();
        VodConfig.Builder vodBuilder = new VodConfig.Builder(initConfig.context)
                .setCacheDirPath(initConfig.playerCacheDir.getAbsolutePath())
                .setMaxCacheSize(300 * 1024 * 1024);

        if (VolcConfigGlobal.ENABLE_ECDN &&
                VolcExtensions.isIntegrate(VolcExtensions.PLAYER_EXTENSION_ECDN)) {
            L.d(TTSDKVodInit.class, "doInit", "ecdn fileKeyRegularExpression", VolcConfig.ECDN_FILE_KEY_REGULAR_EXPRESSION);
            TTVideoEngine.setStringValue(DataLoaderHelper.DATALOADER_KEY_STRING_VDP_FILE_KEY_REGULAR_EXPRESSION, VolcConfig.ECDN_FILE_KEY_REGULAR_EXPRESSION);
            vodBuilder.setLoaderType(2);
        }

        if (initConfig.appInfo.enableVodSDKSecurityApi) {
            TTVideoEngineUtils.setSensitiveScene(SENSITIVE_SCENE_USER_DISAGREE);
        }
        Env.openAppLog(initConfig.appInfo.enableInitAppLog);
        Env.init(new Config.Builder()
                .setApplicationContext(initConfig.context)
                .setAppID(initConfig.appInfo.appId)
                .setAppName(initConfig.appInfo.appName)
                // 合法版本号应大于、等于 2 个分隔符，如："1.3.2"
                .setAppVersion(initConfig.appInfo.appVersion)
                .setAppChannel(initConfig.appInfo.appChannel)
                .securityDeviceId(initConfig.appInfo.enableAppLogSecurityApi)
                // 将 license 文件拷贝到 app 的 assets 文件夹中，并设置 LicenseUri
                // 下面 LicenseUri 对应工程中 assets 路径为：assets/license/vod.lic
                .setLicenseUri(initConfig.appInfo.licenseUri)
                // 可不设置，默认值见下表
                .setVodConfig(vodBuilder.build())
                .build());
        VolcEngineStrategy.init();
        VolcNetSpeedStrategy.init();
        VolcSuperResolutionStrategy.init();
        VolcQualityStrategy.init();
        VolcSourceRefreshStrategy.init();
    }

    private static final List<InitTask> INIT_TASK_PENDING_LIST = new CopyOnWriteArrayList<>();

    private static volatile boolean sIniting;
    private static volatile boolean sInited;

    static void initVod(VolcPlayerInitConfig initConfig) {
        if (sInited || sIniting) {
            return;
        }
        sIniting = true;
        try {
            L.d(TTSDKVodInit.class, "initVod", "start");
            synchronized (TTSDKVodInit.class) {
                doInit(initConfig);
            }
        } finally {
            sIniting = false;
            sInited = true;
            L.d(TTSDKVodInit.class, "initVod", "end");
        }
        for (InitTask initTask : INIT_TASK_PENDING_LIST) {
            L.d(TTSDKVodInit.class, "initVod", "execute pending task", initTask.taskName);
            synchronized (TTSDKVodInit.class) {
                initTask.runnable.run();
            }
        }
        INIT_TASK_PENDING_LIST.clear();

        logAuthResult();
    }

    private static void postInitTask(InitTask initTask) {
        if (sInited) {
            L.d(TTSDKVodInit.class, "postInitTask", "execute", initTask.taskName);
            synchronized (TTSDKVodInit.class) {
                initTask.runnable.run();
            }
        } else {
            L.d(TTSDKVodInit.class, "postInitTask", "enqueue", initTask.taskName, "add to pending list");
            INIT_TASK_PENDING_LIST.add(initTask);
        }
    }

    static void initAppLog() {
        if (!config().appInfo.enableInitAppLog) {
            postInitTask(new InitTask("initAppLog", new Runnable() {
                @Override
                public void run() {
                    Env.openAppLog(true);
                    Env.initAppLog(Env.getConfig());
                }
            }));
        }
    }

    static void startAppLog() {
        if (!config().appInfo.autoStartAppLog) {
            postInitTask(new InitTask("startAppLog", new Runnable() {
                @Override
                public void run() {
                    Env.startAppLog();
                }
            }));
        }
    }

    static void setUserUniqueId(@Nullable String userUniqueId) {
        postInitTask(new InitTask("setUserUniqueId " + userUniqueId, new Runnable() {
            @Override
            public void run() {
                final IAppLogInstance applog = AppLogWrapper.getAppLogInstance();
                if (applog != null) {
                    applog.setUserUniqueID(userUniqueId);
                }
            }
        }));
    }

    @Nullable
    static String getDeviceId() {
        if (!sInited) {
            L.w(TTSDKVodInit.class, "getDeviceId", "return", "not inited");
            return null;
        }
        return TTVideoEngine.getDeviceID();
    }

    static String getTTSDKVersion() {
        return Env.getVersion();
    }

    static void clearDiskCache() {
        if (!sInited) {
            L.w(TTSDKVodInit.class, "clearDiskCache", "return", "not inited");
            return;
        }
        TTVideoEngine.clearAllCaches(true);
    }

    static void logAuthResult() {
        if (!L.ENABLE_LOG) return;

        final List<String> sdks = Arrays.asList(
                C.SDK.SDK_VOD_PLAY,
                C.SDK.SDK_LIVE_PULL,
                C.SDK.SDK_LIVE_PUSH);

        final List<String> features = Arrays.asList(
                C.Feature.FEATURE_H266,
                C.Feature.FEATURE_VR_PANORAMA,
                C.Feature.FEATURE_ABR,
                C.Feature.FEATURE_SUPER_RESOLUTION,
                C.Feature.FEATURE_VOLUME_BALANCE,
                C.Feature.FEATURE_PCDN,
                C.Feature.FEATURE_INTERTRUST_DRM,
                C.Feature.FEATURE_LITE);

        StringBuilder sb = new StringBuilder();
        sb.append("License Result:").append("\n");
        for (String sdk : sdks) {
            int sdkResult = checkSDKAuth(sdk);
            sb.append("[SDK]").append(sdk).append(":").append(sdkResult).append("\n");
            if (C.SDK.SDK_VOD_PLAY.equals(sdk)) {
                sb.append("    [EDITION]").append(getSDKEdition(sdk)).append("\n");
                if (sdkResult == C.LicenseStatus.LICENSE_STATUS_OK) {
                    for (String feature : features) {
                        int featureResult = checkFeatureAuth(C.SDK.SDK_VOD_PLAY, feature);
                        sb.append("    [FEATURE]").append(feature).append(":").append(featureResult).append("\n");
                    }
                }
            }
        }
        L.d(TTSDKVodInit.class, "logAuthResult", sb.toString());
    }

    @Nullable
    static String getSDKEdition(@NonNull String sdk) {
        if (sInited) {
            return LicenseManager.getInstance().getSDKEdition(sdk);
        } else {
            return null;
        }
    }

    static int checkSDKAuth(@NonNull String sdk) {
        if (sInited) {
            return LicenseManager.getInstance().checkSDKAuth(sdk);
        } else {
            return C.LicenseStatus.LICENSE_STATUS_INVALID;
        }
    }

    static int checkFeatureAuth(@NonNull String sdk, @NonNull String feature) {
        if (sInited) {
            return LicenseManager.getInstance().checkFeatureAuth(sdk, feature);
        } else {
            return C.LicenseStatus.LICENSE_STATUS_INVALID;
        }
    }

    private static class InitTask {
        final String taskName;
        final Runnable runnable;

        public InitTask(String taskName, Runnable task) {
            this.taskName = taskName;
            this.runnable = task;
        }
    }
}
