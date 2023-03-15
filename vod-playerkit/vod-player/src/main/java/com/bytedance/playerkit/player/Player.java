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

package com.bytedance.playerkit.player;

import android.view.Surface;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.event.ActionPrepare;
import com.bytedance.playerkit.player.event.ActionSetSurface;
import com.bytedance.playerkit.player.event.StateCompleted;
import com.bytedance.playerkit.player.event.StateError;
import com.bytedance.playerkit.player.event.StatePaused;
import com.bytedance.playerkit.player.event.StatePrepared;
import com.bytedance.playerkit.player.event.StatePreparing;
import com.bytedance.playerkit.player.event.StateReleased;
import com.bytedance.playerkit.player.event.StateStarted;
import com.bytedance.playerkit.player.event.StateStopped;
import com.bytedance.playerkit.player.legacy.PlayerLegacy;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.Track.TrackType;
import com.bytedance.playerkit.utils.event.Dispatcher;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * <b>Player</b> interface provide a common abstract of a media player. Implements should follow the
 * rules of this interface. It's official media player interface of PlayerKit SDK.
 *
 * <p> {@link AVPlayer} is default implement of <b>Player</b> interface. You can also use
 * this interface to implement your own media player with third part player SDK.
 * It's recommended to using {@link com.bytedance.playerkit.player.adapter.PlayerAdapter} to simplify
 * your implements. <b>PlayerAdapter</b> is an adapter interface of MediaPlayer. The API of
 * <b>PlayerAdapter</b> api is very similar with android system player
 * {@link android.media.MediaPlayer}.
 */
public interface Player {

    /**
     * Player state. One of
     * <ul>
     *  <li>{@link #STATE_IDLE}</li>
     *  <li>{@link #STATE_PREPARING}</li>
     *  <li>{@link #STATE_PREPARED}</li>
     *  <li>{@link #STATE_STARTED}</li>
     *  <li>{@link #STATE_PAUSED}</li>
     *  <li>{@link #STATE_COMPLETED}</li>
     *  <li>{@link #STATE_ERROR}</li>
     *  <li>{@link #STATE_STOPPED}</li>
     *  <li>{@link #STATE_RELEASED}</li>
     *  </ul>
     *
     * <p>State change event will be emitted with
     * {@link com.bytedance.playerkit.utils.event.Dispatcher.EventListener}
     * Player state event would be one of
     * <ul>
     *     <li>{@link com.bytedance.playerkit.player.event.StateIDLE} </li>
     *     <li>{@link StatePreparing} </li>
     *     <li>{@link StatePrepared} </li>
     *     <li>{@link StateStarted} </li>
     *     <li>{@link StatePaused} </li>
     *     <li>{@link StateCompleted} </li>
     *     <li>{@link StateError} </li>
     *     <li>{@link StateStopped} </li>
     *     <li>{@link StateReleased} </li>
     * </ul>
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            STATE_IDLE,
            STATE_PREPARING,
            STATE_PREPARED,
            STATE_STARTED,
            STATE_PAUSED,
            STATE_COMPLETED,
            STATE_ERROR,
            STATE_STOPPED,
            STATE_RELEASED})
    @interface PlayerState {
    }

    /**
     * Indicate idle state.
     * <p>
     * <ul>
     *  <li>A new created player instance is in IDLE state.</li>
     *  <li>Calling {@link #reset()} from any state(not include {@link #STATE_RELEASED} state) will
     *  transform player state to idle</li>
     * </ul>
     */
    int STATE_IDLE = 0;
    /**
     * Indicate preparing state.
     * <p>Calling {@link #prepare(MediaSource)} from {@link #STATE_IDLE} or {@link #STATE_STOPPED}
     * will transform player state to preparing.
     */
    int STATE_PREPARING = 1;
    /**
     * Indicate prepared state.
     * <p> Player state will be in prepared state After
     * {@link StatePrepared} event is emitted
     */
    int STATE_PREPARED = 2;
    /**
     * Indicate started state.
     * <p> Calling {@link #start()} from
     * <ul>
     *  <li>{@link #STATE_PREPARED}</li>
     *  <li>{@link #STATE_PAUSED}</li>
     *  <li>{@link #STATE_COMPLETED}</li>
     * </ul>
     * state will transform player state to stated.
     */
    int STATE_STARTED = 3;
    /**
     * Indicate paused state.
     * <p>Calling {@link #pause()} from
     * <ul>
     *   <li>{@link #STATE_PREPARED}</li>
     *   <li>{@link #STATE_STARTED}</li>
     *   <li>{@link #STATE_COMPLETED}</li>
     * </ul>
     * state will transform player state to paused.
     */
    int STATE_PAUSED = 4;
    /**
     * Indicate completed state.
     * <p> Player state will be in completed state After
     * {@link StateCompleted} event is emitted
     */
    int STATE_COMPLETED = 5;
    /**
     * Indicate error state.
     * <p>Player state will be in error state After
     * {@link StateError} event is emitted
     */
    int STATE_ERROR = 6;
    /**
     * Indicate stopped state.
     * <p> Player is in stopped state after calling {@link #stop()}
     */
    int STATE_STOPPED = 7;
    /**
     * Indicate release state.
     * <p>Player is in released state after calling {@link #release()}
     */
    int STATE_RELEASED = 8;

