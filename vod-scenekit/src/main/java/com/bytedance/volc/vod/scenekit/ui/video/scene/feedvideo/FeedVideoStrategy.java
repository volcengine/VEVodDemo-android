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
 * Create Date : 2023/2/16
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo;

import com.bytedance.playerkit.player.volcengine.VolcEngineStrategy;
import com.bytedance.playerkit.player.volcengine.VolcScene;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;

import java.util.List;

public class FeedVideoStrategy {

    public static void setEnabled(boolean enable) {
        if (!VideoSettings.booleanValue(VideoSettings.FEED_VIDEO_ENABLE_PRELOAD)) return;

        VolcEngineStrategy.setEnabled(VolcScene.SCENE_FEED_VIDEO, enable);
    }

    public static void setItems(List<VideoItem> videoItems) {
        if (!VideoSettings.booleanValue(VideoSettings.FEED_VIDEO_ENABLE_PRELOAD)) return;

        if (videoItems == null) return;

        VolcEngineStrategy.setMediaSources(VideoItem.toMediaSources(videoItems, true));
    }

    public static void appendItems(List<VideoItem> videoItems) {
        if (!VideoSettings.booleanValue(VideoSettings.FEED_VIDEO_ENABLE_PRELOAD)) return;

        if (videoItems == null) return;

        VolcEngineStrategy.addMediaSources(VideoItem.toMediaSources(videoItems, true));
    }
}
