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
 * Create Date : 2024/7/8
 */

package com.bytedance.volc.voddemo.ui.minidrama.data.business.model;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DramaItem implements Serializable {

    public static List<DramaItem> createByDramaInfos(List<DramaInfo> dramaInfos) {
        final List<DramaItem> items = new ArrayList<>();
        for (DramaInfo info : dramaInfos) {
            items.add(new DramaItem(info, 1));
        }
        return items;
    }

    public static List<DramaItem> createByEpisodeVideoItems(List<Item> items) {
        if (items == null) return null;
        final List<DramaItem> dramaItems = new ArrayList<>();
        for (Item item : items) {
            dramaItems.add(new DramaItem(item));
        }
        return dramaItems;
    }

    public static int findDramaItemPosition(List<DramaItem> dramaItems, Item item) {
        for (int i = 0; dramaItems != null && i < dramaItems.size(); i++) {
            DramaItem dramaItem = dramaItems.get(i);
            if (dramaItem != null && dramaItem.currentItem == item) {
                return i;
            }
        }
        return -1;
    }

    public final DramaInfo dramaInfo;
    public int currentEpisodeNumber;
    public Item currentItem;
    public List<Item> episodeVideoItems;
    public boolean episodesAllLoaded;
    public Item lastUnlockedItem;

    public DramaItem(Item currentItem) {
        this.currentItem = currentItem;
        this.dramaInfo = currentItem instanceof VideoItem ? EpisodeVideo.getDramaInfo(EpisodeVideo.get(currentItem)) : null;
    }

    public DramaItem(DramaInfo dramaInfo, int currentEpisodeNumber) {
        this.dramaInfo = dramaInfo;
        this.currentEpisodeNumber = currentEpisodeNumber;
    }

    public static String dump(DramaItem dramaItem) {
        if (dramaItem == null) return null;
        return DramaInfo.dump(dramaItem.dramaInfo);
    }
}
