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
 * Create Date : 2024/10/15
 */

package com.bytedance.volc.voddemo.ui.ad.mock;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LoadingLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PauseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayErrorLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayerConfigLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoBottomShadowLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoCoverLayer;
import com.bytedance.volc.voddemo.data.remote.model.base.BaseVideo;
import com.bytedance.volc.voddemo.ui.ad.api.Ad;

/**
 * Mock impl of ShortVideoAdVideoView
 */
@Deprecated
public class MockShortVideoAdVideoView extends FrameLayout implements LifecycleEventObserver {

    @Deprecated
    public interface MockAdVideoListener {
        void onAdVideoCompleted(Ad ad);
    }

    public final VideoView mVideoView;
    private Ad mAd;
    private boolean mActive;
    private MockAdVideoListener mListener;

    public MockShortVideoAdVideoView(@NonNull Context context) {
        this(context, null);
    }

    public MockShortVideoAdVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MockShortVideoAdVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mVideoView = new VideoView(context);
        VideoLayerHost layerHost = new VideoLayerHost(context);
        layerHost.addLayer(new PlayerConfigLayer());
        layerHost.addLayer(new ShortVideoCoverLayer());
        layerHost.addLayer(new ShortVideoBottomShadowLayer());
        layerHost.addLayer(new MockShortVideoAdVideoLayer());
        layerHost.addLayer(new LoadingLayer());
        layerHost.addLayer(new PauseLayer());
        layerHost.addLayer(new PlayErrorLayer());
        if (VideoSettings.booleanValue(VideoSettings.DEBUG_ENABLE_LOG_LAYER)) {
            layerHost.addLayer(new LogLayer());
        }
        layerHost.attachToVideoView(mVideoView);
        mVideoView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        //videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT); // fit mode
        mVideoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL); // immersive mode
        if (VideoSettings.intValue(VideoSettings.COMMON_RENDER_VIEW_TYPE) == DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW) {
            // 推荐使用 TextureView, 兼容性更好
            mVideoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
        } else {
            mVideoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW);
        }
        mVideoView.setPlayScene(PlayScene.SCENE_SHORT);
        PlaybackController playbackController = new PlaybackController();
        playbackController.bind(mVideoView);
        playbackController.addPlaybackListener(new Dispatcher.EventListener() {
            @Override
            public void onEvent(Event event) {
                if (event.code() == PlayerEvent.State.COMPLETED) {
                    if (mListener != null && mAd != null) {
                        mListener.onAdVideoCompleted(mAd);
                    }
                }
            }
        });
        addView(mVideoView, new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT, Gravity.CENTER));
    }

    public void setAdVideoListener(MockAdVideoListener listener) {
        mListener = listener;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_RESUME:
                startDetect();
                break;
            case ON_PAUSE:
                stopDetect(true);
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startDetect();
        final Lifecycle lifecycle = lifecycle();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopDetect(false);
        final Lifecycle lifecycle = lifecycle();
        if (lifecycle != null) {
            lifecycle.removeObserver(this);
        }
    }

    public void bind(Ad ad) {
        if (mAd != ad) {
            mAd = ad;
            final VideoItem video = mAd == null ? null : mAd.get();
            if (video == null) return;
            bind(video);
        }
        startDetect();
    }

    public void bind(VideoItem videoItem) {
        if (videoItem == null) return;

        MediaSource mediaSource = mVideoView.getDataSource();
        if (mediaSource == null) {
            mediaSource = VideoItem.toMediaSource(videoItem);
            mVideoView.bindDataSource(mediaSource);
        } else {
            if (!TextUtils.equals(videoItem.getVid(), mediaSource.getMediaId())) {
                mVideoView.stopPlayback();
                mediaSource = VideoItem.toMediaSource(videoItem);
                mVideoView.bindDataSource(mediaSource);
            } else {
                // vid is same
                if (mVideoView.player() == null) {
                    mediaSource = VideoItem.toMediaSource(videoItem);
                    mVideoView.bindDataSource(mediaSource);
                } else {
                    // do nothing
                }
            }
        }
    }

    private void startDetect() {
        removeCallbacks(mActivateDetector);
        postOnAnimation(mActivateDetector);
    }

    private void stopDetect(boolean pause) {
        removeCallbacks(mActivateDetector);
        post(new Runnable() {
            @Override
            public void run() {
                deactivate(pause);
            }
        });
    }

    private final Runnable mActivateDetector = new Runnable() {
        @Override
        public void run() {
            if (mAd != null &&
                    calculateVisibleRatio() > 0.5 &&
                    isAttachedToWindow() &&
                    getWidth() > 0 &&
                    getHeight() > 0) {
                activate();
            } else {
                deactivate(false);
            }

            if (mAd != null && isAttachedToWindow()) {
                postOnAnimation(mActivateDetector);
            }
        }
    };

    private void activate() {
        if (!mActive) {
            mActive = true;
            mVideoView.startPlayback();
        }
    }

    private void deactivate(boolean pause) {
        if (mActive || !pause) {
            mActive = false;
            if (pause) {
                mVideoView.pausePlayback();
            } else {
                mVideoView.stopPlayback();
            }
        }
    }

    private static Rect sRect;

    private static Rect ensureRect() {
        sRect = sRect == null ? new Rect() : sRect;
        return sRect;
    }

    private float calculateVisibleRatio() {
        final Rect rect = ensureRect();
        float ratio = 0;
        if (getLocalVisibleRect(rect)) {
            final int viewWidth = getWidth();
            final int viewHeight = getHeight();
            final int visibleWidth = rect.width();
            final int visibleHeight = rect.height();
            if (viewWidth > 0 && viewHeight > 0) {
                ratio = (visibleWidth * visibleHeight) / (float) (viewWidth * viewHeight);
            }
        }
        return ratio;
    }

    private Lifecycle lifecycle() {
        if (getContext() instanceof LifecycleOwner) {
            return ((LifecycleOwner) getContext()).getLifecycle();
        }
        return null;
    }
}
