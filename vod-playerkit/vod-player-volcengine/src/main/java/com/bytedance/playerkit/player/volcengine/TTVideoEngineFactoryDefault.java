/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/9/13
 */

package com.bytedance.playerkit.player.volcengine;

import static com.bytedance.playerkit.player.volcengine.VolcPlayer.EngineParams;
import static com.ss.ttvideoengine.TTVideoEngineInterface.IMAGE_LAYOUT_TO_FILL;
import static com.ss.ttvideoengine.TTVideoEngineInterface.PLAYER_OPTION_SEGMENT_FORMAT_FLAG;
import static com.ss.ttvideoengine.TTVideoEngineInterface.SEGMENT_FORMAT_FMP4;
import static com.ss.ttvideoengine.TTVideoEngineInterface.SEGMENT_FORMAT_MP4;

import android.content.Context;
import android.text.TextUtils;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.CollectionUtils;
import com.bytedance.playerkit.utils.L;
import com.ss.ttvideoengine.TTVideoEngine;

import java.util.HashMap;
import java.util.Map;

public class TTVideoEngineFactoryDefault implements TTVideoEngineFactory {

    @Override
    public TTVideoEngine create(Context context, MediaSource mediaSource) {
        final VolcConfig volcConfig = VolcConfig.get(mediaSource);
        final TTVideoEngine player;
        if (volcConfig.enableEngineLooper) {
            Map<String, Object> params = new HashMap<>();
            params.put("enable_looper", true);
            player = new TTVideoEngine(context, VolcEditions.engineCoreType(), params);
        } else {
            player = new TTVideoEngine(context, VolcEditions.engineCoreType());
        }
        return setup(context, player, mediaSource);
    }

    @Override
    public TTVideoEngine setup(Context context, TTVideoEngine player, MediaSource mediaSource) {
        final VolcConfig volcConfig = VolcConfig.get(mediaSource);

        player.setIntOption(TTVideoEngine.PLAYER_OPTION_OUTPUT_LOG, L.ENABLE_LOG ? 1 : 0);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DATALOADER, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_USE_VIDEOMODEL_CACHE, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DEBUG_UI_NOTIFY, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_INT_ENABLE_ERROR_THROW_OPTIMIZE, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_GET_POSITION_SKIP_LOOPER, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_POSITION_UPDATE_INTERVAL, 200);

        if (volcConfig.codecStrategyType == VolcConfig.CODEC_STRATEGY_DISABLE) {
            switch (volcConfig.playerDecoderType) {
                case Player.DECODER_TYPE_HARDWARE:
                    player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_HARDWARE_DECODE, 1);
                    break;
                case Player.DECODER_TYPE_SOFTWARE:
                    player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_HARDWARE_DECODE, 0);
                    break;
            }
            if (mediaSource.getSourceType() == MediaSource.SOURCE_TYPE_ID) {
                switch (volcConfig.sourceEncodeType) {
                    case Track.ENCODER_TYPE_H264:
                        player.setStringOption(TTVideoEngine.PLAYER_OPTION_STRING_SET_VIDEO_CODEC_TYPE, TTVideoEngine.CODEC_TYPE_H264);
                        break;
                    case Track.ENCODER_TYPE_H265:
                        player.setStringOption(TTVideoEngine.PLAYER_OPTION_STRING_SET_VIDEO_CODEC_TYPE, "h265");
                        break;
                    case Track.ENCODER_TYPE_H266:
                        player.setStringOption(TTVideoEngine.PLAYER_OPTION_STRING_SET_VIDEO_CODEC_TYPE, "h266");
                        break;
                }
            }
        }

        if (volcConfig.enableTextureRender) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_USE_TEXTURE_RENDER, 1);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_IMAGE_LAYOUT, IMAGE_LAYOUT_TO_FILL);
            if (volcConfig.enableTextureRenderUsingNativeWindow) {
                player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_TEXTURERENDER_NATIVEWINDOW, 1);
            }
        }

        if (volcConfig.enableHlsSeamlessSwitch) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH, 1);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_MASTER_M3U8_OPTIMIZE, 1);
        }
        if (volcConfig.enableDash) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DASH, 1);
        }
        if (volcConfig.enableMP4SeamlessSwitch) {
            player.setIntOption(PLAYER_OPTION_SEGMENT_FORMAT_FLAG, (1 << SEGMENT_FORMAT_FMP4) | (1 << SEGMENT_FORMAT_MP4));
        }
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_BASH, volcConfig.enableDash || volcConfig.enableMP4SeamlessSwitch ? 1 : 0);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_SET_TRACK_VOLUME, volcConfig.enableAudioTrackVolume ? 1 : 0);

        if (volcConfig.enableSeekEnd) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_SEEK_END, 1);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_SEEK_LASTFRAME, 1);
        }

        if (volcConfig.enableECDN &&
                VolcConfigGlobal.ENABLE_ECDN &&
                VolcExtensions.isIntegrate(VolcExtensions.PLAYER_EXTENSION_ECDN)) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_P2P_CDN_TYPE, 2);
        }

        if (volcConfig.enableSubtitle && (!CollectionUtils.isEmpty(mediaSource.getSubtitles())
                || !TextUtils.isEmpty(mediaSource.getSubtitleAuthToken()))) {
            // 字幕开关，启播或者播放过程中控制打开或者关闭字幕
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_OPEN_SUB, 1);
            // 字幕的线程是否开启，play之前调用
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_OPEN_SUB_THREAD, 1);
            // 使用字幕单路加载优化
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_OPT_SUB_LOAD_TIME, 1);
            EngineParams.get(player).mSubtitleEnabled = true;
        }

        if (volcConfig.enableFrameUpdateCallback) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_VIDEO_FRAME_META_CALLBACK, 1);
        }

        if (volcConfig.enable403SourceRefreshStrategy) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_INT_ENABLE_SOURCE_REFRESH_STRATEGY, 1);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_SEG_ERROR, 1);
        }
        // player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DEMUXER_RW_LOCK, 1);
        // player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_SEEK_INTERRUPT, 1);

        if (!TextUtils.isEmpty(volcConfig.tag)) {
            player.setTag(volcConfig.tag);
        }
        if (!TextUtils.isEmpty(volcConfig.subTag)) {
            player.setSubTag(volcConfig.subTag);
        }
        return player;
    }
}
