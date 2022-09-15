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

package com.bytedance.playerkit.player.playback;

public interface PlaybackEvent {

    class Action {
        public static final int PREPARE_PLAYBACK = 10001;
        public static final int START_PLAYBACK = 10002;
        public static final int STOP_PLAYBACK = 10003;
    }

    class State {
        public static final int BIND_PLAYER = 20001;
        public static final int UNBIND_PLAYER = 20002;
        public static final int BIND_VIDEO_VIEW = 20003;
        public static final int UNBIND_VIDEO_VIEW = 20004;
    }
}
