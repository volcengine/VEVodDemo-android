/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/11/2
 */

package com.bytedance.volc.vod.scenekit.utils;

public class TimeUtils {

    /**
     * 格式化时间 timeMS -> HH:MM:SS
     */
    public static String time2String(long timeMs) {
        if (timeMs < 0) {
            return "";
        }
        long totalSeconds = timeMs / 1000;

        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        StringBuilder timeString = new StringBuilder();
        if (hours > 0) {
            if (hours < 10) {
                timeString.append('0');
            }
            timeString.append(hours).append(':');
        }

        if (minutes < 10) {
            timeString.append('0');
        }
        timeString.append(minutes).append(':');

        if (seconds < 10) {
            timeString.append('0');
        }
        timeString.append(seconds);

        return timeString.toString();
    }
}
