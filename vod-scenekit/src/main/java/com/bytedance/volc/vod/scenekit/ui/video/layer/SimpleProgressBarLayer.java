/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/11/2
 */

package com.bytedance.volc.vod.scenekit.ui.video.layer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.InfoBufferingUpdate;
import com.bytedance.playerkit.player.event.InfoProgressUpdate;
import com.bytedance.playerkit.player.playback.PlaybackController;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;
import com.bytedance.volc.vod.scenekit.ui.widgets.MediaSeekBar;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.R;

public class SimpleProgressBarLayer extends AnimateLayer {
    public static final String ACTION_ENTER_FULLSCREEN = "com.bytedance.volc.vod.scenekit.ui.video.layer/enter_full_screen";
    public static final String EXTRA_MEDIA_SOURCE = "extra_media_source";
    private MediaSeekBar mSeekBar;
    private View mFullScreenView;

    @Override
    public String tag() {
        return "simple_progress";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_simple_progress_layer, parent, false);
        mSeekBar = view.findViewById(R.id.mediaSeekBar);
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

        mFullScreenView = view.findViewById(R.id.fullScreen);
        mFullScreenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFullScreenClick(v);
            }
        });
        return view;
    }

    private void onFullScreenClick(View v) {
        Intent intent = new Intent();
        intent.setAction(ACTION_ENTER_FULLSCREEN);
        LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
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

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        controller.addPlaybackListener(mPlaybackListener);
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        controller.removePlaybackListener(mPlaybackListener);
        dismiss();
    }

    private final Dispatcher.EventListener mPlaybackListener = new Dispatcher.EventListener() {

        @Override
        public void onEvent(Event event) {
            switch (event.code()) {
                case PlayerEvent.Action.START:
                    if (event.owner(Player.class).isPaused()) {
                        animateShow(false);
                    }
                    break;
                case PlayerEvent.State.STARTED: {
                    syncProgress();
                    break;
                }
                case PlayerEvent.State.COMPLETED: {
                    syncProgress();
                    Player player = player();
                    if (player != null && !player.isLooping()) {
                        dismiss();
                    }
                    break;
                }
                case PlayerEvent.State.ERROR:
                case PlayerEvent.State.STOPPED:
                case PlayerEvent.State.RELEASED: {
                    dismiss();
                    break;
                }
                case PlayerEvent.Info.VIDEO_RENDERING_START:
                    animateShow(false);
                    break;
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

    @Override
    public void show() {
        super.show();
        syncProgress();
    }
}