    static String mapState(@PlayerState int state) {
        switch (state) {
            case STATE_IDLE:
                return "idle";
            case STATE_PREPARING:
                return "preparing";
            case STATE_PREPARED:
                return "prepared";
            case STATE_STARTED:
                return "started";
            case STATE_PAUSED:
                return "paused";
            case STATE_COMPLETED:
                return "completed";
            case STATE_ERROR:
                return "error";
            case STATE_STOPPED:
                return "stopped";
            case STATE_RELEASED:
                return "released";
            default:
                throw new IllegalArgumentException("illegal state " + state);
        }
    }

    /**
     * Scaling mode. One of
     * {@link #SCALING_MODE_DEFAULT},
     * {@link #SCALING_MODE_ASPECT_FIT},
     * {@link #SCALING_MODE_ASPECT_FILL}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({SCALING_MODE_DEFAULT,
            SCALING_MODE_ASPECT_FIT,
            SCALING_MODE_ASPECT_FILL})
    @interface ScalingMode {
    }

    int SCALING_MODE_DEFAULT = 0;
    int SCALING_MODE_ASPECT_FIT = 1;
    int SCALING_MODE_ASPECT_FILL = 2;

    static String mapScalingMode(@ScalingMode int mode) {
        switch (mode) {
            case Player.SCALING_MODE_DEFAULT:
                return "default";
            case Player.SCALING_MODE_ASPECT_FIT:
                return "aspect_fit";
            case Player.SCALING_MODE_ASPECT_FILL:
                return "aspect_fill";
            default:
                throw new IllegalArgumentException("unsupported video scaling mode:" + mode);
        }
    }

    /**
     * Decoder type. One of
     * {@link #DECODER_TYPE_UNKNOWN},
     * {@link #DECODER_TYPE_SOFTWARE},
     * {@link #DECODER_TYPE_HARDWARE}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DECODER_TYPE_UNKNOWN,
            DECODER_TYPE_SOFTWARE,
            DECODER_TYPE_HARDWARE})
    @interface DecoderType {
    }

    int DECODER_TYPE_UNKNOWN = 0;
    int DECODER_TYPE_SOFTWARE = 1;
    int DECODER_TYPE_HARDWARE = 2;

    static String mapDecoderType(@DecoderType int type) {
        switch (type) {
            case DECODER_TYPE_UNKNOWN:
                return "unknown";
            case DECODER_TYPE_SOFTWARE:
                return "software";
            case DECODER_TYPE_HARDWARE:
                return "hardware";
            default:
                throw new IllegalArgumentException("unsupported decoder type:" + type);
        }
    }

    interface Factory {
        class Default {
            private static Factory sInstance;

            public static synchronized void set(Factory factory) {
                sInstance = factory;
            }

            public static synchronized Factory get() {
                return sInstance;
            }
        }

        Player create(@NonNull MediaSource source);
    }

    @Nullable
    PlayerLegacy legacy();

    void putExtra(@NonNull String key, @Nullable Object object);

    @Nullable
    <T> T getExtra(@NonNull String key, @NonNull Class<T> clazz);

    void clearExtras();

    /**
     * Registers a listener to receive all events from the player.
     *
     * @param listener The listener to add
     * @see #removePlayerListener(Dispatcher.EventListener)
     */
    void addPlayerListener(@NonNull Dispatcher.EventListener listener);

