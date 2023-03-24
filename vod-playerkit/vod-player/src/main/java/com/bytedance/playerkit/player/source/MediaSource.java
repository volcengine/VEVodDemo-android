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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.ExtraObject;
import com.bytedance.playerkit.utils.L;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Official media source interface of PlayerKit SDK.
 * <p>
 * You can create simple MediaSource by using util method.
 * <pre>{@code
 *   // Quick method for create url media source.
 *   String videoId = "your video id";
 *   String videoUrl = "http://www.yourdomain.com/yourvideo.mp4";
 *   String videoCacheKey = "cache key of yourvideo.mp4 file";
 *   MediaSource urlSource = MediaSource.createUrlSource(videoId, videoUrl, videoCacheKey);
 *
 *   // Quick method for create id media source.
 *   String videoId = "your video id";
 *   String playAuthToken = "your play auth token";
 *   MediaSource idSource = MediaSource.createIdSource(videoId, playAuthToken);
 * }</pre>
 *
 * <p>
 * Or create {@link MediaSource} directly for multi-quality use case.
 * <pre>{@code
 *   // Create a url type media source
 *   MediaSource createUrlMediaSource() {
 *     MediaSource mediaSource = new MediaSource(UUID.randomUUID().toString(), MediaSource.SOURCE_TYPE_URL);
 *     Track track0 = new Track();
 *     track0.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track0.setUrl("http://example.com/video_360p.mp4");
 *     track0.setQuality(new Quality(Quality.QUALITY_RES_360, "360P"));
 *
 *     Track track1 = new Track();
 *     track1.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track1.setUrl("http://example.com/video_480p.mp4");
 *     track1.setQuality(new Quality(Quality.QUALITY_RES_480, "480P"));
 *
 *     Track track2 = new Track();
 *     track2.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track2.setUrl("http://example.com/video_720p.mp4");
 *     track2.setQuality(new Quality(Quality.QUALITY_RES_720, "720P"));
 *
 *     Track track3 = new Track();
 *     track3.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track3.setUrl("https://example.com/video_1080p.mp4");
 *     track3.setQuality(new Quality(Quality.QUALITY_RES_1080, "1080P"));
 *
 *     // You can switch quality of current playback by calling {@link Player#selectTrack(int, Track)}.
 *     // You can only add one track, If you don't have multi quality of tracks.
 *     mediaSource.setTracks(Arrays.asList(track0, track1, track2, track3));
 *     return source;
 *   }
 *
 *   // Create a id type media source
 *   MediaSource createIdMediaSource() {
 *     MediaSource mediaSource = new MediaSource("your media id", MediaSource.SOURCE_TYPE_ID);
 *     mediaSource.setPlayAuthToken("your play auth token of media id");
 *     return source;
 *   }
 * }</pre>
 *
 * @see Player#prepare(MediaSource)
 */
public class MediaSource extends ExtraObject implements Serializable {
    /**
     * Source type. One of:
     * <ul>
     *     <li>{@link #SOURCE_TYPE_URL}</li>
     *     <li>{@link #SOURCE_TYPE_ID}</li>
     * </ul>
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SOURCE_TYPE_URL, SOURCE_TYPE_ID})
    public @interface SourceType {
    }

    /**
     * Url source type
     *
     * @see #MediaSource(String, int)
     * @see #getSourceType()
     */
    public static final int SOURCE_TYPE_URL = 0;
    /**
     * Id source type
     *
     * @see #MediaSource(String, int)
     * @see #getSourceType()
     */
    public static final int SOURCE_TYPE_ID = 1;

