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

package com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.VideoViewFactory;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.vod.scenekit.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;


public class FeedVideoAdapter extends RecyclerView.Adapter<FeedVideoAdapter.ViewHolder> {

    public interface OnItemViewListener {
        void onItemClick(ViewHolder holder);

        void onVideoViewClick(ViewHolder holder);

        void onEvent(ViewHolder viewHolder, Event event);
    }

    private final List<VideoItem> mItems = new ArrayList<>();
    private final OnItemViewListener mOnItemViewListener;

    private VideoViewFactory mVideoViewFactory;

    private boolean mIsLoadingMore;

    public FeedVideoAdapter(OnItemViewListener listener) {
        this.mOnItemViewListener = listener;
    }

    public void setVideoViewFactory(VideoViewFactory videoViewFactory) {
        this.mVideoViewFactory = videoViewFactory;
    }

    public void setItems(List<VideoItem> videoItems) {
        mItems.clear();
        mItems.addAll(videoItems);
        notifyDataSetChanged();
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
                notifyItemRangeInserted(count, videoItems.size());
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

    public void replaceItem(int position, VideoItem videoItem) {
        if (0 <= position && position < mItems.size()) {
            mItems.set(position, videoItem);
            notifyItemChanged(position, new Object() /*Prevent Adapter calling onCreateViewHolder}*/);
        }
    }

    public VideoItem getItem(int position) {
        return mItems.get(position);
    }

    public List<VideoItem> getItems() {
        return mItems;
    }

    public void setLoadingMore(boolean loadingMore) {
        mIsLoadingMore = loadingMore;
    }

    public boolean isLoadingMore() {
        return mIsLoadingMore;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.vevod_feed_video_item, parent, false), mVideoViewFactory, mOnItemViewListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final VideoItem videoItem = mItems.get(position);
        holder.bindSource(position, videoItem, mItems);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements
            FeedVideoPageView.DetailPageNavigator.FeedVideoViewHolder {
        // header
        public TextView videoDescView;
        public TextView followView;
        public ImageView moreView;

        // video
        public FrameLayout videoViewContainer;
        public VideoView sharedVideoView;
        public PlaybackController controller;

        // footer
        public View collectContainer, commentContainer, likeContainer;

        public ViewHolder(@NonNull View itemView, VideoViewFactory videoViewFactory, OnItemViewListener listener) {
            super(itemView);
            initHeader(itemView, listener);
            initVideoView(itemView, videoViewFactory, listener);
            initFooter(itemView, listener);
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(ViewHolder.this);
                }
            });
        }

        private void initHeader(@NonNull View itemView, OnItemViewListener listener) {
            moreView = itemView.findViewById(R.id.more);

            moreView.setOnClickListener(v -> Toast.makeText(v.getContext(), "more is not implement yet", Toast.LENGTH_SHORT).show());

            followView = itemView.findViewById(R.id.follow);
            followView.setOnClickListener(v -> Toast.makeText(v.getContext(), "follow is not implement yet", Toast.LENGTH_SHORT).show());

            videoDescView = itemView.findViewById(R.id.videoDesc);
        }

        private void initVideoView(@NonNull View itemView, VideoViewFactory videoViewFactory, OnItemViewListener listener) {
            videoViewContainer = itemView.findViewById(R.id.videoViewContainer);
            sharedVideoView = videoViewFactory.createVideoView(videoViewContainer, null);
            controller = new PlaybackController();
            controller.bind(sharedVideoView);

            sharedVideoView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVideoViewClick(ViewHolder.this);
                }
            });
            controller.addPlaybackListener(event -> {
                if (listener != null) {
                    listener.onEvent(ViewHolder.this, event);
                }
            });
        }

        private void initFooter(@NonNull View itemView, OnItemViewListener listener) {
            collectContainer = itemView.findViewById(R.id.collectContainer);
            commentContainer = itemView.findViewById(R.id.commentContainer);
            likeContainer = itemView.findViewById(R.id.likeContainer);

            collectContainer.setOnClickListener(v -> Toast.makeText(v.getContext(), "collect is not implement yet", Toast.LENGTH_SHORT).show());
            commentContainer.setOnClickListener(v -> Toast.makeText(v.getContext(), "comment is not implement yet", Toast.LENGTH_SHORT).show());
            likeContainer.setOnClickListener(v -> Toast.makeText(v.getContext(), "like is not implement yet", Toast.LENGTH_SHORT).show());
        }

        void bindSource(int position, VideoItem videoItem, List<VideoItem> videoItems) {
            bindHeader(position, videoItem, videoItems);
            bindVideoView(position, videoItem, videoItems);
            bindFooter(position, videoItem, videoItems);
        }

        void bindHeader(int position, VideoItem videoItem, List<VideoItem> videoItems) {
            videoDescView.setText(videoItem.getTitle());
        }

        void bindVideoView(int position, VideoItem videoItem, List<VideoItem> videoItems) {
            VideoView videoView = sharedVideoView;
            MediaSource mediaSource = videoView.getDataSource();
            if (mediaSource == null) {
                mediaSource = VideoItem.toMediaSource(videoItem);
                videoView.bindDataSource(mediaSource);
            } else {
                if (TextUtils.equals(videoItem.getVid(), mediaSource.getMediaId())) {
                    // vid is same
                    if (videoView.player() == null) {
                        mediaSource = VideoItem.toMediaSource(videoItem);
                        videoView.bindDataSource(mediaSource);
                    } else {
                        // do nothing
                    }
                } else {
                    videoView.stopPlayback();
                    mediaSource = VideoItem.toMediaSource(videoItem);
                    videoView.bindDataSource(mediaSource);
                }
            }
        }

        void bindFooter(int position, VideoItem videoItem, List<VideoItem> videoItems) {
        }

        @Override
        public VideoView getSharedVideoView() {
            return sharedVideoView;
        }

        @Override
        public void detachSharedVideoView(VideoView videoView) {
            if (sharedVideoView == videoView) {
                ViewUtils.removeFromParent(videoView);
                sharedVideoView = null;
            }
        }

        @Override
        public void attachSharedVideoView(VideoView videoView) {
            sharedVideoView = videoView;
            videoViewContainer.addView(videoView);
            sharedVideoView.setPlayScene(PlayScene.SCENE_FEED);
            sharedVideoView.startPlayback();
            int position = getAbsoluteAdapterPosition();
            if (position >= 0) {
                if (itemView.getParent() instanceof RecyclerView) {
                    RecyclerView recyclerView = (RecyclerView) itemView.getParent();
                    recyclerView.smoothScrollToPosition(position);
                }
            }
        }

        @Override
        public Rect calVideoViewTransitionRect() {
            final int[] location = UIUtils.getLocationInWindow(videoViewContainer);
            int left = location[0];
            int top = location[1] - UIUtils.getStatusBarHeight(itemView.getContext());
            int right = left + videoViewContainer.getWidth();
            int bottom = top + videoViewContainer.getHeight();
            return new Rect(left, top, right, bottom);
        }
    }
}


