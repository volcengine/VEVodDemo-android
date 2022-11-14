/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/3
 */

package com.bytedance.volc.vod.scenekit.ui.video.layer.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.VideoLayerHost;

import com.bytedance.volc.vod.scenekit.ui.video.layer.GestureLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TipsLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SpeedSelectDialogLayer extends DialogListLayer<Float> {

    public SpeedSelectDialogLayer() {
        adapter().setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position, RecyclerView.ViewHolder holder) {
                Item<Float> item = adapter().getItem(position);
                if (item != null) {
                    Player player = player();
                    if (player != null) {
                        float speed = player.getSpeed();
                        float select = item.obj;
                        if (select != speed) {
                            player.setSpeed(select);
                            animateDismiss();

                            VideoLayerHost layerHost = layerHost();
                            if (layerHost == null) return;
                            TipsLayer tipsLayer = layerHost.findLayer(TipsLayer.class);
                            if (tipsLayer != null) {
                                tipsLayer.show("Speed is switched to " + item.text);
                            }
                            adapter().setSelected(adapter().findItem(select));
                        }
                    }
                }
            }
        });
        setAnimateDismissListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                VideoLayerHost layerHost = layerHost();
                if (layerHost == null) return;

                TipsLayer tipsLayer = layerHost.findLayer(TipsLayer.class);
                if (tipsLayer == null || !tipsLayer.isShowing()) {
                    GestureLayer layer = layerHost.findLayer(GestureLayer.class);
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
        setTitle(parent.getResources().getString(R.string.vevod_speed_select_dialog_title));
        adapter().setList(createItems(parent.getContext()));
        return super.createDialogView(parent);
    }

    private static List<Item<Float>> createItems(Context context) {
        List<Item<Float>> items = new ArrayList<>();
        items.add(new Item<>(0.5f, "0.5x"));
        items.add(new Item<>(0.75f, "0.75x"));
        items.add(new Item<>(1f, "1.0x" + context.getString(R.string.vevod_speed_select_default)));
        items.add(new Item<>(1.25f, "1.25x"));
        items.add(new Item<>(1.5f, "1.5x"));
        items.add(new Item<>(2.0f, "2.0x"));
        Collections.reverse(items);
        return items;
    }

    @Override
    public void show() {
        super.show();

        Player player = player();
        if (player != null) {
            adapter().setSelected(adapter().findItem(player.getSpeed()));
        }
    }

    private static List<Item<Float>> sItems;

    public static String mapSpeed(Context context, float speed) {
        if (sItems == null) {
            sItems = createItems(context);
        }
        for (Item<Float> item : sItems) {
            if (speed == item.obj) {
                return item.text;
            }
        }
        return null;
    }

    @Override
    public String tag() {
        return "speed_select";
    }

    @Override
    protected int backPressedPriority() {
        return Layers.BackPriority.SPEED_SELECT_DIALOG_LAYER_BACK_PRIORITY;
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        if (toScene != PlayScene.SCENE_FULLSCREEN) {
            dismiss();
        }
    }
}
