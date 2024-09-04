/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/7/11
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.BaseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoPageView;

import java.lang.ref.WeakReference;

public class ShortVideoDebugLayer extends BaseLayer implements VideoView.VideoViewPlaybackActionInterceptor {

    private final WeakReference<ShortVideoPageView> mPageView;

    public ShortVideoDebugLayer(WeakReference<ShortVideoPageView> mPageView) {
        this.mPageView = mPageView;
    }

    @Nullable
    @Override
    public String tag() {
        return "short_video_debug";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        return null;
    }

    @Override
    protected void onBindVideoView(@NonNull VideoView videoView) {
        super.onBindVideoView(videoView);
        videoView.addPlaybackInterceptor(0, this);
    }

    @Override
    protected void onUnBindVideoView(@NonNull VideoView videoView) {
        super.onUnBindVideoView(videoView);
        videoView.removePlaybackInterceptor(0, this);
    }

    @Override
    public String onVideoViewInterceptStartPlayback(VideoView videoView) {
        VideoItem videoItem = VideoItem.get(dataSource());
        if (videoItem == null) return null;
        ShortVideoPageView pageView = mPageView.get();
        if (pageView == null) return null;
        VideoItem adapterItem = pageView.getCurrentItemModel();
        if (!VideoItem.mediaEquals(adapterItem, videoItem)) {
            final String msg = "Episode video [" + VideoItem.dump(videoItem) + "] will be replaced to [" + VideoItem.dump(adapterItem) + "] by adapter notify . Intercept start playback action!";
            Asserts.throwIfDebug(new RuntimeException(L.obj2String(videoView) + " " + msg));
            return msg;
        }
        return null;
    }
}
