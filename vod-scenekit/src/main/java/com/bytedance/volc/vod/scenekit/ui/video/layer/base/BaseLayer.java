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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.scenekit.ui.video.layer.base;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.playback.VideoLayer;

public abstract class BaseLayer extends VideoLayer {

    public void requestShow(@NonNull String reason) {
        show();
    }

    public void requestDismiss(@NonNull String reason) {
        dismiss();
    }

    public void requestHide(@NonNull String reason) {
        hide();
    }
}
