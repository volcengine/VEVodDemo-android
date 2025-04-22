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
import com.ss.texturerender.VideoSurface;
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
            File root = new File(context.getFilesDir(), VolcConfigGlobal.FilesDir.PLAYER_BMF_SR_DIR);
            final File binDir = new File(root, "bin");
            final File cacheDir = new File(root, "cache");
            if (!binDir.exists()) binDir.mkdirs();
            if (!cacheDir.exists()) cacheDir.mkdirs();

            final Bundle bundle = new Bundle();
            bundle.putInt(TextureRenderKeys.KEY_IS_ACTION, TextureRenderKeys.TEXTURE_OPTION_INIT_EFFECT);
            bundle.putInt(TextureRenderKeys.KEY_IS_EFFECT_TYPE, TextureRenderKeys.TYPE_EFFECT_SUPPER_RESOLUTION);
            bundle.putInt(TextureRenderKeys.KEY_IS_USE_BMF_DIRECTINVOKE, 1);
            bundle.putInt(TextureRenderKeys.KEY_IS_USE_BMF_COMPONENT, 1);
            bundle.putInt(TextureRenderKeys.KEY_SR_ALG_TYPE, VideoSurface.SUPER_RES_STAT_BMF_SR_HP_V3_6);
            bundle.putInt(TextureRenderKeys.KEY_SR_BMF_BACKEND, VideoSurface.SUPER_RES_STAT_BMF_BACKEND_OPENGL);
            // 设置超分倍数：
            // 1.5 倍超分：VideoSurface.SUPER_SCALE_TYPE_1_5（推荐）
            // 2 倍超分： VideoSurface.SUPER_SCALE_TYPE_2_0
            bundle.putInt(TextureRenderKeys.KEY_BMF_SCALE_TYPE, VideoSurface.SUPER_SCALE_TYPE_1_5);
            bundle.putInt(TextureRenderKeys.KEY_BMFSR_POOLSIZE, 2);
            bundle.putString(TextureRenderKeys.KEY_KERNEL_BIN_PATH, binDir.getAbsolutePath());
            bundle.putString(TextureRenderKeys.KEY_BMF_PROGRAM_CACHE_DIR, cacheDir.getAbsolutePath());
            // 需要在 openTextureSR 之前调用
            player.setEffect(bundle);

            // 视频分辨率长边超过 1440 不开启超分
            player.setSrMaxTextureSize(1440, 1440);
            // 是否异步初始化超分, 这里建议设置为 false
            // 若设置为 true，只有在开启超分的时候才会开启 textureRender
            player.asyncInitSR(srConfig.enableAsyncInitSuperResolution);
            // 超分播放忽视分辨率限制，设置为 true
            player.ignoreSRResolutionLimit(true);
            // 设置播放过程中可动态控制关闭 or 开启超分
            player.dynamicControlSR(true);
        }

        // 开启超分
        if (!VolcQualityStrategy.isEnableStartupABRSuperResolutionDowngrade(volcConfig)) {
            setEnabled(player, isEnableSuperResolutionOnStartup(volcConfig));
        }
    }

    static void setEnabled(TTVideoEngine player, boolean enabled) {
        if (EngineParams.get(player).mSuperResolutionInitialized) {
            // 内部有机型白名单限制，不在名单内的机型不会开启超分
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