    /**
     * Unregister a listener registered through {@link #addPlayerListener(Dispatcher.EventListener)}.
     * The listener will no longer receive events.
     *
     * @param listener The listener to unregister
     * @see #addPlayerListener(Dispatcher.EventListener)
     */
    void removePlayerListener(@NonNull Dispatcher.EventListener listener);

    /**
     * Unregister all listeners registered through {@link #addPlayerListener(Dispatcher.EventListener)}
     * All the listeners will no longer receive events.
     */
    void removeAllPlayerListener();

    /**
     * Set the {@link Surface} to player. Setting a new surface will un-set the surface that was
     * previously set. Clear the surface by calling {@code setSurface(null)} if the surface is
     * destroyed.
     *
     * <p>If surface changed {@link ActionSetSurface} will be emitted immediately after this method
     * is invoked.
     *
     * @param surface the {@link Surface}
     * @see #getSurface()
     */
    void setSurface(@Nullable Surface surface);

    /**
     * Get the {@link Surface} that was previously set.
     *
     * @return the {@link Surface} hold by player.
     * @see #setSurface(Surface)
     */
    @Nullable
    Surface getSurface();


    /**
     * Sets video scaling mode. To make the target video scaling mode effective during playback. The
     * default video scaling mode is {@link #SCALING_MODE_DEFAULT}.
     *
     * <p>The supported scaling mode are:
     * <ul>
     *  <li>{@link #SCALING_MODE_DEFAULT}</li>
     *  <li>{@link #SCALING_MODE_ASPECT_FIT}</li>
     *  <li>{@link #SCALING_MODE_ASPECT_FILL}</li>
     * </ul>
     *
     * @param scalingMode target video scaling mode.
     * @throws IllegalArgumentException scalingMode must be one of the supported video scaling modes;
     *                                  otherwise, IllegalArgumentException will be thrown.
     */
    void setVideoScalingMode(@ScalingMode int scalingMode) throws IllegalArgumentException;

    /**
     * Get the video scaling mode was previously set.
     *
     * @return scalingMode hold by player
     */
    @ScalingMode
    int getVideoScalingMode();

    /**
     * Sets the volume on this player.
     *
     * <p>Note that the passed volume values are raw scalars in range 0.0 to 1.0. UI controls
     * should be scaled logarithmically.
     *
     * @param leftVolume  left volume scalar. Range [0.0, 1.0]
     * @param rightVolume right volume scalar. Range [0.0, 1.0]
     */
    void setVolume(float leftVolume, float rightVolume);

    /**
     * Gets the volume on this player.
     *
     * @return Volumes array of player. float[0] for leftVolume; float[1] for rightVolume.
     */
    float[] getVolume();

    /**
     * Sets the mute state on this player.
     *
     * @param muted muted state. true for muted. Default value is false.
     */
    void setMuted(boolean muted);

    /**
     * Gets whither the player is muted or not.
     *
     * @return true muted, false not muted. Default value is false.
     */
    boolean isMuted();

    /**
     * Prepares the player with {@link MediaSource} for playback, asynchronously.
     *
     * <p>{@link ActionPrepare} will be emitted immediately after this method is invoked.
     *
     * <p>Then {@link StatePreparing} will be emitted indicate player state is changing to preparing.
     *
     * <p>{@link StatePrepared} will be emitted if prepare succeed.
     *
     * <p>{@link StateError} will be emitted if prepare error.
     *
     * @param source {@link MediaSource} instance
     * @throws IllegalStateException prepare method must be called in one of:
     *                               <ul>
     *                                <li>{@link #STATE_IDLE}</li>
     *                                <li>{@link #STATE_STOPPED}</li>
     *                               </ul>
     *                               Otherwise, IllegalStateException will be thrown.
     * @see #addPlayerListener(Dispatcher.EventListener)
     * @see #getState()
     * @see #isIDLE()
     * @see #isStopped()
     */
    void prepare(@NonNull MediaSource source) throws IllegalStateException;

    /**
     * Set whether {@link #start()} should be invoked automatically when player is prepared.
     *
     * @param startWhenPrepared whether {@link #start()} should be invoked automatically
     * @see #addPlayerListener(Dispatcher.EventListener)
     * @see #prepare(MediaSource)
     * @see StatePrepared
     * @see #isStartWhenPrepared()
     */
    void setStartWhenPrepared(boolean startWhenPrepared);

