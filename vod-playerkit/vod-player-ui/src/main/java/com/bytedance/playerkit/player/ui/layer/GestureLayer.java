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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.player.ui.layer;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.ui.layer.base.BaseLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.TimeProgressDialogLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.VolumeBrightnessDialogLayer;
import com.bytedance.playerkit.player.ui.scene.PlayScene;
import com.bytedance.playerkit.player.ui.utils.GestureHelper;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;

import java.lang.ref.WeakReference;


public class GestureLayer extends BaseLayer {

    @Override
    public String tag() {
        return "gesture";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        final View view = new View(parent.getContext());
        final Gesture gesture = new Gesture(parent.getContext(), this);
        view.setOnTouchListener(gesture::onTouchEvent);
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return view;
    }

    @Override
    public void requestDismiss(@NonNull String reason) {
        // super.requestDismiss(reason);
    }

    @Override
    public void requestHide(@NonNull String reason) {
        // super.requestHide(reason);
    }

    @Override
    protected void onBindVideoView(@NonNull VideoView videoView) {
        super.onBindVideoView(videoView);
        show();
    }

    @Override
    protected void onBindLayerHost(@NonNull VideoLayerHost layerHost) {
        super.onBindLayerHost(layerHost);
        show();
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        VideoLayerHost layerHost = layerHost();
        if (layerHost == null) return;

        if (toScene != PlayScene.SCENE_FULLSCREEN) {
            VolumeBrightnessIconLayer volumeBrightnessIconLayer =
                    layerHost.findLayer(VolumeBrightnessIconLayer.class);
            LockLayer lockLayer = layerHost.findLayer(LockLayer.class);
            if (volumeBrightnessIconLayer != null) {
                volumeBrightnessIconLayer.dismiss();
            }
            if (lockLayer != null) {
                lockLayer.dismiss();
            }
        }

        if (isControllerShowing()) {
            showController();
        }
    }

    @Override
    protected void onLayerHostLockStateChanged(boolean locked) {
        if (locked) {
            dismissController();
        } else {
            showController();
        }
    }

    public void toggleControllerVisibility() {
        if (isControllerShowing()) {
            dismissController();
        } else {
            showController();
        }
    }

    public void showController() {
        VideoLayerHost host = layerHost();
        if (host == null) return;

        L.d(this, "showController");

        // TODO opt layer dismiss logic
        final Player player = player();
        boolean autoDismiss = player == null || !player.isPaused();

        TimeProgressBarLayer timeProgressLayer = host.findLayer(TimeProgressBarLayer.class);
        PlayPauseLayer playPauseLayer = host.findLayer(PlayPauseLayer.class);
        TitleBarLayer titleBarLayer = host.findLayer(TitleBarLayer.class);
        VolumeBrightnessIconLayer volumeBrightnessIconLayer = host.findLayer(VolumeBrightnessIconLayer.class);
        LockLayer lockLayer = host.findLayer(LockLayer.class);

        if (timeProgressLayer != null) {
            timeProgressLayer.animateShow(autoDismiss);
        }
        if (playPauseLayer != null) {
            playPauseLayer.animateShow(autoDismiss);
        }
        if (titleBarLayer != null) {
            titleBarLayer.animateShow(autoDismiss);
        }
        if (playScene() == PlayScene.SCENE_FULLSCREEN) {
            if (volumeBrightnessIconLayer != null) {
                volumeBrightnessIconLayer.animateShow(autoDismiss);
            }
            if (lockLayer != null) {
                lockLayer.animateShow(autoDismiss);
            }
        }
    }

    public void dismissController() {
        final VideoLayerHost host = layerHost();
        if (host == null) return;

        L.d(this, "dismissController");
        TimeProgressBarLayer timeProgressLayer = host.findLayer(TimeProgressBarLayer.class);
        PlayPauseLayer playPauseLayer = host.findLayer(PlayPauseLayer.class);
        TitleBarLayer titleBarLayer = host.findLayer(TitleBarLayer.class);
        VolumeBrightnessIconLayer volumeBrightnessIconLayer = host.findLayer(VolumeBrightnessIconLayer.class);
        LockLayer lockLayer = host.findLayer(LockLayer.class);

        if (timeProgressLayer != null) {
            timeProgressLayer.animateDismiss();
        }
        if (playPauseLayer != null) {
            playPauseLayer.animateDismiss();
        }
        if (titleBarLayer != null) {
            titleBarLayer.animateDismiss();
        }
        if (volumeBrightnessIconLayer != null) {
            volumeBrightnessIconLayer.animateDismiss();
        }
        if (lockLayer != null) {
            lockLayer.animateDismiss();
        }
    }

