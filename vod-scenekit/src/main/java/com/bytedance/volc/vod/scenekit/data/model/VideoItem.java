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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.volcengine.VolcConfig;
import com.bytedance.volc.vod.scenekit.ui.video.layer.TitleBarLayer;
import com.bytedance.playerkit.utils.MD5;
import com.bytedance.volc.vod.scenekit.VideoSettings;


public class VideoItem implements Parcelable {
    public static final String EXTRA_VIDEO_ITEM = "extra_video_item";

    public static final int SOURCE_TYPE_VID = 0;
    public static final int SOURCE_TYPE_URL = 1;

    private VideoItem() {
    }

    protected VideoItem(Parcel in) {
        vid = in.readString();
        playAuthToken = in.readString();
        videoModel = in.readString();
        duration = in.readLong();
        title = in.readString();
        cover = in.readString();
        url = in.readString();
        urlCacheKey = in.readString();
        sourceType = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(vid);
        dest.writeString(playAuthToken);
        dest.writeString(videoModel);
        dest.writeLong(duration);
        dest.writeString(title);
        dest.writeString(cover);
        dest.writeString(url);
        dest.writeString(urlCacheKey);
        dest.writeInt(sourceType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VideoItem> CREATOR = new Creator<VideoItem>() {
        @Override
        public VideoItem createFromParcel(Parcel in) {
            return new VideoItem(in);
        }

        @Override
        public VideoItem[] newArray(int size) {
            return new VideoItem[size];
        }
    };

    public static VideoItem createVidItem(
            @NonNull String vid,
            @NonNull String playAuthToken,
            @Nullable String cover) {
        return createVidItem(vid, playAuthToken, 0, cover, null);
    }

    public static VideoItem createVidItem(
            @NonNull String vid,
            @NonNull String playAuthToken,
            long duration,
            @Nullable String cover,
            @Nullable String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_VID;
        videoItem.vid = vid;
        videoItem.playAuthToken = playAuthToken;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    public static VideoItem createUrlItem(@NonNull String url, @Nullable String cover) {
        return createUrlItem(MD5.getMD5(url), url, null, 0, cover, null);
    }

    public static VideoItem createUrlItem(
            @Nullable String vid,
            @NonNull String url,
            @Nullable String urlCacheKey,
            long duration,
            @Nullable String cover,
            @Nullable String title) {
        VideoItem videoItem = new VideoItem();
        videoItem.sourceType = SOURCE_TYPE_URL;
        videoItem.vid = vid;
        videoItem.url = url;
        videoItem.urlCacheKey = urlCacheKey;
        videoItem.duration = duration;
        videoItem.cover = cover;
        videoItem.title = title;
        return videoItem;
    }

    private String vid;

    private String playAuthToken;

    private String videoModel;

    private long duration;

    private String title;

    private String cover;

    private String url;

    private String urlCacheKey;

    private int sourceType;

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

    @NonNull
    public static MediaSource toMediaSource(VideoItem videoItem, boolean sycProgress) {
        final MediaSource mediaSource;
        if (videoItem.sourceType == VideoItem.SOURCE_TYPE_VID) {
            mediaSource = MediaSource.createIdSource(videoItem.vid, videoItem.playAuthToken);
        } else if (videoItem.sourceType == VideoItem.SOURCE_TYPE_URL) {
            mediaSource = MediaSource.createUrlSource(videoItem.vid, videoItem.url, videoItem.urlCacheKey);
        } else {
            throw new IllegalArgumentException("unsupported source type! " + videoItem.sourceType);
        }
        mediaSource.setCoverUrl(videoItem.cover);
        mediaSource.setDuration(videoItem.duration);
        mediaSource.putExtra(EXTRA_VIDEO_ITEM, videoItem);
        mediaSource.putExtra(TitleBarLayer.EXTRA_TITLE, videoItem.title);
        mediaSource.putExtra(VolcConfig.EXTRA_VOLC_CONFIG, createVolcConfig());
        if (sycProgress) {
            mediaSource.setSyncProgressId(videoItem.vid); // continues play
        }
        return mediaSource;
    }

    @NonNull
    public static VolcConfig createVolcConfig() {
        VolcConfig volcConfig = new VolcConfig();
        volcConfig.codecStrategyType = VideoSettings.intValue(VideoSettings.COMMON_CODEC_STRATEGY);
        volcConfig.playerDecoderType = VideoSettings.intValue(VideoSettings.COMMON_HARDWARE_DECODE);
        volcConfig.sourceEncodeType = VideoSettings.booleanValue(VideoSettings.COMMON_SOURCE_ENCODE_TYPE_H265) ? Track.ENCODE_TYPE_H265 : Track.ENCODE_TYPE_H264;
        volcConfig.enableSuperResolution = VideoSettings.booleanValue(VideoSettings.COMMON_SUPER_RESOLUTION);
        return volcConfig;
    }

    @Nullable
    public static VideoItem findVideoItem(MediaSource mediaSource) {
        if (mediaSource == null) return null;
        return mediaSource.getExtra(VideoItem.EXTRA_VIDEO_ITEM, VideoItem.class);
    }
}
