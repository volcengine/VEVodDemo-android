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

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.data.remote.model.parser.PlayInfoJson2MediaSourceParser;
import com.bytedance.volc.voddemo.data.remote.model.parser.SubtitleInfoJson2SubtitleListParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class BaseVideo implements Serializable {
    public static final String EXTRA_BASE_VIDEO = "extra_base_video";
    public String vid;
    public String videoUrl;
    public String videoModel;
    public String playAuthToken;
    public String subtitleAuthToken;
    public String subtitleModel;
    public String caption;
    public double duration;
    public String coverUrl;

    @Nullable
    private static VideoItem createVideoItem(BaseVideo video) {
        if (video == null) return null;

        // Demonstrate parse SubtitleInfo JSON to PlayerKit Subtitle Model
        // You should implement your own Parser with your AppServer data structure.
        final List<Subtitle> subtitles = TextUtils.isEmpty(video.subtitleModel) ? null :
                new SubtitleInfoJson2SubtitleListParser(video.subtitleModel).safeParse();

        if (!TextUtils.isEmpty(video.playAuthToken)) {
            // vid + playAuthToken
            return VideoItem.createVidItem(
                    video.vid,
                    video.playAuthToken,
                    video.subtitleAuthToken,
                    subtitles,
                    (long) (video.duration * 1000),
                    video.coverUrl,
                    video.caption);
        } else if (!TextUtils.isEmpty(video.videoModel)) {
            final int sourceType = VideoSettings.intValue(VideoSettings.COMMON_SOURCE_TYPE);
            switch (sourceType) {
                case VideoSettings.SourceType.SOURCE_TYPE_MODEL: {
                    VideoItem videoItem = VideoItem.createVideoModelItem(
                            video.vid,
                            video.videoModel,
                            video.subtitleAuthToken,
                            subtitles,
                            (long) (video.duration * 1000),
                            video.coverUrl,
                            video.caption);
                    VideoItem.toMediaSource(videoItem);
                    return videoItem;
                }
                case VideoSettings.SourceType.SOURCE_TYPE_URL: {
                    // Demonstrate parse VideoModel JSON to PlayerKit MediaSource object
                    // You should implement your own Parser with your AppServer data structure.
                    final MediaSource source = new PlayInfoJson2MediaSourceParser(video.videoModel).safeParse();
                    if (source == null) return null;
                    return VideoItem.createMultiStreamUrlItem(
                            video.vid,
                            source,
                            subtitles,
                            (long) (video.duration * 1000),
                            video.coverUrl,
                            video.caption);
                }
                default: {
                    throw new IllegalArgumentException("unsupported sourceType! " + sourceType);
                }
            }
        } else if (!TextUtils.isEmpty(video.videoUrl)) {
            return VideoItem.createUrlItem(
                    video.vid,
                    video.coverUrl,
                    null,
                    subtitles,
                    (long) (video.duration * 1000),
                    video.coverUrl,
                    video.caption);
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
        if (video == null) return null;

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
    public static List<Item> toItems(List<? extends BaseVideo> videos) {
        List<VideoItem> videoItems = videos == null ? null : BaseVideo.toVideoItems(videos);
        return videoItems == null ? null : new ArrayList<>(videoItems);
    }

    @Nullable
    public static <T extends BaseVideo> T get(Item item) {
        if (!(item instanceof VideoItem)) return null;
        VideoItem videoItem = (VideoItem) item;
        return (T) videoItem.getExtra(EXTRA_BASE_VIDEO, BaseVideo.class);
    }
}
