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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.voddemo.ui.video.scene.longvideo;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.volc.vod.scenekit.utils.TimeUtils;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.impl.R;

import java.util.ArrayList;
import java.util.List;


public class LongVideoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    interface OnItemClickListener {
        void onItemClick(Item item, RecyclerView.ViewHolder holder);

        void onHeaderItemClick(VideoItem item, RecyclerView.ViewHolder holder);
    }

    private final OnItemClickListener mItemClickListener;

    public LongVideoAdapter(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public static class Item {
        final static int TYPE_HEADER_BANNER = 1;
        final static int TYPE_GROUP_TITLE = 2;
        final static int TYPE_VIDEO_ITEM = 3;

        int type;
        String title;
        List<VideoItem> videoItems;
        VideoItem videoItem;
    }

    private final List<Item> mItems = new ArrayList<>();

    public void setItems(List<Item> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyDataSetChanged();
    }

    public void appendItems(List<Item> items) {
        int count = mItems.size();
        mItems.addAll(items);
        notifyItemRangeInserted(count, items.size());
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isFullSpanType(getItemViewType(position))) {
                        return gridLayoutManager.getSpanCount();
                    } else if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        int position = holder.getBindingAdapterPosition();
        int type = getItemViewType(position);
        if (isFullSpanType(type)) {
            ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
            if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams lp = (StaggeredGridLayoutManager.LayoutParams) layoutParams;
                lp.setFullSpan(true);
            }
        }
    }

    private boolean isFullSpanType(int type) {
        return type == Item.TYPE_HEADER_BANNER || type == Item.TYPE_GROUP_TITLE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case Item.TYPE_HEADER_BANNER:
                return HeaderViewHolder.create(parent);
            case Item.TYPE_GROUP_TITLE:
                return GroupTitleViewHolder.create(parent);
            case Item.TYPE_VIDEO_ITEM:
                return VideoItemViewHolder.create(parent);
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Item item = getItem(position);
        ((ItemViewHolder) holder).bind(position, item, mItems, mItemClickListener);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).type;
    }

    @NonNull
    private Item getItem(int position) {
        return mItems.get(position);
    }

    abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bind(int position, T item, List<T> items, OnItemClickListener onItemClickListener);
    }

    static abstract class ItemViewHolder extends ViewHolder<Item> {

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    static class VideoItemViewHolder extends ItemViewHolder {
        ImageView cover;
        TextView duration;
        TextView title;
        TextView desc;
        DisplayModeHelper displayModeHelper;

        public VideoItemViewHolder(@NonNull View itemView) {
            super(itemView);
            cover = itemView.findViewById(R.id.cover);
            duration = itemView.findViewById(R.id.duration);
            title = itemView.findViewById(R.id.title);
            desc = itemView.findViewById(R.id.desc);
            desc.setVisibility(View.GONE);
            displayModeHelper = new DisplayModeHelper();
            displayModeHelper.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL_X);
            displayModeHelper.setDisplayView(cover);
            displayModeHelper.setContainerView((FrameLayout) cover.getParent());
        }

        @Override
        void bind(int position, Item item, List<Item> items, OnItemClickListener onItemClickListener) {
            VideoItem videoItem = item.videoItem;
            Glide.with(cover).load(videoItem.getCover()).listener(new RequestListener<Drawable>() {
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
            duration.setText(TimeUtils.time2String(videoItem.getDuration()));
            title.setText(videoItem.getTitle());
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(item, VideoItemViewHolder.this);
                }
            });
        }

        static RecyclerView.ViewHolder create(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.vevod_long_video_item, parent, false);
            return new VideoItemViewHolder(itemView);
        }
    }

    static class GroupTitleViewHolder extends ItemViewHolder {

        TextView title;

        public GroupTitleViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }

        @Override
        void bind(int position, Item item, List<Item> items, OnItemClickListener onItemClickListener) {
            title.setText(item.title);
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(item, GroupTitleViewHolder.this);
                }
            });
        }

        static RecyclerView.ViewHolder create(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.vevod_long_video_item_group_title, parent, false);
            return new GroupTitleViewHolder(itemView);
        }
    }

    static class HeaderViewHolder extends ItemViewHolder {
        ViewPager2 viewPager2;
        HeaderAdapter adapter;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPager2 = itemView.findViewById(R.id.viewPager2);
            adapter = new HeaderAdapter();
            viewPager2.setAdapter(adapter);
        }

        @Override
        void bind(int position, Item item, List<Item> items, OnItemClickListener onItemClickListener) {
            adapter.bind(item, onItemClickListener);
        }

        static RecyclerView.ViewHolder create(ViewGroup parent) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.vevod_long_video_item_header, parent, false);
            return new HeaderViewHolder(itemView);
        }

        static class HeaderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

            private Item mItem;
            private OnItemClickListener mListener;

            public void bind(Item item, OnItemClickListener listener) {
                if (mItem != item) {
                    this.mItem = item;
                    this.mListener = listener;
                    notifyDataSetChanged();
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return HeaderViewItemViewHolder.create(parent);
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                ((HeaderViewItemViewHolder) holder).bind(position, getItem(position), mItem.videoItems, mListener);
            }

            @Override
            public int getItemCount() {
                if (mItem != null) {
                    List<VideoItem> list = mItem.videoItems;
                    if (list != null) {
                        return list.size();
                    }
                }
                return 0;
            }

            private VideoItem getItem(int position) {
                if (mItem != null) {
                    List<VideoItem> list = mItem.videoItems;
                    if (list != null) {
                        return list.get(position);
                    }
                }
                throw new NullPointerException("mItem is null");
            }
        }

        static class HeaderViewItemViewHolder extends ViewHolder<VideoItem> {
            ImageView cover;
            TextView title;
            DisplayModeHelper displayModeHelper;
            RequestListener<Drawable> requestListener;

            static RecyclerView.ViewHolder create(ViewGroup parent) {
                return new HeaderViewItemViewHolder(
                        LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.vevod_long_video_item_header_item,
                                        parent,
                                        false));
            }

            public HeaderViewItemViewHolder(@NonNull View itemView) {
                super(itemView);
                cover = itemView.findViewById(R.id.cover);
                cover.setScaleType(ImageView.ScaleType.FIT_XY);
                title = itemView.findViewById(R.id.title);
                displayModeHelper = new DisplayModeHelper();
                displayModeHelper.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL);
                displayModeHelper.setDisplayView(cover);
                displayModeHelper.setContainerView((FrameLayout) cover.getParent());
                requestListener = new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        displayModeHelper.setDisplayAspectRatio(calDisplayAspectRatio(resource.getIntrinsicWidth(), resource.getIntrinsicHeight(), 0));
                        return false;
                    }
                };
            }

            @Override
            void bind(int position, VideoItem item, List<VideoItem> items, OnItemClickListener onItemClickListener) {
                Glide.with(cover).load(item.getCover()).listener(requestListener).into(cover);
                title.setText(item.getTitle());

                itemView.setOnClickListener(v -> {
                    if (onItemClickListener != null) {
                        onItemClickListener.onHeaderItemClick(item, HeaderViewItemViewHolder.this);
                    }
                });
            }
        }

    }
}
