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


public class InfoDataSourceRefreshed extends Event {

    public static final int REFRESHED_TYPE_PLAY_INFO_FETCHED = 1;
    public static final int REFRESHED_TYPE_SUBTITLE_INFO_FETCHED = 2;
    public static final int REFRESHED_TYPE_MASK_INFO_FETCHED = 3;

    public int mRefreshedType;

    public InfoDataSourceRefreshed() {
        super(PlayerEvent.Info.DATA_SOURCE_REFRESHED);
    }

    public InfoDataSourceRefreshed init(int refreshedType) {
        this.mRefreshedType = refreshedType;
        return this;
    }

    @Override
    public void recycle() {
        super.recycle();
        mRefreshedType = 0;
    }
}
