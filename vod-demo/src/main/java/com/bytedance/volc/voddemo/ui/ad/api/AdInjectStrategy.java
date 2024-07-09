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
 * Create Date : 2024/10/18
 */

package com.bytedance.volc.voddemo.ui.ad.api;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.DrawADItem;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;

import java.util.List;

public final class AdInjectStrategy {
    private static AdLoadStrategy sAdLoadStrategy;

    public static void init(AdLoader.Factory factory) {
        if (sAdLoadStrategy == null) {
            final int prefetchMaxCount = VideoSettings.intValue(VideoSettings.AD_VIDEO_PREFETCH_MAX_COUNT);
            L.d(AdInjectStrategy.class, "init", "prefetchMaxCount", prefetchMaxCount);
            sAdLoadStrategy = new AdLoadStrategy(prefetchMaxCount, factory);
        }
    }

    public static boolean isEnabled() {
        return sAdLoadStrategy != null;
    }

    private int mTotal;
    private int mAdIndex;

    public void injectAd(boolean reset, List<Item> items) {
        int interval = VideoSettings.intValue(VideoSettings.AD_VIDEO_SHOW_INTERVAL);
        injectAd(reset, interval, items);
    }

    public void injectAd(boolean reset, int injectInterval, List<Item> items) {
        if (sAdLoadStrategy == null) return;
        sAdLoadStrategy.start();

        injectInterval = Math.max(injectInterval, 2);

        if (items == null) return;
        int lastIndex;
        if (reset) {
            mTotal = items.size();
            lastIndex = 0;
            mAdIndex = 0;
        } else {
            lastIndex = mTotal;
            mTotal += items.size();
        }
        int index = 0;
        int lastPointIndex;
        while ((lastPointIndex = lastIndex + index) < mTotal) {
            if (lastPointIndex % (injectInterval) == injectInterval - 1) {
                DrawADItem adItem = AdMapper.instance().create(sAdLoadStrategy, mAdIndex);
                if (adItem != null) {
                    items.add(index, adItem);
                    mAdIndex++;
                    mTotal++;
                }
                L.d(this, "injectAd", lastPointIndex, adItem);
            }
            index++;
        }
    }
}
