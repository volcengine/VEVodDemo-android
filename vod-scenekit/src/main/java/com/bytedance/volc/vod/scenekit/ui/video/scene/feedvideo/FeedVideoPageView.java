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


import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.ui.scene.PlayScene;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoAdapter.OnItemViewListener;

import java.util.List;


public class FeedVideoPageView extends FrameLayout {
    private final RecyclerView mRecyclerView;
    private final FeedVideoAdapter mShortVideoAdapter;
    private Lifecycle mLifeCycle;
    private DetailPageNavigator mNavigator;
    private VideoView mCurrentVideoView;

    public interface DetailPageNavigator {

        interface FeedVideoViewHolder {

            VideoView getSharedVideoView();

            void detachSharedVideoView(VideoView videoView);

            void attachSharedVideoView(VideoView videoView);

            Rect calVideoViewTransitionRect();
        }

        boolean isDetail();

        void enterDetail(FeedVideoViewHolder holder);

        void exitDetail();
    }

    public FeedVideoPageView(@NonNull Context context) {
        this(context, null);
    }

    public FeedVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(context,
                LinearLayoutManager.VERTICAL, false) {
            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state,
                                               int position) {
                LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(context) {
                    @Override
                    protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                        return super.calculateSpeedPerPixel(displayMetrics) * 4;
                    }
                };
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
        };
        mShortVideoAdapter = new FeedVideoAdapter(mAdapterListener) {
            @Override
            public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
                if (!isFullScreen()) {
                    if (holder.sharedVideoView != null) {
                        holder.sharedVideoView.stopPlayback();
                    }
                }
            }

            @Override
            public void onViewRecycled(@NonNull ViewHolder holder) {
                super.onViewRecycled(holder);
                if (!isFullScreen()) {
                    if (holder.sharedVideoView != null) {
                        holder.sharedVideoView.stopPlayback();
                    }
                }
            }
        };

        mRecyclerView = new RecyclerView(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mShortVideoAdapter);
        addView(mRecyclerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    public RecyclerView recyclerView() {
        return mRecyclerView;
    }

    final OnItemViewListener mAdapterListener = new OnItemViewListener() {
        @Override
        public void onItemClick(FeedVideoAdapter.ViewHolder holder) {
            if (mNavigator != null) {
                mNavigator.enterDetail(holder);
            }
        }

        @Override
        public void onVideoViewClick(FeedVideoAdapter.ViewHolder holder) {
            // click to play
            VideoView videoView = holder.sharedVideoView;
            if (videoView == null) return;

            final Player player = videoView.player();
            if (player == null) {
                videoView.startPlayback();
                int position = holder.getAbsoluteAdapterPosition();
                if (position >= 0) {
                    mRecyclerView.smoothScrollToPosition(position);
                }
            }
        }

        @Override
        public void onEvent(FeedVideoAdapter.ViewHolder viewHolder, Event event) {
            switch (event.code()) {
                case PlaybackEvent.Action.START_PLAYBACK: {
                    // toggle play
                    final VideoView videoView = viewHolder.controller.videoView();
                    if (mCurrentVideoView != null && videoView != null) {
                        if (mCurrentVideoView != videoView) {
                            mCurrentVideoView.stopPlayback();
                            stopPreload();
                        }
                    }
                    mCurrentVideoView = videoView;
                    break;
                }
                case PlayerEvent.Info.VIDEO_RENDERING_START:
                    final int position = viewHolder.getAbsoluteAdapterPosition();
                    starPreload(position);
                    break;
                case PlaybackEvent.Action.STOP_PLAYBACK:
                    break;

            }
        }
    };

    public void setLifeCycle(Lifecycle lifeCycle) {
        if (mLifeCycle != lifeCycle) {
            if (mLifeCycle != null) {
                mLifeCycle.removeObserver(mLifecycleEventObserver);
            }
            mLifeCycle = lifeCycle;
        }
        if (mLifeCycle != null) {
            mLifeCycle.addObserver(mLifecycleEventObserver);
        }
    }

    private final LifecycleEventObserver mLifecycleEventObserver = new LifecycleEventObserver() {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            switch (event) {
                case ON_RESUME:
                    play();
                    break;
                case ON_PAUSE:
                    pause();
                    break;
                case ON_DESTROY:
                    mLifeCycle.removeObserver(mLifecycleEventObserver);
                    mLifeCycle = null;
                    stop();
                    break;
            }
        }
    };

    public void setDetailPageNavigator(DetailPageNavigator navigator) {
        mNavigator = navigator;
    }

    public void setItems(List<VideoItem> videoItems) {
        mShortVideoAdapter.setItems(videoItems);
    }

    public void appendItems(List<VideoItem> videoItems) {
        mShortVideoAdapter.appendItems(videoItems);
    }

    public void play() {
        if (mCurrentVideoView != null) {
            mCurrentVideoView.startPlayback();
        }
    }

    public void pause() {
        if (mCurrentVideoView != null) {
            mCurrentVideoView.pausePlayback();
        }
    }

    public void stop() {
        if (mCurrentVideoView != null) {
            mCurrentVideoView.stopPlayback();
        }
        mCurrentVideoView = null;
    }

    private void starPreload(int position) {
        if (!VideoSettings.booleanValue(VideoSettings.FEED_VIDEO_ENABLE_PRELOAD)) return;

        int next = position + 1;
        // preload next 5 videos
        int target = Math.min(mShortVideoAdapter.getItemCount(), next + 5);
        while (next < target) {
            VideoItem videoItem = mShortVideoAdapter.getItem(next);
            if (videoItem != null) {
                CacheLoader.Default.get().preload(VideoItem.toMediaSource(videoItem, false),
                        null);
            }
            next++;
        }
    }

    private void stopPreload() {
        if (!VideoSettings.booleanValue(VideoSettings.FEED_VIDEO_ENABLE_PRELOAD)) return;

        CacheLoader.Default.get().stopAllTask();
    }

    public boolean isFullScreen() {
        return mCurrentVideoView != null &&
                mCurrentVideoView.getPlayScene() == PlayScene.SCENE_FULLSCREEN;
    }

    public boolean onBackPressed() {
        if (mCurrentVideoView != null) {
            final VideoLayerHost layerHost = mCurrentVideoView.layerHost();
            if (layerHost != null && layerHost.onBackPressed()) {
                return true;
            }
        }
        if (mNavigator != null && mNavigator.isDetail()) {
            // handled in detail
            return true;
        }
        return false;
    }

    private void exitDetail() {
        if (mNavigator != null && mNavigator.isDetail()) {
            mNavigator.exitDetail();
        }
    }
}
