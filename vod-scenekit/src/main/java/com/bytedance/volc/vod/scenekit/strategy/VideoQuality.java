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
 * Create Date : 2023/5/30
 */

package com.bytedance.volc.vod.scenekit.strategy;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.volcengine.VolcConfig;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.player.volcengine.VolcQuality;
import com.bytedance.playerkit.player.volcengine.VolcQualityConfig;
import com.bytedance.playerkit.player.volcengine.VolcQualityStrategy;
import com.bytedance.playerkit.player.volcengine.VolcScene;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.vod.settingskit.Option;

import java.util.Arrays;
import java.util.List;

public class VideoQuality {
    public static final int VIDEO_QUALITY_DEFAULT = Quality.QUALITY_RES_720;

    public static String qualityDesc(int qualityRes) {
        if (qualityRes == 0) {
            return "未选择";
        }
        Quality quality = VolcQuality.quality(qualityRes);
        if (quality != null) {
            return quality.getQualityDesc();
        }
        return "UnKnown";
    }

    public static final List<Integer> QUALITY_RES_ARRAY_USER_SELECTED = Arrays.asList(
            Quality.QUALITY_RES_DEFAULT,
            Quality.QUALITY_RES_360,
            Quality.QUALITY_RES_480,
            Quality.QUALITY_RES_540,
            Quality.QUALITY_RES_720,
            Quality.QUALITY_RES_1080
    );

    public static final List<Integer> QUALITY_RES_ARRAY_DEFAULT = Arrays.asList(
            Quality.QUALITY_RES_360,
            Quality.QUALITY_RES_480,
            Quality.QUALITY_RES_540,
            Quality.QUALITY_RES_720,
            Quality.QUALITY_RES_1080
    );

    public static int getUserSelectedQualityRes(MediaSource mediaSource) {
        VideoItem videoItem = VideoItem.get(mediaSource);
        if (videoItem != null) {
            return VideoQuality.getUserSelectedQualityRes(videoItem.getPlayScene());
        }
        return Quality.QUALITY_RES_DEFAULT;
    }

    public static int getUserSelectedQualityRes(int playScene) {
        return VideoSettings.intValue(VideoSettings.QUALITY_VIDEO_QUALITY_USER_SELECTED);
    }

    public static void setUserSelectedQualityRes(int playScene, @Quality.QualityRes int qualityRes) {
        Option option = VideoSettings.option(VideoSettings.QUALITY_VIDEO_QUALITY_USER_SELECTED);
        option.userValues().saveValue(option, qualityRes);
    }

    public static boolean isEnableStartupABR(MediaSource mediaSource) {
        return mediaSource != null &&
                (mediaSource.getSourceType() == MediaSource.SOURCE_TYPE_MODEL || mediaSource.getSourceType() == MediaSource.SOURCE_TYPE_ID) &&
                VolcQualityStrategy.isEnableStartupABR(VolcConfig.get(mediaSource));
    }

    @NonNull
    public static VolcQualityConfig sceneGearConfig(int volcScene) {
        final VolcQualityConfig config = new VolcQualityConfig();
        config.enableStartupABR = VideoSettings.intValue(VideoSettings.QUALITY_ENABLE_STARTUP_ABR) >= 1;
        config.enableSupperResolutionDowngrade = VideoSettings.intValue(VideoSettings.QUALITY_ENABLE_STARTUP_ABR) == 2;
        switch (volcScene) {
            case VolcScene.SCENE_SHORT_VIDEO: {
                config.defaultQuality = VolcQuality.QUALITY_720P;
                config.wifiMaxQuality = VolcQuality.QUALITY_720P;
                config.mobileMaxQuality = VolcQuality.QUALITY_480P;

                final VolcQualityConfig.VolcDisplaySizeConfig displaySizeConfig = new VolcQualityConfig.VolcDisplaySizeConfig();
                config.displaySizeConfig = displaySizeConfig;

                final int screenWidth = UIUtils.getScreenWidth(VolcPlayerInit.getContext());
                final int screenHeight = UIUtils.getScreenHeight(VolcPlayerInit.getContext());

                displaySizeConfig.screenWidth = screenWidth;
                displaySizeConfig.screenHeight = screenHeight;
                displaySizeConfig.displayWidth = (int) (screenHeight / 16f * 9);
                displaySizeConfig.displayHeight = screenHeight;
                return config;
            }
            case VolcScene.SCENE_FULLSCREEN: {
                config.defaultQuality = VolcQuality.QUALITY_480P;
                config.wifiMaxQuality = VolcQuality.QUALITY_1080P;
                config.mobileMaxQuality = VolcQuality.QUALITY_720P;

                final VolcQualityConfig.VolcDisplaySizeConfig displaySizeConfig = new VolcQualityConfig.VolcDisplaySizeConfig();
                config.displaySizeConfig = displaySizeConfig;

                final int screenWidth = UIUtils.getScreenWidth(VolcPlayerInit.getContext());
                final int screenHeight = UIUtils.getScreenHeight(VolcPlayerInit.getContext());

                displaySizeConfig.screenWidth = screenWidth;
                displaySizeConfig.screenHeight = screenHeight;
                displaySizeConfig.displayWidth = Math.max(screenWidth, screenHeight);
                displaySizeConfig.displayHeight = (int) (Math.max(screenWidth, screenHeight) / 16f * 9);
                return config;
            }
            case VolcScene.SCENE_UNKNOWN:
            case VolcScene.SCENE_LONG_VIDEO:
            case VolcScene.SCENE_DETAIL_VIDEO:
            case VolcScene.SCENE_FEED_VIDEO:
            default: {
                config.defaultQuality = VolcQuality.QUALITY_480P;
                config.wifiMaxQuality = VolcQuality.QUALITY_540P;
                config.mobileMaxQuality = VolcQuality.QUALITY_360P;

                final VolcQualityConfig.VolcDisplaySizeConfig displaySizeConfig = new VolcQualityConfig.VolcDisplaySizeConfig();
                config.displaySizeConfig = displaySizeConfig;

                final int screenWidth = UIUtils.getScreenWidth(VolcPlayerInit.getContext());
                final int screenHeight = UIUtils.getScreenHeight(VolcPlayerInit.getContext());

                displaySizeConfig.screenWidth = screenWidth;
                displaySizeConfig.screenHeight = screenHeight;
                displaySizeConfig.displayWidth = Math.min(screenWidth, screenHeight);
                displaySizeConfig.displayHeight = (int) (Math.min(screenWidth, screenHeight) / 16f * 9);
                return config;
            }
        }
    }
}
