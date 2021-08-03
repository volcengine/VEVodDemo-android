/*
 * Copyright 2021 bytedance
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
 * Create Date : 2021/2/24
 */
package com.bytedance.volc.voddemo.videoview.layer;

public interface IVideoLayerEvent {

    /**
     * player state
     **/
    int VIDEO_LAYER_EVENT_CALL_PLAY = 100;
    int VIDEO_LAYER_EVENT_VIDEO_RELEASE = 101;
    int VIDEO_LAYER_EVENT_PLAY_COMPLETE = 102;
    int VIDEO_LAYER_EVENT_INTERCEPT_PLAY = 103;
    int VIDEO_LAYER_EVENT_PLAY_START = 104;
    int VIDEO_LAYER_EVENT_PLAY_PLAYING = 105;
    int VIDEO_LAYER_EVENT_PLAY_PAUSE = 106;
    int VIDEO_LAYER_EVENT_BUFFER_START = 107;
    int VIDEO_LAYER_EVENT_BUFFER_UPDATE = 108;
    int VIDEO_LAYER_EVENT_BUFFER_END = 109;
    int VIDEO_LAYER_EVENT_PLAY_PREPARE = 110;
    int VIDEO_LAYER_EVENT_PLAY_PREPARED = 111;
    int VIDEO_LAYER_EVENT_RENDER_START = 112;
    int VIDEO_LAYER_EVENT_PLAY_ERROR = 113;
    int VIDEO_LAYER_EVENT_LOOP_START = 114;
    int VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE = 115;

    /**
     * Stream Change
     */
    int VIDEO_LAYER_EVENT_STREAM_CHANGED = 117;
    int VIDEO_LAYER_EVENT_AUTO_RESOLUTION_CHANGE = 119;
    int VIDEO_LAYER_EVENT_TRY_PLAY_MAIN = 120;

    /**
     * Playback Progress
     */
    int VIDEO_LAYER_EVENT_PROGRESS_CHANGE = 200;
    int VIDEO_LAYER_EVENT_DEFINITION_CHANGE = 201;
    int VIDEO_LAYER_EVENT_REPLAY = 202;
    int VIDEO_LAYER_EVENT_RETRY = 203;

    int VIDEO_LAYER_EVENT_SEEK_START = 207;
    int VIDEO_LAYER_EVENT_SEEK_COMPLETE = 208;
    int VIDEO_PLAY_SPEED_CHANGE = 209;

    /**
     * UI
     */
    int VIDEO_LAYER_EVENT_FULLSCREEN_CHANGE = 300;
    int VIDEO_LAYER_EVENT_VIDEO_VIEW_CLICK = 304;
    int VIDEO_LAYER_EVENT_SHOW_SPEED = 305;
    int VIDEO_LAYER_EVENT_SHOW_DOWNLOAD = 306;
    int VIDEO_LAYER_EVENT_SHOW_RESOLUTION = 308;
    int VIDEO_LAYER_EVENT_TOGGLE_DEBUG_TOOL = 309;
    int VIDEO_LAYER_EVENT_START_TRACK = 310;
    int VIDEO_LAYER_EVENT_STOP_TRACK = 311;
    int VIDEO_LAYER_EVENT_TRACK_PROGRESS_CHANGE = 312;

    int VIDEO_LAYER_EVENT_PREPARE_DETAIL = 400;
    int VIDEO_LAYER_EVENT_ENTER_DETAIL = 401;

    /**
     * message
     */
    int VIDEO_LAYER_EVENT_MASK_INFO = 500;
    int VIDEO_LAYER_EVENT_MASK_ENABLE = 501;
    int VIDEO_LAYER_EVENT_RADIO_MODE_ENABLE = 502;

    int getType();

    Object getParam();

    <T> T getParam(Class<T> clazz);
}
