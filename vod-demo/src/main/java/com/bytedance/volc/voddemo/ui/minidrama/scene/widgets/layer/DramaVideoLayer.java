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

package com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.scene.recommend.DramaRecommendVideoFragment;

import java.util.Locale;

public class DramaVideoLayer extends AnimateLayer implements VideoView.VideoViewPlaybackActionInterceptor {

    public static final String ACTION_DRAMA_VIDEO_LAYER_SHOW_PAY_DIALOG = "action_drama_video_layer_show_pay_dialog";

    public static final String INTERCEPT_START_PLAYBACK_REASON_LOCKED = "locked";
    public static final String INTERCEPT_START_PLAYBACK_REASON_EMPTY = "empty";

    private final Type mType;
    private TextView title;
    private TextView desc;
    private TextView continuePlayMoreDesc;

    private View likeView;
    private View collectView;

    public enum Type {
        DETAIL,
        RECOMMEND
    }

    public DramaVideoLayer(Type type) {
        this.mType = type;
    }

    @Override
    public void onVideoViewBindDataSource(MediaSource dataSource) {
        super.onVideoViewBindDataSource(dataSource);
        syncData();
    }

    @Override
    public String onVideoViewInterceptStartPlayback(VideoView videoView) {
        VideoItem videoItem = VideoItem.get(dataSource());
        if (videoItem == null) {
            return null;
        }
        if (EpisodeVideo.isLocked(videoItem)) {
            // intercept
            L.d(this, "onVideoViewInterceptStartPlayback", "Episode video [" + VideoItem.dump(videoItem) + "] is locked.");
            return INTERCEPT_START_PLAYBACK_REASON_LOCKED;
        }
        if (videoItem.getSourceType() == VideoItem.SOURCE_TYPE_EMPTY) {
            // intercept
            L.d(this, "onVideoViewInterceptStartPlayback", "Episode video [" + VideoItem.dump(videoItem) + "] is empty.");
            return INTERCEPT_START_PLAYBACK_REASON_EMPTY;
        }
        return null;
    }

    @Override
    public void onVideoViewStartPlaybackIntercepted(VideoView videoView, String reason) {
        if (TextUtils.equals(reason, INTERCEPT_START_PLAYBACK_REASON_LOCKED)) {
            Intent intent = new Intent(ACTION_DRAMA_VIDEO_LAYER_SHOW_PAY_DIALOG);
            LocalBroadcastManager.getInstance(videoView.getContext()).sendBroadcast(intent);
        }
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
        continuePlayMoreView.setVisibility(mType == Type.DETAIL ? View.GONE : View.VISIBLE);

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
        videoView.addPlaybackInterceptor(1, this);
    }

    @Override
    protected void onUnBindVideoView(@NonNull VideoView videoView) {
        super.onUnBindVideoView(videoView);
        videoView.removePlaybackInterceptor(1, this);
    }

    @Override
    public void show() {
        super.show();
        syncData();
    }

    private void syncData() {
        final EpisodeVideo episode = EpisodeVideo.get(VideoItem.get(dataSource()));
        if (title != null) {
            title.setText(EpisodeVideo.getDramaTitle(episode));
        }
        if (continuePlayMoreDesc != null) {
            continuePlayMoreDesc.setText(String.format(Locale.getDefault(), continuePlayMoreDesc.getResources().getString(R.string.vevod_mini_drama_video_layer_continue_play_more_desc), EpisodeVideo.getTotalEpisodeNumber(episode)));
        }
        if (desc != null) {
            if (mType == Type.DETAIL) {
                desc.setText(EpisodeVideo.getEpisodeDesc(episode));
            } else {
                desc.setText(String.format(Locale.getDefault(), desc.getResources().getString(R.string.vevod_mini_drama_video_layer_bottom_desc), EpisodeVideo.getEpisodeNumber(episode), EpisodeVideo.getEpisodeDesc(episode)));
            }
        }
    }
}