    public boolean isControllerShowing() {
        final VideoLayerHost host = layerHost();
        if (host == null) return false;
        final TimeProgressBarLayer layer = host.findLayer(TimeProgressBarLayer.class);
        return layer != null && layer.isShowing();
    }

    private static class Gesture extends GestureHelper {

        private static final int STATE_IDLE = 0;
        private static final int STATE_CHANGING_BRIGHTNESS = 1;
        private static final int STATE_CHANGING_VOLUME = 2;
        private static final int STATE_CHANGING_PROGRESS = 3;

        private int mState;

        private final WeakReference<GestureLayer> mLayerRef;

        public Gesture(Context context, GestureLayer layer) {
            super(context);
            this.mLayerRef = new WeakReference<>(layer);
        }

        private boolean check() {
            GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return false;

            View view = gestureLayer.getView();
            if (view == null) return false;

            Player player = gestureLayer.player();
            return player != null && player.isInPlaybackState();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!check()) return false;

            GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return false;

            gestureLayer.toggleControllerVisibility();
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (!check()) return false;

            GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null || gestureLayer.isLocked()) return false;


            final Player player = gestureLayer.player();
            if (player != null && player.isInPlaybackState()) {
                if (player.isPlaying()) {
                    player.pause();
                } else {
                    player.start();
                }
                if (gestureLayer.isControllerShowing()) {
                    gestureLayer.showController();
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return check();
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (!check()) return false;

            final GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null || gestureLayer.isLocked()) return false;

            if (gestureLayer.playScene() != PlayScene.SCENE_FULLSCREEN) return false;

            final View view = Asserts.checkNotNull(gestureLayer.getView());
            final int width = view.getWidth();

            if (mState == STATE_IDLE) {
                if (Math.abs(distanceX) < Math.abs(distanceY)) {
                    // change volume | brightness
                    boolean left = e1.getX() < width / 2f;
                    if (left) {
                        setState(STATE_CHANGING_BRIGHTNESS);
                        startChangeBrightness();
                    } else {
                        setState(STATE_CHANGING_VOLUME);
                        startChangeVolume();
                    }
                } else {
                    // change progress
                    setState(STATE_CHANGING_PROGRESS);
                    startChangeProgressPosition();
                }
            }
            switch (mState) {
                case STATE_IDLE:
                    break;
                case STATE_CHANGING_BRIGHTNESS:
                    changeBrightness(distanceY);
                    return true;
                case STATE_CHANGING_VOLUME:
                    changeVolume(distanceY);
                    return true;
                case STATE_CHANGING_PROGRESS:
                    changeProgressPosition(-distanceX);
                    return true;
            }
            return false;
        }

        @Override
        public boolean onUp(MotionEvent e) {
            L.d(this, "onUp", e);
            handleUpAndCancel();
            return false;
        }

        @Override
        public boolean onCancel(MotionEvent e) {
            handleUpAndCancel();
            return false;
        }

        private void handleUpAndCancel() {
            switch (mState) {
                case STATE_CHANGING_BRIGHTNESS: {
                    stopChangeBrightness();
                    setState(STATE_IDLE);
                    break;
                }
                case STATE_CHANGING_VOLUME: {
                    stopChangeVolume();
                    setState(STATE_IDLE);
                    break;
                }
                case STATE_CHANGING_PROGRESS: {
                    stopChangeProgressPosition();
                    setState(STATE_IDLE);
                    break;
                }
            }
        }

        public void setState(int state) {
            if (mState != state) {
                L.v(this, "setState", mState, state);
            }
            this.mState = state;
        }

        float mBrightnessProgress = 0;
        float mBrightnessProgressLast = 0;

        private void startChangeBrightness() {
            L.v(this, "startChangeBrightness");

            mBrightnessProgress = 0;
            mBrightnessProgressLast = 0;
        }

        private void changeBrightness(float delta) {

            final GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return;

            View view = gestureLayer.getView();
            if (view == null) return;

            VideoLayerHost host = gestureLayer.layerHost();
            if (host == null) return;

            VolumeBrightnessDialogLayer layer = host.findLayer(VolumeBrightnessDialogLayer.class);
            if (layer == null) return;

            int height = view.getHeight();
            if (height <= 0) return;

            float deltaProgress = delta / (height / 2f) * 100f;

            mBrightnessProgress += deltaProgress;

            int deltaWithLast = (int) (mBrightnessProgress - mBrightnessProgressLast);

            if (Math.abs(deltaWithLast) >= 1) {
                int progress = layer.getBrightByProgress();
                int currentProgress = progress + deltaWithLast;
                currentProgress = Math.min(100, Math.max(currentProgress, 0));

                mBrightnessProgressLast = mBrightnessProgressLast + deltaWithLast;

                L.v(this, "changeBrightness", currentProgress);

                layer.setBrightnessByProgress(currentProgress);
            }

            layer.setType(VolumeBrightnessDialogLayer.TYPE_BRIGHTNESS);
            if (!layer.isShowing()) {
                layer.animateShow(false);
            }
        }

