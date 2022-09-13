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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.ui.R;
import com.bytedance.playerkit.player.ui.layer.base.AnimateLayer;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;


public class PauseLayer extends AnimateLayer {

    private ObjectAnimator scaleXAnimator;
    private ObjectAnimator scaleYAnimator;

    @Override
    public String tag() {
        return "pause";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.pause_layer, parent, false);
        view.setOnClickListener(null);
        view.setClickable(false);
        return view;
    }

    @Override
    protected Animator createAnimator() {
        scaleXAnimator = new ObjectAnimator();
        scaleYAnimator = new ObjectAnimator();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleXAnimator, scaleYAnimator);
        set.setInterpolator(new DecelerateInterpolator());
        set.setDuration(150);
        return set;
    }

    @Override
    protected void initAnimateDismissProperty(Animator animator) {
        if (scaleXAnimator != null) {
            // Using scaleX animator animate alpha instead create a new animator instance.
            scaleXAnimator.setPropertyName("alpha");
            scaleXAnimator.setFloatValues(1, 0);
        }
        if (scaleYAnimator != null) {
            scaleYAnimator.setPropertyName("scaleY");
            scaleYAnimator.setFloatValues(1, 1);
        }
    }

    @Override
    protected void initAnimateShowProperty(Animator animator) {
        if (scaleXAnimator != null) {
            scaleXAnimator.setPropertyName("scaleX");
            scaleXAnimator.setFloatValues(1, 1.5f, 1);
        }
        if (scaleYAnimator != null) {
            scaleYAnimator.setPropertyName("scaleY");
            scaleYAnimator.setFloatValues(1, 1.5f, 1);
        }
    }

    @Override
    protected void resetViewAnimateProperty() {
        View view = getView();
        if (view != null) {
            view.setScaleX(1);
            view.setScaleY(1);
            view.setAlpha(1);
        }
    }

    @Override
    public void onVideoViewClick(VideoView videoView) {
        final Player player = player();
        if (player != null && player.isInPlaybackState()) {
            if (player.isPlaying()) {
                player.pause();
                animateShow(false);
            } else {
                player.start();
            }
        } else {
            startPlayback();
        }
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
                case PlayerEvent.Action.START:
                case PlayerEvent.Info.VIDEO_RENDERING_START:
                    animateDismiss();
                    break;
                case PlayerEvent.Action.STOP:
                case PlayerEvent.Action.RELEASE:
                    dismiss();
                    break;
            }
        }
    };
}
