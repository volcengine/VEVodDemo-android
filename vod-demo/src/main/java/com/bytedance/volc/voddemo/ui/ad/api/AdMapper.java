/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/10/21
 */

package com.bytedance.volc.voddemo.ui.ad.api;

import com.bytedance.volc.vod.scenekit.data.model.DrawADItem;

import java.util.HashMap;
import java.util.Map;

public final class AdMapper {
    private final static AdMapper sAdMapper = new AdMapper();

    public static AdMapper instance() {
        return sAdMapper;
    }

    public final Map<String, Ad> mAdMap = new HashMap<>();

    public Ad get(DrawADItem item) {
        return get(item.adId);
    }

    public Ad get(String key) {
        return mAdMap.get(key);
    }

    public DrawADItem create(AdLoadStrategy strategy, int adIndex) {
        final Ad ad = strategy.remove();
        if (ad == null) return null;
        final DrawADItem item = new DrawADItem(ad.id(), adIndex);
        mAdMap.put(ad.id(), ad);
        return item;
    }
}
