/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/5/24
 */

package com.bytedance.playerkit.player.volcengine;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.config.ABRQualityConfig;
import com.ss.ttvideoengine.abr.TTVideoABRConfig;
import com.ss.ttvideoengine.abr.TTVideoABRStartupConfig;
import com.ss.ttvideoengine.abr.TTVideoABRStrategy;
import com.ss.ttvideoengine.setting.SettingsHelper;

public class VolcQualityStrategy {

    static void init() {
        if (VolcConfigGlobal.ENABLE_ABR_INIT || VolcConfigGlobal.ENABLE_STARTUP_ABR_INIT) {
            TTVideoABRStrategy.init();
            SettingsHelper.helper().addListener((s, i) -> TTVideoABRStrategy.init());
        }
    }

    public static boolean isEnableABR(VolcConfig volcConfig) {
        return VolcConfigGlobal.ENABLE_ABR_INIT &&
                VolcEditions.isSupportABR() &&
                VolcExtensions.isIntegrate(VolcExtensions.PLAYER_EXTENSION_ABR) &&
                volcConfig.qualityConfig != null &&
                volcConfig.qualityConfig.qualityMode == VolcQualityConfig.QUALITY_MODE_ABR &&
                volcConfig.qualityConfig.abrQualityConfig != null;
    }

    public static boolean isEnableStartupABR(VolcConfig volcConfig) {
        return VolcConfigGlobal.ENABLE_STARTUP_ABR_INIT &&
                VolcEditions.isSupportStartUpABR() &&
                VolcExtensions.isIntegrate(VolcExtensions.PLAYER_EXTENSION_ABR) &&
                volcConfig.qualityConfig != null &&
                volcConfig.qualityConfig.qualityMode == VolcQualityConfig.QUALITY_MODE_STARTUP_ABR &&
                volcConfig.qualityConfig.abrQualityConfig != null;
    }

    static boolean isEnableStartupABRSuperResolutionDowngrade(VolcConfig volcConfig) {
        return isEnableStartupABR(volcConfig) &&
                volcConfig.qualityConfig != null &&
                volcConfig.qualityConfig.enableSuperResolutionDowngrade &&
                VolcSuperResolutionStrategy.isInitSuperResolution(volcConfig);
    }

    @NonNull
    public static TTVideoABRConfig createTTVideoABRConfig(@NonNull ABRQualityConfig abrConfig) {
        TTVideoABRConfig videoABRConfig = new TTVideoABRConfig();
        videoABRConfig.screenWidth = abrConfig.screenWidth;
        videoABRConfig.screenHeight = abrConfig.screenHeight;
        videoABRConfig.displayWidth = abrConfig.displayWidth;
        videoABRConfig.displayHeight = abrConfig.displayHeight;
        videoABRConfig.defaultResolution = VolcQuality.quality2Resolution(abrConfig.defaultQuality);
        videoABRConfig.mobileMaxResolution = VolcQuality.quality2Resolution(abrConfig.mobileMaxQuality);
        videoABRConfig.wifiMaxResolution = VolcQuality.quality2Resolution(abrConfig.wifiMaxQuality);
        return videoABRConfig;
    }


    @NonNull
    public static TTVideoABRStartupConfig createTTVideoABRStartupConfig(VolcConfig volcConfig) {
        TTVideoABRStartupConfig startupConfig = new TTVideoABRStartupConfig();
        startupConfig.screenWidth = volcConfig.qualityConfig.abrQualityConfig.screenWidth;
        startupConfig.screenHeight = volcConfig.qualityConfig.abrQualityConfig.screenHeight;
        startupConfig.displayWidth = volcConfig.qualityConfig.abrQualityConfig.displayWidth;
        startupConfig.displayHeight = volcConfig.qualityConfig.abrQualityConfig.displayHeight;
        startupConfig.defaultResolution = VolcQuality.quality2Resolution(volcConfig.qualityConfig.abrQualityConfig.defaultQuality);
        startupConfig.mobileMaxResolution = VolcQuality.quality2Resolution(volcConfig.qualityConfig.abrQualityConfig.mobileMaxQuality);
        startupConfig.wifiMaxResolution = VolcQuality.quality2Resolution(volcConfig.qualityConfig.abrQualityConfig.wifiMaxQuality);
        startupConfig.userSelectedResolution = VolcQuality.quality2Resolution(volcConfig.qualityConfig.userSelectedQuality);
        startupConfig.enableSuperResolutionDowngrade = isEnableStartupABRSuperResolutionDowngrade(volcConfig);
        return startupConfig;
    }
}
