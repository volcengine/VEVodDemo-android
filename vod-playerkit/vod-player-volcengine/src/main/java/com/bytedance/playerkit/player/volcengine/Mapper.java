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
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.SubtitleText;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.utils.Asserts;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.SubDesInfoModel;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.model.BareVideoInfo;
import com.ss.ttvideoengine.model.BareVideoModel;
import com.ss.ttvideoengine.model.IVideoInfo;
import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.SubInfo;
import com.ss.ttvideoengine.model.VideoInfo;
import com.ss.ttvideoengine.model.VideoRef;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.source.Source;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.source.VideoModelSource;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.TTHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class Mapper {

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

    static Track findTrackWithVideoInfo(@Nullable MediaSource mediaSource, @Nullable IVideoInfo videoInfo) {
        if (mediaSource == null) return null;
        return findTrackWithVideoInfo(mediaSource.getTracksByMediaType(), videoInfo);
    }

    @Nullable
    static Track findTrackWithVideoInfo(@Nullable List<Track> tracks, @Nullable IVideoInfo videoInfo) {
        return findTrackWithResolution(tracks, videoInfo == null ? null : videoInfo.getResolution());
    }

    @Nullable
    static Track findTrackWithResolution(@Nullable List<Track> tracks, @Nullable Resolution resolution) {
        if (resolution == null) return null;
        if (tracks == null || tracks.isEmpty()) return null;
        for (Track track : tracks) {
            Quality quality = track.getQuality();
            if (quality != null) {
                if (quality.getQualityTag() != null) {
                    if (quality.getQualityTag() == resolution) {
                        return track;
                    }
                } else {
                    Quality target = Mapper.resolution2Quality(resolution);
                    if (quality.equals(target)) {
                        return track;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static Quality definition2Quality(@Track.TrackType int trackType, String definition) {
        Resolution resolution = null;
        switch (trackType) {
            case TRACK_TYPE_AUDIO:
                resolution = TTHelper.defaultAudioResolutionMap().get(definition);
                if (resolution == null) {
                    resolution = TTHelper.defaultVideoResolutionMap().get(definition);
                }
                break;
            case TRACK_TYPE_VIDEO:
                resolution = TTHelper.defaultVideoResolutionMap().get(definition);
                break;
        }
        return resolution2Quality(resolution);
    }

    @Nullable
    public static Quality resolution2Quality(Resolution resolution) {
        Quality quality = VolcQuality.resolution2Quality(resolution);
        if (quality == null) return null;

        Quality q = new Quality();
        q.setQualityRes(quality.getQualityRes());
        q.setQualityDynamicRange(quality.getQualityDynamicRange());
        q.setQualityFps(quality.getQualityFps());
        q.setQualityDesc(quality.getQualityDesc());
        q.setQualityTag(resolution);
        return q;
    }

    @Nullable
    static Resolution track2Resolution(Track track) {
        if (track != null) {
            return VolcQuality.quality2Resolution(track.getQuality());
        }
        return null;
    }

    public static IVideoModel mediaSource2VideoModel(MediaSource source, CacheKeyFactory cacheKeyFactory) {
        List<VideoInfo> videoInfos = new ArrayList<>();
        List<Track> videoTracks = source.getTracks(TRACK_TYPE_VIDEO);
        if (videoTracks != null && !videoTracks.isEmpty()) {
            for (Track track : videoTracks) {
                final List<String> urls = new ArrayList<>();
                urls.add(track.getUrl());
                if (track.getBackupUrls() != null) {
                    urls.addAll(track.getBackupUrls());
                }

                Resolution resolution = track2Resolution(track);
                resolution = resolution == null ? Resolution.Standard : resolution;
                videoInfos.add(
                        new BareVideoInfo.Builder()
                                .mediaType(trackType2VideoModelMediaType(track.getTrackType()))
                                .urls(urls)
                                .fileId(track.getFileId())
                                .fileHash(cacheKeyFactory.generateCacheKey(source, track))
                                .size(track.getFileSize())
                                .bitrate(track.getBitrate())
                                .spadea(track.getEncryptedKey())
                                .resolution(resolution/* can't be null*/)
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
                Resolution resolution = track2Resolution(track);
                resolution = resolution == null ? Resolution.Standard : resolution;
                videoInfos.add(
                        new BareVideoInfo.Builder()
                                .mediaType(trackType2VideoModelMediaType(track.getTrackType()))
                                .urls(Arrays.asList(track.getUrl()))
                                .fileId(track.getFileId())
                                .fileHash(cacheKeyFactory.generateCacheKey(source, track))
                                .size(track.getFileSize())
                                .bitrate(track.getBitrate())
                                .spadea(track.getEncryptedKey())
                                .resolution(resolution /* can't be null*/)
                                .format(trackFormat2VideoModelFormat(track.getFormat()))
                                .codecType(trackEncodeType2VideoModelEncodeType(track.getEncoderType()))
                                .build()
                );
            }
        }
        return new BareVideoModel.Builder()
                .vid(source.getMediaId())
                .setVideoInfos(videoInfos)
                .adaptive(source.isSupportABR())
                .duration(source.getDuration())
                .dynamicType(segmentType2DynamicType(source.getSegmentType()))
                .build();
    }

    public static int dynamicType2SegmentType(String dynamicType) {
        switch (dynamicType) {
            case TTVideoEngine.DYNAMIC_TYPE_SEGMENTBASE:
                return MediaSource.SEGMENT_TYPE_SEGMENT_BASE;
        }
        return -1;
    }

    public static String segmentType2DynamicType(@MediaSource.SegmentType int segmentType) {
        switch (segmentType) {
            case MediaSource.SEGMENT_TYPE_SEGMENT_BASE:
                return TTVideoEngine.DYNAMIC_TYPE_SEGMENTBASE;
        }
        return null;
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

    public static void updateVideoModelMediaSource(MediaSource mediaSource) {
        if (mediaSource.getSourceType() == MediaSource.SOURCE_TYPE_MODEL
                && mediaSource.getTracks() == null) {
            IVideoModel videoModel = VolcVideoModelCache.acquire(mediaSource.getModelJson());
            updateMediaSource(mediaSource, videoModel);
        }
    }

    @Nullable
    public static Track findTrack(MediaSource mediaSource, MediaSource mediaSource1, Track t1) {
        if (mediaSource == mediaSource1) return t1;
        if (mediaSource.getTracks() == mediaSource1.getTracks()) return t1;

        CacheKeyFactory cacheKeyFactory = VolcPlayerInit.getCacheKeyFactory();

        List<Track> tracks = mediaSource.getTracks();
        for (Track track : tracks) {
            String cacheKey = cacheKeyFactory.generateCacheKey(mediaSource, track);
            String cacheKey1 = cacheKeyFactory.generateCacheKey(mediaSource1, t1);
            if (TextUtils.equals(cacheKey, cacheKey1)) {
                return track;
            }
        }
        return null;
    }

    public static void updateMediaSource(MediaSource mediaSource, MediaSource mediaSource1) {
        if (mediaSource == mediaSource1) return;
        if (!MediaSource.mediaEquals(mediaSource, mediaSource1)) {
            throw new IllegalArgumentException("MediaSource is not media equal!" + MediaSource.dump(mediaSource) + " " + MediaSource.dump(mediaSource1));
        }

        if (mediaSource.getTracks() == null) {
            mediaSource.setTracks(mediaSource1.getTracks());
        }
    }

    public static void updateMediaSource(MediaSource mediaSource, IVideoModel videoModel) {
        if (videoModel == null) return;
        mediaSource.setMediaProtocol(videoModelFormat2MediaSourceMediaProtocol(videoModel));
        mediaSource.setSegmentType(dynamicType2SegmentType(videoModel.getDynamicType()));
        mediaSource.setSupportABR(videoModel.getVideoRefBool(VideoRef.VALUE_VIDEO_REF_ENABLE_ADAPTIVE));
        if (mediaSource.getTracks() == null) {
            mediaSource.setTracks(videoInfoList2TrackList(videoModel));
        }
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

    private static String trackFormat2VideoModelFormat(@Track.Format int format) {
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

    private static int videoModelEncodeType2TrackEncodeType(String encodeType) {
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
    private static String trackEncodeType2VideoModelEncodeType(@Track.EncoderType int encoderType) {
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
        final int trackType = MediaSource.mediaType2TrackType(mediaSource);
        switch (mediaSource.getSourceType()) {
            case MediaSource.SOURCE_TYPE_URL: {
                if (isDirectUrlSeamlessSwitchEnabled(mediaSource)) {
                    return mediaSource2VideoModelSource(mediaSource, cacheKeyFactory);
                } else {
                    List<Track> tracks = mediaSource.getTracks(trackType);
                    if (selector != null && tracks != null) {
                        Track result = selector.selectTrack(selectType, trackType, tracks, mediaSource);
                        return mediaSource2DirectUrlSource(mediaSource, result, cacheKeyFactory);
                    }
                    return null;
                }
            }
            case MediaSource.SOURCE_TYPE_ID: {
                return mediaSource2VidPlayAuthTokenSource(mediaSource);
            }
            case MediaSource.SOURCE_TYPE_MODEL: {
                updateVideoModelMediaSource(mediaSource);
                return mediaSource2VideoModelSource(mediaSource, cacheKeyFactory);
            }
        }
        return null;
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

    /**
     * @throws IllegalArgumentException when mediaSource.getSourceType() is not MediaSource.SOURCE_TYPE_URL
     */
    public static boolean isDirectUrlSeamlessSwitchEnabled(MediaSource mediaSource) {
        Asserts.checkArgument(mediaSource.getSourceType() == MediaSource.SOURCE_TYPE_URL);
        final VolcConfig config = VolcConfig.get(mediaSource);
        final int protocol = mediaSource.getMediaProtocol();
        boolean isProtocolSeamlessSwitchingEnabled =
                (protocol == MediaSource.MEDIA_PROTOCOL_DEFAULT && config.enableMP4SeamlessSwitch) ||
                        (protocol == MediaSource.MEDIA_PROTOCOL_HLS && config.enableHlsSeamlessSwitch) ||
                        (protocol == MediaSource.MEDIA_PROTOCOL_DASH && config.enableDash);
        return isProtocolSeamlessSwitchingEnabled && mediaSource.isSupportABR();
    }

    public static VideoModelSource mediaSource2VideoModelSource(MediaSource mediaSource, CacheKeyFactory cacheKeyFactory) {
        IVideoModel videoModel;
        if (mediaSource.getSourceType() == MediaSource.SOURCE_TYPE_MODEL) {
            videoModel = VolcVideoModelCache.acquire(mediaSource.getModelJson());
        } else {
            videoModel = Mapper.mediaSource2VideoModel(mediaSource, cacheKeyFactory);
        }
        VideoModelSource strategySource = new VideoModelSource.Builder()
                .setVid(videoModel != null ? videoModel.getVideoRefStr(VideoRef.VALUE_VIDEO_REF_VIDEO_ID) : mediaSource.getMediaId())
                .setVideoModel(videoModel)
                .setTag(mediaSource)
                .build();
        return strategySource;
    }

    public static DirectUrlSource mediaSource2DirectUrlSource(MediaSource mediaSource, Track track, CacheKeyFactory cacheKeyFactory) {
        if (track == null) return null;
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
        DirectUrlSource strategySource = builder.setTag(mediaSource).build();
        return strategySource;
    }

    public static VidPlayAuthTokenSource mediaSource2VidPlayAuthTokenSource(MediaSource mediaSource) {
        VidPlayAuthTokenSource.Builder builder = new VidPlayAuthTokenSource.Builder()
                .setVid(mediaSource.getMediaId())
                .setPlayAuthToken(mediaSource.getPlayAuthToken());

        final VolcConfig volcConfig = VolcConfig.get(mediaSource);
        if (volcConfig.codecStrategyType != VolcConfig.CODEC_STRATEGY_DISABLE) {
            builder.setCodecStrategy(volcConfig.codecStrategyType);
        } else {
            final String encodeType = trackEncodeType2VideoModelEncodeType(volcConfig.sourceEncodeType);
            builder.setEncodeType(encodeType);
        }
        VidPlayAuthTokenSource strategySource = builder.setTag(mediaSource).build();
        return strategySource;
    }

    public static int mapVolcScene2EngineScene(int volcScene) {
        switch (volcScene) {
            case VolcScene.SCENE_SHORT_VIDEO:
                return StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
            case VolcScene.SCENE_FEED_VIDEO:
                return StrategyManager.STRATEGY_SCENE_SHORT_VIDEO;
            default:
                throw new IllegalArgumentException("unsupported scene " + volcScene);
        }
    }

    static Map<String, String> findHeaders(MediaSource mediaSource, @Nullable Track track) {
        final Map<String, String> headers = track == null ? null : track.getHeaders();
        if (headers != null && !headers.isEmpty()) {
            return headers;
        }
        return mediaSource.getHeaders();
    }

    static float displayAspectRatio(@NonNull MediaSource mediaSource, @Nullable Track track) {
        if (mediaSource.getDisplayAspectRatio() > 0) {
            return mediaSource.getDisplayAspectRatio();
        }
        if (track != null && track.getVideoWidth() > 0 && track.getVideoHeight() > 0) {
            if (track.getRotate() == 0 || track.getRotate() == 180) {
                return track.getVideoWidth() / (float) track.getVideoHeight();
            } else {
                return track.getVideoHeight() / (float) track.getVideoWidth();
            }
        }
        return 0;
    }

    @Nullable
    private static Subtitle parseSubtitle(@Nullable JSONObject object) {
        if (object == null) return null;
        Subtitle subtitle = new Subtitle();
        subtitle.setUrl(object.optString("url"));
        subtitle.setLanguageId(object.optInt("language_id"));
        subtitle.setFormat(object.optString("format"));
        subtitle.setLanguage(object.optString("language"));
        subtitle.setIndex(object.optInt("id"));
        subtitle.setExpire(object.optLong("expire"));
        subtitle.setSubtitleId(object.optInt("sub_id"));
        return subtitle;
    }

    @Nullable
    private static JSONObject subtitle2Json(Subtitle subtitle) {
        if (subtitle == null) return null;
        JSONObject object = new JSONObject();
        try {
            object.put("url", subtitle.getUrl());
            object.put("language_id", subtitle.getLanguageId());
            object.put("format", subtitle.getFormat());
            object.put("language", subtitle.getLanguage());
            object.put("id", subtitle.getIndex());
            object.put("expire", subtitle.getExpire());
            object.put("sub_id", subtitle.getSubtitleId());
            return object;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public static String dumpResolutionsLog(IVideoModel videoModel) {
        final StringBuilder resolutionInfo = new StringBuilder();
        final Resolution[] resolutions = videoModel.getSupportResolutions();
        Arrays.sort(resolutions);
        for (Resolution r : resolutions) {
            VideoInfo info = videoModel.getVideoInfo(r);
            resolutionInfo.append(r).append("[")
                    .append(info.getValueInt(VideoInfo.VALUE_VIDEO_INFO_BITRATE))
                    .append("bps")
                    .append(" ")
                    .append(TTVideoEngine.getCacheFileSize(info.getValueStr(VideoInfo.VALUE_VIDEO_INFO_FILE_HASH)))
                    .append("/")
                    .append(info.getValueLong(VideoInfo.VALUE_VIDEO_INFO_SIZE))
                    .append("B")
                    .append("]")
                    .append(",");
        }
        return resolutionInfo.toString();
    }

    @Nullable
    public static List<Subtitle> subtitleModel2Subtitles(@Nullable JSONObject subtitleModel) {
        if (subtitleModel == null) return null;
        JSONArray jsonArray = subtitleModel.optJSONArray("list");
        if (jsonArray == null) return null;

        List<Subtitle> subtitles = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Subtitle subtitle = parseSubtitle(jsonArray.optJSONObject(i));
            if (subtitle != null) {
                subtitles.add(subtitle);
            }
        }
        return subtitles;
    }

    @Nullable
    public static JSONObject subtitles2SubtitleModel(@Nullable List<Subtitle> subtitles) {
        if (subtitles == null) return null;

        JSONObject jsonObject = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            for (Subtitle subtitle : subtitles) {
                JSONObject object = subtitle2Json(subtitle);
                if (object != null) {
                    jsonArray.put(object);
                }
            }
            jsonObject.put("list", jsonArray);
            return jsonObject;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static SubDesInfoModel subtitleModel2SubtitleSource(@Nullable JSONObject subtitleModel) {
        if (subtitleModel == null) return null;
        SubDesInfoModel subtitleSource = new SubDesInfoModel(subtitleModel);
        if (subtitleSource.getSubModelList() != null && !subtitleSource.getSubModelList().isEmpty()) {
            return subtitleSource;
        }
        return null;
    }

    @Nullable
    public static SubDesInfoModel subtitles2SubtitleSource(@Nullable List<Subtitle> subtitles) {
        return Mapper.subtitleModel2SubtitleSource(Mapper.subtitles2SubtitleModel(subtitles));
    }

    public static List<SubInfo> findSubInfoList(IVideoModel videoModel) {
        if (videoModel == null) return null;
        return videoModel.getSubInfoList();
    }

    public static List<SubInfo> findSubInfoListWithLanguageIds(List<SubInfo> subInfoList, List<Integer> subtitleLanguageIds) {
        if (subInfoList == null) return null;
        if (subtitleLanguageIds == null) return subInfoList;

        List<SubInfo> list = new ArrayList<>();
        for (int languageId : subtitleLanguageIds) {
            for (SubInfo subInfo : subInfoList) {
                if (subInfo.getValueInt(SubInfo.VALUE_SUB_INFO_LANGUAGE_ID) == languageId) {
                    list.add(subInfo);
                }
            }
        }
        return list;
    }

    @Nullable
    public static List<Subtitle> subInfoList2Subtitles(List<SubInfo> subInfoList) {
        if (subInfoList == null) return null;
        List<Subtitle> subtitles = new ArrayList<>();
        for (SubInfo subInfo : subInfoList) {
            Subtitle subtitle = subInfo2Subtitle(subInfo);
            subtitles.add(subtitle);
        }
        return subtitles;
    }

    public static Subtitle subInfo2Subtitle(SubInfo subInfo) {
        Subtitle subtitle = new Subtitle();
        subtitle.setSubtitleId(subInfo.getValueInt(SubInfo.VALUE_SUB_INFO_ID));
        subtitle.setLanguageId(subInfo.getValueInt(SubInfo.VALUE_SUB_INFO_LANGUAGE_ID));
        subtitle.setFormat(subInfo.getValueStr(SubInfo.VALUE_SUB_INFO_FORMAT));
        return subtitle;
    }

    @Nullable
    public static String subtitleList2SubtitleIds(List<SubInfo> subInfoList) {
        if (subInfoList == null) return null;
        StringBuilder sb = new StringBuilder();
        for (SubInfo subInfo : subInfoList) {
            sb.append(subInfo.getValueInt(SubInfo.VALUE_SUB_INFO_ID)).append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    @Nullable
    public static SubtitleText mapSubtitleFrameInfo2SubtitleText(@Nullable String subtitleFrameInfo) {
        if (TextUtils.isEmpty(subtitleFrameInfo)) return null;
        try {
            JSONObject json = new JSONObject(subtitleFrameInfo);
            SubtitleText subtitleText = new SubtitleText();
            subtitleText.setText(json.optString("info"));
            subtitleText.setPts(json.optLong("pts"));
            subtitleText.setDuration(json.optLong("duration"));
            return subtitleText;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
