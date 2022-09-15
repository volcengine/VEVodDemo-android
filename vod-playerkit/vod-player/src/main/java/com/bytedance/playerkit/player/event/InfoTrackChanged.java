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

package com.bytedance.playerkit.player.event;

import android.annotation.SuppressLint;

import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.event.Event;


public class InfoTrackChanged extends Event {
    @Track.TrackType
    public int trackType;
    public Track pre;
    public Track current;

    public InfoTrackChanged() {
        super(PlayerEvent.Info.TRACK_CHANGED);
    }


    public InfoTrackChanged init(@Track.TrackType int trackType, Track pre, Track current) {
        this.trackType = trackType;
        this.pre = pre;
        this.current = current;
        return this;
    }

    @SuppressLint("WrongConstant")
    @Override
    public void recycle() {
        super.recycle();
        trackType = 0;
        pre = null;
        current = null;
    }
}
