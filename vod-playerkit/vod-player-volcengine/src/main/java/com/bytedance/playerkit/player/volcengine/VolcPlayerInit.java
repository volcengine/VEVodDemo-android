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

import static com.ss.ttvideoengine.TTVideoEngineInterface.PLAYER_OPTION_ENABLE_BMF;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;
import com.pandora.common.env.Env;
import com.pandora.common.env.config.Config;
import com.pandora.common.env.config.VodConfig;
import com.pandora.ttlicense2.LicenseManager;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;


public class VolcPlayerInit {

    private final static AtomicBoolean sInited = new AtomicBoolean(false);

    private static Context sContext;
    private static CacheKeyFactory sCacheKeyFactory;
    private static TrackSelector sTrackSelector;

    public static void init(final Context context, AppInfo appInfo) {
        init(context, appInfo, CacheKeyFactory.DEFAULT, TrackSelector.DEFAULT);
    }

    public static void init(Context context,
                            AppInfo appInfo,
                            CacheKeyFactory cacheKeyFactory,
                            TrackSelector trackSelector) {

        if (sInited.getAndSet(true)) return;

        sContext = context.getApplicationContext();

        if (trackSelector != null) {
            sTrackSelector = trackSelector;
        } else {
            sTrackSelector = TrackSelector.DEFAULT;
        }
        if (cacheKeyFactory != null) {
            sCacheKeyFactory = cacheKeyFactory;
        } else {
            sCacheKeyFactory = CacheKeyFactory.DEFAULT;
        }

        initVOD(context, appInfo);

        CacheLoader.Default.set(new VolcCacheLoader(context, new VolcCacheTask.Factory(context)));

        Player.Factory.Default.set(new VolcPlayerFactory(context));
    }

    public static Context getContext() {
        return sContext;
    }

    public static TrackSelector getTrackSelector() {
        return sTrackSelector;
    }

    public static CacheKeyFactory getCacheKeyFactory() {
        return sCacheKeyFactory;
    }

    public static File cacheDir(Context context) {
        return new File(context.getCacheDir(), "bytedance/playerkit/volcplayer/video_cache");
    }

    private static void initVOD(Context context, AppInfo appInfo) {
        if (L.ENABLE_LOG) {
            TTVideoEngineLog.turnOn(TTVideoEngineLog.LOG_DEBUG, 1);
            LicenseManager.turnOnLogcat(true);
        }

        if (VolcConfigGlobal.ENABLE_HLS_CACHE_MODULE) {
            TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_ENABLE_HLS_PROXY, 1);
        }
        if (VolcConfigGlobal.ENABLE_USE_ORIGINAL_URL) {
            TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_ENABLE_USE_ORIGINAL_URL, 1);
        }
        if (VolcConfigGlobal.ENABLE_SUPER_RESOLUTION) {
            TTVideoEngine.setIntValue(PLAYER_OPTION_ENABLE_BMF, 1); // enable bmf super resolution
        }

        TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_INT_NEED_SPEED_TEST_BY_TIMEINTERNAL, 1); // speed test

        File videoCacheDir = cacheDir(context);
        if (!videoCacheDir.exists()) videoCacheDir.mkdirs();
        VodConfig.Builder vodBuilder = new VodConfig.Builder(context)
                .setCacheDirPath(videoCacheDir.getAbsolutePath())
                .setMaxCacheSize(300 * 1024 * 1024);

        Env.init(new Config.Builder()
                .setApplicationContext(context)
                .setAppID(appInfo.appId)
                .setAppName(appInfo.appName)
                // 合法版本号应大于、等于 2 个分隔符，如："1.3.2"
                .setAppVersion(appInfo.appVersion)
                .setAppChannel(appInfo.appChannel)
                // 将 license 文件拷贝到 app 的 assets 文件夹中，并设置 LicenseUri
                // 下面 LicenseUri 对应工程中 assets 路径为：assets/license/vod.lic
                .setLicenseUri(appInfo.licenseUri)
                // 可不设置，默认值见下表
                .setVodConfig(vodBuilder.build())
                .build());
    }


    public static class AppInfo {
        public final String appId;
        public final String appName;
        public final String appRegion;
        public final String appChannel;
        public final String appVersion;
        public final String licenseUri;

        private AppInfo(Builder builder) {
            this.appId = builder.appId;
            this.appName = builder.appName;
            this.appRegion = builder.appRegion;
            this.appChannel = builder.appChannel;
            this.appVersion = builder.appVersion;
            this.licenseUri = builder.licenseUri;
        }

        public static class Builder {
            private String appId;
            private String appName;
            private String appRegion;
            private String appChannel;
            private String appVersion;
            private String licenseUri;

            public Builder setAppId(@NonNull String appId) {
                Asserts.checkNotNull(appId, "appId shouldn't be null");
                this.appId = appId;
                return this;
            }

            public Builder setAppName(@NonNull String appName) {
                Asserts.checkNotNull(appName, "appName shouldn't be null");
                this.appName = appName;
                return this;
            }

            public Builder setAppRegion(@NonNull String appRegion) {
                Asserts.checkNotNull(appRegion, "appRegion shouldn't be null");
                this.appRegion = appRegion;
                return this;
            }

            public Builder setAppChannel(@Nullable String appChannel) {
                this.appChannel = appChannel;
                return this;
            }

            public Builder setAppVersion(@NonNull String appVersion) {
                Asserts.checkNotNull(appVersion, "appVersion shouldn't be null");
                this.appVersion = appVersion;
                return this;
            }

            public Builder setLicenseUri(@NonNull String licenseUri) {
                Asserts.checkNotNull(licenseUri, "licenseUri shouldn't be null");
                this.licenseUri = licenseUri;
                return this;
            }

            public AppInfo build() {
                Asserts.checkNotNull(appId, "appId shouldn't be null");
                Asserts.checkNotNull(appName, "appName shouldn't be null");
                Asserts.checkNotNull(appRegion, "appRegion shouldn't be null");
                Asserts.checkNotNull(appVersion, "appVersion shouldn't be null");
                Asserts.checkNotNull(licenseUri, "licenseUri shouldn't be null");
                return new AppInfo(this);
            }
        }
    }
}
