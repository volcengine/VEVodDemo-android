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

import static com.bytedance.playerkit.player.volcengine.VolcSuperResolutionStrategy.isInitSuperResolution;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.config.ABRQualityConfig;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.Numbers;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.abr.TTVideoABRConfig;
import com.ss.ttvideoengine.abr.TTVideoABRStrategy;
import com.ss.ttvideoengine.model.IVideoInfo;
import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.VideoRef;
import com.ss.ttvideoengine.selector.strategy.GearStrategy;
import com.ss.ttvideoengine.selector.strategy.GearStrategyConfig;
import com.ss.ttvideoengine.selector.strategy.IGearStrategyListener;
import com.ss.ttvideoengine.setting.SettingsHelper;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.superresolution.SRStrategyConfig;

import java.util.List;
import java.util.Map;

public class VolcQualityStrategy {

    interface Listener {
        default void onStartupTrackSelected(StartupTrackResult startupTrackResult) { /**/ }
    }

    static void init() {
        if (VolcConfigGlobal.ENABLE_STARTUP_ABR_INIT) {
            VolcNetSpeedStrategy.init();
            SettingsHelper.helper().addListener((s, i) -> initGlobalConfig());
            initGlobalConfig();
        } else if (VolcConfigGlobal.ENABLE_ABR_INIT) {
            TTVideoABRStrategy.init();
        }
    }

    static void initStartupABR(TTVideoEngine player, MediaSource mediaSource, Listener listener) {
        final VolcConfig volcConfig = VolcConfig.get(mediaSource);
        final VolcQualityConfig qualityConfig = volcConfig.qualityConfig;
        if (qualityConfig == null) return; // assert not possible

        // 开启 ABR 起播选档
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_GEAR_STRATEGY, 1);

