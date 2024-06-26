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
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.VideoViewFactory;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.OnPageChangeCallbackCompat;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.ViewPager2Helper;

import java.util.List;

public class ShortVideoPageView extends FrameLayout implements LifecycleEventObserver {
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
        mShortVideoAdapter.setVideoViewFactory(new ShortVideoViewFactory());
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

        mViewPager.post(this::play);
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
            mViewPager.postDelayed(this::play, 100);
        }
    }

    public void replaceItem(int position, VideoItem videoItem) {
        if (videoItem == null) return;

        VideoItem.playScene(videoItem, PlayScene.SCENE_SHORT);

        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;
        L.d(this, "replaceItem", position, VideoItem.dump(videoItem));

        final int currentPosition = getCurrentItem();
        if (currentPosition == position) {
            stop();
        }
        mShortVideoAdapter.replaceItem(position, videoItem);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (currentPosition == position) {
            mViewPager.postDelayed(this::play, 100);
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
            return;
        }
        final VideoItem videoItem = mShortVideoAdapter.getItem(currentPosition);
        L.d(this, "togglePlayback", currentPosition, VideoItem.dump(videoItem));
        final VideoView videoView = (VideoView) findVideoViewByPosition(mViewPager, currentPosition);
        if (mController.videoView() == null) {
            if (videoView != null) {
                mController.bind(videoView);
                mController.startPlayback();
            }
        } else {
            if (videoView != null && videoView != mController.videoView()) {
                mController.stopPlayback();
                mController.bind(videoView);
            }
            mController.startPlayback();
        }
    }

    public void play() {
        final int currentPosition = mViewPager.getCurrentItem();
        if (currentPosition >= 0 && mShortVideoAdapter.getItemCount() > 0) {
            togglePlayback(currentPosition);
        }
    }

    public void resume() {
        if (!mInterceptStartPlaybackOnResume) {
            play();
        }
        mInterceptStartPlaybackOnResume = false;
    }

    public void pause() {
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
