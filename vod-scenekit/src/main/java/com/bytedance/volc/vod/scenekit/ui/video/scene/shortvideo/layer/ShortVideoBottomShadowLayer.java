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
 * Create Date : 2024/10/21
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.layer;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.BaseLayer;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;

public class ShortVideoBottomShadowLayer extends BaseLayer {

    @Nullable
    @Override
    public String tag() {
        return "ShortVideoBottomShadowLayer";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = new View(parent.getContext());
        view.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) UIUtils.dip2Px(parent.getContext(), 280), Gravity.BOTTOM));
        view.setBackground(ContextCompat.getDrawable(parent.getContext(), R.drawable.vevod_short_video_bottom_shadow));
        return view;
    }

    @Override
    protected void onBindVideoView(@NonNull VideoView videoView) {
        super.onBindVideoView(videoView);
        show();
    }
}
