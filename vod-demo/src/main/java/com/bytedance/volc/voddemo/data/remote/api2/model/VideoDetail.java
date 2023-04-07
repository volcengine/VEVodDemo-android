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

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.remote.api2.parser.PlayInfoJson2MediaSourceParser;

import org.json.JSONException;


public class VideoDetail {
    public String vid;
    public String caption;
    public double duration;
    public String coverUrl;
    public String videoModel;
    public String playAuthToken;

    public String subtitleAuthToken; // unused for now

    @Nullable
    public static VideoItem toVideoItem(VideoDetail detail) {
        if (detail.playAuthToken != null) {
            // vid + playAuthToken
            return VideoItem.createVidItem(
                    detail.vid,
                    detail.playAuthToken,
                    (long) (detail.duration * 1000),
                    detail.coverUrl,
                    detail.caption);
        } else if (detail.videoModel != null) {
            final int sourceType = VideoSettings.intValue(VideoSettings.COMMON_SOURCE_TYPE);
            switch (sourceType) {
                case VideoSettings.SourceType.SOURCE_TYPE_MODEL: {
                    VideoItem videoItem = VideoItem.createVideoModelItem(
                            detail.vid,
                            detail.videoModel,
                            (long) (detail.duration * 1000),
                            detail.coverUrl,
                            detail.caption);
                    VideoItem.toMediaSource(videoItem, false);
                    return videoItem;
                }
                case VideoSettings.SourceType.SOURCE_TYPE_URL: {
                    MediaSource source = null;
                    try {
                        // Demonstrate parse VideoModel JSON to MediaSource object
                        // You should implement your own Parser with your AppServer data structure.
                        source = new PlayInfoJson2MediaSourceParser(detail.videoModel).parse();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (source == null) return null;
                    return VideoItem.createMultiStreamUrlItem(
                            detail.vid,
                            source,
                            (long) (detail.duration * 1000),
                            detail.coverUrl,
                            detail.caption);
                }
            }
        }
        return null;
    }
}
