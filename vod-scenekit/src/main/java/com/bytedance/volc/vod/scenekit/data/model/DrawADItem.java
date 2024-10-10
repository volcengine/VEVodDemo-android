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
 * Create Date : 2024/10/10
 */

package com.bytedance.volc.vod.scenekit.data.model;

import android.text.TextUtils;

import com.bytedance.playerkit.utils.ExtraObject;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;

import java.io.Serializable;

public class DrawADItem extends ExtraObject implements Item, Serializable {
    public final String adId;
    public final int adIndex;

    public DrawADItem(String adId, int adIndex) {
        this.adId = adId;
        this.adIndex = adIndex;
    }

    public static boolean itemEquals(DrawADItem item1, DrawADItem item2) {
        if (item1 == item2) return true;
        if (item1 == null || item2 == null) return false;
        return TextUtils.equals(item1.adId, item2.adId);
    }

    public String dump() {
        return L.obj2String(this) + " " + adId + " " + adIndex;
    }

    public static String dump(DrawADItem item) {
        if (!L.ENABLE_LOG) return null;
        if (item == null) return null;
        return item.dump();
    }

    @Override
    public int itemType() {
        return ItemType.ITEM_TYPE_AD;
    }
}