        // ABR 起播选档与 ABR 不支持同时开启
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DASH_ABR, 0);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_HLS_ABR, 0);

        // 配置起播选档参数
        final GearStrategyConfig gearConfig = player.getGearStrategyEngineConfig();
        initStartupABRConfig(gearConfig, volcConfig);
        gearConfig.setGearStrategyListener(new IGearStrategyListener() {

            @Override
            public void onBeforeSelect(IVideoModel videoModel,
                                       Map<String, String> params,
                                       int selectType,
                                       Object userData) {
            }

            @Override
            public void onAfterSelect(IVideoModel videoModel,
                                      Map<String, String> map,
                                      int selectType,
                                      Object userData) {
                final StartupTrackResult result = new StartupTrackResult(mediaSource, map, videoModel);
                log(videoModel, "playback", result);
                listener.onStartupTrackSelected(result);
            }
        });

        // 超分降档
        if (isInitSuperResolution(volcConfig) &&
                qualityConfig.enableSupperResolutionDowngrade) {
            player.initSRStrategyConfig(createStartupABRSRConfig(volcConfig));
        }
    }

    public static boolean isEnableABR(VolcConfig volcConfig) {
        return VolcConfigGlobal.ENABLE_ABR_INIT &&
                VolcEditions.isSupportABR() &&
                VolcExtensions.isIntegrate(VolcExtensions.PLAYER_EXTENSION_ABR) &&
                volcConfig.qualityConfig != null &&
                volcConfig.qualityConfig.qualityMode == VolcQualityConfig.QUALITY_MODE_ABR;
    }

    public static boolean isEnableStartupABR(VolcConfig volcConfig) {
        return VolcConfigGlobal.ENABLE_STARTUP_ABR_INIT &&
                VolcEditions.isSupportStartUpABR() &&
                VolcExtensions.isIntegrate(VolcExtensions.PLAYER_EXTENSION_ABR) &&
                volcConfig.qualityConfig != null &&
                volcConfig.qualityConfig.qualityMode == VolcQualityConfig.QUALITY_MODE_STARTUP_ABR;
    }

    static boolean isEnableStartupABRSuperResolutionDowngrade(VolcConfig volcConfig) {
        return isEnableStartupABR(volcConfig) &&
                volcConfig.qualityConfig != null &&
                volcConfig.qualityConfig.enableSupperResolutionDowngrade;
    }

    private static void initGlobalConfig() {
        StrategyManager.instance().initGearGlobalConfig();
        //final GearStrategyConfig globalConfig = GearStrategy.getGlobalConfig();
        //globalConfig.setIntValue(GearStrategy.KEY_ABR_STARTUP_USE_CACHE, 2);
        //GearStrategy.setGlobalConfig(globalConfig);
        TTVideoABRStrategy.init();
    }

    private static void initStartupABRConfig(@NonNull GearStrategyConfig gearConfig, @NonNull VolcConfig volcConfig) {
        initGlobalConfig();

        gearConfig.setIntValue(GearStrategy.KEY_ABR_WITH_SR, isInitSuperResolution(volcConfig) ? 1 : 0);
        gearConfig.setIntValue(GearStrategy.KEY_QUICK_GET_FILE_CACHE, 1);

        VolcQualityConfig qualityConfig = volcConfig.qualityConfig;
        if (qualityConfig == null) return;

        ABRQualityConfig abrQualityConfig = volcConfig.qualityConfig.abrQualityConfig;
        gearConfig.setIntValue(GearStrategy.KEY_SCREEN_WIDTH, abrQualityConfig.screenWidth);
        gearConfig.setIntValue(GearStrategy.KEY_SCREEN_HEIGHT, abrQualityConfig.screenHeight);
        gearConfig.setIntValue(GearStrategy.KEY_DISPLAY_WIDTH, abrQualityConfig.displayWidth);
        gearConfig.setIntValue(GearStrategy.KEY_DISPLAY_HEIGHT, abrQualityConfig.displayHeight);

        Resolution mobile4GMaxResolution = VolcQuality.quality2Resolution(abrQualityConfig.mobileMaxQuality);
        Resolution wifiDefaultResolution = VolcQuality.quality2Resolution(abrQualityConfig.defaultQuality);
        Resolution wifiMaxResolution = VolcQuality.quality2Resolution(abrQualityConfig.wifiMaxQuality);
        Resolution userSelectedResolution = VolcQuality.quality2Resolution(qualityConfig.userSelectedQuality);

        if (mobile4GMaxResolution != null) {
            gearConfig.setIntValue(GearStrategy.KEY_4G_MAX_RESOLUTION, mobile4GMaxResolution.getIndex());
        }
        if (wifiDefaultResolution != null) {
            gearConfig.setIntValue(GearStrategy.KEY_WIFI_DEFAULT_RESOLUTION, wifiDefaultResolution.getIndex());
        }
        if (wifiMaxResolution != null) {
            gearConfig.setIntValue(GearStrategy.KEY_WIFI_MAX_RESOLUTION, wifiMaxResolution.getIndex());
        }
        if (userSelectedResolution != null) {
            gearConfig.setIntValue(GearStrategy.KEY_USER_EXPECTED_RESOLUTION, userSelectedResolution.getIndex());
        }
    }

    static StartupTrackResult selectStartupABR(int selectType, MediaSource mediaSource, IVideoModel videoModel) {
        VolcConfig volcConfig = VolcConfig.get(mediaSource);
        final GearStrategyConfig gearConfig = new GearStrategyConfig();
        initStartupABRConfig(gearConfig, volcConfig);
        if (isInitSuperResolution(volcConfig)) {
            gearConfig.setObjectValue(GearStrategy.KEY_SR_STRATEGY_CONFIG, createStartupABRSRConfig(volcConfig));
        }
        final Map<String, String> result = GearStrategy.select(videoModel, GearStrategy.GEAR_STRATEGY_SELECT_TYPE_PRELOAD, gearConfig);
        final StartupTrackResult gearResult = new StartupTrackResult(mediaSource, result, videoModel);
        log(videoModel, selectType == GearStrategy.GEAR_STRATEGY_SELECT_TYPE_PLAY ? "playback" : "preload", gearResult);
        return gearResult;
    }

    private static SRStrategyConfig createStartupABRSRConfig(VolcConfig volcConfig) {
        // 开启超分降档，默认仅支持 720 降档 480
        return new SRStrategyConfig()
                .enableSR(isEnableStartupABRSuperResolutionDowngrade(volcConfig));
    }

    static void setUserSelectedTrack(TTVideoEngine player, Track selected) {
        Resolution userSelectedResolution = selected == null ? Resolution.Undefine : Mapper.track2Resolution(selected);
        if (userSelectedResolution != null) {
            player.getGearStrategyEngineConfig().setIntValue(GearStrategy.KEY_USER_EXPECTED_RESOLUTION, userSelectedResolution.getIndex());
        }
    }

    private static void log(IVideoModel videoModel, String type, StartupTrackResult result) {
        L.d(VolcQualityStrategy.class, "select", "GearStrategy",
                type,
                videoModel.getVideoRefStr(VideoRef.VALUE_VIDEO_REF_VIDEO_ID),
                L.string(result.videoInfo == null ? null : result.videoInfo.getResolution()),
                L.string(result.originVideoInfo == null ? null : result.originVideoInfo.getResolution()),
                Mapper.dumpResolutionsLog(videoModel));
    }

    static class StartupTrackResult {
        @Nullable
        final Track originTrack;
        @Nullable
        final Track track;
        final int downgradeType;

        private final IVideoInfo originVideoInfo;
        private final IVideoInfo videoInfo;

        StartupTrackResult(MediaSource mediaSource, Map<String, String> map, IVideoModel videoModel) {
            final long videoBitrate = Numbers.safeParseInt(map.get(GearStrategy.GEAR_STRATEGY_KEY_VIDEO_BITRATE), 0);
            videoInfo = GearStrategy.getVideoInfo(videoModel, videoBitrate);
            downgradeType = Numbers.safeParseInt(map.get(GearStrategy.GEAR_STRATEGY_KEY_DOWNGRADE_TYPE), 0);
            final long originBitrate = Numbers.safeParseInt(map.get(GearStrategy.GEAR_STRATEGY_KEY_VIDEO_BITRATE_ORIGIN), 0);
            originVideoInfo = originBitrate > 0 ? GearStrategy.getVideoInfo(videoModel, originBitrate) : null;

            originTrack = Mapper.findTrackWithVideoInfo(mediaSource, originVideoInfo);
            track = Mapper.findTrackWithVideoInfo(mediaSource, videoInfo);
        }
    }
}
