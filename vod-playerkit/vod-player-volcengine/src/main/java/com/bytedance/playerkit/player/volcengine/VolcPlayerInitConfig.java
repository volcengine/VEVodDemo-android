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
 * Create Date : 2025/2/28
 */

package com.bytedance.playerkit.player.volcengine;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.PlayerKit;
import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.SubtitleSelector;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;

import java.io.File;

public class VolcPlayerInitConfig {
    @NonNull
    public final Context context;
    @NonNull
    public final AppInfo appInfo;
    @Nullable
    public final PlayerKit.PlayerKitConfig playerKitConfig;
    @NonNull
    public final File playerCacheDir;
    @NonNull
    public final TTVideoEngineFactory videoEngineFactory;
    @NonNull
    public final CacheKeyFactory cacheKeyFactory;
    @NonNull
    public final TrackSelector trackSelector;
    @NonNull
    public final SubtitleSelector subtitleSelector;
    @NonNull
    public final VolcConfigUpdater configUpdater;
    @Nullable
    public final VolcSourceRefreshStrategy.VolcUrlRefreshFetcher.Factory urlRefreshFetcherFactory;
    @NonNull
    public final Handler workerHandler;

    private VolcPlayerInitConfig(Builder builder) {
        this.context = builder.context.getApplicationContext();
        this.appInfo = builder.appInfo;
        this.playerCacheDir = builder.playerCacheDir == null ? new File(context.getCacheDir(), VolcConfigGlobal.CacheDir.PLAYER_CACHE_DIR) : builder.playerCacheDir;
        this.cacheKeyFactory = builder.cacheKeyFactory == null ? CacheKeyFactory.DEFAULT : builder.cacheKeyFactory;
        if (builder.playerKitConfig == null) {
            this.playerKitConfig = new PlayerKit.PlayerKitConfig.Builder()
                    .setPlayerFactory(new VolcPlayerFactory())
                    .build();
        } else {
            this.playerKitConfig = builder.playerKitConfig;
        }
        this.videoEngineFactory = builder.ttVideoEngineFactory == null ? TTVideoEngineFactory.DEFAULT : builder.ttVideoEngineFactory;
        this.trackSelector = builder.trackSelector == null ? TrackSelector.DEFAULT : builder.trackSelector;
        this.subtitleSelector = builder.subtitleSelector == null ? new VolcSubtitleSelector() : builder.subtitleSelector;
        this.configUpdater = builder.configUpdater == null ? VolcConfigUpdater.DEFAULT : builder.configUpdater;
        this.urlRefreshFetcherFactory = builder.urlRefreshFetcherFactory;
        if (builder.workerHandler == null) {
            HandlerThread handlerThread = new HandlerThread("VolcPlayerThread");
            handlerThread.start();
            this.workerHandler = new Handler(handlerThread.getLooper());
        } else {
            this.workerHandler = builder.workerHandler;
        }
    }

    public static class Builder {
        private Context context;
        private AppInfo appInfo;
        private PlayerKit.PlayerKitConfig playerKitConfig;
        private File playerCacheDir;
        private TTVideoEngineFactory ttVideoEngineFactory;
        private CacheKeyFactory cacheKeyFactory;
        private TrackSelector trackSelector;
        private SubtitleSelector subtitleSelector;
        private VolcConfigUpdater configUpdater;
        private VolcSourceRefreshStrategy.VolcUrlRefreshFetcher.Factory urlRefreshFetcherFactory;
        private Handler workerHandler;

        public Builder setContext(@NonNull Context context) {
            this.context = Asserts.checkNotNull(context);
            return this;
        }

        public Builder setAppInfo(@NonNull AppInfo appInfo) {
            this.appInfo = Asserts.checkNotNull(appInfo);
            return this;
        }

        public Builder setPlayerKitConfig(@Nullable PlayerKit.PlayerKitConfig playerKitConfig) {
            this.playerKitConfig = playerKitConfig;
            return this;
        }

        public Builder setPlayerCacheDir(@Nullable File playerCacheDir) {
            this.playerCacheDir = playerCacheDir;
            return this;
        }

        public Builder setCacheKeyFactory(@Nullable CacheKeyFactory cacheKeyFactory) {
            this.cacheKeyFactory = cacheKeyFactory;
            return this;
        }

