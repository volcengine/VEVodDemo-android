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
 * Create Date : 2024/10/14
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.bytedance.volc.vod.scenekit.data.model.ItemType;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.viewholder.ShortVideoItemViewHolder;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;

public class ShortVideoViewHolderFactory implements ViewHolder.Factory {
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case ItemType.ITEM_TYPE_VIDEO:
                final ViewHolder holder = new ShortVideoItemViewHolder(new FrameLayout(parent.getContext()));
                holder.itemView.setTag(holder);
                return holder;
        }
        throw new IllegalArgumentException("unsupported type!");
    }
}
