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

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.ui.R;
import com.bytedance.playerkit.player.ui.layer.base.AnimateLayer;
import com.bytedance.playerkit.player.ui.scene.PlayScene;
import com.bytedance.playerkit.player.ui.utils.UIUtils;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;

public class PlayPauseLayer extends AnimateLayer {

    @Override
    public String tag() {
        return "play_pause";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        final ImageView ivPlayPause = (ImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.play_pause_layer, parent, false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) ivPlayPause.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        ivPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlayPause();
            }
        });
        return ivPlayPause;
    }

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        controller.addPlaybackListener(mPlaybackListener);
        show();
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        controller.removePlaybackListener(mPlaybackListener);
        show();
    }

    protected void togglePlayPause() {
        final Player player = player();
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else if (player.isPaused() || player.isCompleted()) {
                player.start();
            } else {
                L.e(PlayPauseLayer.this, "wrong state", player.dump());
            }
        } else {
            startPlayback();
        }
    }

    protected void setPlayPause(boolean isPlay) {
        final ImageView playButton = getView();
        if (playButton == null) return;

        playButton.setSelected(isPlay);
    }

    protected void syncState() {
        final PlaybackController controller = controller();
        if (controller != null) {
            final Player player = controller.player();
            if (player != null) {
                setPlayPause(player.isPlaying());
            } else {
                setPlayPause(false);
            }
        } else {
            setPlayPause(false);
        }
    }

    private void syncTheme() {
        final ImageView playButton = getView();
        if (playButton == null) return;
        VideoView videoView = videoView();
        if (videoView == null) return;
        if (videoView.getPlayScene() == PlayScene.SCENE_FULLSCREEN) {
            playButton.setImageDrawable(ResourcesCompat.getDrawable(playButton.getResources(),
                    R.drawable.play_pause_layer_fullscreen_ic_selector, null));
            final int size = (int) UIUtils.dip2Px(context(), 48);
            playButton.getLayoutParams().width = size;
            playButton.getLayoutParams().height = size;
            playButton.requestLayout();
        } else {
            playButton.setImageDrawable(ResourcesCompat.getDrawable(playButton.getResources(),
                    R.drawable.play_pause_layer_halfscreen_ic_selector, null));
            final int size = (int) UIUtils.dip2Px(context(), 40);
            playButton.getLayoutParams().width = size;
            playButton.getLayoutParams().height = size;
            playButton.requestLayout();
        }
    }

    private final Dispatcher.EventListener mPlaybackListener = new Dispatcher.EventListener() {

        @Override
        public void onEvent(Event event) {
            switch (event.code()) {
                case PlaybackEvent.Action.START_PLAYBACK:
                    if (player() == null) {
                        dismiss();
                    }
                    break;
            }

            switch (event.code()) {
                case PlayerEvent.State.STARTED:
                    syncState();
                    break;
                case PlayerEvent.State.PAUSED:
                    syncState();
                    break;
                case PlayerEvent.State.COMPLETED:
                    syncState();
                    dismiss();
                    break;
                case PlayerEvent.State.STOPPED:
                case PlayerEvent.State.RELEASED:
                    show();
                    break;
                case PlayerEvent.State.ERROR:
                    dismiss();
                    break;
                case PlayerEvent.Info.BUFFERING_END:
                case PlayerEvent.Info.BUFFERING_START:
                    dismiss();
                    break;
            }
        }
    };

    @Override
    public void show() {
        super.show();
        syncState();
        syncTheme();
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        syncState();
        syncTheme();
    }
}
