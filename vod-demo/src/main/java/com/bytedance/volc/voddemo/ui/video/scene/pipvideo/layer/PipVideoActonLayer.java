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

package com.bytedance.volc.voddemo.ui.video.scene.pipvideo.layer;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.InfoBufferingUpdate;
import com.bytedance.playerkit.player.event.InfoProgressUpdate;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;
import com.bytedance.volc.vod.scenekit.ui.widgets.MediaSeekBar;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.PipVideoController;

public class PipVideoActonLayer extends AnimateLayer {

    private MediaSeekBar mSeekBar;
    private ImageView mPlayPause;

    @Nullable
    @Override
    public String tag() {
        return "pip_video_action";
    }

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_pip_video_action_layer, parent, false);
        view.findViewById(R.id.actionBarToggle).setOnClickListener(v -> {
            PipVideoController.instance().pipToMain(context());
        });
        view.findViewById(R.id.actionBarClose).setOnClickListener(v -> {
            PipVideoController.instance().closePip();
        });
        mSeekBar = view.findViewById(R.id.mediaSeekbar);
        mSeekBar.setTextVisibility(false);
        SeekBar seek = ((SeekBar) mSeekBar.findViewById(R.id.seekBar));
        seek.setThumb(null);
        seek.setOnTouchListener((View v, MotionEvent event) -> true);
        mSeekBar.setOnSeekListener(new MediaSeekBar.OnUserSeekListener() {
            @Override
            public void onUserSeekStart(long startPosition) {
            }

            @Override
            public void onUserSeekPeeking(long peekPosition) {
            }

            @Override
            public void onUserSeekStop(long startPosition, long seekToPosition) {
                final Player player = player();
                if (player == null) return;

                if (player.isInPlaybackState()) {
                    if (player.isCompleted()) {
                        player.start();
                        player.seekTo(seekToPosition);
                    } else {
                        player.seekTo(seekToPosition);
                    }
                }
            }
        });
        mPlayPause = view.findViewById(R.id.playPause);
        mPlayPause.setOnClickListener(v -> togglePlayPause());
        return view;
    }

    @Override
    public void show() {
        super.show();
        syncPlayPause();
        syncProgress();
    }

    @Override
    protected void onBindLayerHost(@NonNull VideoLayerHost layerHost) {
        super.onBindLayerHost(layerHost);
        show();
    }

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        controller.addPlaybackListener(mPlaybackListener);
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        controller.removePlaybackListener(mPlaybackListener);
    }

    private final Dispatcher.EventListener mPlaybackListener = new Dispatcher.EventListener() {

        @Override
        public void onEvent(Event event) {
            switch (event.code()) {
                case PlaybackEvent.Action.START_PLAYBACK:
                    hide();
                    animateShow(true);
                    break;
                case PlaybackEvent.State.BIND_PLAYER:
                    if (player() != null) {
                        syncProgress();
                        syncPlayPause();
                    }
                    break;
                case PlayerEvent.State.PREPARING:
                case PlayerEvent.State.PREPARED:
                case PlayerEvent.State.STARTED:
                case PlayerEvent.State.PAUSED:
                case PlayerEvent.State.COMPLETED:
                case PlayerEvent.State.ERROR:
                case PlayerEvent.State.RELEASED: {
                    syncProgress();
                    syncPlayPause();
                    break;
                }
                case PlayerEvent.Info.PROGRESS_UPDATE: {
                    InfoProgressUpdate e = event.cast(InfoProgressUpdate.class);
                    setProgress(e.currentPosition, e.duration, -1);
                    break;
                }
                case PlayerEvent.Info.BUFFERING_UPDATE: {
                    InfoBufferingUpdate e = event.cast(InfoBufferingUpdate.class);
                    setProgress(-1, -1, e.percent);
                    break;
                }
            }
        }
    };

    private void setProgress(long currentPosition, long duration, int bufferPercent) {
        if (mSeekBar != null) {
            if (duration >= 0) {
                mSeekBar.setDuration(duration);
            }
            if (currentPosition >= 0) {
                mSeekBar.setCurrentPosition(currentPosition);
            }
            if (bufferPercent >= 0) {
                mSeekBar.setCachePercent(bufferPercent);
            }
        }
    }

    private void syncProgress() {
        final PlaybackController controller = this.controller();
        if (controller != null) {
            final Player player = controller.player();
            if (player != null) {
                if (player.isInPlaybackState()) {
                    setProgress(player.getCurrentPosition(), player.getDuration(), player.getBufferedPercentage());
                }
            }
        }
    }

    protected void syncPlayPause() {
        final PlaybackController controller = controller();
        if (controller != null) {
            final Player player = controller.player();
            if (player != null) {
                L.d(this, "syncPlayPause", player.isPlaying());
                mPlayPause.setSelected(player.isPlaying());
            } else {
                L.d(this, "syncPlayPause", false);
                mPlayPause.setSelected(false);
            }
        } else {
            L.d(this, "syncPlayPause", false);
            mPlayPause.setSelected(false);
        }
    }

    protected void togglePlayPause() {
        final Player player = player();
        if (player != null) {
            if (player.isPlaying()) {
                player.pause();
            } else if (player.isPaused() || player.isCompleted()) {
                player.start();
            } else {
                L.e(this, "wrong state", player.dump());
            }
        } else {
            startPlayback();
        }
    }
}
