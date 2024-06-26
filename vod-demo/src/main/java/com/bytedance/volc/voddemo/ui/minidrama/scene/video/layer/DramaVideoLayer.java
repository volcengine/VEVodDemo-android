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

package com.bytedance.volc.voddemo.ui.minidrama.scene.video.layer;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaRecommendVideoFragment;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaVideoViewFactory;

import java.util.Locale;

public class DramaVideoLayer extends AnimateLayer {

    private final DramaVideoViewFactory.Type mType;

    private TextView title;
    private TextView desc;
    private TextView continuePlayMoreDesc;

    private View likeView;
    private View collectView;

    public DramaVideoLayer(DramaVideoViewFactory.Type type) {
        this.mType = type;
    }

    @Override
    public void onVideoViewBindDataSource(MediaSource dataSource) {
        super.onVideoViewBindDataSource(dataSource);
        syncData();
    }

    @Nullable
    @Override
    public String tag() {
        return "drama_video";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_mini_drama_video_layer, parent, false);

        title = view.findViewById(R.id.title);
        desc = view.findViewById(R.id.desc);

        View continuePlayMoreView = view.findViewById(R.id.continuePlayMoreView);
        continuePlayMoreDesc = view.findViewById(R.id.continuePlayMoreDesc);
        continuePlayMoreView.setOnClickListener(v -> LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(new Intent(DramaRecommendVideoFragment.ACTION_PLAY_MORE_CLICK)));
        continuePlayMoreView.setVisibility(mType == DramaVideoViewFactory.Type.DETAIL ? View.GONE : View.VISIBLE);

        likeView = view.findViewById(R.id.likeView);
        likeView.setOnClickListener(v -> likeView.setSelected(!likeView.isSelected()));

        collectView = view.findViewById(R.id.collectView);
        collectView.setOnClickListener(v -> collectView.setSelected(!collectView.isSelected()));
        return view;
    }

    @Override
    protected void onBindVideoView(@NonNull VideoView videoView) {
        super.onBindVideoView(videoView);
        show();
    }

    @Override
    public void show() {
        super.show();
        syncData();
    }

    private void syncData() {
        final EpisodeVideo episode = (EpisodeVideo) EpisodeVideo.get(VideoItem.get(dataSource()));
        if (episode != null) {
            if (episode.episodeInfo != null) {
                if (episode.episodeInfo.dramaInfo != null) {
                    if (title != null) {
                        title.setText(episode.episodeInfo.dramaInfo.dramaTitle);
                    }
                    if (continuePlayMoreDesc != null) {
                        continuePlayMoreDesc.setText(String.format(Locale.getDefault(), continuePlayMoreDesc.getResources().getString(R.string.vevod_mini_drama_video_layer_continue_play_more_desc), episode.episodeInfo.dramaInfo.totalEpisodeNumber));
                    }
                }
                if (desc != null) {
                    if (mType == DramaVideoViewFactory.Type.DETAIL) {
                        desc.setText(episode.episodeInfo.episodeDesc);
                    } else {
                        desc.setText(String.format(Locale.getDefault(), desc.getResources().getString(R.string.vevod_mini_drama_video_layer_bottom_desc), episode.episodeInfo.episodeNumber, episode.episodeInfo.episodeDesc));
                    }
                }
            }
        }
    }
}
