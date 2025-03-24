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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer;

import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.ui.video.layer.CoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoStrategy;

public class ShortVideoCoverLayer extends CoverLayer {

    /**
     * 针对续播场景优化向上翻页封面体验
     */
    private final boolean mSyncStartProgress;

    public ShortVideoCoverLayer(boolean syncStartProgress) {
        this.mSyncStartProgress = syncStartProgress;
    }

    @Override
    public String tag() {
        return "short_video_cover";
    }

    @Override
    protected void load() {
        if (!VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_IMAGE_COVER)) return;

        super.load();
    }

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        if (mSyncStartProgress) {
            controller.addPlaybackListener(mPlaybackListener);
        } else {
            super.onBindPlaybackController(controller);
        }
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        if (mSyncStartProgress) {
            controller.removePlaybackListener(mPlaybackListener);
        } else {
            super.onBindPlaybackController(controller);
        }
    }

    private final Dispatcher.EventListener mPlaybackListener = new Dispatcher.EventListener() {

        @Override
        public void onEvent(Event event) {
            switch (event.code()) {
                case PlayerEvent.Action.SET_SURFACE: {
                    Player player = player();
                    if (player != null && player.isInPlaybackState()) {
                        dismiss();
                    }
                    break;
                }
                case PlayerEvent.Info.VIDEO_RENDERING_START: {
                    dismiss();
                    break;
                }
            }
        }
    };

    @Override
    public void onSurfaceDestroy(Surface surface) {
        L.d(this, "onSurfaceDestroy", surface);
        show();
    }

    @Override
    protected void handleEvent(int code, @Nullable Object obj) {
        super.handleEvent(code, obj);
        if (code == Layers.Event.VIEW_PAGER_ON_PAGE_PEEK_START.ordinal()) {
            startPreRenderCover("ViewPager#onPagePeekStart");
        }
    }

    public void startPreRenderCover(String reason) {
        final VideoView videoView = videoView();
        if (videoView == null) return;

        if (videoView.getSurface() == null || !videoView.getSurface().isValid()) return;

        if (player() != null) {
            return;
        }

        final boolean rendered = ShortVideoStrategy.renderFrame(videoView);
        if (rendered) {
            L.d(this, "startPreRenderCover", reason, videoView, videoView.getSurface(), "preRender success");
            dismiss();
        } else {
            L.d(this, "startPreRenderCover", reason, videoView, videoView.getSurface(), "preRender failed");
        }
    }
}
