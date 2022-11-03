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
 * Create Date : 2022/11/2
 */

package com.bytedance.volc.vod.scenekit.ui.video.layer;

import static com.bytedance.playerkit.player.playback.DisplayModeHelper.calDisplayAspectRatio;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.BaseLayer;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;

public class CoverLayer extends BaseLayer {

    private final DisplayModeHelper mDisplayModeHelper = new DisplayModeHelper();

    @Override
    public String tag() {
        return "cover";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        ImageView imageView = new ImageView(parent.getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setBackgroundColor(parent.getResources().getColor(android.R.color.black));
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.gravity = Gravity.CENTER;
        imageView.setLayoutParams(lp);
        mDisplayModeHelper.setDisplayView(imageView);
        mDisplayModeHelper.setContainerView((FrameLayout) parent);
        return imageView;
    }

    @Override
    public void onVideoViewBindDataSource(MediaSource dataSource) {
        show();
    }

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        controller.addPlaybackListener(mPlaybackListener);
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        controller.removePlaybackListener(mPlaybackListener);
    }

    private final Dispatcher.EventListener mPlaybackListener = new Dispatcher.EventListener() {

        @Override
        public void onEvent(Event event) {
            switch (event.code()) {
                case PlaybackEvent.Action.START_PLAYBACK: {
                    final Player player = player();
                    if (player != null && player.isInPlaybackState()) {
                        return;
                    }
                    show();
                    break;
                }
                case PlaybackEvent.Action.STOP_PLAYBACK: {
                    show();
                    break;
                }
                case PlayerEvent.Action.SET_SURFACE: {
                    final Player player = player();
                    if (player != null && player.isInPlaybackState()) {
                        dismiss();
                    } else {
                        show();
                    }
                    break;
                }
                case PlayerEvent.Action.START: {
                    final Player player = player();
                    if (player != null && player.isPaused()) {
                        dismiss();
                    }
                    break;
                }
                case PlayerEvent.Action.STOP:
                case PlayerEvent.Action.RELEASE: {
                    show();
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
    public void show() {
        super.show();
        load();
    }

    protected void load() {
        final ImageView imageView = getView();
        if (imageView == null) return;
        final String coverUrl = resolveCoverUrl();
        Activity activity = activity();
        if (activity != null && !activity.isDestroyed()) {
            Glide.with(imageView).load(coverUrl).listener(mGlideListener).into(imageView);
        }
    }

    private final RequestListener<Drawable> mGlideListener = new RequestListener<Drawable>() {
        @Override
        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
            return false;
        }

        @Override
        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
            mDisplayModeHelper.setDisplayAspectRatio(calDisplayAspectRatio(resource.getIntrinsicWidth(), resource.getIntrinsicHeight(), 0));
            VideoView videoView = videoView();
            if (videoView != null) {
                mDisplayModeHelper.setDisplayMode(videoView.getDisplayMode());
            }
            return false;
        }
    };


    String resolveCoverUrl() {
        final VideoView videoView = videoView();
        if (videoView == null) return null;

        final MediaSource mediaSource = videoView.getDataSource();
        if (mediaSource == null) return null;

        return mediaSource.getCoverUrl();
    }
}
