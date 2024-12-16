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

package com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.volc.vod.scenekit.data.model.DrawADItem;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;
import com.bytedance.volc.voddemo.ui.ad.api.Ad;
import com.bytedance.volc.voddemo.ui.ad.api.AdMapper;
import com.bytedance.volc.voddemo.ui.ad.mock.MockShortVideoAdVideoView;

import java.util.List;

public class ShortVideoDrawADItemViewHolder extends ViewHolder {
    public final FrameLayout videoViewContainer;
    public final MockShortVideoAdVideoView mockAdVideoView;
    public DrawADItem mItem;

    public ShortVideoDrawADItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.videoViewContainer = (FrameLayout) itemView;
        this.videoViewContainer.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        this.mockAdVideoView = new MockShortVideoAdVideoView(itemView.getContext());
        this.videoViewContainer.addView(mockAdVideoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void bind(List<Item> items, int position) {

        final DrawADItem item = (DrawADItem) items.get(position);
        if (mItem != item) {
            mItem = item;
            Ad ad = AdMapper.instance().get(item);
            final VideoItem videoItem = ad == null ? null : ad.get();
            if (videoItem == null) return;
            mockAdVideoView.bind(ad);
        }
    }

    @Override
    public Item getBindingItem() {
        return mItem;
    }
}
