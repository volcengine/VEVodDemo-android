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

package com.bytedance.playerkit.player.ui.utils;

import java.util.Formatter;
import java.util.Locale;


public class TimeUtils {

    private static Formatter sFormatter;
    private static StringBuilder sFormatBuilder;

    /**
     * 格式化时间 timeMS -> HH:MM:SS
     *
     * @param timeMs 根据短视频的应用场景，使用int
     */
    public static String time2String(long timeMs) {
        if (timeMs < 0) {
            return ""; //or throw an exception
        }

        long totalSeconds = timeMs / 1000;

        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;

        if (sFormatter == null) {
            sFormatBuilder = new StringBuilder();
            sFormatter = new Formatter(sFormatBuilder, Locale.getDefault());
        }

        sFormatBuilder.setLength(0);
        if (hours > 0) {
            return sFormatter.format("%02d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return sFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }
}