    /**
     * Get whether {@link #start()} should be invoked automatically when player is prepared.
     *
     * @return startWhenPrepared flag hold by player
     */
    boolean isStartWhenPrepared();

    /**
     * @return The {@link MediaSource} instance hold by player
     */
    @Nullable
    MediaSource getDataSource();

    /**
     * Get current playing track.
     *
     * @param trackType Track type. One of {@link TrackType}
     * @return current playing track
     */
    @Nullable
    Track getCurrentTrack(@TrackType int trackType);

    /**
     * Get current selected but not playing track. Once the track is playing return null.
     *
     * @param trackType Track type. One of {@link TrackType}
     * @return pending to play track
     */
    @Nullable
    Track getPendingTrack(@TrackType int trackType);

    /**
     * Get selected track. Return {@link #getPendingTrack(int)} if not null.
     * Otherwise, return {@link #getCurrentTrack(int)}
     *
     * @param trackType Track type. One of {@link TrackType}
     * @return selected track
     */
    @Nullable
    Track getSelectedTrack(@TrackType int trackType);

    /**
     * Get track list by {@link TrackType}.
     *
     * @param trackType Track type. One of {@link TrackType}
     * @return a list of track if contains; null if not contains
     */
    @Nullable
    List<Track> getTracks(@TrackType int trackType);

    /**
     * @param trackType Track type. One of {@link TrackType}
     * @param track     desired track to play; null if auto
     */
    void selectTrack(@TrackType int trackType, @Nullable Track track)
            throws UnsupportedOperationException;

    /**
     * @param trackType Track type. One of {@link TrackType}
     * @return true if player is support smooth track switching for
     */
    boolean isSupportSmoothTrackSwitching(@TrackType int trackType);

    /**
     * Set playback start time. It's recommend to use this api instead {@link #seekTo(long)} to set
     * start play time.
     *
     * @param startTime playback start time, in MS
     * @throws IllegalStateException when player current state is not one of:
     *                               <ul>
     *                                 <li>{@link #STATE_IDLE}</li>
     *                                 <li>{@link #STATE_PREPARING}</li>
     *                                 <li>{@link #STATE_STOPPED}</li>
     *                               </ul>
     * @see #getState()
     * @see #isIDLE()
     * @see #isPreparing()
     * @see #isStopped()
     */
    void setStartTime(long startTime) throws IllegalStateException;

    /**
     * Get playback start time holded by player.
     *
     * @return playback start time, in MS
     */
    long getStartTime();

    /**
     * Seeks to specified time position.
     *
     * @param seekTo the offset in milliseconds from the start to seek to
     * @throws IllegalStateException when player current state is not in {@link #isInPlaybackState()}
     * @see #isInPlaybackState()
     */
    void seekTo(long seekTo) throws IllegalStateException;

    /**
     * Starts or resumes playback.
     * If playback had previously been paused, playback will continue from where it was paused.
     * If playback is prepared, playback will start at {@link #getStartTime()}
     *
     * @throws IllegalStateException when player current state is not in {@link #isInPlaybackState()}
     * @see #isInPlaybackState()
     */
    void start() throws IllegalStateException;

    /**
     * Pauses playback. Call start() to resume playback.
     *
     * @throws IllegalStateException when player current state is not in {@link #isInPlaybackState()}
     * @see #isInPlaybackState()
     */
    void pause() throws IllegalStateException;

    /**
     * Stops playback. Call prepare() + start() to start playback again. Call release() to release
     * the player when this player instance is no longer required.
     *
     * <p> {@code stop()} will not clear playback info such like:
     * <ul>
     *     <li>{@link MediaSource} instance can still be get from {@link #getDataSource()}</li>
     *     <li>{@link Surface} instance can still be get from {@link #getSurface()}</li>
     * </ul>
     *
     * <p> Listeners added by {@link #addPlayerListener(Dispatcher.EventListener)} will not be
     * removed either.
     *
     * @throws IllegalStateException when player current state is not in:
     *                               <ul>
     *                                   <li>{@link #STATE_PREPARING}</li>
     *                                   <li>{@link #isInPlaybackState()}</li>
     *                                   <li>{@link #STATE_STOPPED}</li>
     *                               </ul>
     * @see #reset()
     * @see #release()
     */
    void stop() throws IllegalStateException;

