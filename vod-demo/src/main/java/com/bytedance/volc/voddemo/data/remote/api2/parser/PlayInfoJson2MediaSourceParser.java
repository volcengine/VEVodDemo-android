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
 * Create Date : 2023/4/27
 */

package com.bytedance.volc.voddemo.data.remote.api2.parser;

import android.text.TextUtils;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.volcengine.Mapper;
import com.bytedance.playerkit.utils.Numbers;
import com.bytedance.playerkit.utils.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PlayInfoJson2MediaSourceParser implements Parser<MediaSource> {
    public final String mPlayInfoJson;

    public PlayInfoJson2MediaSourceParser(String playInfoJson) {
        this.mPlayInfoJson = playInfoJson;
    }

    /**
     * @see <a href="https://www.volcengine.com/docs/4/2918#vodplayinfomodel">VodPlayInfoModel</a>
     */
    @Override
    public MediaSource parse() throws JSONException {
        JSONObject result = new JSONObject(mPlayInfoJson);
        float duration = Numbers.safeParseFloat(result.optString("Duration"), 0f);
        String fileType = result.optString("FileType");
        boolean enableAdaptive = result.optBoolean("EnableAdaptive");

        int totalCount = result.optInt("TotalCount");

        JSONArray thumbInfoList = result.optJSONArray("ThumbInfoList"); // 雪碧图列表
        JSONArray subtitleInfoList = result.optJSONArray("SubtitleInfoList"); // 字幕信息数组
        JSONObject barrageMaskInfo = result.optJSONObject("BarrageMaskInfo"); // 蒙板弹幕信息

        MediaSource mediaSource = new MediaSource(result.optString("Vid"), MediaSource.SOURCE_TYPE_URL);
        List<Track> tracks = parseTracks(result);
        mediaSource.setMediaProtocol(findProtocolFromTracks(tracks));
        mediaSource.setTracks(tracks);
        mediaSource.setCoverUrl(result.optString("PosterUrl"));
        mediaSource.setDuration((long) (duration * 1000));
        mediaSource.setMediaType(fileType2MediaType(fileType));
        mediaSource.setSupportABR(enableAdaptive);
        mediaSource.setSegmentType(adaptiveType2SegmentType(parseAdaptiveType(result)));
        return mediaSource;
    }

    private List<Track> parseTracks(JSONObject result) {
        if (result == null) return null;
        JSONArray playInfoList = result.optJSONArray("PlayInfoList"); // 播放地址列表
        if (playInfoList == null) return null;

        List<Track> tracks = new ArrayList<>();
        for (int i = 0; i < playInfoList.length(); i++) {
            JSONObject playInfo = null;
            try {
                playInfo = playInfoList.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (playInfo != null) {
                final Track track = playInfo2Track(result, playInfo);
                tracks.add(track);
            }
        }
        return tracks;
    }

    /**
     * @see <a href="https://www.volcengine.com/docs/4/2918#vodplayinfo">VodPlayInfo</a>
     */
    public static Track playInfo2Track(JSONObject result, JSONObject playInfo) {
        Track track = new Track();

        track.setUrl(playInfo.optString("MainPlayUrl")); // must set
        track.setBackupUrls(Arrays.asList(playInfo.optString("BackupPlayUrl"))); // optional set

        track.setFileId(playInfo.optString("FileId")); // optional set
        track.setFileHash(playInfo.optString("Md5"));  // recommend set(For disk cache key)

        @Track.TrackType final int trackType = fileType2TrackType(playInfo.optString("FileType"));
        track.setTrackType(trackType); // must set for dash

        track.setQuality(Mapper.definition2Quality(trackType, playInfo.optString(trackType == Track.TRACK_TYPE_AUDIO ? "Quality" : "Definition"))); // must set if using {@link com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.QualitySelectDialogLayer}
        track.setFormat(format2Format(playInfo.optString("Format"))); // must set for (dash or smooth track switching streaming)
        track.setEncoderType(encoderType2EncoderType(playInfo.optString("Codec"))); // must set for (dash or smooth track switching streaming)

        // LogoType // optional

        track.setBitrate(playInfo.optInt("Bitrate")); // must set for (dash or smooth track switching streaming)
        track.setVideoWidth(playInfo.optInt("Width")); // must set for (dash or smooth track switching streaming)
        track.setVideoHeight(playInfo.optInt("Height")); // must set for (dash or smooth track switching streaming)
        track.setFileSize(playInfo.optLong("Size")); // optional set

        // CheckInfo // optional

        track.setIndexRange(playInfo.optString("IndexRange")); // must set for dash
        track.setInitRange(playInfo.optString("InitRange")); // must set for dash

        //track.setPreloadSize(500 * 1024 * 1024);

        track.setEncryptedKey(playInfo.optString("PlayAuth")); // must set for private DRM
        track.setEncryptedKeyId(playInfo.optString("PlayAuthId")); // must set for private DRM

        // BarrageMaskOffset

        // Volume

        float duration = Numbers.safeParseFloat(result.optString("Duration"), 0f); // optional set
        track.setDuration((long) (duration * 1000));
        return track;
    }

    public static int encoderType2EncoderType(String encoderType) {
        if (encoderType != null) {
            switch (encoderType) {
                case "h264":
                    return Track.ENCODER_TYPE_H264;
                case "h265":
                    return Track.ENCODER_TYPE_H265;
                case "h266":
                    return Track.ENCODER_TYPE_H266;
            }
        }
        return Track.ENCODER_TYPE_H264;
    }

    public static int format2Format(String format) {
        if (format != null) {
            switch (format) {
                case "dash":
                    return Track.FORMAT_DASH;
                case "hls":
                    return Track.FORMAT_HLS;
                case "mp4":
                    return Track.FORMAT_MP4;
            }
        }
        return Track.FORMAT_MP4;
    }

    public static int fileType2TrackType(String fileType) {
        if (TextUtils.equals("video", fileType)) {
            return Track.TRACK_TYPE_VIDEO;
        } else if (TextUtils.equals("audio", fileType)) {
            return Track.TRACK_TYPE_AUDIO;
        }
        return Track.TRACK_TYPE_VIDEO;
    }

    @MediaSource.MediaProtocol
    public static int findProtocolFromTracks(List<Track> tracks) {
        if (tracks != null && !tracks.isEmpty()) {
            for (Track track : tracks) {
                if (track.getTrackType() == Track.TRACK_TYPE_VIDEO) {
                    int format = track.getFormat();
                    switch (format) {
                        case Track.FORMAT_DASH:
                            return MediaSource.MEDIA_PROTOCOL_DASH;
                        case Track.FORMAT_HLS:
                            if (isTracksAllFormat(format, tracks)) {
                                return MediaSource.MEDIA_PROTOCOL_HLS;
                            }
                            return MediaSource.MEDIA_PROTOCOL_DEFAULT;
                    }
                }
            }
        }
        return MediaSource.MEDIA_PROTOCOL_DEFAULT;
    }

    public static boolean isTracksAllFormat(int format, List<Track> tracks) {
        for (Track track : tracks) {
            if (track.getFormat() != format) {
                return false;
            }
        }
        return true;
    }

    public static int fileType2MediaType(String fileType) {
        if (TextUtils.equals("video", fileType)) {
            return MediaSource.MEDIA_TYPE_VIDEO;
        } else if (TextUtils.equals("audio", fileType)) {
            return MediaSource.MEDIA_TYPE_AUDIO;
        }
        return MediaSource.MEDIA_TYPE_VIDEO;
    }

    private static String parseAdaptiveType(JSONObject result) {
        if (result == null) return null;
        JSONObject adaptiveInfo = result.optJSONObject("AdaptiveInfo");
        if (adaptiveInfo == null) return null;

        return adaptiveInfo.optString("AdaptiveType");
    }

    private static int adaptiveType2SegmentType(String adaptiveType) {
        if (TextUtils.isEmpty(adaptiveType)) {
            return -1;
        }
        switch (adaptiveType) {
            case "segment_base":
                return MediaSource.SEGMENT_TYPE_SEGMENT_BASE;
        }
        return -1;
    }


}