    /**
     * Media protocol type. One of:
     * <ul>
     *     <li>{@link #MEDIA_PROTOCOL_DEFAULT}</li>
     *     <li>{@link #MEDIA_PROTOCOL_DASH}</li>
     *     <li>{@link #MEDIA_PROTOCOL_HLS}</li>
     * </ul>
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEDIA_PROTOCOL_DEFAULT, MEDIA_PROTOCOL_DASH, MEDIA_PROTOCOL_HLS})
    public @interface MediaProtocol {
    }

    /**
     * Default media protocol type. eg. mp4/flv and other streams.
     *
     * @see #setMediaProtocol(int)
     * @see #getMediaProtocol()
     */
    public static final int MEDIA_PROTOCOL_DEFAULT = 0;
    /**
     * DASH media protocol type. Works with {@link #SOURCE_TYPE_ID}
     */
    public static final int MEDIA_PROTOCOL_DASH = 1;
    /**
     * HLS media protocol type. Works with {@link #SOURCE_TYPE_ID}
     */
    public static final int MEDIA_PROTOCOL_HLS = 2;

    /**
     * Media type of {@code MediaSource}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MEDIA_TYPE_AUDIO, MEDIA_TYPE_VIDEO})
    public @interface MediaType {
    }

    /**
     * Video media type.
     */
    public static final int MEDIA_TYPE_VIDEO = 0;
    /**
     * Audio media type.
     */
    public static final int MEDIA_TYPE_AUDIO = 1;

    public static String mapSourceType(@SourceType int sourceType) {
        switch (sourceType) {
            case SOURCE_TYPE_URL:
                return "url";
            case SOURCE_TYPE_ID:
                return "id";
        }
        throw new IllegalArgumentException("unsupported sourceType! " + sourceType);
    }

    public static String mapMediaProtocol(@MediaProtocol int mediaProtocol) {
        switch (mediaProtocol) {
            case MEDIA_PROTOCOL_DEFAULT:
                return "default";
            case MEDIA_PROTOCOL_DASH:
                return "dash";
            case MEDIA_PROTOCOL_HLS:
                return "hls";
        }
        throw new IllegalArgumentException("unsupported mediaProtocol! " + mediaProtocol);
    }

    public static String mapMediaType(@MediaType int mediaType) {
        switch (mediaType) {
            case MEDIA_TYPE_VIDEO:
                return "video";
            case MEDIA_TYPE_AUDIO:
                return "audio";
        }
        throw new IllegalArgumentException("unsupported mediaType! " + mediaType);
    }

    /**
     * Compare two {@code MediaSource} object's {@link #mediaId}. Useful when you want to reuse
     * player.
     *
     * @param a one instance of {@link MediaSource}
     * @param b the other instance of {@link MediaSource}
     * @return true if media id is equal. Otherwise, false.
     */
    public static boolean mediaEquals(MediaSource a, MediaSource b) {
        return Objects.equals(a == null ? null : a.mediaId,
                b == null ? null : b.mediaId);
    }

    /**
     * Unique id of {@code MediaSource} instance. You can make two instance of {@code MediaSource}
     * hash equal by set same uniqueId id when using {@code MediaSource} as key of hash container.
     * eg.{@link java.util.HashMap}
     *
     * @see #MediaSource(String, String, int)
     * @see #getUniqueId()
     * @see #hashCode()
     * @see #equals(Object)
     */
    private final String uniqueId;
    /**
     * Media id of video/audio.
     */
    private final String mediaId;

    @SourceType
    private final int sourceType;
    @MediaProtocol
    private int mediaProtocol;
    @MediaType
    private int mediaType;
    /**
     * As key of storing the progress of playback.
     */
    private String syncProgressId;

    /**
     * Auth token of {@link #mediaId}. Only works with {@link #SOURCE_TYPE_ID}
     */
    private String playAuthToken;

    /**
     * Additional headers of video url http request
     */
    private Map<String, String> headers;

    /**
     * Cover url of video
     */
    private String coverUrl;
    /**
     * Duration of video/audio in MS.
     */
    private long duration;
    /**
     * Display aspect ratio of video in float (width/height).
     */
    private float displayAspectRatio;

    /**
     * A list of url track of video/audio stream in different quality.
     */
    private List<Track> tracks;

