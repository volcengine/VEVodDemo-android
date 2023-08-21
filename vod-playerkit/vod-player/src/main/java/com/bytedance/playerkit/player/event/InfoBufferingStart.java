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

import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.utils.event.Event;


public class InfoBufferingStart extends Event {

    public static final int BUFFERING_TYPE_IO = 0;
    public static final int BUFFERING_TYPE_DECODER = 1;

    public static final int BUFFERING_STAGE_BEFORE_FIRST_FRAME = 0;
    public static final int BUFFERING_STAGE_AFTER_FIRST_FRAME = 1;

    public static final int BUFFERING_REASON_DEFAULT = 0;
    public static final int BUFFERING_REASON_SEEK = 1;
    public static final int BUFFERING_REASON_TRACK_CHANGE = 2;
    public int bufferId;

    public int bufferingType;
    public int bufferingStage;
    public int bufferingReason;

    public InfoBufferingStart() {
        super(PlayerEvent.Info.BUFFERING_START);
    }

    public InfoBufferingStart init(int bufferId, int bufferingType, int bufferingStage, int bufferingReason) {
        this.bufferId = bufferId;
        this.bufferingType = bufferingType;
        this.bufferingStage = bufferingStage;
        this.bufferingReason = bufferingReason;
        return this;
    }

    @Override
    public void recycle() {
        super.recycle();
        this.bufferId = 0;
        this.bufferingType = 0;
        this.bufferingStage = 0;
        this.bufferingReason = 0;
    }

    public static String mapBufferingType(int bufferingType) {
        switch (bufferingType) {
            case BUFFERING_TYPE_IO:
                return "io";
            case BUFFERING_TYPE_DECODER:
                return "decoder";
        }
        return null;
    }

    public static String mapBufferingStage(int bufferingStage) {
        switch (bufferingStage) {
            case BUFFERING_STAGE_BEFORE_FIRST_FRAME:
                return "before";
            case BUFFERING_STAGE_AFTER_FIRST_FRAME:
                return "after";
        }
        return null;
    }

    public static String mapBufferingReason(int bufferingReason) {
        switch (bufferingReason) {
            case BUFFERING_REASON_DEFAULT:
                return "default";
            case BUFFERING_REASON_SEEK:
                return "seek";
            case BUFFERING_REASON_TRACK_CHANGE:
                return "track";
        }
        return null;
    }
}
