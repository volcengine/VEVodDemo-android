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

package com.bytedance.playerkit.player.volcengine;

import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_AUDIO;
import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_VIDEO;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.model.BareVideoInfo;
import com.ss.ttvideoengine.model.BareVideoModel;
import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.VideoInfo;
import com.ss.ttvideoengine.model.VideoRef;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.source.Source;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.strategy.source.StrategySource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class Mapper {

    private static final Map<Resolution, Quality> map = new LinkedHashMap<>();
    private static final List<Track> mockResolutionTracks = new ArrayList<>();

    static {
        map.put(Resolution.Standard,
                new Quality(Quality.QUALITY_RES_360,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "360P",
                        Resolution.Standard
                ));

        map.put(Resolution.High,
                new Quality(Quality.QUALITY_RES_480,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "480P",
                        Resolution.High
                ));

        map.put(Resolution.SuperHigh,
                new Quality(Quality.QUALITY_RES_720,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "720P",
                        Resolution.SuperHigh
                ));

        map.put(Resolution.ExtremelyHigh,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "1080P",
                        Resolution.ExtremelyHigh));

        map.put(Resolution.FourK,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "4K",
                        Resolution.FourK));

        //Resolution.HDR,
        //Resolution.Auto,

        map.put(Resolution.L_Standard,
                new Quality(Quality.QUALITY_RES_240,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "240P",
                        Resolution.L_Standard));

        map.put(Resolution.H_High,
                new Quality(Quality.QUALITY_RES_540,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "540P",
                        Resolution.H_High));


        map.put(Resolution.TwoK,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "2K",
                        Resolution.H_High));


        map.put(Resolution.ExtremelyHigh_50F,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_50,
                        "1080P 50FPS",
                        Resolution.ExtremelyHigh_50F));

        map.put(Resolution.TwoK_50F,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_50,
                        "2K 50FPS",
                        Resolution.TwoK_50F));

        map.put(Resolution.FourK_50F,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_50,
                        "4K 50FPS",
                        Resolution.FourK_50F));

        map.put(Resolution.ExtremelyHigh_60F,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_60,
                        "1080P 60FPS",
                        Resolution.ExtremelyHigh_60F));

        map.put(Resolution.TwoK_60F,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_60,
                        "2K 60FPS",
                        Resolution.TwoK_60F));

        map.put(Resolution.FourK_60F,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_60,
                        "4K 60FPS",
                        Resolution.FourK_60F));

        map.put(Resolution.ExtremelyHigh_120F,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_120,
                        "1080P 120FPS",
                        Resolution.ExtremelyHigh_120F));

        map.put(Resolution.TwoK_120F,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_120,
                        "2K 120FPS",
                        Resolution.TwoK_120F));

        map.put(Resolution.FourK_120F,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_DYNAMIC_RANGE_SDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "4K 120FPS",
                        Resolution.FourK_120F));

        map.put(Resolution.L_Standard_HDR,
                new Quality(Quality.QUALITY_RES_240,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "240P HDR",
                        Resolution.L_Standard_HDR));


        map.put(Resolution.Standard_HDR,
                new Quality(Quality.QUALITY_RES_360,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "360P HDR",
                        Resolution.Standard_HDR));

        map.put(Resolution.High_HDR,
                new Quality(Quality.QUALITY_RES_480,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "480P HDR",
                        Resolution.High_HDR));

        map.put(Resolution.H_High_HDR,
                new Quality(Quality.QUALITY_RES_540,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "540P HDR",
                        Resolution.H_High_HDR));

        map.put(Resolution.SuperHigh_HDR,
                new Quality(Quality.QUALITY_RES_720,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "720P HDR",
                        Resolution.SuperHigh_HDR));


        map.put(Resolution.ExtremelyHigh_HDR,
                new Quality(Quality.QUALITY_RES_1080,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "1080P HDR",
                        Resolution.ExtremelyHigh_HDR));

        map.put(Resolution.TwoK_HDR,
                new Quality(Quality.QUALITY_RES_2K,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "2K HDR",
                        Resolution.TwoK_HDR));

        map.put(Resolution.FourK_HDR,
                new Quality(Quality.QUALITY_RES_4K,
                        Quality.QUALITY_DYNAMIC_RANGE_HDR,
                        Quality.QUALITY_FPS_DEFAULT,
                        "4K HDR",
                        Resolution.FourK_HDR));

        for (Map.Entry<Resolution, Quality> entry : map.entrySet()) {
            Track track = new Track();
            track.setQuality(entry.getValue());
            mockResolutionTracks.add(track);
        }
    }


    static Track findTrackWithDirectUrlSource(MediaSource mediaSource, List<Track> tracks, DirectUrlSource source, CacheKeyFactory cacheKeyFactory) {
        if (source == null) return null;
        DirectUrlSource.UrlItem urlItem = source.firstItem();
        if (urlItem == null) return null;
        for (Track track : tracks) {
            if (track.getFileHash() != null && TextUtils.equals(urlItem.getCacheKey(), cacheKeyFactory.generateCacheKey(mediaSource, track))) {
                return track;
            } else if (TextUtils.equals(urlItem.getUrl(), track.getUrl())) {
                return track;
            }
        }
        return null;
    }

    @Nullable
    static Track findTrackWithResolution(List<Track> tracks, Resolution resolution) {
        if (resolution == null) return null;
        for (Track track : tracks) {
            Quality quality = track.getQuality();
            if (quality != null && quality.getQualityTag() == resolution) {
                return track;
            }
        }
        return null;
    }

    static Quality resolution2Quality(Resolution resolution) {
        Quality quality = map.get(resolution);
        if (quality == null) return null;

        Quality q = new Quality();
        q.setQualityRes(quality.getQualityRes());
        q.setQualityDynamicRange(quality.getQualityDynamicRange());
        q.setQualityFps(quality.getQualityFps());
        q.setQualityDesc(quality.getQualityDesc());
        q.setQualityTag(resolution);
        return q;
    }

    static Resolution track2Resolution(Track track) {
        return quality2Resolution(track.getQuality());
    }

    static Resolution quality2Resolution(Quality quality) {
        if (quality != null) {
            Resolution resolution = (Resolution) quality.getQualityTag();
            if (resolution != null) return resolution;

            for (Map.Entry<Resolution, Quality> entry : map.entrySet()) {
                Quality value = entry.getValue();
                if (Objects.equals(value, quality)) {
                    return entry.getKey();
                }
            }
        }
        return Resolution.High;
    }

    public static IVideoModel mediaSource2VideoModel(MediaSource source, CacheKeyFactory cacheKeyFactory) {
        List<VideoInfo> videoInfos = new ArrayList<>();
        List<Track> videoTracks = source.getTracks(TRACK_TYPE_VIDEO);
        if (videoTracks != null && !videoTracks.isEmpty()) {
            for (Track track : videoTracks) {
                videoInfos.add(
                        new BareVideoInfo.Builder()
                                .mediaType(trackType2VideoModelMediaType(track.getTrackType()))
                                .urls(Arrays.asList(track.getUrl()))
                                .fileId(track.getFileId())
                                .fileHash(cacheKeyFactory.generateCacheKey(source, track))
                                .size(track.getFileSize())
                                .bitrate(track.getBitrate())
                                .spadea(track.getEncryptedKey())
                                .resolution(track2Resolution(track))
                                .vWidth(track.getVideoWidth())
                                .vHeight(track.getVideoHeight())
                                .format(trackFormat2VideoModelFormat(track.getFormat()))
                                .codecType(trackEncodeType2VideoModelEncodeType(track.getEncoderType()))
                                .build()
                );
            }
        }
        List<Track> audioTracks = source.getTracks(TRACK_TYPE_AUDIO);
        if (audioTracks != null && !audioTracks.isEmpty()) {
            for (Track track : audioTracks) {
                videoInfos.add(
                        new BareVideoInfo.Builder()
                                .mediaType(trackType2VideoModelMediaType(track.getTrackType()))
                                .urls(Arrays.asList(track.getUrl()))
                                .fileId(track.getFileId())
                                .fileHash(cacheKeyFactory.generateCacheKey(source, track))
                                .size(track.getFileSize())
                                .bitrate(track.getBitrate())
                                .spadea(track.getEncryptedKey())
                                .resolution(track2Resolution(track))
                                .format(trackFormat2VideoModelFormat(track.getFormat()))
                                .codecType(trackEncodeType2VideoModelEncodeType(track.getEncoderType()))
                                .build()
                );
            }
        }
        return new BareVideoModel.Builder()
                .vid(source.getMediaId())
                .setVideoInfos(videoInfos)
                .duration(source.getDuration())
                .build();
    }

    public static int mapScalingMode(int mode) {
        int scalingMode = TTVideoEngine.IMAGE_LAYOUT_TO_FILL;
        switch (mode) {
            case Player.SCALING_MODE_ASPECT_FIT:
                scalingMode = TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT;
                break;
            case Player.SCALING_MODE_ASPECT_FILL:
                scalingMode = TTVideoEngine.IMAGE_LAYOUT_ASPECT_FILL;
                break;
            case Player.SCALING_MODE_DEFAULT:
                scalingMode = TTVideoEngine.IMAGE_LAYOUT_TO_FILL;
                break;
        }
        return scalingMode;
    }

    public static void updateMediaSource(MediaSource mediaSource, IVideoModel videoModel) {
        mediaSource.setMediaProtocol(videoModelFormat2MediaSourceMediaProtocol(videoModel));
        mediaSource.setTracks(videoInfoList2TrackList(videoModel));

        long duration = videoModel.getVideoRefInt(VideoRef.VALUE_VIDEO_REF_VIDEO_DURATION) * 1000L;
        if (mediaSource.getDuration() <= 0) { // using app server
            mediaSource.setDuration(duration);
        }
        String coverUrl = videoModel.getVideoRefStr(VideoRef.VALUE_VIDEO_REF_POSTER_URL);
        if (TextUtils.isEmpty(mediaSource.getCoverUrl())) { // using app server
            mediaSource.setCoverUrl(coverUrl);
        }

        final Track videoTrack = mediaSource.getFirstTrack(TRACK_TYPE_VIDEO);
        if (videoTrack != null) {
            mediaSource.setDisplayAspectRatio(calTrackDisplayAspectRatio(videoTrack));
        }
    }

    public static int calTrackDisplayAspectRatio(Track track) {
        if (track != null
                && track.getTrackType() == TRACK_TYPE_VIDEO
                && track.getVideoWidth() > 0
                && track.getVideoHeight() > 0) {
            switch (track.getRotate()) {
                case 0:
                case 180:
                    return track.getVideoWidth() / track.getVideoHeight();
                case 90:
                case 270:
                    return track.getVideoHeight() / track.getVideoWidth();

            }
        }
        return 0;
    }

    private static List<Track> videoInfoList2TrackList(IVideoModel videoModel) {
        List<Track> tracks = new ArrayList<>();
        List<VideoInfo> videoInfos = videoModel.getVideoInfoList();
        for (VideoInfo videoInfo : videoInfos) {
            tracks.add(videoInfo2Track(videoModel, videoInfo));
        }
        return tracks;
    }

    private static Track videoInfo2Track(IVideoModel videoModel, VideoInfo info) {
        Track track = new Track();
        track.setMediaId(videoModel.getVideoRefStr(VideoRef.VALUE_VIDEO_REF_VIDEO_ID));
        track.setTrackType(videoModelMediaType2TrackType(info.getMediatype()));
        track.setUrl(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_MAIN_URL));
        track.setBackupUrls(transBackupUrls(info));
        track.setFileId(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_FILEID));
        track.setFileHash(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_FILE_HASH));
        track.setFileSize(info.getValueLong(VideoInfo.VALUE_VIDEO_INFO_SIZE));
        // FIXME: openAPI 2.0 DURATION is second in float, using int is not accurate.
        //track.setDuration(videoModel.getVideoRefInt(VideoRef.VALUE_VIDEO_REF_VIDEO_DURATION) * 1000L);
        track.setBitrate(info.getValueInt(VideoInfo.VALUE_VIDEO_INFO_BITRATE));
        //track.setPreloadSize();

        track.setEncryptedKey(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_PLAY_AUTH));
        track.setEncryptedKeyId(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_KID));

        track.setFormat(videoModelFormat2TrackFormat(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_FORMAT_TYPE)));
        track.setEncoderType(videoModelEncodeType2TrackEncodeType(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_CODEC_TYPE)));
        track.setVideoWidth(info.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VWIDTH));
        track.setVideoHeight(info.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VHEIGHT));
        track.setQuality(resolution2Quality(info.getResolution()));
        return track;
    }

    private static int videoModelFormat2TrackFormat(String format) {
        if (format != null) {
            switch (format) {
                case TTVideoEngine.FORMAT_TYPE_DASH:
                    return Track.FORMAT_DASH;
                case TTVideoEngine.FORMAT_TYPE_HLS:
                    return Track.FORMAT_HLS;
                case TTVideoEngine.FORMAT_TYPE_MP4:
                    return Track.FORMAT_MP4;
            }
        }
        return Track.FORMAT_MP4;
    }

    public static String trackFormat2VideoModelFormat(@Track.Format int format) {
        switch (format) {
            case Track.FORMAT_DASH:
                return TTVideoEngine.FORMAT_TYPE_DASH;
            case Track.FORMAT_HLS:
                return TTVideoEngine.FORMAT_TYPE_HLS;
            case Track.FORMAT_MP4:
                return TTVideoEngine.FORMAT_TYPE_MP4;
        }
        throw new IllegalArgumentException("unsupported format " + format);
    }

    public static int videoModelEncodeType2TrackEncodeType(String encodeType) {
        if (encodeType != null) {
            switch (encodeType) {
                case Source.EncodeType.H264:
                    return Track.ENCODER_TYPE_H264;
                case Source.EncodeType.h265:
                    return Track.ENCODER_TYPE_H265;
                case Source.EncodeType.h266:
                    return Track.ENCODER_TYPE_H266;
            }
        }
        return Track.ENCODER_TYPE_H264;
    }

    @Nullable
    public static String trackEncodeType2VideoModelEncodeType(@Track.EncoderType int encoderType) {
        switch (encoderType) {
            case Track.ENCODER_TYPE_H264:
                return Source.EncodeType.H264;
            case Track.ENCODER_TYPE_H265:
                return Source.EncodeType.h265;
            case Track.ENCODER_TYPE_H266:
                return Source.EncodeType.h266;
        }
        return null;
    }

    private static int videoModelFormat2MediaSourceMediaProtocol(IVideoModel videoModel) {
        if (videoModel.hasFormat(IVideoModel.Format.DASH)) {
            return MediaSource.MEDIA_PROTOCOL_DASH;
        } else if (videoModel.hasFormat(IVideoModel.Format.HLS)) {
            return MediaSource.MEDIA_PROTOCOL_HLS;
        } else {
            return MediaSource.MEDIA_PROTOCOL_DEFAULT;
        }
    }

    private static int videoModelMediaType2TrackType(int mediaType) {
        if (mediaType == VideoRef.TYPE_AUDIO) {
            return TRACK_TYPE_AUDIO;
        } else if (mediaType == VideoRef.TYPE_VIDEO) {
            return TRACK_TYPE_VIDEO;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static int trackType2VideoModelMediaType(@Track.TrackType int trackType) {
        if (trackType == TRACK_TYPE_VIDEO) {
            return VideoRef.TYPE_VIDEO;
        } else if (trackType == TRACK_TYPE_AUDIO) {
            return VideoRef.TYPE_AUDIO;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private static List<String> transBackupUrls(VideoInfo info) {
        String backUpUrl = info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_BACKUP_URL_1);
        List<String> urls = new ArrayList<>();
        if (!TextUtils.isEmpty(backUpUrl)) {
            urls.add(backUpUrl);
        }
        return urls;
    }


    static StrategySource mediaSource2StrategySource(MediaSource mediaSource,
                                                     CacheKeyFactory cacheKeyFactory,
                                                     TrackSelector selector,
                                                     int selectType) throws IOException {
        if (mediaSource == null) {
            throw new IOException(new NullPointerException("mediaSource is null"));
        }
        final int trackType = mediaSource.getMediaType() == MediaSource.MEDIA_TYPE_AUDIO ?
                TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;
        switch (mediaSource.getSourceType()) {
            case MediaSource.SOURCE_TYPE_URL: {
                Track track = null;
                List<Track> tracks = mediaSource.getTracks(trackType);
                if (selector != null && tracks != null) {
                    track = selector.selectTrack(selectType, trackType, tracks, mediaSource);
                }
                return mediaSource2DirectUrlSource(mediaSource, track, cacheKeyFactory);
            }
            case MediaSource.SOURCE_TYPE_ID: {
                Track track = null;
                if (selector != null) {
                    track = selector.selectTrack(selectType, trackType, mockResolutionTracks, mediaSource);
                }
                if (track != null) {
                    return mediaSource2VidPlayAuthTokenSource(mediaSource, track.getQuality());
                }
            }
        }
        return null;
    }

    public static DirectUrlSource mediaSource2DirectUrlSource(MediaSource mediaSource, Track track, CacheKeyFactory cacheKeyFactory) {
        if (track != null) {
            VolcConfig volcConfig = VolcConfig.get(mediaSource);
            DirectUrlSource.Builder builder = new DirectUrlSource.Builder()
                    .setVid(mediaSource.getMediaId())
                    .addItem(new DirectUrlSource.UrlItem.Builder()
                            .setUrl(track.getUrl())
                            .setCacheKey(cacheKeyFactory.generateCacheKey(mediaSource, track))
                            .setEncodeType(Mapper.trackEncodeType2VideoModelEncodeType(track.getEncoderType()))
                            .setPlayAuth(track.getEncryptedKey())
                            .build());
            if (volcConfig.codecStrategyType != VolcConfig.CODEC_STRATEGY_DISABLE) {
                builder.setCodecStrategy(volcConfig.codecStrategyType);
            }
            return builder.build();
        }
        return null;
    }

    public static VidPlayAuthTokenSource mediaSource2VidPlayAuthTokenSource(MediaSource mediaSource, Quality quality) {
        Resolution resolution = null;
        if (quality != null) {
            resolution = quality2Resolution(quality);
        }

        final VolcConfig volcConfig = VolcConfig.get(mediaSource);

        VidPlayAuthTokenSource.Builder builder = new VidPlayAuthTokenSource.Builder()
                .setVid(mediaSource.getMediaId())
                .setPlayAuthToken(mediaSource.getPlayAuthToken())
                .setResolution(resolution);

        if (volcConfig.codecStrategyType != VolcConfig.CODEC_STRATEGY_DISABLE) {
            builder.setCodecStrategy(volcConfig.codecStrategyType);
        } else {
            final String encodeType = trackEncodeType2VideoModelEncodeType(volcConfig.sourceEncodeType);
            builder.setEncodeType(encodeType);
        }
        return builder.build();
    }

    public static List<StrategySource> mediaSources2StrategySources(List<MediaSource> mediaSources, CacheKeyFactory cacheKeyFactory, TrackSelector selector, int selectType) {
        if (mediaSources == null || mediaSources.isEmpty()) return null;
        List<StrategySource> strategySources = new ArrayList<>(mediaSources.size());
        for (MediaSource mediaSource : mediaSources) {
            StrategySource strategySource = null;
            try {
                strategySource = mediaSource2StrategySource(mediaSource, cacheKeyFactory, selector, selectType);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (strategySource != null) {
                strategySources.add(strategySource);
            }
        }
        return strategySources;
    }
    public static int mapVolcScene2EngineScene(int volcScene) {
        switch (volcScene) {
            case VolcC.SCENE_SHORT_VIDEO:
                return StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
            case VolcC.SCENE_FEED_VIDEO:
                return StrategyManager.STRATEGY_SCENE_SHORT_VIDEO;
            default:
                throw new IllegalArgumentException("unsupported scene " + volcScene);
        }
    }
}
