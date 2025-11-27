/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/6/13
 */

package com.bytedance.playerkit.player.config;

import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.utils.L;

import java.io.Serializable;

public class ABRQualityConfig implements Serializable {
    public int screenWidth;
    public int screenHeight;
    public int displayWidth;
    public int displayHeight;

    public Quality defaultQuality;
    public Quality wifiMaxQuality;
    public Quality mobileMaxQuality;

    public String dump() {
        return L.obj2String(this) + " Screen(" + screenWidth + "x" + screenHeight + ")" + " Display(" + displayWidth + "x" + displayHeight + ")" + " default=" + Quality.dump(defaultQuality, false) + " wifiMax=" + Quality.dump(wifiMaxQuality, false) + " mobileMax=" + Quality.dump(mobileMaxQuality, false);
    }

    public static String dump(ABRQualityConfig config) {
        if (!L.ENABLE_LOG) return null;
        if (config == null) return null;
        return config.dump();
    }
}
