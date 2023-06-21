/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/6/12
 */

package com.bytedance.volc.vod.scenekit.ui.video.layer.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.InfoSubtitleChanged;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.strategy.VideoSubtitle;
import com.bytedance.volc.vod.scenekit.ui.video.layer.GestureLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TipsLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;

import java.util.ArrayList;
import java.util.List;

public class SubtitleSelectDialogLayer extends DialogListLayer<Subtitle> {

    public SubtitleSelectDialogLayer() {
        super();

        adapter().setOnItemClickListener((position, holder) -> {
            Item<Subtitle> item = adapter().getItem(position);
            if (item != null) {
                Player player = player();
                if (player != null) {
                    if (item.obj == null) {
                        player.setSubtitleEnabled(false);
                        select(null);
                        animateDismiss();

                        Context context = context();
                        if (context == null) return;
                        showTips((context.getString(R.string.vevod_subtitle_select_tips_hide)));
                    } else {
                        player.setSubtitleEnabled(true);
                        player.selectSubtitle(item.obj);
                        animateDismiss();

                        Context context = context();
                        if (context == null) return;
                        showTips(context.getString(R.string.vevod_subtitle_select_tips_will_switch, VideoSubtitle.subtitle2String(item.obj)));
                    }
                }
            }
        });
        setAnimateDismissListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                VideoLayerHost host = layerHost();
                if (host == null) return;

                TipsLayer tipsLayer = host.findLayer(TipsLayer.class);
                if (tipsLayer == null || !tipsLayer.isShowing()) {
                    GestureLayer layer = host.findLayer(GestureLayer.class);
                    if (layer != null) {
                        layer.showController();
                    }
                }
            }
        });
    }

    @Nullable
    @Override
    protected View createDialogView(@NonNull ViewGroup parent) {
        setTitle(parent.getResources().getString(R.string.vevod_time_progress_subtitle));
        return super.createDialogView(parent);
    }

    @Nullable
    @Override
    public String tag() {
        return "subtitle select";
    }

    @Override
    protected int backPressedPriority() {
        return Layers.BackPriority.SUBTITLE_SELECT_DIALOG_LAYER_BACK_PRIORITY;
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        if (playScene() != PlayScene.SCENE_FULLSCREEN) {
            dismiss();
        }
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
                case PlayerEvent.Info.SUBTITLE_LIST_INFO_READY:
                    syncData();
                    break;
                case PlayerEvent.Info.SUBTITLE_CHANGED:
                    syncData();

                    Player player = event.owner(Player.class);
                    if (player == null) return;

                    InfoSubtitleChanged subtitleChanged = event.cast(InfoSubtitleChanged.class);
                    if (subtitleChanged.pre == null) return;

                    Context context = context();
                    if (context == null) return;
                    showTips((context.getString(R.string.vevod_subtitle_select_tips_switched, VideoSubtitle.subtitle2String(subtitleChanged.current))));
                    break;
            }

        }
    };

    @Override
    public void show() {
        super.show();
        syncData();
    }

    public void syncData() {
        final Player player = player();
        if (player == null) return;

        List<Subtitle> subtitles = player.getSubtitles();

        bindData(subtitles);

        Subtitle selected = player.isSubtitleEnabled() ? player.getSelectedSubtitle() : null;
        select(selected);
    }

    private void bindData(List<Subtitle> subtitles) {
        final Context context = context();
        if (context == null) return;
        final List<DialogListLayer.Item<Subtitle>> items = new ArrayList<>();
        items.add(new Item<>(null, context.getString(R.string.vevod_subtitle_hide)));
        if (subtitles != null) {
            for (Subtitle subtitle : subtitles) {
                if (subtitle != null) {
                    items.add(new DialogListLayer.Item<>(subtitle, VideoSubtitle.subtitle2String(subtitle)));
                }
            }
        }
        adapter().setList(items);
    }

    private void select(Subtitle subtitle) {
        adapter().setSelected(adapter().findItem(subtitle));
    }

    private void showTips(String tips) {
        VideoLayerHost host = layerHost();
        if (host == null) return;
        TipsLayer tipsLayer = host.findLayer(TipsLayer.class);
        if (tipsLayer == null) return;
        tipsLayer.show(tips);
    }
}
