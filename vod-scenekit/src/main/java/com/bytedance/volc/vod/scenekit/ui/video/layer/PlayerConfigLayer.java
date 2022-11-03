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

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayer;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.volc.vod.scenekit.VideoSettings;

public class PlayerConfigLayer extends VideoLayer {
    @Nullable
    @Override
    public String tag() {
        return "loop";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        return null;
    }

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        controller.addPlaybackListener(eventListener);
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        controller.removePlaybackListener(eventListener);
    }

    final Dispatcher.EventListener eventListener = event -> {
        switch (event.code()) {
            case PlayerEvent.Action.PREPARE:
                Player player = event.owner(Player.class);
                player.setLooping(true);
                player.setSuperResolutionEnabled(VideoSettings.booleanValue(VideoSettings.COMMON_SUPER_RESOLUTION));
                break;
        }
    };
}
