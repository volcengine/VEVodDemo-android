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
 * Create Date : 2023/6/21
 */

package com.bytedance.volc.vod.scenekit.strategy;

import com.bytedance.playerkit.player.volcengine.VolcSuperResolutionConfig;
import com.bytedance.volc.vod.scenekit.VideoSettings;

public class VideoSR {
    public static VolcSuperResolutionConfig createConfig(int playScene) {
        VolcSuperResolutionConfig config = new VolcSuperResolutionConfig();
        config.enableSuperResolutionOnStartup = VideoSettings.booleanValue(VideoSettings.COMMON_SUPER_RESOLUTION);
        return config;
    }
}
