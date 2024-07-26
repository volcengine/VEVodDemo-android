/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.VideoViewFactory;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.OnPageChangeCallbackCompat;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.ViewPager2Helper;

import java.util.List;

public class ShortVideoPageView extends FrameLayout implements LifecycleEventObserver, Dispatcher.EventListener {
    private final ViewPager2 mViewPager;
    private final ShortVideoAdapter mShortVideoAdapter;
    private final PlaybackController mController = new PlaybackController();
    private Lifecycle mLifeCycle;
    private boolean mInterceptStartPlaybackOnResume;

    public ShortVideoPageView(@NonNull Context context) {
        this(context, null);
    }

    public ShortVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mViewPager = new ViewPager2(context);
        ViewPager2Helper.setup(mViewPager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mShortVideoAdapter = new ShortVideoAdapter();
        mShortVideoAdapter.setVideoViewFactory(new ShortVideoViewFactory(this));
        mViewPager.setAdapter(mShortVideoAdapter);
        mViewPager.registerOnPageChangeCallback(new OnPageChangeCallbackCompat(mViewPager) {
            @Override
            public void onPageSelected(ViewPager2 pager, int position) {
                super.onPageSelected(pager, position);
                togglePlayback(position);
            }

            @Override
            public void onPagePeekStart(ViewPager2 pager, int position, int peekPosition) {
                super.onPagePeekStart(pager, position, peekPosition);
                VideoView videoView = findVideoViewByPosition(pager, peekPosition);
                if (videoView != null) {
                    VideoLayerHost host = videoView.layerHost();
                    if (host != null) {
                        host.notifyEvent(Layers.Event.VIEW_PAGER_ON_PAGE_PEEK_START.ordinal(), null);
                    }
                }
            }
        });
        addView(mViewPager, new LayoutParams(LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mController.addPlaybackListener(this);
    }

    @Override
    public void onEvent(Event event) {
        if (event.code() == PlaybackEvent.Action.STOP_PLAYBACK) {
            removeCallbacks(mPlayRunnable);
        }
    }

    public ViewPager2 viewPager() {
        return mViewPager;
    }

    public void setLifeCycle(Lifecycle lifeCycle) {
        if (mLifeCycle != lifeCycle) {
            if (mLifeCycle != null) {
                mLifeCycle.removeObserver(this);
            }
            mLifeCycle = lifeCycle;
        }
        if (mLifeCycle != null) {
            mLifeCycle.addObserver(this);
        }
    }

    public void setVideoViewFactory(VideoViewFactory videoViewFactory) {
        mShortVideoAdapter.setVideoViewFactory(videoViewFactory);
    }

    @MainThread
    public void addPlaybackListener(Dispatcher.EventListener listener) {
        mController.addPlaybackListener(listener);
    }

    @MainThread
    public void removePlaybackListener(Dispatcher.EventListener listener) {
        mController.removePlaybackListener(listener);
    }

    public void setItems(List<VideoItem> videoItems) {
        L.d(this, "setItems", VideoItem.dump(videoItems));

        VideoItem.playScene(videoItems, PlayScene.SCENE_SHORT);
        mShortVideoAdapter.setItems(videoItems);
        ShortVideoStrategy.setItems(videoItems);

        play();
    }

    public void prependItems(List<VideoItem> videoItems) {
        L.d(this, "prependItems", VideoItem.dump(videoItems));

        VideoItem.playScene(videoItems, PlayScene.SCENE_SHORT);
        mShortVideoAdapter.prependItems(videoItems);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
    }

    public void appendItems(List<VideoItem> videoItems) {
        L.d(this, "appendItems", VideoItem.dump(videoItems));

        VideoItem.playScene(videoItems, PlayScene.SCENE_SHORT);
        mShortVideoAdapter.appendItems(videoItems);
        ShortVideoStrategy.appendItems(videoItems);
    }

    public void deleteItem(int position) {
        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;

        final int currentPosition = getCurrentItem();
        VideoItem videoItem = mShortVideoAdapter.getItem(position);
        L.d(this, "deleteItem", position, VideoItem.dump(videoItem));

        mShortVideoAdapter.deleteItem(position);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (currentPosition == position) {
            play();
        }
    }

    public void deleteItems(int position, int count) {
        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;

        final int currentPosition = getCurrentItem();
        L.d(this, "deleteItems", position, count);
        mShortVideoAdapter.deleteItems(position, count);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (position <= currentPosition && currentPosition < position + count) {
            play();
        }
    }

    public void replaceItem(int position, VideoItem videoItem) {
        if (videoItem == null) return;

        VideoItem.playScene(videoItem, PlayScene.SCENE_SHORT);

        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;
        L.d(this, "replaceItem", position, VideoItem.dump(videoItem));
        final int currentPosition = getCurrentItem();
        mShortVideoAdapter.replaceItem(position, videoItem);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (currentPosition == position) {
            play();
        }
    }

    public void replaceItems(int position, List<VideoItem> videoItems) {
        if (videoItems == null) return;
        if (mShortVideoAdapter.getItemCount() <= position || position < 0) return;

        VideoItem.playScene(videoItems, PlayScene.SCENE_SHORT);
        L.d(this, "replaceItems", position, VideoItem.dump(videoItems));
        final int currentPosition = getCurrentItem();
        mShortVideoAdapter.replaceItems(position, videoItems);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (position <= currentPosition && currentPosition < position + videoItems.size()) {
            play();
        }
    }

    public List<VideoItem> getItems() {
        return mShortVideoAdapter.getItems();
    }

    public VideoItem getItem(int position) {
        return mShortVideoAdapter.getItem(position);
    }

    public int getItemCount() {
        return mShortVideoAdapter.getItemCount();
    }

    public void setCurrentItem(int position, boolean smoothScroll) {
        L.d(this, "setCurrentItem", position, smoothScroll);
        mViewPager.setCurrentItem(position, smoothScroll);
    }

    public int indexOf(VideoItem videoItem) {
        if (videoItem == null) return -1;
        for (int i = 0; i < mShortVideoAdapter.getItemCount(); i++) {
            if (VideoItem.mediaEquals(videoItem, mShortVideoAdapter.getItem(i))) {
                return i;
            }
        }
        return -1;
    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    public View getCurrentItemView() {
        final int currentPosition = mViewPager.getCurrentItem();
        return ViewPager2Helper.findItemViewByPosition(mViewPager, currentPosition);
    }

    public VideoView getCurrentItemVideoView() {
        final int currentPosition = mViewPager.getCurrentItem();
        return findVideoViewByPosition(mViewPager, currentPosition);
    }

    public VideoItem getCurrentItemModel() {
        final int currentPosition = mViewPager.getCurrentItem();
        return mShortVideoAdapter.getItem(currentPosition);
    }

    private void togglePlayback(int currentPosition) {
        if (!mLifeCycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            L.d(this, "togglePlayback", currentPosition, "returned",
                    L.string(mLifeCycle.getCurrentState()));
            return;
        }
        final VideoItem videoItem = mShortVideoAdapter.getItem(currentPosition);
        final VideoView videoView = findVideoViewByPosition(mViewPager, currentPosition);
        L.d(this, "togglePlayback", currentPosition, videoView, VideoItem.dump(videoItem));
        if (videoView == null) return;
        VideoView currentVideoView = mController.videoView();
        if (currentVideoView == null) {
            mController.bind(videoView);
            currentVideoView = videoView;
        } else {
            if (videoView != mController.videoView()) {
                currentVideoView.stopPlayback();
                mController.bind(videoView);
                currentVideoView = videoView;
            }
        }
        currentVideoView.startPlayback();
    }

    public void play() {
        play(2);
    }

    private Runnable mPlayRunnable;

    private void play(final int retryCount) {
        final int currentPosition = mViewPager.getCurrentItem();
        if (!mLifeCycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            L.d(this, "play", currentPosition, "returned",
                    L.string(mLifeCycle.getCurrentState()));
            removeCallbacks(mPlayRunnable);
            return;
        }
        if (mShortVideoAdapter.getItemCount() <= currentPosition) {
            L.d(this, "play", currentPosition, "returned",
                    "adapter is empty!");
            removeCallbacks(mPlayRunnable);
            return;
        }
        final VideoItem adapterItem = mShortVideoAdapter.getItem(currentPosition);
        final VideoView videoView = findVideoViewByPosition(mViewPager, currentPosition);
        if (videoView == null) {
            L.d(this, "play", currentPosition, "returned",
                    "videoView == null! Wait OnPageSelected be invoked!", VideoItem.dump(adapterItem));
            removeCallbacks(mPlayRunnable);
            return;
        }
        final VideoItem viewItem = VideoItem.get(videoView.getDataSource());
        if (!VideoItem.mediaEquals(adapterItem, viewItem) ||
                (viewItem.getSourceType() == VideoItem.SOURCE_TYPE_EMPTY &&
                        adapterItem.getSourceType() != VideoItem.SOURCE_TYPE_EMPTY)) {
            L.d(this, "play", currentPosition, "post and waiting",
                    "retryCount:" + retryCount, "newest data not bind yet! Wait adapter onBindViewHolder invoke!",
                    videoView, VideoItem.dump(viewItem),
                    VideoItem.dump(adapterItem));
            removeCallbacks(mPlayRunnable);
            if (retryCount > 0) {
                mPlayRunnable = new Runnable() {
                    @Override
                    public void run() {
                        int nextRetryCount = retryCount - 1;
                        play(nextRetryCount);
                    }
                };
                postOnAnimation(mPlayRunnable);
            }
            return;
        }
        L.d(this, "play");
        togglePlayback(currentPosition);
    }

    public void resume() {
        L.d(this, "resume");
        if (!mInterceptStartPlaybackOnResume) {
            play();
        }
        mInterceptStartPlaybackOnResume = false;
    }

    public void pause() {
        L.d(this, "pause");
        Player player = mController.player();
        if (player != null && (player.isPaused() || (!player.isLooping() && player.isCompleted()))) {
            mInterceptStartPlaybackOnResume = true;
        } else {
            mInterceptStartPlaybackOnResume = false;
            mController.pausePlayback();
        }
    }

    public void setInterceptStartPlaybackOnResume(boolean interceptStartPlay) {
        this.mInterceptStartPlaybackOnResume = interceptStartPlay;
    }

    public void stop() {
        L.d(this, "stop");
        mController.stopPlayback();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_RESUME:
                ShortVideoStrategy.setEnabled(true);
                ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
                resume();
                break;
            case ON_PAUSE:
                ShortVideoStrategy.setEnabled(false);
                pause();
                break;
            case ON_DESTROY:
                mLifeCycle.removeObserver(this);
                mLifeCycle = null;
                stop();
                break;
        }
    }

    public boolean onBackPressed() {
        final VideoView videoView = mController != null ? mController.videoView() : null;

        if (videoView != null) {
            final VideoLayerHost layerHost = videoView.layerHost();
            if (layerHost != null && layerHost.onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private static VideoView findVideoViewByPosition(ViewPager2 pager, int position) {
        ShortVideoAdapter.ViewHolder viewHolder = findItemViewHolderByPosition(pager, position);
        return viewHolder == null ? null : viewHolder.videoView;
    }

    private static ShortVideoAdapter.ViewHolder findItemViewHolderByPosition(ViewPager2 pager, int position) {
        View itemView = ViewPager2Helper.findItemViewByPosition(pager, position);
        if (itemView != null) {
            return (ShortVideoAdapter.ViewHolder) itemView.getTag();
        }
        return null;
    }


}
