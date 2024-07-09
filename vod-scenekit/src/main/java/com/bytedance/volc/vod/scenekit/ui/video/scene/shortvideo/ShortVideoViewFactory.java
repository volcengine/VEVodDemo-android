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
 * Create Date : 2024/3/28
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo;

import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LoadingLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PauseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayErrorLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayerConfigLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.VideoViewFactory;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoCoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoDebugLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoProgressBarLayer;

import java.lang.ref.WeakReference;

public class ShortVideoViewFactory implements VideoViewFactory {

    private final WeakReference<ShortVideoPageView> mPageViewRef;

    public ShortVideoViewFactory(ShortVideoPageView pageView) {
        this.mPageViewRef = new WeakReference<>(pageView);
    }

    @Override
    public VideoView createVideoView(ViewGroup parent, Object o) {
        VideoView videoView = new VideoView(parent.getContext());
        parent.addView(videoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        VideoLayerHost layerHost = new VideoLayerHost(parent.getContext());
        layerHost.addLayer(new PlayerConfigLayer());
        layerHost.addLayer(new ShortVideoCoverLayer());
        layerHost.addLayer(new LoadingLayer());
        layerHost.addLayer(new PauseLayer());
        layerHost.addLayer(new ShortVideoProgressBarLayer());
        layerHost.addLayer(new PlayErrorLayer());
        if (VideoSettings.booleanValue(VideoSettings.DEBUG_ENABLE_LOG_LAYER)) {
            layerHost.addLayer(new LogLayer());
            layerHost.addLayer(new ShortVideoDebugLayer(mPageViewRef));
        }
        layerHost.attachToVideoView(videoView);
        videoView.setBackgroundColor(parent.getResources().getColor(android.R.color.black));
        //videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT); // fit mode
        videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL); // immersive mode
        if (VideoSettings.intValue(VideoSettings.COMMON_RENDER_VIEW_TYPE) == DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW) {
            // 推荐使用 TextureView, 兼容性更好
            videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
        } else {
            videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW);
        }
        videoView.setPlayScene(PlayScene.SCENE_SHORT);
        return videoView;
    }
}
