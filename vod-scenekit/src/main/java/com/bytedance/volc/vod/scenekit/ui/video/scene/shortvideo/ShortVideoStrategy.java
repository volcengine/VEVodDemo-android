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

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo;

import com.bytedance.playerkit.player.playback.DisplayModeHelper;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.volcengine.VolcEngineStrategy;
import com.bytedance.playerkit.player.volcengine.VolcScene;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;

import java.util.List;

public class ShortVideoStrategy {

    public static void setEnabled(boolean enable) {
        if (!VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_STRATEGY)) return;

        VolcEngineStrategy.setEnabled(VolcScene.SCENE_SHORT_VIDEO, enable);
    }

    public static void setItems(List<VideoItem> videoItems) {
        if (!VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_STRATEGY)) return;

        if (videoItems == null) return;

        VolcEngineStrategy.setMediaSources(VideoItem.toMediaSources(videoItems));
    }

    public static void appendItems(List<VideoItem> videoItems) {
        if (!VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_STRATEGY)) return;

        if (videoItems == null) return;

        VolcEngineStrategy.addMediaSources(VideoItem.toMediaSources(videoItems));
    }

    public static boolean renderFrame(VideoView videoView) {
        if (!VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_STRATEGY)) return false;
        if (videoView == null) return false;

        int[] frameInfo = new int[2];
        VolcEngineStrategy.renderFrame(videoView.getDataSource(), videoView.getSurface(), frameInfo);
        int videoWidth = frameInfo[0];
        int videoHeight = frameInfo[1];
        if (videoWidth > 0 && videoHeight > 0) {
            videoView.setDisplayAspectRatio(DisplayModeHelper.calDisplayAspectRatio(videoWidth, videoHeight, 0));
            return true;
        }
        return false;
    }
}
