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
 * Create Date : 2024/10/17
 */

package com.bytedance.volc.vod.scenekit.data.utils;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.data.model.DrawADItem;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Comparator;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;

import java.util.ArrayList;
import java.util.List;

public class ItemHelper {

    private static final Comparator<Item> ITEM_COMPARATOR = new Comparator<Item>() {
        @Override
        public boolean compare(Item o1, Item o2) {
            if (o1 == o2) {
                return true;
            }
            if (o1 instanceof VideoItem && o2 instanceof VideoItem) {
                return VideoItem.itemEquals((VideoItem) o1, (VideoItem) o2);
            }
            if (o1 instanceof DrawADItem && o2 instanceof DrawADItem) {
                return DrawADItem.itemEquals((DrawADItem)o1, (DrawADItem)o2);
            }
            return false;
        }
    };

    public static List<Item> toItems(List<? extends Item> list) {
        if (list == null) return null;
        return new ArrayList<>(list);
    }

    public static Comparator<Item> comparator() {
        return ITEM_COMPARATOR;
    }

    public static String dump(List<Item> items) {
        if (!L.ENABLE_LOG) return null;
        if (items == null) return null;

        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append(dump(item)).append("\n");
        }
        return sb.toString();
    }

    public static Object dump(Item item) {
        if (item instanceof VideoItem) {
            return VideoItem.dump((VideoItem) item);
        }
        if (item instanceof DrawADItem) {
            return DrawADItem.dump((DrawADItem) item);
        }
        return L.obj2String(item);
    }
}