    /**
     * Utility method for quick create single url source.
     *
     * @param mediaId  Media id of video/audio. Pass null if you don't know.
     * @param url      Url of video
     * @param cacheKey Cache key of media resource
     * @return Instance of url MediaSource.
     */
    public static MediaSource createUrlSource(@Nullable String mediaId, @NonNull String url,
                                              @Nullable String cacheKey) {
        MediaSource mediaSource = new MediaSource(mediaId, SOURCE_TYPE_URL);
        Track track = new Track();
        track.setTrackType(Track.TRACK_TYPE_VIDEO);
        track.setUrl(url);
        track.setFileHash(cacheKey);
        mediaSource.setTracks(Arrays.asList(track));
        return mediaSource;
    }

    /**
     * Utility method for quick create id + playAuthToken source.
     *
     * @param mediaId       Media id of video/audio. Null is not allowed.
     * @param playAuthToken Auth token of {@link #mediaId}.
     * @return Instance of id MediaSource.
     */
    public static MediaSource createIdSource(@NonNull String mediaId, @NonNull String playAuthToken) {
        MediaSource mediaSource = new MediaSource(mediaId, SOURCE_TYPE_ID);
        mediaSource.setPlayAuthToken(playAuthToken);
        return mediaSource;
    }

    public MediaSource(@Nullable String mediaId, @SourceType int sourceType) {
        this(UUID.randomUUID().toString(), mediaId, sourceType);
    }

    public MediaSource(@NonNull String uniqueId, @Nullable String mediaId, @SourceType int sourceType) {
        Asserts.checkNotNull(uniqueId);
        this.uniqueId = uniqueId;
        this.mediaId = mediaId == null ? uniqueId : mediaId;
        this.sourceType = sourceType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MediaSource that = (MediaSource) o;
        return Objects.equals(uniqueId, that.uniqueId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uniqueId);
    }

    /**
     * @return Unique id of {@code MediaSource} instance. You can make two instance of
     * {@code MediaSource} hash equal by set same uniqueId id when using {@code MediaSource} as key
     * of hash container. eg.{@link java.util.HashMap}
     */
    public String getUniqueId() {
        return uniqueId;
    }

    /**
     * @return Source type. One of:
     * <ul>
     *     <li>{@link #SOURCE_TYPE_URL}</li>
     *     <li>{@link #SOURCE_TYPE_ID}</li>
     * </ul>
     */
    @SourceType
    public int getSourceType() {
        return sourceType;
    }

    /**
     * @return Media id of video/audio.
     */
    @NonNull
    public String getMediaId() {
        return mediaId;
    }

    /**
     * @return Media protocol type. One of:
     * <ul>
     *     <li>{@link #MEDIA_PROTOCOL_DEFAULT}</li>
     *     <li>{@link #MEDIA_PROTOCOL_DASH}</li>
     *     <li>{@link #MEDIA_PROTOCOL_HLS}</li>
     * </ul>
     */
    @MediaProtocol
    public int getMediaProtocol() {
        return mediaProtocol;
    }


    /**
     * Set Media protocol type
     *
     * @param mediaProtocol Media protocol type. One of:
     *                      <ul>
     *                          <li>{@link #MEDIA_PROTOCOL_DEFAULT}</li>
     *                          <li>{@link #MEDIA_PROTOCOL_DASH}</li>
     *                          <li>{@link #MEDIA_PROTOCOL_HLS}</li>
     *                      </ul>
     */
    public void setMediaProtocol(@MediaProtocol int mediaProtocol) {
        this.mediaProtocol = mediaProtocol;
    }

    /**
     * Get Media type
     *
     * @return mediaType. One of:
     * <ul>
     *     <li>{@link #MEDIA_TYPE_VIDEO}</li>
     *     <li>{@link #MEDIA_TYPE_AUDIO}</li>
     * </ul>
     */
    @MediaType
    public int getMediaType() {
        return mediaType;
    }

