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


import com.ss.ttvideoengine.TTVideoEngine;

public class VolcSettings {
    public static boolean PLAYER_OPTION_HARDWARE_DECODER_ASYNC_INIT = true;

    public static boolean OPTION_USE_AUDIO_TRACK_VOLUME = false;

    public static boolean PLAYER_OPTION_USE_TEXTURE_RENDER = true;

    public static boolean PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH = true;

    public static boolean PLAYER_OPTION_ENABLE_DASH = true;

    public static boolean PLAYER_OPTION_OUTPUT_LOG = false;

    public static boolean PLAYER_OPTION_ASYNC_PLAYER = true;

    public static Boolean PLAYER_OPTION_ENABLE_HARDWARE_DECODE;

    public static String PLAYER_OPTION_STRING_SET_VIDEO_CODEC_TYPE = TTVideoEngine.CODEC_TYPE_H264;

    public static boolean PLAYER_OPTION_ENABLE_SEEK_END = true;

    public static boolean PLAYER_OPTION_ENABLE_SUPER_RESOLUTION_ABILITY = true;
}
