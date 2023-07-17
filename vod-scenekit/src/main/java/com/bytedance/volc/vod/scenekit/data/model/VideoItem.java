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

package com.bytedance.volc.vod.scenekit.data.model;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.volcengine.Mapper;
import com.bytedance.playerkit.player.volcengine.VolcConfig;
import com.bytedance.playerkit.utils.MD5;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.strategy.VideoSR;
import com.bytedance.volc.vod.scenekit.strategy.VideoSubtitle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class VideoItem implements Serializable {
    public static final String EXTRA_VIDEO_ITEM = "extra_video_item";

    public static final int SOURCE_TYPE_VID = 0;
    public static final int SOURCE_TYPE_URL = 1;
    public static final int SOURCE_TYPE_MODEL = 2;

    private VideoItem() {
    }

    public static VideoItem createVidItem(
            @NonNull String vid,
            @NonNull String playAuthToken,
            @Nullable String cover) {
        return createVidItem(vid, playAuthToken, null,0, cover, null);
    }

    public static VideoItem createVidItem(
            @NonNull String vid,
            @NonNull String playAuthToken,
            @Nullable String subtitleAuthToken,
            long duration,
            @Nullable String cover,
            @Nullable String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_VID;
        videoItem.vid = vid;
        videoItem.playAuthToken = playAuthToken;
        videoItem.subtitleAuthToken = subtitleAuthToken;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    public static VideoItem createUrlItem(@NonNull String url, @Nullable String cover) {
        return createUrlItem(MD5.getMD5(url), url, null, null,0, cover, null);
    }

    public static VideoItem createUrlItem(
            @NonNull String vid,
            @NonNull String url,
            @Nullable String urlCacheKey,
            @Nullable List<Subtitle> subtitles,
            long duration,
            @Nullable String cover,
            @Nullable String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_URL;
        videoItem.vid = vid;
        videoItem.url = url;
        videoItem.urlCacheKey = urlCacheKey;
        videoItem.subtitles = subtitles;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    public static VideoItem createMultiStreamUrlItem(
            @Nullable String vid,
            @NonNull MediaSource mediaSource,
            long duration,
            @Nullable String cover,
            @Nullable String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_URL;
        videoItem.vid = vid;
        videoItem.mediaSource = mediaSource;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    public static VideoItem createVideoModelItem(
            @NonNull String vid,
            @NonNull String videoModel,
            @Nullable String subtitleAuthToken,
            long duration,
            @Nullable String cover,
            @Nullable String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_MODEL;
        videoItem.vid = vid;
        videoItem.videoModel = videoModel;
        videoItem.subtitleAuthToken = subtitleAuthToken;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    private String vid;

    private String playAuthToken;

    private String subtitleAuthToken;

    private String videoModel;

    private MediaSource mediaSource;

    private long duration;

    private String title;

    private String cover;

    private String url;

    private String urlCacheKey;

    private List<Subtitle> subtitles;

    private int sourceType;


    private String tag;

    private String subTag;

    private int playScene;

    public String getVid() {
        return vid;
    }

    public String getPlayAuthToken() {
        return playAuthToken;
    }

    public String getVideoModel() {
        return videoModel;
    }

    public long getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public String getCover() {
        return cover;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlCacheKey() {
        return urlCacheKey;
    }

    public int getSourceType() {
        return sourceType;
    }

    private static MediaSource createMediaSource(VideoItem videoItem) {
        if (videoItem.sourceType == VideoItem.SOURCE_TYPE_VID) {
            MediaSource mediaSource = MediaSource.createIdSource(videoItem.vid, videoItem.playAuthToken);
            mediaSource.setSubtitleAuthToken(videoItem.subtitleAuthToken);
            return mediaSource;
        } else if (videoItem.sourceType == VideoItem.SOURCE_TYPE_URL) {
            MediaSource mediaSource = MediaSource.createUrlSource(videoItem.vid, videoItem.url, videoItem.urlCacheKey);
            mediaSource.setSubtitles(videoItem.subtitles);
            return mediaSource;
        } else if (videoItem.sourceType == VideoItem.SOURCE_TYPE_MODEL) {
            MediaSource mediaSource = MediaSource.createModelSource(videoItem.vid, videoItem.videoModel);
            Mapper.updateVideoModelMediaSource(mediaSource);
            mediaSource.setSubtitleAuthToken(videoItem.subtitleAuthToken); // TODO
            return mediaSource;
        } else {
            throw new IllegalArgumentException("unsupported source type! " + videoItem.sourceType);
        }
    }

    @NonNull
    public static MediaSource toMediaSource(VideoItem videoItem, boolean syncProgress) {
        if (videoItem.mediaSource == null) {
            videoItem.mediaSource = createMediaSource(videoItem);
        }
        final MediaSource mediaSource = videoItem.mediaSource;
        VideoItem.set(mediaSource, videoItem);
        VolcConfig.set(mediaSource, createVolcConfig(videoItem));
        if (syncProgress) {
            mediaSource.setSyncProgressId(videoItem.vid); // continues play
        }
        return mediaSource;
    }

    public static List<MediaSource> toMediaSources(List<VideoItem> videoItems, boolean syncProgress) {
        List<MediaSource> sources = new ArrayList<>();
        if (videoItems != null) {
            for (VideoItem videoItem : videoItems) {
                sources.add(VideoItem.toMediaSource(videoItem, syncProgress));
            }
        }
        return sources;
    }

    @NonNull
    public static VolcConfig createVolcConfig(VideoItem videoItem) {
        VolcConfig volcConfig = new VolcConfig();
        volcConfig.codecStrategyType = VideoSettings.intValue(VideoSettings.COMMON_CODEC_STRATEGY);
        volcConfig.playerDecoderType = VideoSettings.intValue(VideoSettings.COMMON_HARDWARE_DECODE);
        volcConfig.sourceEncodeType = VideoSettings.booleanValue(VideoSettings.COMMON_SOURCE_ENCODE_TYPE_H265) ? Track.ENCODER_TYPE_H265 : Track.ENCODER_TYPE_H264;
        volcConfig.superResolutionConfig = VideoSR.createConfig(videoItem.playScene);
        volcConfig.enableECDN = VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_ECDN);
        volcConfig.enableSubtitle = VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_SUBTITLE);
        volcConfig.subtitleLanguageIds = VideoSubtitle.createLanguageIds();
        volcConfig.tag = videoItem.tag;
        volcConfig.subTag = videoItem.subTag;
        return volcConfig;
    }

    public static void set(MediaSource mediaSource, VideoItem videoItem) {
        if (mediaSource == null) return;

        if (!TextUtils.isEmpty(videoItem.cover)) {
            mediaSource.setCoverUrl(videoItem.cover);
        }
        if (videoItem.duration > 0) {
            mediaSource.setDuration(videoItem.duration);
        }
        mediaSource.putExtra(EXTRA_VIDEO_ITEM, videoItem);
    }

    @Nullable
    public static VideoItem get(MediaSource mediaSource) {
        if (mediaSource == null) return null;
        return mediaSource.getExtra(VideoItem.EXTRA_VIDEO_ITEM, VideoItem.class);
    }

    public static void tag(VideoItem videoItem, String tag, String subTag) {
        if (videoItem == null) return;
        videoItem.tag = tag;
        videoItem.subTag = subTag;
    }

    public static void tag(List<VideoItem> videoItems, String tag, String subTag) {
        for (VideoItem videoItem : videoItems) {
            tag(videoItem, tag, subTag);
        }
    }

    public static void playScene(List<VideoItem> videoItems, int playScene) {
        for (VideoItem videoItem : videoItems) {
            playScene(videoItem, playScene);
        }
    }

    public static void playScene(VideoItem videoItem, int playScene) {
        if (videoItem == null) return;
        videoItem.playScene = playScene;
    }
}