        public Builder setTTVideoEngineFactory(@Nullable TTVideoEngineFactory ttVideoEngineFactory) {
            this.ttVideoEngineFactory = ttVideoEngineFactory;
            return this;
        }

        public Builder setTrackSelector(@Nullable TrackSelector trackSelector) {
            this.trackSelector = trackSelector;
            return this;
        }

        public Builder setSubtitleSelector(@Nullable SubtitleSelector subtitleSelector) {
            this.subtitleSelector = subtitleSelector;
            return this;
        }

        public Builder setConfigUpdater(@Nullable VolcConfigUpdater volcConfigUpdater) {
            this.configUpdater = volcConfigUpdater;
            return this;
        }

        public Builder setUrlRefreshFetcherFactory(@Nullable VolcSourceRefreshStrategy.VolcUrlRefreshFetcher.Factory urlRefreshFetcherFactory) {
            this.urlRefreshFetcherFactory = urlRefreshFetcherFactory;
            return this;
        }

        public Builder setWorkerHandler(@Nullable Handler workerHandler) {
            this.workerHandler = workerHandler;
            return this;
        }

        public VolcPlayerInitConfig build() {
            Asserts.checkNotNull(context);
            Asserts.checkNotNull(appInfo);
            return new VolcPlayerInitConfig(this);
        }
    }

    public static class AppInfo {
        public final String appId;
        public final String appName;
        public final String appChannel;
        public final String appVersion;
        public final boolean enableInitAppLog;
        public final boolean autoStartAppLog;
        public final boolean enableAppLogSecurityApi;
        public final boolean enableVodSDKSecurityApi;
        public final String licenseUri;

        private AppInfo(Builder builder) {
            this.appId = builder.appId;
            this.appName = builder.appName;
            this.appChannel = builder.appChannel;
            this.appVersion = builder.appVersion;
            this.enableInitAppLog = builder.enableInitAppLog;
            this.autoStartAppLog = builder.autoStartAppLog;
            this.enableAppLogSecurityApi = builder.enableAppLogSecurityApi;
            this.enableVodSDKSecurityApi = builder.enableVodSDKSecurityApi;
            this.licenseUri = builder.licenseUri;
        }

        public static class Builder {
            private String appId;
            private String appName;
            private String appChannel;
            private String appVersion;
            private boolean enableInitAppLog = true;
            private boolean autoStartAppLog = true;
            private boolean enableAppLogSecurityApi = true;
            private boolean enableVodSDKSecurityApi;
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

            public Builder setAppChannel(@Nullable String appChannel) {
                this.appChannel = appChannel;
                return this;
            }

            public Builder setAppVersion(@NonNull String appVersion) {
                Asserts.checkNotNull(appVersion, "appVersion shouldn't be null");
                this.appVersion = appVersion;
                return this;
            }

            public Builder setEnableInitAppLog(boolean enableInitAppLog) {
                this.enableInitAppLog = enableInitAppLog;
                return this;
            }

            public Builder setAutoStartAppLog(boolean autoStartAppLog) {
                this.autoStartAppLog = autoStartAppLog;
                return this;
            }

            public Builder setEnableAppLogSecurityApi(boolean enableAppLogSecurityApi) {
                this.enableAppLogSecurityApi = enableAppLogSecurityApi;
                return this;
            }

            public Builder setEnableVodSDKSecurityApi(boolean enableVodSDKSecurityApi) {
                this.enableVodSDKSecurityApi = enableVodSDKSecurityApi;
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
                Asserts.checkNotNull(appVersion, "appVersion shouldn't be null");
                Asserts.checkNotNull(licenseUri, "licenseUri shouldn't be null");
                return new AppInfo(this);
            }
        }

        public static String dump(AppInfo appInfo) {
            return L.obj2String(appInfo) +
                    "[appId:" + appInfo.appId +
                    " appName:" + appInfo.appName +
                    " appChannel:" + appInfo.appChannel +
                    " appVersion:" + appInfo.appVersion +
                    " enableInitAppLog:" + appInfo.enableInitAppLog +
                    " autoStartAppLog:" + appInfo.autoStartAppLog +
                    " enableAppLogSecurityApi:" + appInfo.enableAppLogSecurityApi +
                    " enableVodSDKSecurityApi:" + appInfo.enableVodSDKSecurityApi +
                    " licenseUri:" + appInfo.licenseUri +
                    "]";
        }
    }
}
