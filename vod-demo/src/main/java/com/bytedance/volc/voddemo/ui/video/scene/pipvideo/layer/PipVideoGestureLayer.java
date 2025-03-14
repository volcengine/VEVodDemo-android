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

package com.bytedance.volc.voddemo.ui.video.scene.pipvideo.layer;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.BaseLayer;

public class PipVideoGestureLayer extends BaseLayer {

    @Nullable
    @Override
    public String tag() {
        return "";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = new View(parent.getContext());
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoLayerHost host = layerHost();
                if (host == null) return;
                final PipVideoActonLayer actonLayer = host.findLayer(PipVideoActonLayer.class);
                if (actonLayer == null) return;
                if (actonLayer.isShowing()) {
                    actonLayer.animateDismiss();
                } else {
                    actonLayer.animateShow(true);
                }
            }
        });
        return view;
    }

    @Override
    protected void onBindLayerHost(@NonNull VideoLayerHost layerHost) {
        super.onBindLayerHost(layerHost);
        show();
    }

    @Override
    public void requestDismiss(@NonNull String reason) {
        //super.requestDismiss(reason);
    }

    @Override
    public void requestHide(@NonNull String reason) {
        //super.requestHide(reason);
    }
}

