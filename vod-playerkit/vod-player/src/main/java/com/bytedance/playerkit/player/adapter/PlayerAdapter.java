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

package com.bytedance.playerkit.player.adapter;

import android.media.MediaPlayer;
import android.os.Looper;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerException;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public interface PlayerAdapter {

    class Info {
        /* Do not change these values without updating their counterparts
         * in include/media/mediaplayer.h!
         */
        /**
         * Unspecified media player info.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_UNKNOWN = MediaPlayer.MEDIA_INFO_UNKNOWN;

        /**
         * The player just pushed the very first video frame for rendering.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_VIDEO_RENDERING_START = MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

        /**
         * The player just pushed the very first audio frame for rendering.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_AUDIO_RENDERING_START = 4;


        public static final int MEDIA_INFO_VIDEO_RENDERING_START_BEFORE_START = 5;

        /**
         * MediaPlayer is temporarily pausing playback internally in order to
         * buffer more data.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_BUFFERING_START = MediaPlayer.MEDIA_INFO_BUFFERING_START;

        /**
         * MediaPlayer is resuming playback after filling buffers.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_BUFFERING_END = MediaPlayer.MEDIA_INFO_BUFFERING_END;


        /**
         * Estimated network bandwidth information (kbps) is available; currently this event fires
         * simultaneously as {@link #MEDIA_INFO_BUFFERING_START} and {@link #MEDIA_INFO_BUFFERING_END}
         * when playing network files.
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_NETWORK_BANDWIDTH = 703;

        /**
         * The media cannot be seeked (e.g live stream)
         *
         * @see Listener#onInfo(PlayerAdapter, int, int)
         */
        public static final int MEDIA_INFO_NOT_SEEKABLE = MediaPlayer.MEDIA_INFO_NOT_SEEKABLE;
    }

    class SourceLoadInfo {
        public static final int SOURCE_INFO_PLAY_INFO_FETCHED = 0;

        public static final int SOURCE_INFO_SUBTITLE_INFO_FETCHED = 1;

        public static final int SOURCE_INFO_MASK_INFO_FETCHED = 2;
    }

    interface Factory {
        PlayerAdapter create(Looper eventLooper);
    }

    /**
     * Same with {@link android.media.MediaDataSource} with no api limit.
     */
    abstract class MediaDataSource implements Closeable {

        public abstract int readAt(long position, byte[] buffer, int offset, int size)
                throws IOException;

        public abstract long getSize() throws IOException;
    }

    void setListener(final Listener listener);

    void setSurface(@Nullable Surface surface);

    void setDisplay(@Nullable SurfaceHolder display);

    void setVideoScalingMode(@Player.ScalingMode int mode);

    void setDataSource(@NonNull MediaSource source) throws IOException;

    boolean isSupportSmoothTrackSwitching(@Track.TrackType int trackType);

    void selectTrack(@Track.TrackType int trackType, @Nullable Track track) throws IllegalStateException;

    Track getPendingTrack(@Track.TrackType int trackType) throws IllegalStateException;

    Track getCurrentTrack(@Track.TrackType int trackType) throws IllegalStateException;

    List<Track> getTracks(@Track.TrackType int trackType) throws IllegalStateException;

    void setStartTime(long startTime);

    void setStartWhenPrepared(boolean startWhenPrepared);

    boolean isStartWhenPrepared();

    long getStartTime();

    void prepareAsync() throws IllegalStateException;

    void start() throws IllegalStateException;

    boolean isPlaying();

    void pause() throws IllegalStateException;

    void stop() throws IllegalStateException;

    void reset();

    void release();

    void seekTo(long seekTo);

    long getDuration();

    long getCurrentPosition();

    int getBufferedPercentage();

    long getBufferedDuration();

    long getBufferedDuration(@Track.TrackType int trackType);

    int getVideoWidth();

    int getVideoHeight();

    void setLooping(boolean looping);

    boolean isLooping();

    void setVolume(float leftVolume, float rightVolume);

    float[] getVolume();

    void setMuted(boolean muted);

    boolean isMuted();

    void setSpeed(final float speed);

    float getSpeed();

    void setAudioPitch(final float audioPitch);

    float getAudioPitch();

    void setAudioSessionId(int audioSessionId);

    int getAudioSessionId();

    void setSuperResolutionEnabled(boolean enabled);

    boolean isSuperResolutionEnabled();

    String dump();

    interface Listener extends SourceInfoListener, TrackListener {

        void onPrepared(@NonNull PlayerAdapter mp);

        void onCompletion(@NonNull PlayerAdapter mp);

        void onError(@NonNull PlayerAdapter mp, int code, @NonNull String msg);

        void onSeekComplete(@NonNull PlayerAdapter mp);

        void onVideoSizeChanged(@NonNull PlayerAdapter mp, int width, int height);

        void onSARChanged(@NonNull PlayerAdapter mp, int num, int den);

        void onBufferingUpdate(@NonNull PlayerAdapter mp, int percent);

        void onProgressUpdate(@NonNull PlayerAdapter mp, long position);

        void onInfo(@NonNull PlayerAdapter mp, int what, int extra);

        void onCacheHint(PlayerAdapter mp, long cacheSize);
    }

    interface SourceInfoListener {
        void onSourceInfoLoadStart(PlayerAdapter mp, int type, MediaSource source);

        void onSourceInfoLoadComplete(PlayerAdapter mp, int type, MediaSource source);

        void onSourceInfoLoadError(PlayerAdapter mp, int type, PlayerException e);
    }

    interface TrackListener {

        void onTrackInfoReady(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @NonNull List<Track> tracks);

        void onTrackWillChange(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @Nullable Track current, @NonNull Track target);

        void onTrackChanged(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @NonNull Track pre, @NonNull Track current);
    }
}
