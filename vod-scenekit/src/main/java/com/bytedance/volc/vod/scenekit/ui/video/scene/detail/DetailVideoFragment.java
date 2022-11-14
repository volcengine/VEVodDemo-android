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

package com.bytedance.volc.vod.scenekit.ui.video.scene.detail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.volc.vod.scenekit.ui.video.layer.CoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.FullScreenLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.GestureLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LoadingLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LockLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayCompleteLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayErrorLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayPauseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.SyncStartTimeLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TimeProgressBarLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TipsLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TitleBarLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.VolumeBrightnessIconLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.MoreDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.QualitySelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.SpeedSelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.TimeProgressDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.VolumeBrightnessDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.utils.ViewUtils;
import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.scene.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoPageView;


public class DetailVideoFragment extends BaseFragment {
    public static final String EXTRA_VIDEO_ITEM = "extra_video_item";

    private VideoItem mVideoItem;

    private VideoView mVideoView;

    private VideoView mSharedVideoView;
    private View mTransitionView;

    public DetailVideoFragment() {
    }

    public static DetailVideoFragment newInstance() {
        return newInstance((VideoItem) null);
    }

    public static DetailVideoFragment newInstance(VideoItem videoItem) {
        Bundle bundle = null;
        if (videoItem != null) {
            bundle = new Bundle();
            bundle.putParcelable(EXTRA_VIDEO_ITEM, videoItem);
        }
        return newInstance(bundle);
    }

    public static DetailVideoFragment newInstance(@Nullable Bundle bundle) {
        DetailVideoFragment fragment = new DetailVideoFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    protected FeedVideoPageView.DetailPageNavigator.FeedVideoViewHolder mFeedVideoViewHolder;

    public void setFeedVideoViewHolder(FeedVideoPageView.DetailPageNavigator.FeedVideoViewHolder holder) {
        mFeedVideoViewHolder = holder;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_detail_video_fragment;
    }

    @Override
    public boolean onBackPressed() {
        if (mVideoView != null) {
            final VideoLayerHost layerHost = mVideoView.layerHost();
            if (layerHost != null && layerHost.onBackPressed()) {
                return true;
            }
        }
        if (mFeedVideoViewHolder != null) {
            mVideoView = null;
            return animateExit();
        }
        return super.onBackPressed();
    }

    private boolean animateExit() {
        if (isVisible()) {
            startExitTransition(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    giveBackSharedVideoView();
                    requireActivity().onBackPressed();
                }
            });
            return true;
        } else {
            giveBackSharedVideoView();
            return false;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mVideoItem = bundle.getParcelable(EXTRA_VIDEO_ITEM);
        }
    }

    @Override
    protected void initBackPressedHandler() {
        if (mFeedVideoViewHolder != null) {
            super.initBackPressedHandler();
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTransitionView = view.findViewById(R.id.transitionView);
        if (mFeedVideoViewHolder != null) {
            mSharedVideoView = mFeedVideoViewHolder.getSharedVideoView();
            ViewUtils.removeFromParent(mSharedVideoView);
            startEnterTransition();
            takeOverSharedVideoView();
        } else {
            mVideoView = createVideoView(requireActivity());
            if (mVideoItem != null) {
                mVideoView.bindDataSource(VideoItem.toMediaSource(mVideoItem, true));
            }
        }

        FrameLayout container = view.findViewById(R.id.videoViewContainer);
        container.addView(mVideoView,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        mVideoView.setPlayScene(PlayScene.SCENE_DETAIL);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.startPlayback();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.pausePlayback();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mVideoView != null) {
            mVideoView.stopPlayback();
            mVideoView = null;
        }
    }

    private VideoView createVideoView(Context context) {
        VideoView videoView = new VideoView(context);
        VideoLayerHost layerHost = new VideoLayerHost(context);
        layerHost.addLayer(new GestureLayer());
        layerHost.addLayer(new FullScreenLayer());
        layerHost.addLayer(new CoverLayer());

        layerHost.addLayer(new TimeProgressBarLayer());
        layerHost.addLayer(new TitleBarLayer());
        layerHost.addLayer(new QualitySelectDialogLayer());
        layerHost.addLayer(new SpeedSelectDialogLayer());
        layerHost.addLayer(new MoreDialogLayer());
        layerHost.addLayer(new TipsLayer());
        layerHost.addLayer(new SyncStartTimeLayer());
        layerHost.addLayer(new VolumeBrightnessIconLayer());
        layerHost.addLayer(new VolumeBrightnessDialogLayer());
        layerHost.addLayer(new TimeProgressDialogLayer());
        layerHost.addLayer(new PlayErrorLayer());
        layerHost.addLayer(new PlayPauseLayer());
        layerHost.addLayer(new LockLayer());
        layerHost.addLayer(new LoadingLayer());
        layerHost.addLayer(new PlayCompleteLayer());
        if (VideoSettings.booleanValue(VideoSettings.DEBUG_ENABLE_LOG_LAYER)) {
            layerHost.addLayer(new LogLayer());
        }
        layerHost.attachToVideoView(videoView);
        videoView.setBackgroundColor(getResources().getColor(android.R.color.black));
        videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT);
        videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
        new PlaybackController().bind(videoView);
        return videoView;
    }

    private void giveBackSharedVideoView() {
        ViewUtils.removeFromParent(mSharedVideoView);
        mFeedVideoViewHolder.attachSharedVideoView(mSharedVideoView);
        mSharedVideoView = null;
        mFeedVideoViewHolder = null;
    }

    private void takeOverSharedVideoView() {
        mFeedVideoViewHolder.detachSharedVideoView(mSharedVideoView);
        mVideoView = mSharedVideoView;
    }

    private void startEnterTransition() {
        final Rect startRect = mFeedVideoViewHolder.calVideoViewTransitionRect();
        mTransitionView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mTransitionView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                if (isVisible()) {
                    final float targetY = mTransitionView.getY();
                    startTransitionAnimation(startRect.top, targetY, null);
                }
            }
        });
    }

    private void startExitTransition(Animator.AnimatorListener listener) {
        final float startY = mTransitionView.getY();
        final Rect targetRect = mFeedVideoViewHolder.calVideoViewTransitionRect();
        startTransitionAnimation(startY, targetRect.top, listener);
    }

    private void startTransitionAnimation(float startY, float targetY, Animator.AnimatorListener listener) {
        ValueAnimator animator = ValueAnimator
                .ofFloat(startY, targetY)
                .setDuration(300);
        animator.addUpdateListener(animation -> mTransitionView.setY((Float) animation.getAnimatedValue()));
        if (listener != null) {
            animator.addListener(listener);
        }
        animator.start();
    }
}
