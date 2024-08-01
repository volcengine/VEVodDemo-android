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


import static com.bytedance.playerkit.player.playback.DisplayModeHelper.calDisplayAspectRatio;
import static com.bytedance.volc.vod.scenekit.VideoSettings.booleanValue;

import android.view.Surface;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.ActionSetSurface;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.volcengine.VolcPreRenderSurfaceHolder;
import com.bytedance.playerkit.player.volcengine.VolcPreRenderSurfaceHolder.PreRenderListener;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.ui.video.layer.CoverLayer;

public class ShortVideoCoverLayer extends CoverLayer {
    /**
     * 针对续播场景优化向上翻页封面体验
     */
    private final boolean mSyncStartProgress;

    private final VolcPreRenderSurfaceHolder mPreRenderSurfaceHolder;

    public ShortVideoCoverLayer(boolean syncStartProgress) {
        this.mSyncStartProgress = syncStartProgress;
        this. mPreRenderSurfaceHolder = new VolcPreRenderSurfaceHolder();
    }

    @Override
    public String tag() {
        return "short_video_cover";
    }

    @Override
    public void onVideoViewBindDataSource(MediaSource dataSource) {
        super.onVideoViewBindDataSource(dataSource);
        mPreRenderSurfaceHolder.onVideoViewBindDataSource(dataSource);
    }

    @Override
    public void onSurfaceAvailable(Surface surface, int width, int height) {
        L.d(this, "onSurfaceAvailable", MediaSource.dump(dataSource()), surface, width, height);
        mPreRenderSurfaceHolder.onSurfaceAvailable(surface, width, height);
    }

    @Override
    public void onSurfaceDestroy(Surface surface) {
        L.d(this, "onSurfaceDestroy", MediaSource.dump(dataSource()), surface);
        show();
        mPreRenderSurfaceHolder.onSurfaceDestroyed(surface);
    }

    @Override
    protected void onBindVideoView(@NonNull VideoView videoView) {
        super.onBindVideoView(videoView);
        mPreRenderSurfaceHolder.setPreRenderListener(mPreRenderListener);
    }

    @Override
    protected void onUnBindVideoView(@NonNull VideoView videoView) {
        super.onUnBindVideoView(videoView);
        mPreRenderSurfaceHolder.setPreRenderListener(null);
    }

    @Override
    protected void load() {
        if (!booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_IMAGE_COVER)) return;

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
                    final ActionSetSurface e = event.cast(ActionSetSurface.class);
                    final Player player = player();
                    if (player != null && player.getSurface() != e.surface && player.isInPlaybackState()) {
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

    private final PreRenderListener mPreRenderListener = new PreRenderListener() {
        @Override
        public void onPreRenderVideoSizeChanged(MediaSource mediaSource, int videoWidth, int videoHeight) {
            final VideoView videoView = videoView();
            if (videoView == null) return;
            videoView.setDisplayAspectRatio(calDisplayAspectRatio(videoWidth, videoHeight, 0));
        }

        @Override
        public void onPreRenderFirstFrame(MediaSource mediaSource, int videoWidth, int videoHeight) {
            final VideoView videoView = videoView();
            if (videoView == null) return;

            L.d(ShortVideoCoverLayer.this, "onPreRenderFirstFrame", MediaSource.dump(mediaSource), videoWidth, videoHeight);

            videoView.setDisplayAspectRatio(calDisplayAspectRatio(videoWidth, videoHeight, 0));

            if (videoView.getDisplayViewType() == DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW) {
                dismiss();
            }
        }

        @Override
        public void onPreRenderEndError(MediaSource mediaSource, int errorCode) {
            L.d(ShortVideoCoverLayer.this, "onPreRenderEndError", MediaSource.dump(mediaSource), errorCode);
        }

        @Override
        public void onPreRenderEndRelease(MediaSource mediaSource) {
            L.d(ShortVideoCoverLayer.this, "onPreRenderEndRelease", MediaSource.dump(mediaSource));
        }
    };
}
