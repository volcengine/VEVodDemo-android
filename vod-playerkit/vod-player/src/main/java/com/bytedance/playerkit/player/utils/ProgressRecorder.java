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

package com.bytedance.playerkit.player.utils;

import java.util.HashMap;
import java.util.Map;


public class ProgressRecorder {

    private static final Map<String, Long> map = new HashMap<>();

    public static void recordProgress(String key, long progress) {
        if (progress >= 0) {
            map.put(key, progress);
        }
    }

    public static void removeProgress(String key) {
        if (key == null) return;
        map.remove(key);
    }

    public static long getProgress(String key) {
        if (key == null) return -1;
        final Long value = map.get(key);
        if (value == null) return -1;
        return value;
    }
}
