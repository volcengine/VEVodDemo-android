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
 * Create Date : 2024/4/1
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PauseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.DialogLayer;
import com.bytedance.volc.vod.scenekit.utils.TimeUtils;
import com.bytedance.volc.voddemo.impl.R;

public class DramaTimeProgressDialogLayer extends DialogLayer {
    private TextView mCurrentProgressView;
    private TextView mDurationView;

    private long mCurrentPosition;

    @Nullable
    @Override
    public String tag() {
        return "drama_time_progress_dialog";
    }


    @Override
    protected View createDialogView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_mini_drama_video_time_progress_dialog, parent, false);
        mCurrentProgressView = view.findViewById(R.id.currentProgress);
        mDurationView = view.findViewById(R.id.duration);
        setAnimateDismissListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                final VideoLayerHost layerHost = layerHost();
                if (layerHost == null) return;
                final DramaVideoLayer dramaVideoLayer = layerHost.findLayer(DramaVideoLayer.class);
                if (dramaVideoLayer != null) {
                    dramaVideoLayer.animateShow(false);
                }
                final PauseLayer pauseLayer = layerHost.findLayer(PauseLayer.class);
                if (pauseLayer != null) {
                    Player player = player();
                    if (player != null && player.isPaused()) {
                        pauseLayer.animateShow(false);
                    }
                }
            }
        });
        return view;
    }

    @Override
    protected int backPressedPriority() {
        return Layers.BackPriority.TIME_PROGRESS_DIALOG_LAYER_PRIORITY;
    }

    public void setCurrentPosition(long currentPosition, long duration) {
        mCurrentPosition = currentPosition;
        if (isShowing()) {
            mCurrentProgressView.setText(TimeUtils.time2String(currentPosition));
            mDurationView.setText(TimeUtils.time2String(duration));
        }
    }

    private void syncPosition() {
        Player player = player();
        if (player == null || !player.isInPlaybackState()) return;
        if (mCurrentPosition == 0) {
            mCurrentPosition = player.getCurrentPosition();
        }
        setCurrentPosition(mCurrentPosition, player.getDuration());
    }

    @Override
    public void show() {
        super.show();
        syncPosition();
    }

    public long getCurrentPosition() {
        return mCurrentPosition;
    }

    @Override
    public void animateDismiss() {
        super.animateDismiss();
    }
}
