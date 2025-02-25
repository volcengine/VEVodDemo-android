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

import static com.bytedance.playerkit.player.volcengine.VolcExtensions.PLAYER_EXTENSION_SUPER_RESOLUTION;
import static com.bytedance.playerkit.player.volcengine.VolcExtensions.isIntegrate;
import static com.bytedance.playerkit.player.volcengine.VolcPlayer.EngineParams;
import static com.ss.ttvideoengine.TTVideoEngineInterface.PLAYER_OPTION_ENABLE_BMF;

import android.content.Context;
import android.os.Bundle;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.ss.texturerender.TextureRenderKeys;
import com.ss.ttvideoengine.TTVideoEngine;

import java.io.File;

public class VolcSuperResolutionStrategy {
    static void init() {
        if (isIntegrate(PLAYER_EXTENSION_SUPER_RESOLUTION)) {
            TTVideoEngine.setIntValue(PLAYER_OPTION_ENABLE_BMF, 1); // enable bmf super resolution
        }
    }

    static boolean isInitSuperResolution(VolcConfig volcConfig) {
        return isIntegrate(PLAYER_EXTENSION_SUPER_RESOLUTION) &&
                VolcEditions.isSupportSuperResolution() &&
                volcConfig != null &&
                volcConfig.enableTextureRender &&
                volcConfig.superResolutionConfig != null &&
                volcConfig.superResolutionConfig.enableSuperResolutionAbility;
    }

    static boolean isEnableSuperResolutionOnStartup(VolcConfig volcConfig) {
        return isInitSuperResolution(volcConfig) &&
                volcConfig != null &&
                volcConfig.superResolutionConfig != null &&
                volcConfig.superResolutionConfig.enableSuperResolutionOnStartup;
    }

    static void initSuperResolution(Context context, TTVideoEngine player, MediaSource mediaSource, Track track) {
        final VolcConfig volcConfig = VolcConfig.get(mediaSource);

        if (!isInitSuperResolution(volcConfig)) return;

        final VolcSuperResolutionConfig srConfig = volcConfig.superResolutionConfig;

        // 初始化超分
        if (!EngineParams.get(player).mSuperResolutionInitialized) {
            EngineParams.get(player).mSuperResolutionInitialized = true;
            // 必须要保障该文件夹路径是存在的，并且可读写的
            File file = new File(context.getFilesDir(), VolcSuperResolutionConfig.sSuperResolutionBinPath);
            if (!file.exists()) {
                file.mkdirs();
            }
            // 配置允许超分的最大分辨率
            float displayAspectRatio = Mapper.displayAspectRatio(mediaSource, track);
            if (displayAspectRatio > 1) { // 横版
                player.setSrMaxTextureSize(Math.max(srConfig.maxTextureWidth, srConfig.maxTextureHeight),
                        Math.min(srConfig.maxTextureWidth, srConfig.maxTextureHeight));
            } else if (displayAspectRatio > 0) { // 竖版
                player.setSrMaxTextureSize(Math.min(srConfig.maxTextureWidth, srConfig.maxTextureHeight),
                        Math.max(srConfig.maxTextureWidth, srConfig.maxTextureHeight));
            } else {
                player.setSrMaxTextureSize(srConfig.maxTextureWidth, srConfig.maxTextureHeight);
            }
            // 是否异步初始化超分, 这里设置为 false
            // 若设置为 true，只有在开启超分的时候才会开启 textureRender
            player.asyncInitSR(srConfig.enableAsyncInitSuperResolution);
            // 设置播放过程中可动态控制关闭 or 开启超分
            player.dynamicControlSR(true);
            // algType 取值:
            //  5：bmf v1 效果好
            //  6：bmf v2 功耗低
            player.setSRInitConfig(5, file.getAbsolutePath(), "SR", "SR", 2, 0, 0);
            // 超分播放忽视分辨率限制，推荐使用
            player.ignoreSRResolutionLimit(true);
            //player.setIntOption(TTVideoEngine.PLAYER_OPTION_OPEN_TEXTURE_AFTER_FIRST_FRAME, 1);
            // 开启 mali gpu 优化
            if (srConfig.enableSuperResolutionMaliGPUOpt) {
                Bundle bundle = new Bundle();
                bundle.putBoolean(TextureRenderKeys.KEY_SR_IS_MALI_SYNC, false);
                player.setLensParams(bundle);
            }
        }

        // 开启超分
        if (!VolcQualityStrategy.isEnableStartupABRSuperResolutionDowngrade(volcConfig)) {
            setEnabled(player, isEnableSuperResolutionOnStartup(volcConfig));
        }
    }

    static void setEnabled(TTVideoEngine player, boolean enabled) {
        if (EngineParams.get(player).mSuperResolutionInitialized) {
            player.openTextureSR(true, enabled);
            if (player.isPrepared() && player.getPlaybackState() == TTVideoEngine.PLAYBACK_STATE_PAUSED) {
                player.forceDraw();
            }
        }
    }

    static boolean isEnabled(TTVideoEngine player) {
        return player != null && EngineParams.get(player).mSuperResolutionInitialized && player.isplaybackUsedSR();
    }
}
