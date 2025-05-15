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
 * Create Date : 2024/10/14
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.utils.ItemHelper;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LoadingLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.LogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PauseLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayErrorLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.PlayerConfigLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.SubtitleLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoBottomShadowLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoCoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoProgressBarLayer;
import com.bytedance.volc.vod.scenekit.ui.video.viewholder.VideoViewHolder;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;

import java.util.List;

public class ShortVideoItemViewHolder extends VideoViewHolder {
    public final FrameLayout videoViewContainer;
    public final VideoView videoView;
    public VideoItem videoItem;

    public ShortVideoItemViewHolder(@NonNull View itemView) {
        super(itemView);
        this.videoViewContainer = (FrameLayout) itemView;
        this.videoViewContainer.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        this.videoView = createVideoView(videoViewContainer);
    }

    @Override
    public VideoView videoView() {
        return videoView;
    }

    @Override
    public void bind(List<Item> items, int position) {
        final VideoView videoView = videoView();
        if (videoView == null) return;

        final Item item = items.get(position);
        this.videoItem = (VideoItem) item;
        L.d(this, "bind", position, ItemHelper.dump(item));
        MediaSource mediaSource = videoView.getDataSource();
        if (mediaSource == null) {
            mediaSource = VideoItem.toMediaSource(videoItem);
            videoView.bindDataSource(mediaSource);
        } else {
            if (!TextUtils.equals(videoItem.getVid(), mediaSource.getMediaId())) {
                videoView.stopPlayback();
                mediaSource = VideoItem.toMediaSource(videoItem);
                videoView.bindDataSource(mediaSource);
            } else {
                // vid is same
                if (videoView.player() == null) {
                    mediaSource = VideoItem.toMediaSource(videoItem);
                    videoView.bindDataSource(mediaSource);
                } else {
                    // do nothing
                }
            }
        }
    }

    @Override
    public Item getBindingItem() {
        return videoItem;
    }

    @NonNull
    private static VideoView createVideoView(@NonNull FrameLayout parent) {
        VideoView videoView = new VideoView(parent.getContext());
        VideoLayerHost layerHost = new VideoLayerHost(parent.getContext());
        layerHost.addLayer(new PlayerConfigLayer());
        layerHost.addLayer(new SubtitleLayer());
        layerHost.addLayer(new ShortVideoCoverLayer(false));
        layerHost.addLayer(new ShortVideoBottomShadowLayer());
        layerHost.addLayer(new LoadingLayer());
        layerHost.addLayer(new PauseLayer());
        layerHost.addLayer(new ShortVideoProgressBarLayer());
        layerHost.addLayer(new PlayErrorLayer());
        if (VideoSettings.booleanValue(VideoSettings.DEBUG_ENABLE_LOG_LAYER)) {
            layerHost.addLayer(new LogLayer());
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
        new PlaybackController().bind(videoView);
        parent.addView(videoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return videoView;
    }
}



