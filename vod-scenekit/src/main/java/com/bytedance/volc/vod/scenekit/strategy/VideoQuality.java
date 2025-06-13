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

import com.bytedance.playerkit.player.config.ABRQualityConfig;
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
    public static VolcQualityConfig sceneQualityConfig(int volcScene) {
        final VolcQualityConfig config = new VolcQualityConfig();
        final int abrType = VideoSettings.intValue(VideoSettings.QUALITY_ENABLE_ABR);
        if (abrType == VideoSettings.ABRType.ABR_TYPE_ABR) {
            config.qualityMode = VolcQualityConfig.QUALITY_MODE_ABR;
        } else if (abrType == VideoSettings.ABRType.ABR_TYPE_STARTUP_ABR) {
            config.qualityMode = VolcQualityConfig.QUALITY_MODE_STARTUP_ABR;
        } else if (abrType == VideoSettings.ABRType.ABR_TYPE_STARTUP_ABR_AND_SR_DOWNGRADE) {
            config.qualityMode = VolcQualityConfig.QUALITY_MODE_STARTUP_ABR;
            config.enableSupperResolutionDowngrade = true;
        }
        config.abrQualityConfig = sceneABRQualityConfig(volcScene);
        return config;
    }

    @NonNull
    public static ABRQualityConfig sceneABRQualityConfig(int volcScene) {
        switch (volcScene) {
            case VolcScene.SCENE_SHORT_VIDEO: {
                ABRQualityConfig abrQualityConfig = new ABRQualityConfig();
                abrQualityConfig.defaultQuality = VolcQuality.QUALITY_480P;
                abrQualityConfig.wifiMaxQuality = VolcQuality.QUALITY_1080P;
                abrQualityConfig.mobileMaxQuality = VolcQuality.QUALITY_1080P;

                final int screenWidth = UIUtils.getScreenWidth(VolcPlayerInit.config().context);
                final int screenHeight = UIUtils.getScreenHeight(VolcPlayerInit.config().context);

                abrQualityConfig.screenWidth = Math.min(screenWidth, screenHeight);
                abrQualityConfig.screenHeight = Math.max(screenWidth, screenHeight);

                abrQualityConfig.displayWidth = Math.min(screenWidth, screenHeight);
                abrQualityConfig.displayHeight = (int) (abrQualityConfig.displayWidth * 16f / 9);
                return abrQualityConfig;
            }
            case VolcScene.SCENE_FULLSCREEN: {
                ABRQualityConfig abrQualityConfig = new ABRQualityConfig();
                abrQualityConfig.defaultQuality = VolcQuality.QUALITY_480P;
                abrQualityConfig.wifiMaxQuality = VolcQuality.QUALITY_1080P;
                abrQualityConfig.mobileMaxQuality = VolcQuality.QUALITY_1080P;

                final int screenWidth = UIUtils.getScreenWidth(VolcPlayerInit.config().context);
                final int screenHeight = UIUtils.getScreenHeight(VolcPlayerInit.config().context);

                abrQualityConfig.screenWidth = Math.max(screenWidth, screenHeight);
                abrQualityConfig.screenHeight = Math.min(screenWidth, screenHeight);

                abrQualityConfig.displayHeight = Math.min(screenWidth, screenHeight);
                abrQualityConfig.displayWidth = (int) (abrQualityConfig.displayHeight * 16f / 9);
                return abrQualityConfig;
            }
            case VolcScene.SCENE_UNKNOWN:
            case VolcScene.SCENE_LONG_VIDEO:
            case VolcScene.SCENE_DETAIL_VIDEO:
            case VolcScene.SCENE_FEED_VIDEO:
            default: {
                ABRQualityConfig abrQualityConfig = new ABRQualityConfig();
                abrQualityConfig.defaultQuality = VolcQuality.QUALITY_480P;
                abrQualityConfig.wifiMaxQuality = VolcQuality.QUALITY_540P;
                abrQualityConfig.mobileMaxQuality = VolcQuality.QUALITY_540P;

                final int screenWidth = UIUtils.getScreenWidth(VolcPlayerInit.config().context);
                final int screenHeight = UIUtils.getScreenHeight(VolcPlayerInit.config().context);

                abrQualityConfig.screenWidth = Math.min(screenWidth, screenHeight);
                abrQualityConfig.screenHeight = Math.max(screenWidth, screenHeight);

                abrQualityConfig.displayWidth = Math.min(screenWidth, screenHeight);
                abrQualityConfig.displayHeight = (int) (abrQualityConfig.displayWidth / 16f * 9);
                return abrQualityConfig;
            }
        }
    }
}