    /**
     * Set Media type
     *
     * @param mediaType One of:
     *                  <ul>
     *                      <li>{@link #MEDIA_TYPE_VIDEO}</li>
     *                      <li>{@link #MEDIA_TYPE_AUDIO}</li>
     *                  </ul>
     */
    public void setMediaType(@MediaType int mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * @return syncProgressId. Key of storing/syncing playback progress.
     */
    public String getSyncProgressId() {
        return syncProgressId;
    }

    /**
     * Set key of storing/syncing playback progress.
     *
     * @param syncProgressId sync progress id
     */
    public void setSyncProgressId(String syncProgressId) {
        this.syncProgressId = syncProgressId;
    }

    /**
     * @return Auth token of {@link #mediaId}. Only works with {@link #SOURCE_TYPE_ID}
     */
    public String getPlayAuthToken() {
        return playAuthToken;
    }

    /**
     * Set Auth token of {@link #mediaId}. Only works with {@link #SOURCE_TYPE_ID}
     *
     * @param playAuthToken Auth token of {@link #mediaId}
     */
    public void setPlayAuthToken(String playAuthToken) {
        this.playAuthToken = playAuthToken;
    }

    /**
     * @return Additional headers of video url http request
     */
    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * Set Additional headers of video url http request
     *
     * @param headers Additional headers
     */
    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    /**
     * @return Cover url of video
     */
    public String getCoverUrl() {
        return coverUrl;
    }

    /**
     * Set Cover url of video
     *
     * @param coverUrl Cover url of video
     */
    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    /**
     * @return Duration of video/audio in MS.
     */
    public long getDuration() {
        return duration;
    }

    /**
     * Set duration of video/audio in MS.
     *
     * @param duration In MS
     */
    public void setDuration(long duration) {
        this.duration = duration;
    }

    /**
     * Set display aspect ratio of video
     *
     * @param displayAspectRatio display aspect ratio of video in float (width/height).
     */
    public void setDisplayAspectRatio(float displayAspectRatio) {
        this.displayAspectRatio = displayAspectRatio;
    }

    /**
     * Get display aspect ratio of video
     *
     * @return {@link #displayAspectRatio} in float (width/height).
     */
    public float getDisplayAspectRatio() {
        return this.displayAspectRatio;
    }

    /**
     * @return list of {@link Track}
     */
    public List<Track> getTracks() {
        return tracks;
    }

    /**
     * Set list of track. List will be sorted by {@link Track#getBitrate()}.
     *
     * @param tracks list of {@link Track}
     */
    public void setTracks(List<Track> tracks) {
        final List<Track> list = new ArrayList<>(tracks);
        Collections.sort(list, new Comparator<Track>() {
            @Override
            public int compare(Track o1, Track o2) {
                return Integer.compare(o1.getBitrate(), o2.getBitrate());
            }
        });
        this.tracks = list;
    }

    /**
     * @param trackType track type
     * @return List of track by trackType
     */
    @Nullable
    public List<Track> getTracks(@Track.TrackType int trackType) {
        if (tracks == null) return null;
        List<Track> result = new ArrayList<>();
        for (Track track : tracks) {
            if (track.getTrackType() == trackType) {
                result.add(track);
            }
        }
        return result;
    }

    /**
     * @param trackType track type
     * @return First track of trackType
     */
    @Nullable
    public Track getFirstTrack(@Track.TrackType int trackType) {
        if (tracks == null) return null;
        for (Track track : tracks) {
            if (track.getTrackType() == trackType) {
                return track;
            }
        }
        return null;
    }

    public static String dump(MediaSource source) {
        if (!L.ENABLE_LOG) return null;

        if (source == null) return null;
        return source.dump();
    }

    public String dump() {
        return String.format("[%s %s %s %s %s]", L.obj2String(this), mapMediaType(mediaType), mapSourceType(sourceType), mapMediaProtocol(mediaProtocol), mediaId);
    }
}
