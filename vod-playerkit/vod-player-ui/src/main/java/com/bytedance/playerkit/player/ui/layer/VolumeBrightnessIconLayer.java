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

package com.bytedance.playerkit.player.ui.layer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.ui.R;
import com.bytedance.playerkit.player.ui.layer.base.AnimateLayer;
import com.bytedance.playerkit.player.ui.layer.dialog.VolumeBrightnessDialogLayer;


public class VolumeBrightnessIconLayer extends AnimateLayer {

    private View mVolume;

    private View mBrightness;

    @Override
    public String tag() {
        return "volume";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.volume_brightness_icon_layer, parent, false);
        mVolume = view.findViewById(R.id.volumeContainer);
        mBrightness = view.findViewById(R.id.brightnessContainer);
        mVolume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                VolumeBrightnessDialogLayer layer = layerHost().findLayer(VolumeBrightnessDialogLayer.class);
                if (layer != null) {
                    layer.setType(VolumeBrightnessDialogLayer.TYPE_VOLUME);
                    layer.animateShow(true);
                }
            }
        });

        mBrightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                VolumeBrightnessDialogLayer layer = layerHost().findLayer(VolumeBrightnessDialogLayer.class);
                if (layer != null) {
                    layer.setType(VolumeBrightnessDialogLayer.TYPE_BRIGHTNESS);
                    layer.animateShow(true);
                }
            }
        });
        return view;
    }

    public View getVolumeView() {
        return mVolume;
    }

    public View getBrightnessView() {
        return mBrightness;
    }
}
