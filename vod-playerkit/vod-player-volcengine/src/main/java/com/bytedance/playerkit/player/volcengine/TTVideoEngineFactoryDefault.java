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

import static com.ss.ttvideoengine.ITTVideoEngineInternal.PLAYER_TYPE_OWN;
import static com.ss.ttvideoengine.TTVideoEngineInterface.IMAGE_LAYOUT_TO_FILL;

import android.content.Context;

import com.bytedance.playerkit.player.source.MediaSource;
import com.ss.ttvideoengine.TTVideoEngine;

import java.util.HashMap;
import java.util.Map;

public class TTVideoEngineFactoryDefault implements TTVideoEngineFactory {

    @Override
    public TTVideoEngine create(Context context, MediaSource mediaSource) {

        onTTVideoEngineWillCreate(context, mediaSource);

        final TTVideoEngine player;
        if (VolcSettings.PLAYER_OPTION_ASYNC_PLAYER) {
            Map<String, Object> params = new HashMap<>();
            params.put("enable_looper", true);
            player = new TTVideoEngine(context, PLAYER_TYPE_OWN, params);
        } else {
            player = new TTVideoEngine(context, PLAYER_TYPE_OWN);
        }
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DATALOADER, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_USE_VIDEOMODEL_CACHE, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DEBUG_UI_NOTIFY, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_DASH, 1);
        player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_BASH, 1);

        if (VolcSettings.PLAYER_OPTION_USE_TEXTURE_RENDER) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_USE_TEXTURE_RENDER, 1);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_IMAGE_LAYOUT, IMAGE_LAYOUT_TO_FILL);
        }

        if (VolcSettings.PLAYER_OPTION_ENABLE_HARDWARE_DECODE != null) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_HARDWARE_DECODE, VolcSettings.PLAYER_OPTION_ENABLE_HARDWARE_DECODE ? 1 : 0);
        }

        player.setStringOption(TTVideoEngine.PLAYER_OPTION_STRING_SET_VIDEO_CODEC_TYPE, VolcSettings.PLAYER_OPTION_STRING_SET_VIDEO_CODEC_TYPE);

        player.setIntOption(TTVideoEngine.PLAYER_OPTION_OUTPUT_LOG, VolcSettings.PLAYER_OPTION_OUTPUT_LOG ? 1 : 0);

        if (VolcSettings.PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH, 1);
        }

        player.setIntOption(TTVideoEngine.PLAYER_OPTION_SET_TRACK_VOLUME, VolcSettings.OPTION_USE_AUDIO_TRACK_VOLUME ? 1 : 0);

        if (VolcSettings.PLAYER_OPTION_ENABLE_SEEK_END) {
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_KEEP_FORMAT_THREAD_ALIVE, 1);
            player.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_SEEK_END, 1);
        }

        if (VolcSettings.PLAYER_OPTION_HARDWARE_DECODER_ASYNC_INIT) {
            if (mediaSource.getSourceType() == MediaSource.SOURCE_TYPE_ID) {
                // for vid
                player.setAsyncInit(true, -1);
            } else {
                // TODO for direct url
                // player.setAsyncInit(true, 1); h265
                // player.setAsyncInit(true, 0); h264
            }
        }
        return player;
    }

    protected void onTTVideoEngineWillCreate(Context context, MediaSource mediaSource) {
    }
}
