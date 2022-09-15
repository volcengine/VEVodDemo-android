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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.settingskit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {

    private static final Map<String, List<SettingItem>> sMap = new HashMap<>();

    public static synchronized void put(String key, List<SettingItem> settings) {
        sMap.put(key, settings);
    }

    public static synchronized void putAll(String key, List<SettingItem> settings) {
        List<SettingItem> items = sMap.get(key);
        if (items != null) {
            items.addAll(settings);
        } else {
            items = settings;
        }
        sMap.put(key, items);
    }

    public static synchronized List<SettingItem> get(String key) {
        return sMap.get(key);
    }
}
