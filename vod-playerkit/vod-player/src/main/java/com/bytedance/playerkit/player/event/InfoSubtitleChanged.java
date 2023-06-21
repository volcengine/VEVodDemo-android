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
 * Create Date : 2023/6/12
 */

package com.bytedance.playerkit.player.event;

import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.utils.event.Event;

public class InfoSubtitleChanged extends Event {

    public Subtitle pre;
    public Subtitle current;


    public InfoSubtitleChanged() {
        super(PlayerEvent.Info.SUBTITLE_CHANGED);
    }

    public InfoSubtitleChanged init(Subtitle pre, Subtitle current) {
        this.pre = pre;
        this.current = current;
        return this;
    }

    @Override
    public void recycle() {
        super.recycle();
        this.pre = null;
        this.current = null;
    }
}
