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

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.scene.VideoViewFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ShortVideoAdapter extends RecyclerView.Adapter<ShortVideoAdapter.ViewHolder> {

    private final ArrayList<VideoItem> mItems = new ArrayList<>();

    private final Map<Integer, WeakReference<ShortVideoAdapter.ViewHolder>> mHolders = new HashMap<>();

    private VideoViewFactory mVideoViewFactory;

    public void setVideoViewFactory(VideoViewFactory videoViewFactory) {
        this.mVideoViewFactory = videoViewFactory;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<VideoItem> videoItems) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mItems.size();
            }

            @Override
            public int getNewListSize() {
                return videoItems.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                VideoItem oldOne = mItems.get(oldItemPosition);
                VideoItem newOne = videoItems.get(newItemPosition);
                return VideoItem.mediaEquals(oldOne, newOne);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return true;
            }

            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                return new Object();
            }
        }, false);

        diffResult.dispatchUpdatesTo(new AdapterListUpdateCallback(this));
        mItems.clear();
        mItems.addAll(videoItems);
    }

    public void prependItems(List<VideoItem> videoItems) {
        if (videoItems != null && !videoItems.isEmpty()) {
            mItems.addAll(0, videoItems);
            notifyItemRangeInserted(0, videoItems.size());
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void appendItems(List<VideoItem> videoItems) {
        if (videoItems != null && !videoItems.isEmpty()) {
            int count = mItems.size();
            mItems.addAll(videoItems);
            if (count > 0) {
                notifyItemRangeInserted(count, mItems.size());
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public void deleteItem(int position) {
        if (position >= 0 && position < mItems.size()) {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void deleteItems(int position, int count) {
        if (position >= 0 && position < mItems.size()) {
            List<VideoItem> items = new ArrayList<>(mItems);
            items.removeAll(items.subList(position, Math.min(position + count, items.size())));
            setItems(items);
        }
    }

    public void replaceItem(int position, VideoItem videoItem) {
        if (0 <= position && position < mItems.size()) {
            mItems.set(position, videoItem);
            notifyItemChanged(position, new Object() /*Prevent Adapter calling onCreateViewHolder}*/);
        }
    }

    public void replaceItems(int position, List<VideoItem> videoItems) {
        if (0 <= position && position < mItems.size()) {
            for (int i = 0; i < videoItems.size(); i++) {
                mItems.set(position + i, videoItems.get(i));
            }
            notifyItemRangeChanged(position, videoItems.size(), new Object() /*Prevent Adapter calling onCreateViewHolder}*/);
        }
    }

    public VideoItem getItem(int position) {
        return mItems.get(position);
    }

    public List<VideoItem> getItems() {
        return mItems;
    }

    public ViewHolder getViewHolder(int position) {
        WeakReference<ViewHolder> ref = mHolders.get(position);
        if (ref != null) {
            return ref.get();
        }
        return null;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return ViewHolder.create(parent, mVideoViewFactory);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final VideoItem videoItem = mItems.get(position);
        L.d(this, "onBindViewHolder", position, holder, holder.videoItem, videoItem);
        holder.bind(position, videoItem);

        final WeakReference<ShortVideoAdapter.ViewHolder> ref = mHolders.get(position);
        if (ref == null || ref.get() != holder) {
            mHolders.put(position, new WeakReference<>(holder));
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.videoView.stopPlayback();

    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.videoView.stopPlayback();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final VideoView videoView;
        public final FrameLayout videoViewContainer;
        public VideoItem videoItem;

        public static ViewHolder create(ViewGroup parent, VideoViewFactory videoViewFactory) {
            FrameLayout frameLayout = new FrameLayout(parent.getContext());
            frameLayout.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
            return new ViewHolder(frameLayout, videoViewFactory);
        }

        public ViewHolder(@NonNull View itemView, VideoViewFactory videoViewFactory) {
            super(itemView);
            itemView.setTag(this);
            videoViewContainer = (FrameLayout) itemView;
            videoView = videoViewFactory.createVideoView(videoViewContainer, null);
        }

        public void bind(int position, VideoItem videoItem) {
            this.videoItem = videoItem;
            MediaSource mediaSource = videoView.getDataSource();
            if (mediaSource == null) {
                mediaSource = VideoItem.toMediaSource(videoItem);
                videoView.bindDataSource(mediaSource);
            } else {
                if (!TextUtils.equals(videoItem.getVid(), mediaSource.getMediaId())) {
                    videoView.stopPlayback();
                    mediaSource = VideoItem.toMediaSource(videoItem);
                    videoView.bindDataSource(mediaSource);
                } else {
                    // vid is same
                    if (videoView.player() == null) {
                        mediaSource = VideoItem.toMediaSource(videoItem);
                        videoView.bindDataSource(mediaSource);
                    } else {
                        // do nothing
                    }
                }
            }
        }
    }
}

