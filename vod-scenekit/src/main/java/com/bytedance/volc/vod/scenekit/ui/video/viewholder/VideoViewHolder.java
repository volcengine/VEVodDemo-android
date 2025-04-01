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
 * Create Date : 2024/10/22
 */

package com.bytedance.volc.vod.scenekit.ui.video.viewholder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.MultiTypeAdapter;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;

public abstract class VideoViewHolder extends ViewHolder {

    public VideoViewHolder(@NonNull View itemView) {
        super(itemView);
        L.d(this, "create");
    }

    public abstract VideoView videoView();

    @Override
    public void onViewDetachedFromWindow() {
        super.onViewDetachedFromWindow();

        actionStop();
    }

    @Override
    public void onViewRecycled() {
        super.onViewRecycled();

        actionStop();
    }

    @Override
    public void executeAction(int action, @Nullable Object o) {
        final VideoView videoView = videoView();
        if (videoView == null) return;

        switch (action) {
            case ViewHolderAction.ACTION_PLAY:
                actionPlay();
                break;
            case ViewHolderAction.ACTION_STOP:
                actionStop();
                break;
            case ViewHolderAction.ACTION_PAUSE:
                actionPause();
                break;
            case ViewHolderAction.ACTION_VIEW_PAGER_ON_PAGE_PEEK_START:
                actionOnPagerPeekStart();
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        final VideoView videoView = videoView();
        if (videoView != null) {
            VideoLayerHost host = videoView.layerHost();
            if (host != null && host.onBackPressed()) {
                return true;
            }
        }
        return super.onBackPressed();
    }

    @Nullable
    private Item getAdapterItem(int position) {
        final RecyclerView.Adapter<?> adapter = getBindingAdapter();
        if (adapter instanceof MultiTypeAdapter) {
            if (position >= 0 && position < adapter.getItemCount()) {
                return ((MultiTypeAdapter) adapter).getItem(position);
            }
        }
        return null;
    }

    @Override
    public boolean isPaused() {
        final VideoView videoView = videoView();
        final Player player = videoView == null ? null : videoView.player();
        if (player != null && (player.isPaused() || (!player.isLooping() && player.isCompleted()))) {
            return true;
        } else {
            return super.isPaused();
        }
    }

    private void actionOnPagerPeekStart() {
        final VideoView videoView = videoView();
        if (videoView == null) return;

        VideoLayerHost host = videoView.layerHost();
        if (host != null) {
            host.notifyEvent(Layers.Event.VIEW_PAGER_ON_PAGE_PEEK_START.ordinal(), null);
        }
    }

    private void actionPlay() {
        final VideoView videoView = videoView();
        if (videoView == null) return;

        videoView.startPlayback();
    }

    private void actionPause() {
        final VideoView videoView = videoView();
        if (videoView == null) return;
        if (videoView.player() == null) return;

        L.d(this, "actionPause", getBindingAdapterPosition());
        videoView.pausePlayback();
    }

    private void actionStop() {
        final VideoView videoView = videoView();
        if (videoView == null) return;
        if (videoView.player() == null) return;

        L.d(this, "actionStop", getBindingAdapterPosition());
        videoView.stopPlayback();
    }
}
