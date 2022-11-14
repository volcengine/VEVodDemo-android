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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.player.source;

import android.media.MediaPlayer;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Track implements Serializable {

    /**
     * Track type. One of
     * {@link #TRACK_TYPE_UNKNOWN},
     * {@link #TRACK_TYPE_VIDEO},
     * {@link #TRACK_TYPE_AUDIO},
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TRACK_TYPE_UNKNOWN, TRACK_TYPE_VIDEO, TRACK_TYPE_AUDIO})
    public @interface TrackType {
    }

    public static final int TRACK_TYPE_UNKNOWN = MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_UNKNOWN;
    public static final int TRACK_TYPE_VIDEO = MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_VIDEO;
    public static final int TRACK_TYPE_AUDIO = MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO;

    /**
     * Source encode type. One of
     * {@link #ENCODER_TYPE_H264},
     * {@link #ENCODER_TYPE_H265},
     * {@link #ENCODER_TYPE_H266}
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ENCODE_TYPE_UNKNOWN, ENCODER_TYPE_H264, ENCODER_TYPE_H265, ENCODER_TYPE_H266})
    public @interface EncoderType {
    }

    public static final int ENCODE_TYPE_UNKNOWN = 0;
    public static final int ENCODER_TYPE_H264 = 1;
    public static final int ENCODER_TYPE_H265 = 2;
    public static final int ENCODER_TYPE_H266 = 3;

    public static String mapEncoderType(@EncoderType int encodeType) {
        switch (encodeType) {
            case ENCODE_TYPE_UNKNOWN:
                return "unknown";
            case ENCODER_TYPE_H264:
                return "H264";
            case ENCODER_TYPE_H265:
                return "H265";
            case ENCODER_TYPE_H266:
                return "H266";
            default:
                throw new IllegalArgumentException("Unsupported encodeType " + encodeType);
        }
    }

    /**
     * Format type. One of
     * {@link #FORMAT_MP4},
     * {@link #FORMAT_DASH},
     * {@link #FORMAT_HLS}
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FORMAT_MP4, FORMAT_DASH, FORMAT_HLS})
    public @interface Format {
    }

    public static final int FORMAT_MP4 = 0;
    public static final int FORMAT_DASH = 1;
    public static final int FORMAT_HLS = 2;

    public static String mapTrackType(@TrackType int trackType) {
        switch (trackType) {
            case TRACK_TYPE_VIDEO:
                return "video";
            case TRACK_TYPE_AUDIO:
                return "audio";
        }
        throw new IllegalArgumentException("Unsupported trackType " + trackType);
    }

    private String mediaId;

    private int index;
    @TrackType
    private int trackType = TRACK_TYPE_UNKNOWN;

    private String url;
    private List<String> backupUrls;

    private String fileId;
    private String fileHash;
    private long fileSize;
    private int bitrate;
    private long preloadSize;

    private String encryptedKey;
    private String encryptedKeyId;

    @Format
    private int format;
    @EncoderType
    private int encoderType;
    private int videoWidth;
    private int videoHeight;
    private int rotate;
    private long duration;
    private int fps;

    private Quality quality;
    private Map<String, String> headers;

    public String getMediaId() {
        return mediaId;
    }

    public void setMediaId(String mediaId) {
        this.mediaId = mediaId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @TrackType
    public int getTrackType() {
        return trackType;
    }

    public void setTrackType(@TrackType int trackType) {
        this.trackType = trackType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setBackupUrls(List<String> backupUrls) {
        this.backupUrls = backupUrls;
    }

    public List<String> getBackupUrls() {
        return backupUrls;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileHash() {
        return fileHash;
    }

    public void setFileHash(String fileHash) {
        this.fileHash = fileHash;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public long getPreloadSize() {
        return preloadSize;
    }

    public void setPreloadSize(long preloadSize) {
        this.preloadSize = preloadSize;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    public String getEncryptedKeyId() {
        return encryptedKeyId;
    }

    public void setEncryptedKeyId(String encryptedKeyId) {
        this.encryptedKeyId = encryptedKeyId;
    }

    @Format
    public int getFormat() {
        return format;
    }

    public void setFormat(@Format int format) {
        this.format = format;
    }

    @EncoderType
    public int getEncoderType() {
        return encoderType;
    }

    public void setEncoderType(@EncoderType int encoderType) {
        this.encoderType = encoderType;
    }

    public int getBitrate() {
        return bitrate;
    }

    public void setBitrate(int bitrate) {
        this.bitrate = bitrate;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Nullable
    public Quality getQuality() {
        return quality;
    }

    public void setQuality(@Nullable Quality quality) {
        this.quality = quality;
    }

    @Nullable
    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(@Nullable Map<String, String> headers) {
        this.headers = headers;
    }

    public String dump() {
        if (quality != null) {
            return String.format(Locale.getDefault(), "[%s %s %dP %dFPS %s]",
                    L.obj2String(this),
                    mapEncoderType(encoderType),
                    quality.getQualityRes(),
                    quality.getQualityFps(),
                    Quality.mapQualityDynamicRange(quality.getQualityDynamicRange()));
        } else {
            return String.format(Locale.getDefault(), "[%s]", L.obj2String(this));
        }
    }

    public static String dump(Track track) {
        if (!L.ENABLE_LOG) return null;

        if (track == null) return null;
        return track.dump();
    }

    public static String dump(List<Track> tracks) {
        if (!L.ENABLE_LOG) return null;

        StringBuilder sb = new StringBuilder();
        for (Track track : tracks) {
            sb.append(track.dump()).append(", ");
        }
        return sb.toString();
    }
}
