/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/3/19
 */

package com.bytedance.volc.voddemo.ui.video.scene.pipvideo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LoadingLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayCompleteLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayErrorLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.VideoViewFactory;
import com.bytedance.volc.vod.scenekit.ui.video.scene.pipvideo.PipVideoScene;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.vod.settingskit.SettingItem;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.layer.PipVideoActonLayer;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.layer.PipVideoGestureLayer;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.permission.PipVideoPermission;

import java.lang.ref.WeakReference;
import java.util.List;

public class PipVideoController {
    public static final String ACTION_TOGGLE_PIP_TO_MAIN = "action_toggle_pip_to_main";
    public static final String EXTRA_CURRENT_VIDEO_ITEM = "extra_current_video_item";

    @SuppressLint("StaticFieldLeak")
    private static PipVideoController sInstance;
    private final PipVideoPermission mPipVideoPermission;
    private final Context mContext;

    private MainVideoInfo mMainVideoInfo;
    private PipVideoScene mPipVideoScene;

    public static class MainVideoInfo {
        public final String sessionKey;
        public final WeakReference<Activity> mainActivityRef;
        public final Class<? extends Activity> mainActivityClass;
        public final WeakReference<VideoView> mainVideoViewRef;

        public MainVideoInfo(PipVideoConfig config) {
            this.sessionKey = config.sessionKey;
            this.mainActivityRef = new WeakReference<>(config.mainActivity);
            this.mainVideoViewRef = new WeakReference<>(config.mainVideoView);
            this.mainActivityClass = config.mainActivity.getClass();
        }
    }

    public static class PipVideoConfig {
        private final String sessionKey;
        private final Activity mainActivity;
        private final VideoView mainVideoView;
        private final List<VideoItem> videoItems;
        private final int playIndex;

        public PipVideoConfig(String sessionKey,
                              Activity mainActivity,
                              VideoView mainVideoView,
                              List<VideoItem> videoItems,
                              int playIndex) {
            this.sessionKey = sessionKey;
            this.mainActivity = mainActivity;
            this.mainVideoView = mainVideoView;
            this.videoItems = videoItems;
            this.playIndex = playIndex;
        }
    }

    public static synchronized PipVideoController instance() {
        if (sInstance == null) {
            sInstance = new PipVideoController();
        }
        return sInstance;
    }

    public PipVideoController() {
        mContext = VolcPlayerInit.config().context;
        mPipVideoPermission = new PipVideoPermission(mContext);
    }