        private void stopChangeBrightness() {
            L.v(this, "stopChangeBrightness");

            mBrightnessProgress = 0;
            mBrightnessProgressLast = 0;

            final GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return;

            VideoLayerHost host = gestureLayer.layerHost();
            if (host == null) return;

            VolumeBrightnessDialogLayer brightnessLayer = host.findLayer(VolumeBrightnessDialogLayer.class);
            if (brightnessLayer == null) return;

            brightnessLayer.animateDismiss();
        }

        private float mVolumeProgress = 0;
        private float mVolumeProgressLast = 0;

        private void startChangeVolume() {
            L.v(this, "startChangeVolume");
            mVolumeProgress = 0;
            mVolumeProgressLast = 0;
        }

        private void changeVolume(float delta) {
            final GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return;

            View view = gestureLayer.getView();
            if (view == null) return;

            VideoLayerHost host = gestureLayer.layerHost();
            if (host == null) return;

            VolumeBrightnessDialogLayer layer = host.findLayer(VolumeBrightnessDialogLayer.class);
            if (layer == null) return;

            int height = view.getHeight();
            if (height <= 0) return;

            float deltaProgress = delta / (height / 2f) * 100f;

            mVolumeProgress += deltaProgress;

            int deltaWithLast = (int) (mVolumeProgress - mVolumeProgressLast);

            if (Math.abs(deltaWithLast) >= 5) {
                int progress = layer.getVolumeByProgress();

                int currentProgress = progress + deltaWithLast;
                currentProgress = Math.min(100, Math.max(currentProgress, 0));

                L.v(this, "changeVolume", currentProgress);

                mVolumeProgressLast = mVolumeProgressLast + deltaWithLast;

                layer.setVolumeByProgress(currentProgress);
            }

            layer.setType(VolumeBrightnessDialogLayer.TYPE_VOLUME);
            if (!layer.isShowing()) {
                layer.animateShow(false);
            }
        }

        private void stopChangeVolume() {
            L.v(this, "stopChangeVolume");

            mVolumeProgress = 0;
            mVolumeProgressLast = 0;

            final GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return;

            final VideoLayerHost host = gestureLayer.layerHost();
            if (host == null) return;

            VolumeBrightnessDialogLayer layer = host.findLayer(VolumeBrightnessDialogLayer.class);
            if (layer == null) return;

            layer.animateDismiss();
        }


        long mProgressPosition;
        long mProgressPositionLast;

        private void startChangeProgressPosition() {
            L.v(this, "startChangeProgressPosition");
            mProgressPosition = 0;
        }

        private void changeProgressPosition(float delta) {
            L.v(this, "changeProgressPosition");

            final GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return;

            View view = gestureLayer.getView();
            if (view == null) return;

            VideoLayerHost host = gestureLayer.layerHost();
            if (host == null) return;

            TimeProgressDialogLayer layer = host.findLayer(TimeProgressDialogLayer.class);
            if (layer == null) return;

            int width = view.getWidth();
            if (width <= 0) return;

            Player player = gestureLayer.player();
            if (player == null) return;

            long duration = player.getDuration();

            float deltaProgress = delta / (width / 2f) * duration;

            mProgressPosition += deltaProgress;

            long deltaWithLast = (long) (mProgressPosition - mProgressPositionLast);

            if (Math.abs(deltaWithLast) > 100/*MS*/) {
                long progress = layer.isShowing() ? layer.getCurrentPosition() : player.getCurrentPosition();
                long currentProgress = progress + deltaWithLast;
                currentProgress = Math.min(duration, Math.max(0, currentProgress));

                mProgressPositionLast = mProgressPositionLast + deltaWithLast;

                L.v(this, "changeProgressPosition", progress, currentProgress);

                if (!layer.isShowing()) {
                    layer.animateShow(false);
                }

                layer.setCurrentPosition(currentProgress, duration);
            }
        }

        private void stopChangeProgressPosition() {
            L.v(this, "stopChangeProgressPosition");
            mProgressPosition = 0;
            mProgressPositionLast = 0;

            final GestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return;

            VideoLayerHost host = gestureLayer.layerHost();
            if (host == null) return;

            TimeProgressDialogLayer progressLayer = host.findLayer(TimeProgressDialogLayer.class);
            if (progressLayer == null) return;

            if (progressLayer.isShowing()) {
                progressLayer.animateDismiss();
                long currentPosition = progressLayer.getCurrentPosition();

                final Player player = gestureLayer.player();
                if (player != null && player.isInPlaybackState()) {
                    player.seekTo(currentPosition);
                }
            }
        }
    }
}