    /**
     * Resets the player to {@link #STATE_IDLE} state. Call prepare() + start()  to start playback
     * again. Call release() to release the player when this player instance is no longer required.
     *
     * <p> Calling {@code reset()} will clear all playback info. But listeners added by
     * {@link #addPlayerListener(Dispatcher.EventListener)} will not be removed.
     *
     * @see #stop()
     * @see #release()
     */
    void reset();

    /**
     * Releases the player. This method must be called when the player is no longer required. The
     * player must not be used after calling this method.
     */
    void release();

    /**
     * @return The duration of media content in milliseconds.
     * <p> Returns 0 if current player state is not one of:
     * <ul>
     *     <li>{@link #isInPlaybackState()} </li>
     *     <li>{@link #STATE_STOPPED}</li>
     * </ul>
     */
    long getDuration();

    /**
     * @return The playback position in the current media content in milliseconds.
     */
    long getCurrentPosition();

    /**
     * @return Buffered percent in player memory cache queue.
     */
    @IntRange(from = 0, to = 100)
    int getBufferedPercentage();

    /**
     * @return Buffered duration in memory cache queue since {@link #getCurrentPosition()}.
     */
    long getBufferedDuration();

    /**
     * @return Width of video frame in px
     */
    int getVideoWidth();

    /**
     * @return Height of video frame in px
     */
    int getVideoHeight();

    /**
     * @return Sample aspect ratio of video frame in float.
     */
    float getVideoSampleAspectRatio();

    /**
     * Set loop playback when complete.
     * {@link StateCompleted} will be emitted once video is complete. {@link #start()} will be
     * called if looping is true.
     *
     * @param looping true looping playback. Otherwise false.
     */
    void setLooping(boolean looping);

    /**
     * @return looping true looping playback. Otherwise false.
     */
    boolean isLooping();

    /**
     * Set playback speed factor
     *
     * @param speed (0, 2] in float. Recommend values [0.5, 1, 1.5, 2].
     */
    void setSpeed(float speed);

    /**
     * @return playback speed factor
     */
    float getSpeed();

    /**
     * Set audio pitch factor
     *
     * @param audioPitch audio pitch factor
     */
    void setAudioPitch(float audioPitch);

    /**
     * @return Audio pitch factor.
     */
    float getAudioPitch();

    /**
     * Set the audio session ID.
     *
     * @param audioSessionId the audio session ID.
     */
    void setAudioSessionId(int audioSessionId);

    /**
     * @return the audio session ID.
     * @see #setAudioSessionId(int)
     */
    int getAudioSessionId();

    void setSuperResolutionEnabled(boolean enabled);

    boolean isSuperResolutionEnabled();

    /**
     * @return true: IO buffering. Otherwise, false.
     */
    boolean isBuffering();

    @PlayerState
    int getState();

    /**
     * @see #STATE_IDLE
     */
    boolean isIDLE();

    /**
     * @see #STATE_PREPARING
     */
    boolean isPreparing();

    /**
     * @see #STATE_PREPARED
     */
    boolean isPrepared();

    /**
     * @see #STATE_STARTED
     */
    boolean isPlaying();

    /**
     * @see #STATE_PAUSED
     */
    boolean isPaused();

    /**
     * @see #STATE_COMPLETED
     */
    boolean isCompleted();

    /**
     * @see #STATE_STOPPED
     */
    boolean isStopped();

    /**
     * @see #STATE_RELEASED
     */
    boolean isReleased();

    /**
     * @see #STATE_ERROR
     */
    boolean isError();

    /**
     * @return true if {@link #getState()} is in playback state.
     * <ul>
     *   <li>{@link #STATE_PREPARED}</li>
     *   <li>{@link #STATE_STARTED}</li>
     *   <li>{@link #STATE_PAUSED}</li>
     *   <li>{@link #STATE_COMPLETED}</li>
     * </ul>
     */
    boolean isInPlaybackState();

    /**
     * @return {@link PlayerException} instance if the player is in {@link #STATE_ERROR} state after
     * {@link StateError} event is emitted. Otherwise, return null.
     */
    @Nullable
    PlayerException getPlayerException();

    String dump();
}
