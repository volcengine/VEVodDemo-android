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

import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.InfoBufferingStart;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;

public class LoadingLayer extends AnimateLayer {

    private static Handler mHandler;

    public LoadingLayer() {
        if (mHandler == null) mHandler = new Handler();
    }

    @Override
    public String tag() {
        return "loading";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        ProgressBar progressBar = (ProgressBar) LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_loading_layer, parent, false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) progressBar.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        return progressBar;
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
                case PlaybackEvent.Action.STOP_PLAYBACK: {
                    dismiss();
                    break;
                }
                case PlayerEvent.Action.PREPARE:
                case PlayerEvent.Action.START: {
                    showOpt();
                    break;
                }
                case PlayerEvent.Action.PAUSE:
                case PlayerEvent.Action.RELEASE: {
                    dismiss();
                    break;
                }
                case PlayerEvent.Action.SET_SURFACE: {
                    Player player = event.owner(Player.class);
                    if (player.isPlaying() && player.isBuffering()) {
                        showOpt();
                    } else if (player.isPreparing()) {
                        showOpt();
                    } else {
                        dismiss();
                    }
                    break;
                }
                case PlayerEvent.Info.VIDEO_RENDERING_START:
                case PlayerEvent.Info.VIDEO_RENDERING_START_BEFORE_START:
                case PlayerEvent.State.STARTED: {
                    Player player = event.owner(Player.class);
                    if (player.isPlaying() && player.isBuffering()) {
                        showOpt();
                    } else {
                        dismiss();
                    }
                    break;
                }
                case PlayerEvent.Info.BUFFERING_END:
                case PlayerEvent.State.COMPLETED:
                case PlayerEvent.State.ERROR: {
                    dismiss();
                    break;
                }
                case PlayerEvent.Info.BUFFERING_START: {
                    InfoBufferingStart e = event.cast(InfoBufferingStart.class);

                    int bufferNum = e.bufferId; // buffer 次数
                    int bufferType = e.bufferingType; // buffer 类型
                    int bufferStage = e.bufferingStage; // buffer 首帧前/后
                    int bufferReason = e.bufferingReason; // buffer 原因

                    Player player = event.owner(Player.class);
                    if (player.isPlaying()) {
                        showOpt();
                    }
                    break;
                }
            }
        }
    };

    @Override
    public void dismiss() {
        super.dismiss();
        mHandler.removeCallbacks(mShowRunnable);
    }

    private void showOpt() {
       showOpt(1000);
    }

    private void showOpt(long delayMills) {
        mHandler.removeCallbacks(mShowRunnable);
        mHandler.postDelayed(mShowRunnable, delayMills);
    }

    private final Runnable mShowRunnable = new Runnable() {
        @Override
        public void run() {
            animateShow(false);
        }
    };
}
