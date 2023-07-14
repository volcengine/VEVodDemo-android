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
 * Create Date : 2023/6/28
 */

package com.bytedance.playerkit.player.event;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.utils.event.Event;

public class InfoFrameInfoUpdate extends Event {
    @Player.FrameType
    public int frameType;
    public long pts;
    public long clockTime;

    public InfoFrameInfoUpdate() {
        super(PlayerEvent.Info.FRAME_INFO_UPDATE);
    }

    public InfoFrameInfoUpdate init(@Player.FrameType int frameType, long pts, long clockTime) {
        this.frameType = frameType;
        this.pts = pts;
        this.clockTime = clockTime;
        return this;
    }

    @Override
    public void recycle() {
        super.recycle();
        frameType = Player.FRAME_TYPE_UNKNOWN;
        pts = 0;
        clockTime = 0;
    }
}