    public void requestPermission(Context context, SettingItem item, RecyclerView.ViewHolder holder) {
        mPipVideoPermission.requestPermission(new PipVideoPermission.Callback() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResult(boolean isGranted) {
                if (isGranted) {
                    Toast.makeText(context, R.string.vevod_pip_video_permission_request_granted, Toast.LENGTH_SHORT).show();
                } else {
                    item.option.userValues().saveValue(item.option, false);
                    RecyclerView.Adapter<?> adapter = holder.getBindingAdapter();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    Toast.makeText(context, R.string.vevod_pip_video_permission_request_not_granted_switching_error, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRationale(Context context, PipVideoPermission.UserAction userAction) {
                showRationaleDialog(context, userAction);
            }
        });
    }

    public void requestMainToPip(PipVideoConfig config) {
        if (mPipVideoPermission.isPermissionGranted()) {
            mainToPip(config);
        } else {
            mPipVideoPermission.requestPermission(new PipVideoPermission.Callback() {
                @Override
                public void onResult(boolean isGranted) {
                    if (isGranted) {
                        mainToPip(config);
                    } else {
                        Toast.makeText(config.mainActivity, R.string.vevod_pip_video_permission_request_not_granted, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onRationale(Context context, PipVideoPermission.UserAction userAction) {
                    showRationaleDialog(context, userAction);
                }
            });
        }
    }

    public void mainToPip(PipVideoConfig config) {
        if (!isPipPermissionGranted()) return;

        // 1. unbind main video player
        if (config.mainVideoView == null) return;
        final PlaybackController mainController = config.mainVideoView.controller();
        if (mainController == null) return;
        if (mainController.player() == null) {
            // only toggle to pip mode when main video playback has been started.
            return;
        }
        mainController.unbindPlayer();

        // 2. record main session info and switch app to background
        mMainVideoInfo = new MainVideoInfo(config);
        //config.mainActivity.finish();
        config.mainActivity.moveTaskToBack(true);

        // 3. open pip
        openPip(config);
    }

    public void pipToMain(Context context) {
        if (mPipVideoScene == null) return;
        final PlaybackController controller = mPipVideoScene.videoView().controller();
        if (controller != null) {
            controller.unbindPlayer();
        }
        mPipVideoScene.windowView().dismiss();
        intentIntoMainVideo(context);
    }

    public void closePip() {
        if (mPipVideoScene == null) return;
        mPipVideoScene.videoView().stopPlayback();
        mPipVideoScene.windowView().dismiss();
    }

    public void dismissPip() {
        if (mPipVideoScene == null) return;
        mPipVideoScene.windowView().dismiss();
    }

    public MainVideoInfo getMainVideoInfo() {
        return mMainVideoInfo;
    }

    public void resetMainVideoInfo() {
        mMainVideoInfo = null;
    }

    @Nullable
    public VideoItem getCurrentVideoItem() {
        List<MediaSource> playlist = mPipVideoScene.getPlaylist();
        if (playlist == null || playlist.isEmpty()) return null;
        int playIndex = mPipVideoScene.getPlayIndex();
        if (playIndex < 0 || playIndex >= playlist.size()) return null;
        return VideoItem.get(playlist.get(playIndex));
    }

    public boolean isPipPermissionGranted() {
        return mPipVideoPermission.isPermissionGranted();
    }

    public boolean isPipShowing() {
        return mPipVideoScene != null && mPipVideoScene.windowView().isShowing();
    }

    private void openPip(PipVideoConfig config) {
        // 3. create PipVideoWindow
        if (mPipVideoScene == null) {
            mPipVideoScene = new PipVideoScene(new ContextThemeWrapper(mContext, R.style.VEVodAppTheme),
                    new PipVideoViewFactory());
        } else {
            // stop playback before bind playlist
            mPipVideoScene.videoView().stopPlayback();
        }

        // 4. bind playlist
        mPipVideoScene.setPlaylist(VideoItem.toMediaSources(config.videoItems));

        // 5. set PipWindow init size and position
        initPipWindow(config.mainVideoView.getPlayScene());

        // 6. show and play
        mPipVideoScene.windowView().show();
        mPipVideoScene.playIndex(config.playIndex);
    }

    private void initPipWindow(int mainVideoPlayScene) {
        float ratio = mainVideoPlayScene == PlayScene.SCENE_SHORT ? 9 / 16f : 16 / 9f;
        int screenWidth = UIUtils.getScreenWidth(mContext);
        int screenHeight = UIUtils.getScreenHeight(mContext);
        int screenMinSize = Math.min(screenWidth, screenHeight);
        int margin = (int) UIUtils.dip2Px(mContext, 12);
        int width;
        int height;
        if (ratio < 1) {
            width = (int) (screenMinSize * (6 / 11f));
            height = (int) (width / ratio);
        } else {
            width = screenMinSize - 2 * margin;
            height = (int) (width / ratio);
        }
        int initMarginBottom = (int) UIUtils.dip2Px(mContext, 120);
        int x = margin;
        int y = screenHeight - height - initMarginBottom;
        mPipVideoScene.windowView().setWindowInitWidth(width);
        mPipVideoScene.windowView().setWindowInitHeight(height);
        mPipVideoScene.windowView().setWindowMargin(margin);
        mPipVideoScene.windowView().setWindowInitX(x);
        mPipVideoScene.windowView().setWindowInitY(y);
        mPipVideoScene.windowView().setInitShow(true);

        mPipVideoScene.windowView().setRadius(UIUtils.dip2Px(mContext, 8));
        mPipVideoScene.windowView().setCardBackgroundColor(Color.BLACK);
        mPipVideoScene.windowView().setCardElevation(UIUtils.dip2Px(mContext, 1));
    }

    private void intentIntoMainVideo(Context context) {
        if (mMainVideoInfo == null) return;
        Intent intent = new Intent(context, mMainVideoInfo.mainActivityClass);
        intent.setAction(ACTION_TOGGLE_PIP_TO_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        final VideoItem currentVideoItem = getCurrentVideoItem();
        if (currentVideoItem == null) return;
        intent.putExtra(EXTRA_CURRENT_VIDEO_ITEM, currentVideoItem);
        context.startActivity(intent);
    }

    private void showRationaleDialog(Context context, PipVideoPermission.UserAction userAction) {
        new AlertDialog.Builder(context)
                .setMessage(R.string.vevod_pip_video_rationale_dialog_msg)
                .setPositiveButton(R.string.vevod_pip_video_rationale_dialog_positive_button_text, (dialog, which) -> userAction.granted())
                .setNegativeButton(R.string.vevod_pip_video_rationable_dialog_negative_button_text, (dialog, which) -> userAction.denied())
                .setCancelable(false)
                .show();
    }

    private static class PipVideoViewFactory implements VideoViewFactory {

        @Override
        public VideoView createVideoView(ViewGroup parent, @Nullable Object o) {
            VideoView videoView = new VideoView(parent.getContext());
            VideoLayerHost layerHost = new VideoLayerHost(parent.getContext());
            layerHost.addLayer(new PipVideoGestureLayer());
            layerHost.addLayer(new PipVideoActonLayer());
            layerHost.addLayer(new LoadingLayer());
            layerHost.addLayer(new PlayErrorLayer());
            layerHost.addLayer(new PlayCompleteLayer());
            if (VideoSettings.booleanValue(VideoSettings.DEBUG_ENABLE_LOG_LAYER)) {
                layerHost.addLayer(new LogLayer());
            }
            PlaybackController controller = new PlaybackController();
            controller.bind(videoView);
            layerHost.attachToVideoView(videoView);
            videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL);
            videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
            videoView.setPlayScene(PlayScene.SCENE_PIP);
            return videoView;
        }
    }
}
