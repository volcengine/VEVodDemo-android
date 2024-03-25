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

package com.bytedance.volc.vod.scenekit.ui.video.layer.base;

import static com.bytedance.volc.vod.scenekit.ui.video.layer.Layers.VisibilityRequestReason.REQUEST_DISMISS_REASON_DIALOG_SHOW;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.VideoLayer;
import com.bytedance.playerkit.player.playback.VideoLayerHost;


public abstract class DialogLayer extends AnimateLayer implements VideoLayerHost.BackPressedHandler {

    @Nullable
    @Override
    protected final View createView(@NonNull ViewGroup parent) {
        return createDialogView(parent);
    }

    abstract protected View createDialogView(@NonNull ViewGroup parent);

    @Override
    public boolean onBackPressed() {
        if (isShowing()) {
            animateDismiss();
            return true;
        } else {
            return false;
        }
    }

    protected abstract int backPressedPriority();

    @CallSuper
    @Override
    protected void onBindLayerHost(@NonNull VideoLayerHost layerHost) {
        layerHost.registerBackPressedHandler(this, backPressedPriority());
    }

    @CallSuper
    @Override
    protected void onUnbindLayerHost(@NonNull VideoLayerHost layerHost) {
        layerHost.unregisterBackPressedHandler(this);
    }

    @Override
    public void show() {
        boolean isShowing = isShowing();
        super.show();
        if (!isShowing && isShowing()) {
            dismissLayers();
        }
    }

    private void dismissLayers() {
        final VideoLayerHost layerHost = layerHost();
        if (layerHost == null) return;

        for (int i = 0; i < layerHost.layerSize(); i++) {
            VideoLayer layer = layerHost.findLayer(i);
            if (layer != null && layer != this) {
                if (layer instanceof AnimateLayer) {
                    ((AnimateLayer) layer).requestAnimateDismiss(REQUEST_DISMISS_REASON_DIALOG_SHOW);
                } else if (layer instanceof BaseLayer) {
                    ((BaseLayer) layer).requestDismiss(REQUEST_DISMISS_REASON_DIALOG_SHOW);
                }
            }
        }
    }

    @Override
    public void requestDismiss(@NonNull String reason) {
        if (!TextUtils.equals(reason, REQUEST_DISMISS_REASON_DIALOG_SHOW)) {
            super.requestDismiss(reason);
        }
    }

    @Override
    public void requestHide(@NonNull String reason) {
        if (!TextUtils.equals(reason, REQUEST_DISMISS_REASON_DIALOG_SHOW)) {
            super.requestHide(reason);
        }
    }
}
