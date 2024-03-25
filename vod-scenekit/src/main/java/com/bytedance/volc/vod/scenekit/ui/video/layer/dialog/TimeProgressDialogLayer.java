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

package com.bytedance.volc.vod.scenekit.ui.video.layer.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.Player;

import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.DialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.utils.TimeUtils;
import com.bytedance.volc.vod.scenekit.R;

public class TimeProgressDialogLayer extends DialogLayer {

    private ProgressBar mProgressBar;
    private TextView mTime;

    private long mCurrentPosition;

    @Override
    public String tag() {
        return "time_progress_dialog";
    }

    @Override
    protected View createDialogView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_time_progress_dialog_layer, parent, false);

        mProgressBar = view.findViewById(R.id.progressBar);
        mTime = view.findViewById(R.id.time);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateDismiss();
            }
        });
        return view;
    }

    @Override
    protected int backPressedPriority() {
        return Layers.BackPriority.TIME_PROGRESS_DIALOG_LAYER_PRIORITY;
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        if (playScene() != PlayScene.SCENE_FULLSCREEN) {
            dismiss();
        }
    }

    public void setCurrentPosition(long currentPosition, long duration) {
        mCurrentPosition = currentPosition;
        if (isShowing()) {
            int progress = (int) (currentPosition / (float) duration * 100);
            mProgressBar.setProgress(progress);
            mTime.setText(String.format("%s / %s", TimeUtils.time2String(currentPosition), TimeUtils.time2String(duration)));
        }
    }

    public long getCurrentPosition() {
        return mCurrentPosition;
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
}
