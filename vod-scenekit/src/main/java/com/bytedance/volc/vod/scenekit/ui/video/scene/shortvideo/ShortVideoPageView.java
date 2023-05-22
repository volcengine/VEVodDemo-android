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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.OnPageChangeCallbackCompat;

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
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mShortVideoAdapter = new ShortVideoAdapter();
        mViewPager.setAdapter(mShortVideoAdapter);
        mViewPager.registerOnPageChangeCallback(new OnPageChangeCallbackCompat(mViewPager) {
            @Override
            public void onPageSelected(int position, ViewPager2 pager) {
                togglePlayback(position);
            }
        });
        addView(mViewPager, new LayoutParams(LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        mController.addPlaybackListener(new Dispatcher.EventListener() {
            @Override
            public void onEvent(Event event) {
                switch (event.code()) {
                    case PlayerEvent.State.COMPLETED: {
                        final Player player = event.owner(Player.class);
                        if (player != null && !player.isLooping() &&
                                VideoSettings.intValue(VideoSettings.SHORT_VIDEO_PLAYBACK_COMPLETE_ACTION) == 1 /* 1 播放下一个 */) {
                            final int currentPosition = getCurrentItem();
                            final int nextPosition = currentPosition + 1;
                            if (nextPosition < mShortVideoAdapter.getItemCount()) {
                                setCurrentItem(nextPosition, true);
                            }
                        }
                        break;
                    }
                }
            }
        });
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

    public void setItems(List<VideoItem> videoItems) {
        mShortVideoAdapter.setItems(videoItems);
        ShortVideoStrategy.setItems(videoItems);

        mViewPager.getChildAt(0).post(this::play);
    }

    public void prependItems(List<VideoItem> videoItems) {
        mShortVideoAdapter.prependItems(videoItems);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
    }

    public void appendItems(List<VideoItem> videoItems) {
        mShortVideoAdapter.appendItems(videoItems);
        ShortVideoStrategy.appendItems(videoItems);
    }

    public void deleteItem(int position) {
        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;

        final int currentPosition = getCurrentItem();
        mShortVideoAdapter.deleteItem(position);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (currentPosition == position && position < mShortVideoAdapter.getItemCount()) {
            mViewPager.postDelayed(this::play, 100);
        }
    }

    public int getItemCount() {
        return mShortVideoAdapter.getItemCount();
    }

    public void setCurrentItem(int position, boolean smoothScroll) {
        mViewPager.setCurrentItem(position, smoothScroll);
    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    public View getCurrentItemView() {
        final int currentPosition = mViewPager.getCurrentItem();
        return findItemViewByPosition(mViewPager, currentPosition);
    }

    private void togglePlayback(int currentPosition) {
        if (!mLifeCycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            return;
        }
        L.d(this, "togglePlayback", currentPosition);
        final VideoView videoView = (VideoView) findItemViewByPosition(mViewPager, currentPosition);
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

    public void stop() {
        mController.stopPlayback();
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_CREATE:
                ShortVideoStrategy.setEnabled(true);
                break;
            case ON_RESUME:
                ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
                resume();
                break;
            case ON_PAUSE:
                pause();
                break;
            case ON_DESTROY:
                ShortVideoStrategy.setEnabled(false);
                mLifeCycle.removeObserver(this);
                mLifeCycle = null;
                stop();
                break;
        }
    }

    @Nullable
    private static View findItemViewByPosition(ViewPager2 pager, int position) {
        final RecyclerView recyclerView = (RecyclerView) pager.getChildAt(0);
        if (recyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                return layoutManager.findViewByPosition(position);
            }
        }
        return null;
    }

}
