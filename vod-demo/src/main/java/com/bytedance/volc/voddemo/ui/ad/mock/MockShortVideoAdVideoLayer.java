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

package com.bytedance.volc.voddemo.ui.ad.mock;

import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;
import com.bytedance.volc.voddemo.impl.R;
/**
 * Mock impl of ShortVideoAdVideoLayer
 */
@Deprecated
public class MockShortVideoAdVideoLayer extends AnimateLayer {
    private TextView mTitle;
    private TextView mAdIcon;
    private TextView mContent;
    private TextView mButton;

    @Nullable
    @Override
    public String tag() {
        return "MockAdVideoLayer";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_short_video_ad_video_layer_mock, parent, false);
        mTitle = view.findViewById(R.id.ad_title);
        mAdIcon = view.findViewById(R.id.ad_ic);
        mContent = view.findViewById(R.id.ad_content);
        mButton = view.findViewById(R.id.ad_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.volcengine.com/product/vod"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                v.getContext().startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void onVideoViewBindDataSource(MediaSource dataSource) {
        super.onVideoViewBindDataSource(dataSource);
        // VideoItem videoItem = VideoItem.get(dataSource);
        mTitle.setText(R.string.vevod_mock_short_video_ad_layer_title);
        mAdIcon.setText(R.string.vevod_mock_short_video_ad_layer_ad);
        mContent.setText(R.string.vevod_mock_short_video_ad_layer_content);
        mButton.setText(R.string.vevod_mock_short_video_ad_layer_button_text);
    }

    @Override
    protected void onBindVideoView(@NonNull VideoView videoView) {
        super.onBindVideoView(videoView);
        show();
    }
}
