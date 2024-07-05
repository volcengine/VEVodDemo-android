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
 * Create Date : 2024/7/4
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.video.layer;

import android.content.Context;
import android.content.Intent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.BaseLayer;
import com.bytedance.volc.vod.scenekit.utils.GestureHelper;

import java.lang.ref.WeakReference;

public class DramaGestureLayer extends BaseLayer {
    public static final String ACTION_DRAMA_GESTURE_LAYER_SHOW_SPEED = "action_drama_gesture_layer_on_touch_event_long_press";
    public static final String ACTION_DRAMA_GESTURE_LAYER_DISMISS_SPEED = "action_drama_gesture_layer_on_touch_event_up";

    @Nullable
    @Override
    public String tag() {
        return "drama_gesture";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = new View(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        Gesture gesture = new Gesture(parent.getContext(), this);
        view.setOnTouchListener(gesture::onTouchEvent);
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

    private static class Gesture extends GestureHelper {
        private WeakReference<DramaGestureLayer> mLayerRef;

        public Gesture(Context context, DramaGestureLayer layer) {
            super(context);
            this.mLayerRef = new WeakReference<>(layer);
        }

        private boolean check() {
            DramaGestureLayer gestureLayer = mLayerRef.get();
            if (gestureLayer == null) return false;

            View view = gestureLayer.getView();
            if (view == null) return false;

            Player player = gestureLayer.player();
            return player != null && player.isInPlaybackState();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return check();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            final DramaGestureLayer layer = mLayerRef.get();
            if (layer == null) return;

            layer.showSpeed();
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {

            final DramaGestureLayer layer = mLayerRef.get();
            if (layer == null) return super.onSingleTapConfirmed(e);

            layer.dispatchInterceptedVideoViewOnClick();

            return true;
        }

        @Override
        public boolean onUp(MotionEvent e) {
            final DramaGestureLayer layer = mLayerRef.get();
            if (layer == null) return false;
            layer.dismissSpeed();
            return super.onUp(e);
        }

        @Override
        public boolean onCancel(MotionEvent e) {
            final DramaGestureLayer layer = mLayerRef.get();
            if (layer == null) return false;
            layer.dismissSpeed();
            return super.onCancel(e);
        }
    }

    private void dispatchInterceptedVideoViewOnClick() {
        final VideoView videoView = videoView();
        if (videoView == null) return;

        L.d(this, "dispatchInterceptedVideoViewOnClick");

        videoView.performClick();
    }

    protected void showSpeed() {
        final Context context = context();
        if (context == null) return;

        L.d(this, "showSpeed");

        final Player player = player();
        if (player != null && player.isPlaying()) {
            player.setSpeed(2f);
        }
        Intent intent = new Intent(ACTION_DRAMA_GESTURE_LAYER_SHOW_SPEED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    protected void dismissSpeed() {

        final Context context = context();
        if (context == null) return;

        L.d(this, "dismissSpeed");

        final Player player = player();
        if (player != null) {
            player.setSpeed(1f);
        }

        Intent intent = new Intent(ACTION_DRAMA_GESTURE_LAYER_DISMISS_SPEED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}
