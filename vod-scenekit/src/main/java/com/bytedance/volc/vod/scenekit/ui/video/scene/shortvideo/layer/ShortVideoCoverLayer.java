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

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.utils.ProgressRecorder;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.ui.video.layer.CoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoStrategy;

public class ShortVideoCoverLayer extends CoverLayer {
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
            if (!isPreRenderWithStartTime() && videoView.getDisplayViewType() == DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW) {
                dismiss();
            }
        } else {
            L.d(this, "startPreRenderCover", reason, videoView, videoView.getSurface(), "preRender failed");
        }
    }

    private boolean isPreRenderWithStartTime() {
        MediaSource mediaSource = dataSource();
        if (mediaSource == null) return false;
        return ProgressRecorder.getProgress(mediaSource.getSyncProgressId()) > 0;
    }
}
