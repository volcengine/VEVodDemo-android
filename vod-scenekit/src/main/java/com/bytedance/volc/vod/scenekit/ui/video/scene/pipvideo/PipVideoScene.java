/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/3/19
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.pipvideo;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.VideoViewFactory;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;

import java.util.List;

public class PipVideoScene implements Dispatcher.EventListener {
    @NonNull
    private final PipWindowView mWindowView;
    @NonNull
    private final VideoView mVideoView;

    private List<MediaSource> mPlaylist;
    private int mPlayIndex;

    public PipVideoScene(Context context, VideoViewFactory videoViewFactory) {
        mWindowView = new PipWindowView(context);
        mVideoView = videoViewFactory.createVideoView(mWindowView, null);
        mVideoView.setPlayScene(PlayScene.SCENE_PIP);
        mWindowView.addView(mVideoView, new FrameLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER));
        final PlaybackController controller = mVideoView.controller();
        if (controller != null) {
            controller.addPlaybackListener(this);
        }
    }

    public void setPlaylist(List<MediaSource> playlist) {
        mPlaylist = playlist;
    }

    public List<MediaSource> getPlaylist() {
        return mPlaylist;
    }

    public int getPlayIndex() {
        return mPlayIndex;
    }

    @NonNull
    public VideoView videoView() {
        return mVideoView;
    }

    @NonNull
    public PipWindowView windowView() {
        return mWindowView;
    }

    public void playIndex(int playIndex) {
        if (mPlaylist == null || mPlaylist.isEmpty()) return;
        if (playIndex >= mPlaylist.size()) return;

        mPlayIndex = playIndex;
        final MediaSource mediaSource = mPlaylist.get(mPlayIndex);

        L.d(this, "playIndex", mPlayIndex, MediaSource.dump(mediaSource));
        if (mediaSource == null) return;
        if (MediaSource.mediaEquals(mediaSource, mVideoView.getDataSource())) {
            mVideoView.startPlayback();
        } else {
            mVideoView.stopPlayback();
            mVideoView.bindDataSource(mediaSource);
            mVideoView.startPlayback();
        }
    }

    @Override
    public void onEvent(Event event) {
        switch (event.code()) {
            case PlayerEvent.State.COMPLETED: {
                final Player player = event.owner(Player.class);
                if (player != null && !player.isLooping()) {
                    playIndex(mPlayIndex + 1);
                }
                break;
            }
        }
    }
}
