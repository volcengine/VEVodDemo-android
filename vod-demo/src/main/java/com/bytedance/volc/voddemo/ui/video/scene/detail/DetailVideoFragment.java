/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/3/11
 */

package com.bytedance.volc.voddemo.ui.video.scene.detail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.layer.CoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.FullScreenLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.GestureLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LoadingLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LockLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayCompleteLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayErrorLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayPauseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.SubtitleLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.SyncStartTimeLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TimeProgressBarLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TipsLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TitleBarLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.VolumeBrightnessIconLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.MoreDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.QualitySelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.SpeedSelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.SubtitleSelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.TimeProgressDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.VolumeBrightnessDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoPageView;
import com.bytedance.volc.vod.scenekit.utils.ViewUtils;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.PipVideoController;

import java.util.Collections;
import java.util.UUID;


public class DetailVideoFragment extends BaseFragment {

    public static final String EXTRA_MEDIA_SOURCE = "extra_media_source";
    public static final String EXTRA_CONTINUES_PLAYBACK = "extra_continues_playback";

    private MediaSource mMediaSource;

    private boolean mContinuesPlayback;

    private VideoView mVideoView;

    private VideoView mSharedVideoView;
    private View mTransitionView;
    private boolean mInterceptStartPlaybackOnResume;

    private boolean mUserExiting;

    public interface DetailVideoSceneEventListener {
        void onEnterDetail();

        void onExitDetail();
    }

    public DetailVideoFragment() {
    }

    public static DetailVideoFragment newInstance() {
        return newInstance(null);
    }

    public static Bundle createBundle(MediaSource mediaSource, boolean continuesPlay) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MEDIA_SOURCE, mediaSource);
        bundle.putBoolean(EXTRA_CONTINUES_PLAYBACK, continuesPlay);
        return bundle;
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
            mUserExiting = true;
            return animateExit();
        }
        if (mContinuesPlayback) {
            if (mVideoView != null) {
                final PlaybackController controller = mVideoView.controller();
                if (controller != null) {
                    controller.unbindPlayer();
                }
                mVideoView = null;
            }
        }
        mUserExiting = true;
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
            return super.onBackPressed();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mMediaSource = (MediaSource) bundle.getSerializable(EXTRA_MEDIA_SOURCE);
            mContinuesPlayback = bundle.getBoolean(EXTRA_CONTINUES_PLAYBACK);
        }
        mPipSessionKey = UUID.randomUUID().toString();
        mUserExiting = false;
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

            // take Over SharedVideoView
            mFeedVideoViewHolder.detachSharedVideoView(mSharedVideoView);
            mVideoView = mSharedVideoView;
        } else {
            mVideoView = createVideoView(requireActivity());
            mVideoView.bindDataSource(mMediaSource);
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
        registerReceiver();
        dismissPip();
        resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
        pause();
    }

    @Override
    public void onStop() {
        super.onStop();
        enterPip(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stop();
    }

    private void play() {
        if (mVideoView != null) {
            mVideoView.startPlayback();
        }
    }

    private void resume() {
        if (!mInterceptStartPlaybackOnResume) {
            play();
        }
        mInterceptStartPlaybackOnResume = false;
    }

    private void pause() {
        if (mVideoView != null) {
            Player player = mVideoView.player();
            if (player != null && (player.isPaused() || (!player.isLooping() && player.isCompleted()))) {
                mInterceptStartPlaybackOnResume = true;
            } else {
                mInterceptStartPlaybackOnResume = false;
                mVideoView.pausePlayback();
            }
        }
    }

    private void stop() {
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
        layerHost.addLayer(new SubtitleLayer());
        layerHost.addLayer(new CoverLayer());

        layerHost.addLayer(new TimeProgressBarLayer());
        layerHost.addLayer(new TitleBarLayer());
        layerHost.addLayer(new QualitySelectDialogLayer());
        layerHost.addLayer(new SpeedSelectDialogLayer());
        layerHost.addLayer(new SubtitleSelectDialogLayer());
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
        if (VideoSettings.intValue(VideoSettings.COMMON_RENDER_VIEW_TYPE) == DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW) {
            // 推荐使用 TextureView, 兼容性更好
            videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
        } else {
            videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW);
        }
        new PlaybackController().bind(videoView);
        return videoView;
    }

    private void giveBackSharedVideoView() {
        ViewUtils.removeFromParent(mSharedVideoView);
        mFeedVideoViewHolder.attachSharedVideoView(mSharedVideoView);
        mSharedVideoView = null;
        mFeedVideoViewHolder = null;
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

    private boolean mRegistered;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case TitleBarLayer.ACTION_VIDEO_LAYER_TOGGLE_PIP_MODE: {
                    enterPip(true);
                    break;
                }
            }
        }
    };

    private void registerReceiver() {
        if (!mRegistered) {
            mRegistered = true;
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(TitleBarLayer.ACTION_VIDEO_LAYER_TOGGLE_PIP_MODE);
            LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(mBroadcastReceiver, intentFilter);
        }
    }

    private void unregisterReceiver() {
        if (mRegistered) {
            mRegistered = false;
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mBroadcastReceiver);
        }
    }

    private String mPipSessionKey;

    private void enterPip(boolean request) {
        if (!VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP)) return;

        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }

        if (mUserExiting) {
            // 用户退出页面时，不切换小窗
            return;
        }

        if (mInterceptStartPlaybackOnResume) {
            // 用户暂停视频后，不切换小窗
            return;
        }

        if (mVideoView == null) return;

        VideoItem videoItem = VideoItem.get(mVideoView.getDataSource());
        if (videoItem == null) return;
        if (request) {
            PipVideoController.instance().requestMainToPip(new PipVideoController.PipVideoConfig(mPipSessionKey,
                    activity,
                    mVideoView,
                    Collections.singletonList(videoItem),
                    0));
        } else {
            PipVideoController.instance().mainToPip(new PipVideoController.PipVideoConfig(mPipSessionKey,
                    activity,
                    mVideoView,
                    Collections.singletonList(videoItem),
                    0));
        }
    }

    public void dismissPip() {
        PipVideoController.instance().dismissPip();
        PipVideoController.MainVideoInfo mainVideoInfo = PipVideoController.instance().getMainVideoInfo();
        if (mainVideoInfo != null && TextUtils.equals(mainVideoInfo.sessionKey, mPipSessionKey)) {
            PipVideoController.instance().resetMainVideoInfo();
        }
    }
}
