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
 * Create Date : 2023/12/28
 */

package com.bytedance.volc.voddemo.ui.video.scene.fullscreen;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.layer.CoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.GestureLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LoadingLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LockLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayCompleteLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayErrorLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayPauseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayerConfigLayer;
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
import com.bytedance.volc.vod.scenekit.utils.OrientationHelper;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.PipVideoController;

import java.util.Collections;
import java.util.UUID;

public class FullScreenVideoFragment extends BaseFragment {

    public static final String ACTION_USER_EXIT_FULLSCREEN = " com.bytedance.volc.voddemo.ui.video.scene.fullscreen.FullScreenVideoFragment/userExit";
    public static final String EXTRA_MEDIA_SOURCE = "extra_media_source";
    public static final String EXTRA_CONTINUES_PLAYBACK = "extra_continues_playback";
    public static final String EXTRA_SCREEN_ORIENTATION_DEGREE = "extra_screen_orientation";

    private MediaSource mMediaSource;

    private boolean mContinuesPlayback;
    private VideoView mVideoView;

    private FrameLayout mVideoViewContainer;

    private OrientationHelper mOrientationHelper;
    private boolean mInterceptStartPlaybackOnResume;

    public FullScreenVideoFragment() {
    }

    public static FullScreenVideoFragment newInstance() {
        return newInstance(null);
    }

    public static FullScreenVideoFragment newInstance(@Nullable Bundle bundle) {
        FullScreenVideoFragment fragment = new FullScreenVideoFragment();
        if (bundle != null) {
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public static Bundle createBundle(MediaSource mediaSource, boolean continuesPlay, int screenOrientation) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_MEDIA_SOURCE, mediaSource);
        bundle.putBoolean(EXTRA_CONTINUES_PLAYBACK, continuesPlay);
        bundle.putInt(EXTRA_SCREEN_ORIENTATION_DEGREE, screenOrientation);
        return bundle;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_fullscreen_video_fragment;
    }

    @Override
    public boolean onBackPressed() {
        if (mVideoView != null) {
            final VideoLayerHost layerHost = mVideoView.layerHost();
            if (layerHost != null && layerHost.onBackPressed()) {
                return true;
            }
            if (mContinuesPlayback) {
                final PlaybackController controller = mVideoView.controller();
                if (controller != null) {
                    controller.unbindPlayer();
                }
                mVideoView = null;
            }
        }
        restorePirateMode();
        sendUserExitBroadcast();
        return super.onBackPressed();
    }

    private void sendUserExitBroadcast() {
        if (mMediaSource == null) return;

        Intent intent = new Intent(ACTION_USER_EXIT_FULLSCREEN);
        intent.putExtra(EXTRA_MEDIA_SOURCE, mMediaSource);
        intent.putExtra(EXTRA_CONTINUES_PLAYBACK, mContinuesPlayback);
        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        int screenOrientationDegree = -1;
        if (bundle != null) {
            mMediaSource = (MediaSource) bundle.getSerializable(EXTRA_MEDIA_SOURCE);
            mContinuesPlayback = bundle.getBoolean(EXTRA_CONTINUES_PLAYBACK);
            screenOrientationDegree = bundle.getInt(EXTRA_SCREEN_ORIENTATION_DEGREE);
        }

        mOrientationHelper = new OrientationHelper(requireActivity(), (last, current) -> {

            if (!mOrientationHelper.isEnabled()) return;
            if (mVideoView == null) return;

            L.v(mOrientationHelper, "orientationChanged", current);

            switch (current) {
                case OrientationHelper.ORIENTATION_90:
                    setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                    break;
                case OrientationHelper.ORIENTATION_270:
                    setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    break;
            }
        });
        mOrientationHelper.enable();

        initFullScreenSystemBar();
        initLandscapeMode(screenOrientationDegree);

        mPipSessionKey = UUID.randomUUID().toString();
    }

    @Override
    protected void initBackPressedHandler() {
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mVideoViewContainer = view.findViewById(R.id.videoViewContainer);
        mVideoView = createVideoView(view);
        PlaybackController controller = new PlaybackController();
        controller.bind(mVideoView);
        mVideoView.bindDataSource(mMediaSource);
        mVideoView.setPlayScene(PlayScene.SCENE_FULLSCREEN);
        mVideoViewContainer.addView(mVideoView,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT, Gravity.CENTER));

        controller.preparePlayback();
        final Player player = controller.player();
        if (player != null && (player.isPaused() || (!player.isLooping() && player.isCompleted()))) {
            mInterceptStartPlaybackOnResume = true;
        }
    }

    @NonNull
    private static VideoView createVideoView(@NonNull View view) {
        VideoView videoView = new VideoView(view.getContext());

        VideoLayerHost layerHost = new VideoLayerHost(view.getContext());
        layerHost.addLayer(new PlayerConfigLayer());
        layerHost.addLayer(new GestureLayer());
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
        videoView.setBackgroundColor(view.getResources().getColor(android.R.color.black));
        videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT);
        if (VideoSettings.intValue(VideoSettings.COMMON_RENDER_VIEW_TYPE) == DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW) {
            // 推荐使用 TextureView, 兼容性更好
            videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
        } else {
            videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW);
        }
        return videoView;
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        resume();
        dismissPip();
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOrientationHelper.disable();
    }

    private void resume() {
        if (mVideoView == null) return;

        if (!mInterceptStartPlaybackOnResume) {
            mVideoView.startPlayback();
        }
        mInterceptStartPlaybackOnResume = false;
    }

    private void pause() {
        if (mVideoView == null) return;

        Player player = mVideoView.player();
        if (player != null && (player.isPaused() || (!player.isLooping() && player.isCompleted()))) {
            mInterceptStartPlaybackOnResume = true;
        } else {
            mInterceptStartPlaybackOnResume = false;
            mVideoView.pausePlayback();
        }
    }

    private void stop() {
        if (mVideoView == null) return;

        mVideoView.stopPlayback();
        mVideoView = null;
    }

    private void initFullScreenSystemBar() {
        final Activity activity = requireActivity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (UIUtils.hasDisplayCutout(activity.getWindow())) {
                activity.getWindow().getAttributes().layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            }
        }
        activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final View decorView = activity.getWindow().getDecorView();
        int uiOptions = decorView.getSystemUiVisibility();
        uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                // Set the content to appear under the system bars so that the
                // content doesn't resize when the system bars hide and show.
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                // Hide the nav bar and status bar
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void initLandscapeMode(int screenOrientationDegree) {
        final int targetOrientation;
        if (screenOrientationDegree == OrientationHelper.ORIENTATION_90) {
            targetOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
        } else {
            targetOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }
        setRequestOrientation(targetOrientation);
    }

    private void restorePirateMode() {
        setRequestOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    private void setRequestOrientation(int targetOrientation) {
        final Activity activity = requireActivity();
        if (activity.getRequestedOrientation() != targetOrientation) {
            L.v(this, "setRequestOrientation", targetOrientation);
            activity.runOnUiThread(() ->
                    activity.setRequestedOrientation(targetOrientation)
            );
        }
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
