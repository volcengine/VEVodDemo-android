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
 * Create Date : 2024/10/15
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder;

import android.text.TextUtils;
import android.view.Gravity;
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
import com.bytedance.volc.vod.scenekit.ui.video.layer.SubtitleLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoPageView;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoBottomShadowLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer.ShortVideoCoverLayer;
import com.bytedance.volc.vod.scenekit.ui.video.viewholder.VideoViewHolder;
import com.bytedance.volc.vod.scenekit.ui.widgets.MediaSeekBar;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaBottomProgressBarLayer;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaGestureLayer;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaTimeProgressDialogLayer;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaVideoLayer;

import java.util.List;

public class DramaEpisodeVideoViewHolder extends VideoViewHolder {
    public final FrameLayout videoViewContainer;
    public final VideoView videoView;
    public VideoItem videoItem;

    public DramaEpisodeVideoViewHolder(@NonNull View itemView, DramaVideoLayer.Type type, ShortVideoPageView pageView, DramaGestureLayer.DramaGestureContract gestureContract) {
        super(itemView);
        this.videoViewContainer = (FrameLayout) itemView;
        this.videoViewContainer.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));
        this.videoView = createVideoView(videoViewContainer, type, pageView, gestureContract);
    }

    @NonNull
    protected VideoView createVideoView(@NonNull FrameLayout parent, DramaVideoLayer.Type type, ShortVideoPageView pageView, DramaGestureLayer.DramaGestureContract gestureContract) {
        VideoView videoView = new VideoView(parent.getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.bottomMargin = (int) UIUtils.dip2Px(parent.getContext(), 12);
        lp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        parent.addView(videoView, lp);

        MediaSeekBar seekBar = new MediaSeekBar(parent.getContext());
        FrameLayout.LayoutParams lp1 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) UIUtils.dip2Px(parent.getContext(), 24));
        lp1.gravity = Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM;
        lp1.bottomMargin = (int) UIUtils.dip2Px(parent.getContext(), 1);
        parent.addView(seekBar, lp1);

        VideoLayerHost layerHost = new VideoLayerHost(parent.getContext());
        layerHost.addLayer(new DramaGestureLayer(gestureContract));
        layerHost.addLayer(new SubtitleLayer());
        layerHost.addLayer(new ShortVideoCoverLayer(true));
        layerHost.addLayer(new ShortVideoBottomShadowLayer());
        layerHost.addLayer(new DramaVideoLayer(type));
        layerHost.addLayer(new LoadingLayer());
        layerHost.addLayer(new PauseLayer());
        layerHost.addLayer(new DramaBottomProgressBarLayer(seekBar));
        layerHost.addLayer(new DramaTimeProgressDialogLayer());
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
        return videoView;
    }

    @Override
    public void bind(List<Item> items, int position) {
        final Item item = items.get(position);
        L.d(this, "bind", position, ItemHelper.dump(item));
        this.videoItem = (VideoItem) item;
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

    @Override
    public VideoView videoView() {
        return videoView;
    }
}
