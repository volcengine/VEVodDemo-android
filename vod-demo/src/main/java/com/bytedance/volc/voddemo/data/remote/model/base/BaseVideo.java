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
 * Create Date : 2024/3/26
 */

package com.bytedance.volc.voddemo.data.remote.model.base;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.remote.model.parser.PlayInfoJson2MediaSourceParser;

import org.json.JSONException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BaseVideo implements Serializable {
    public static final String EXTRA_BASE_VIDEO = "extra_base_video";
    public String vid;
    public String caption;
    public double duration;
    public String coverUrl;
    public String videoModel;
    public String playAuthToken;
    public String subtitleAuthToken;

    @Nullable
    private static VideoItem createVideoItem(BaseVideo video) {
        if (video.playAuthToken != null) {
            // vid + playAuthToken
            return VideoItem.createVidItem(
                    video.vid,
                    video.playAuthToken,
                    video.subtitleAuthToken,
                    (long) (video.duration * 1000),
                    video.coverUrl,
                    video.caption);
        } else if (video.videoModel != null) {
            final int sourceType = VideoSettings.intValue(VideoSettings.COMMON_SOURCE_TYPE);
            switch (sourceType) {
                case VideoSettings.SourceType.SOURCE_TYPE_MODEL: {
                    VideoItem videoItem = VideoItem.createVideoModelItem(
                            video.vid,
                            video.videoModel,
                            video.subtitleAuthToken,
                            (long) (video.duration * 1000),
                            video.coverUrl,
                            video.caption);
                    VideoItem.toMediaSource(videoItem);
                    return videoItem;
                }
                case VideoSettings.SourceType.SOURCE_TYPE_URL: {
                    MediaSource source = null;
                    try {
                        // Demonstrate parse VideoModel JSON to MediaSource object
                        // You should implement your own Parser with your AppServer data structure.
                        source = new PlayInfoJson2MediaSourceParser(video.videoModel).parse();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (source == null) return null;
                    return VideoItem.createMultiStreamUrlItem(
                            video.vid,
                            source,
                            (long) (video.duration * 1000),
                            video.coverUrl,
                            video.caption);
                }
                default: {
                    throw new IllegalArgumentException("unsupported sourceType! " + sourceType);
                }
            }
        } else {
            return VideoItem.createEmptyItem(video.vid,
                    (long) (video.duration * 1000),
                    video.coverUrl,
                    video.caption
            );
        }
    }

    @Nullable
    public static VideoItem toVideoItem(BaseVideo video) {
        VideoItem videoItem = createVideoItem(video);
        if (videoItem != null) {
            videoItem.putExtra(EXTRA_BASE_VIDEO, video);
        }
        return videoItem;
    }

    @Nullable
    public static List<VideoItem> toVideoItems(List<? extends BaseVideo> videos) {
        if (videos == null) return null;
        List<VideoItem> items = new ArrayList<>();
        for (BaseVideo detail : videos) {
            VideoItem videoItem = toVideoItem(detail);
            if (videoItem != null) {
                items.add(videoItem);
            }
        }
        return items;
    }

    @Nullable
    public static <T extends BaseVideo> T get(VideoItem videoItem) {
        if (videoItem == null) return null;
        return (T) videoItem.getExtra(EXTRA_BASE_VIDEO, BaseVideo.class);
    }
}
