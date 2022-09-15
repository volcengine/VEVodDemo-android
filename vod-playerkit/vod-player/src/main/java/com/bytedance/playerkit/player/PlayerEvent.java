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

package com.bytedance.playerkit.player;

import com.bytedance.playerkit.player.event.ActionPause;
import com.bytedance.playerkit.player.event.ActionPrepare;
import com.bytedance.playerkit.player.event.ActionRelease;
import com.bytedance.playerkit.player.event.ActionSeekTo;
import com.bytedance.playerkit.player.event.ActionSetLooping;
import com.bytedance.playerkit.player.event.ActionSetSpeed;
import com.bytedance.playerkit.player.event.ActionSetSurface;
import com.bytedance.playerkit.player.event.ActionStart;
import com.bytedance.playerkit.player.event.ActionStop;
import com.bytedance.playerkit.player.event.InfoAudioRenderingStart;
import com.bytedance.playerkit.player.event.InfoBufferingEnd;
import com.bytedance.playerkit.player.event.InfoBufferingStart;
import com.bytedance.playerkit.player.event.InfoBufferingUpdate;
import com.bytedance.playerkit.player.event.InfoCacheUpdate;
import com.bytedance.playerkit.player.event.InfoDataSourceRefreshed;
import com.bytedance.playerkit.player.event.InfoProgressUpdate;
import com.bytedance.playerkit.player.event.InfoSeekComplete;
import com.bytedance.playerkit.player.event.InfoSeekingStart;
import com.bytedance.playerkit.player.event.InfoTrackChanged;
import com.bytedance.playerkit.player.event.InfoTrackInfoReady;
import com.bytedance.playerkit.player.event.InfoTrackWillChange;
import com.bytedance.playerkit.player.event.InfoVideoRenderingStart;
import com.bytedance.playerkit.player.event.InfoVideoRenderingStartBeforeStart;
import com.bytedance.playerkit.player.event.InfoVideoSARChanged;
import com.bytedance.playerkit.player.event.InfoVideoSizeChanged;
import com.bytedance.playerkit.player.event.StateCompleted;
import com.bytedance.playerkit.player.event.StateError;
import com.bytedance.playerkit.player.event.StateIDLE;
import com.bytedance.playerkit.player.event.StatePaused;
import com.bytedance.playerkit.player.event.StatePrepared;
import com.bytedance.playerkit.player.event.StatePreparing;
import com.bytedance.playerkit.player.event.StateReleased;
import com.bytedance.playerkit.player.event.StateStarted;
import com.bytedance.playerkit.player.event.StateStopped;
import com.bytedance.playerkit.utils.event.Event;

public interface PlayerEvent {

    /**
     * Player action event type constants.
     *
     * @see Event#code()
     */
    class Action {
        /**
         * @see ActionSetSurface
         */
        public static final int SET_SURFACE = 1001;
        /**
         * @see ActionPrepare
         */
        public static final int PREPARE = 1002;
        /**
         * @see ActionStart
         */
        public static final int START = 1003;
        /**
         * @see ActionPause
         */
        public static final int PAUSE = 1004;
        /**
         * @see ActionStop
         */
        public static final int STOP = 1005;
        /**
         * @see ActionRelease
         */
        public static final int RELEASE = 1006;
        /**
         * @see ActionSeekTo
         */
        public static final int SEEK_TO = 1007;
        /**
         * @see ActionSetLooping
         */
        public static final int SET_LOOPING = 1008;
        /**
         * @see ActionSetSpeed
         */
        public static final int SET_SPEED = 1009;
    }

    /**
     * Player state event type constants.
     *
     * @see Event#code()
     */
    class State {
        /**
         * @see StateIDLE
         */
        public static final int IDLE = 2001;
        /**
         * @see StatePreparing
         */
        public static final int PREPARING = 2002;
        /**
         * @see StatePrepared
         */
        public static final int PREPARED = 2003;
        /**
         * @see StateStarted
         */
        public static final int STARTED = 2004;
        /**
         * @see StatePaused
         */
        public static final int PAUSED = 2005;
        /**
         * @see StateStopped
         */
        public static final int STOPPED = 2006;
        /**
         * @see StateReleased
         */
        public static final int RELEASED = 2007;
        /**
         * @see StateCompleted
         */
        public static final int COMPLETED = 2008;
        /**
         * @see StateError
         */
        public static final int ERROR = 2009;
    }

    /**
     * Player info event type constants.
     *
     * @see Event#code()
     */
    class Info {
        /**
         * @see InfoDataSourceRefreshed
         */
        public static final int DATA_SOURCE_REFRESHED = 3001;
        /**
         * @see InfoVideoSizeChanged
         */
        public static final int VIDEO_SIZE_CHANGED = 3002;
        /**
         * @see InfoVideoSARChanged
         */
        public static final int VIDEO_SAR_CHANGED = 3003;
        /**
         * @see InfoVideoRenderingStart
         */
        public static final int VIDEO_RENDERING_START = 3004;
        /**
         * @see InfoAudioRenderingStart
         */
        public static final int AUDIO_RENDERING_START = 3005;

        /**
         * @see InfoVideoRenderingStartBeforeStart
         */
        public static final int VIDEO_RENDERING_START_BEFORE_START = 3006;

        /**
         * @see InfoBufferingStart
         */
        public static final int BUFFERING_START = 3007;
        /**
         * @see InfoBufferingEnd
         */
        public static final int BUFFERING_END = 3008;
        /**
         * @see InfoBufferingUpdate
         */
        public static final int BUFFERING_UPDATE = 3009;
        /**
         * @see InfoSeekingStart
         */
        public static final int SEEKING_START = 3010;
        /**
         * @see InfoSeekComplete
         */
        public static final int SEEK_COMPLETE = 3011;
        /**
         * @see InfoProgressUpdate
         */
        public static final int PROGRESS_UPDATE = 3012;
        /**
         * @see InfoTrackInfoReady
         */
        public static final int TRACK_INFO_READY = 3013;
        /**
         * @see InfoTrackWillChange
         */
        public static final int TRACK_WILL_CHANGE = 3014;
        /**
         * @see InfoTrackChanged
         */
        public static final int TRACK_CHANGED = 3015;
        /**
         * @see InfoCacheUpdate
         */
        public static final int CACHE_UPDATE = 3016;
    }
}
