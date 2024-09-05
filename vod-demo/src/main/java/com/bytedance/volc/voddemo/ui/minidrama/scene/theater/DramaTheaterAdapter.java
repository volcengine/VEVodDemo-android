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
 * Create Date : 2024/9/5
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.theater;

import static com.bytedance.playerkit.player.playback.DisplayModeHelper.calDisplayAspectRatio;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.impl.R;

import java.util.ArrayList;
import java.util.List;


public class DramaTheaterAdapter extends RecyclerView.Adapter<DramaTheaterAdapter.ViewHolder> {

    private final List<DramaInfo> mItems = new ArrayList<>();

    public void setItems(List<DramaInfo> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void appendItems(List<DramaInfo> items) {
        if (items != null && !items.isEmpty()) {
            int count = mItems.size();
            mItems.addAll(items);
            if (count > 0) {
                notifyItemRangeInserted(count, mItems.size());
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public List<DramaInfo> getItems() {
        return mItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public DramaInfo getItem(int position) {
        return mItems.get(position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        static ViewHolder create(ViewGroup parent) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_mini_drama_theater_grid_item, parent, false));
        }

        public final ImageView cover;
        private final DisplayModeHelper displayModeHelper;

        public final TextView title;
        public final TextView desc;
        public final TextView playCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.cover);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            playCount = itemView.findViewById(R.id.playCount);

            displayModeHelper = new DisplayModeHelper();
            displayModeHelper.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL);
            displayModeHelper.setDisplayView(cover);
            displayModeHelper.setContainerView((FrameLayout) cover.getParent());
        }

        public void bind(DramaInfo drama) {
            title.setText(drama.dramaTitle);
            desc.setText(String.format(desc.getResources().getString(R.string.vevod_mini_drama_theater_grid_item_total_desc), drama.totalEpisodeNumber));
            Glide.with(cover).load(drama.coverUrl).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    displayModeHelper.setDisplayAspectRatio(calDisplayAspectRatio(resource.getIntrinsicWidth(), resource.getIntrinsicHeight(), 0));
                    return false;
                }
            }).into(cover);
        }
    }
}