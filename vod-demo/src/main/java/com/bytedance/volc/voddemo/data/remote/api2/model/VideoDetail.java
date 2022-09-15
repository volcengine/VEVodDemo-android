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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.voddemo.data.remote.api2.model;

import androidx.annotation.Nullable;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;


public class VideoDetail {
    public String vid;
    public String caption;
    public double duration;
    public String coverUrl;
    public String videoModel; // unused for now
    public String playAuthToken;
    public String subtitleAuthToken; // unused for now

    @Nullable
    public static VideoItem toVideoItem(VideoDetail detail) {
        if (detail.playAuthToken != null) {
            return VideoItem.createVidItem(
                    detail.vid,
                    detail.playAuthToken,
                    (long) (detail.duration * 1000),
                    detail.coverUrl,
                    detail.caption);
        }
        return null;
    }
}
