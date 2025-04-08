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

import static com.bytedance.playerkit.player.Player.mapState;
import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_AUDIO;
import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_VIDEO;
import static com.bytedance.playerkit.player.source.Track.TrackType;
import static com.bytedance.playerkit.player.source.Track.mapTrackType;

import android.os.Looper;
import android.view.Surface;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.adapter.PlayerAdapter;
import com.bytedance.playerkit.player.event.ActionPause;
import com.bytedance.playerkit.player.event.ActionPrepare;
import com.bytedance.playerkit.player.event.ActionRelease;
import com.bytedance.playerkit.player.event.ActionSeekTo;
import com.bytedance.playerkit.player.event.ActionSetLooping;
import com.bytedance.playerkit.player.event.ActionSetSpeed;
import com.bytedance.playerkit.player.event.ActionSetSurface;
import com.bytedance.playerkit.player.event.ActionStart;
import com.bytedance.playerkit.player.event.InfoAudioRenderingStart;
import com.bytedance.playerkit.player.event.InfoBufferingEnd;
import com.bytedance.playerkit.player.event.InfoBufferingStart;
import com.bytedance.playerkit.player.event.InfoBufferingUpdate;
import com.bytedance.playerkit.player.event.InfoCacheUpdate;
import com.bytedance.playerkit.player.event.InfoDataSourceRefreshed;
import com.bytedance.playerkit.player.event.InfoFrameInfoUpdate;
import com.bytedance.playerkit.player.event.InfoProgressUpdate;
import com.bytedance.playerkit.player.event.InfoSeekComplete;
import com.bytedance.playerkit.player.event.InfoSeekingStart;
import com.bytedance.playerkit.player.event.InfoSubtitleCacheUpdate;
import com.bytedance.playerkit.player.event.InfoSubtitleChanged;
import com.bytedance.playerkit.player.event.InfoSubtitleFileLoadFinish;
import com.bytedance.playerkit.player.event.InfoSubtitleInfoReady;
import com.bytedance.playerkit.player.event.InfoSubtitleStateChanged;
import com.bytedance.playerkit.player.event.InfoSubtitleTextUpdate;
import com.bytedance.playerkit.player.event.InfoSubtitleWillChange;
import com.bytedance.playerkit.player.event.InfoTrackChanged;
import com.bytedance.playerkit.player.event.InfoTrackInfoReady;
import com.bytedance.playerkit.player.event.InfoTrackWillChange;
import com.bytedance.playerkit.player.event.InfoVideoRenderingStart;
import com.bytedance.playerkit.player.event.InfoVideoRenderingStartBeforeStart;
import com.bytedance.playerkit.player.event.InfoVideoSARChanged;
import com.bytedance.playerkit.player.event.InfoVideoSizeChanged;
import com.bytedance.playerkit.player.event.StateCompleted;
import com.bytedance.playerkit.player.event.StateError;
import com.bytedance.playerkit.player.event.StatePaused;
import com.bytedance.playerkit.player.event.StatePrepared;
import com.bytedance.playerkit.player.event.StatePreparing;
import com.bytedance.playerkit.player.event.StateReleased;
import com.bytedance.playerkit.player.event.StateStarted;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.SubtitleText;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.utils.ProgressRecorder;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.ExtraObject;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;

/**
 * Default implements of {@link Player}. Wrapper the {@link PlayerAdapter} instance methods and
 * callbacks.
 * <p> Using {@link Dispatcher} to dispatch the events. There are three type of events.
 * <ul>
 *   <li>{@link PlayerEvent.Action} action event</li>
 *   <li>{@link PlayerEvent.State} state event</li>
 *   <li>{@link PlayerEvent.Info} info event</li>
 * </ul>
 */
public class AVPlayer extends ExtraObject implements Player {
    private final Dispatcher mDispatcher;
    private final String mPlayerType;

    private Surface mSurface;
    private PlayerAdapter mPlayer;
    private MediaSource mMediaSource;
    @PlayerState
    private int mState;

    private long mStartTime;

    private boolean mIsBuffering;
    private int mBufferIndex = -1;
    private int mBufferPercentage;
    private boolean mSeekable = true;
    private final float[] mVolume = new float[]{1F, 1F};
    private PlayerException mPlayerException;
    private float mVideoSampleAspectRatio;
    private boolean mLooping;

    @ScalingMode
    private int mVideoScalingMode = SCALING_MODE_DEFAULT;

    public AVPlayer(PlayerAdapter.Factory playerFactory,
                    Looper eventLooper) {
        L.d(this, "constructor", playerFactory.type());
        final Listener listener = new Listener(this);
        this.mDispatcher = new Dispatcher(eventLooper);
        this.mPlayerType = playerFactory.type();
        this.mPlayer = playerFactory.create(eventLooper);
        this.mPlayer.setListener(listener);
        setState(STATE_IDLE);
    }

    private static class Listener implements PlayerAdapter.Listener {

        private final WeakReference<AVPlayer> mPlayerRef;

        Listener(AVPlayer player) {
            this.mPlayerRef = new WeakReference<>(player);
        }

        @Override
        public void onPrepared(@NonNull PlayerAdapter mp) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            if (player.isPreparing()) {
                L.d(player, "onPrepared");
                player.setState(STATE_PREPARED);
                player.mDispatcher.obtain(StatePrepared.class, player).dispatch();
                if (player.isStartWhenPrepared()) {
                    player.start();
                }
            } else {
                L.w(player, "onPrepared", "wrong state", player.dump());
            }
        }

        @Override
        public void onCompletion(@NonNull PlayerAdapter mp) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;
            if (player.isError()) return;

            final long currentPosition = player.getCurrentPosition();
            final long duration = player.getDuration();
            L.d(player, "onCompletion", "loop", player.isLooping(), currentPosition, duration);
            player.notifyProgressUpdate( /* currentPosition */ duration, duration);
            player.clearProgress();
            player.setState(STATE_COMPLETED);
            player.mDispatcher.obtain(StateCompleted.class, player).dispatch();

            if (player.isLooping()) {
                player.start();
            }
        }

        @Override
        public void onError(@NonNull PlayerAdapter mp, int code, @NonNull String msg) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;
            if (player.isError()) return;

            L.d(player, "onError", player, code, msg);
            player.moveToErrorState(new PlayerException(code, msg));
        }

        @Override
        public void onVideoSizeChanged(@NonNull PlayerAdapter mp, int width, int height) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onVideoSizeChanged", width, height);

            player.mDispatcher.obtain(InfoVideoSizeChanged.class, player).init(width, height).dispatch();
        }

        @Override
        public void onSARChanged(@NonNull PlayerAdapter mp, int num, int den) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;
            L.d(player, "onSARChanged", num, den);

            player.mVideoSampleAspectRatio = num / (float) den;
            player.mDispatcher.obtain(InfoVideoSARChanged.class, player).init(num, den).dispatch();
        }

        @Override
        public void onBufferingUpdate(@NonNull PlayerAdapter mp, int percent) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            if (player.mBufferPercentage != percent) {
                L.v(player, "onBufferingUpdate", percent);
                player.mBufferPercentage = percent;

                player.mDispatcher.obtain(InfoBufferingUpdate.class, player).init(percent).dispatch();
            }
        }

        @Override
        public void onProgressUpdate(@NonNull PlayerAdapter mp, long position) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            final long duration = player.getDuration();
            if (duration >= 0 && position >= 0) {
                player.recordProgress();
                player.notifyProgressUpdate(position, duration);
            }
        }

        @Override
        public void onInfo(@NonNull PlayerAdapter mp, int what, @Nullable Object extra) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;
            switch (what) {
                case PlayerAdapter.Info.MEDIA_INFO_VIDEO_RENDERING_START_BEFORE_START:
                    L.d(player, "onInfo", "video rendering start before start");
                    player.mDispatcher.obtain(InfoVideoRenderingStartBeforeStart.class, player).dispatch();
                    break;
                case PlayerAdapter.Info.MEDIA_INFO_VIDEO_RENDERING_START: {
                    L.d(player, "onInfo", "video rendering start");
                    player.mDispatcher.obtain(InfoVideoRenderingStart.class, player).dispatch();
                    break;
                }
                case PlayerAdapter.Info.MEDIA_INFO_AUDIO_RENDERING_START: {
                    L.d(player, "onInfo", "audio rendering start");
                    player.mDispatcher.obtain(InfoAudioRenderingStart.class, player).dispatch();
                    break;
                }
                case PlayerAdapter.Info.MEDIA_INFO_BUFFERING_START: {
                    player.mIsBuffering = true;
                    player.mBufferIndex++;
                    int bufferingType = 0;
                    int bufferingStage = 0;
                    int bufferingReason = 0;
                    if (extra instanceof Object[]) {
                        final Object[] params = (Object[]) extra;
                        if (params.length >= 3) {
                            bufferingType = (int) params[0];
                            bufferingStage = (int) params[1];
                            bufferingReason = (int) params[2];
                        }
                    }
                    L.d(player, "onInfo", "buffering start", player.mBufferIndex,
                            InfoBufferingStart.mapBufferingType(bufferingType),
                            InfoBufferingStart.mapBufferingStage(bufferingStage),
                            InfoBufferingStart.mapBufferingReason(bufferingReason));
                    player.mDispatcher.obtain(InfoBufferingStart.class, player).init(
                                    player.mBufferIndex,
                                    bufferingType,
                                    bufferingStage,
                                    bufferingReason)
                            .dispatch();
                    break;
                }
                case PlayerAdapter.Info.MEDIA_INFO_BUFFERING_END: {
                    player.mIsBuffering = false;
                    L.d(player, "onInfo", "buffering end", player.mBufferIndex);
                    player.mDispatcher.obtain(InfoBufferingEnd.class, player).init(player.mBufferIndex).dispatch();
                    break;
                }
                case PlayerAdapter.Info.MEDIA_INFO_NOT_SEEKABLE: {
                    player.mSeekable = false;
                    L.d(player, "onInfo", "not seekable");
                    break;
                }
                default: {
                    L.w(player, "onInfo", "unsupported", what, extra);
                    break;
                }
            }
        }

        @Override
        public void onCacheHint(PlayerAdapter mp, long cacheSize) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onCacheHint", cacheSize);
            player.mDispatcher.obtain(InfoCacheUpdate.class, player).init(cacheSize).dispatch();
        }

        @Override
        public void onMediaSourceUpdateStart(PlayerAdapter mp, int type, MediaSource source) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onMediaSourceUpdateStart", type, source);
        }

        @Override
        public void onMediaSourceUpdated(PlayerAdapter mp, int type, MediaSource source) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onMediaSourceUpdated", type, source);
            int refreshType = 0;
            if (type == PlayerAdapter.MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_PLAY_INFO_FETCHED) {
                refreshType = InfoDataSourceRefreshed.REFRESHED_TYPE_PLAY_INFO_FETCHED;
            } else if (type == PlayerAdapter.MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_SUBTITLE_INFO_FETCHED) {
                refreshType = InfoDataSourceRefreshed.REFRESHED_TYPE_SUBTITLE_INFO_FETCHED;
            } else if (type == PlayerAdapter.MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_MASK_INFO_FETCHED) {
                refreshType = InfoDataSourceRefreshed.REFRESHED_TYPE_MASK_INFO_FETCHED;
            }
            if (refreshType > 0) {
                player.mDispatcher.obtain(InfoDataSourceRefreshed.class, player).init(refreshType).dispatch();
            }
        }

        @Override
        public void onMediaSourceUpdateError(PlayerAdapter mp, int type, PlayerException e) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onMediaSourceUpdateError", e, type);
        }

        @Override
        public void onSeekComplete(@NonNull PlayerAdapter mp) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onSeekComplete", player.getCurrentPosition());
            player.mDispatcher.obtain(InfoSeekComplete.class, player).dispatch();
        }

        @Override
        public void onTrackInfoReady(@NonNull PlayerAdapter mp, @TrackType int trackType, @NonNull List<Track> tracks) {
            // select default resolution
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onTrackInfoReady", mapTrackType(trackType), Track.dump(tracks));
            player.mDispatcher.obtain(InfoTrackInfoReady.class, player).init(trackType, tracks).dispatch();
        }

        @Override
        public void onTrackWillChange(@NonNull PlayerAdapter mp, @TrackType int trackType,
                                      @Nullable Track current, @NonNull Track target) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onTrackWillChange", mapTrackType(trackType), Track.dump(current), Track.dump(target));
            player.mDispatcher.obtain(InfoTrackWillChange.class, player).init(trackType, current, target).dispatch();
        }

        @Override
        public void onTrackChanged(@NonNull PlayerAdapter mp, @TrackType int trackType,
                                   @Nullable Track pre, @NonNull Track current) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onTrackChanged", mapTrackType(trackType), Track.dump(pre), Track.dump(current));

            if (player.isCompleted() && !player.isSupportSmoothTrackSwitching(trackType)) {
                player.start();
            }
            player.mDispatcher.obtain(InfoTrackChanged.class, player).init(trackType, pre, current).dispatch();
        }

        @Override
        public void onSubtitleStateChanged(@NonNull PlayerAdapter mp, boolean enabled) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onSubtitleStateChanged", enabled);

            player.mDispatcher.obtain(InfoSubtitleStateChanged.class, player).init(enabled).dispatch();
        }

        @Override
        public void onSubtitleInfoReady(@NonNull PlayerAdapter mp, List<Subtitle> subtitles) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onSubtitleInfoReady", Subtitle.dump(subtitles));

            player.mDispatcher.obtain(InfoSubtitleInfoReady.class, player).init(subtitles).dispatch();
        }

        @Override
        public void onSubtitleFileLoadFinish(@NonNull PlayerAdapter mp, int success, String info) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onSubtitleFileLoadFinish", success, info);

            player.mDispatcher.obtain(InfoSubtitleFileLoadFinish.class, player).init(success, info).dispatch();
        }

        @Override
        public void onSubtitleWillChange(@NonNull PlayerAdapter mp, Subtitle current, Subtitle target) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onSubtitleWillChange", Subtitle.dump(current), Subtitle.dump(target));

            player.mDispatcher.obtain(InfoSubtitleWillChange.class, player).init(current, target).dispatch();
        }

        @Override
        public void onSubtitleChanged(@NonNull PlayerAdapter mp, Subtitle pre, Subtitle current) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.d(player, "onSubtitleChanged", Subtitle.dump(pre), Subtitle.dump(current));

            player.mDispatcher.obtain(InfoSubtitleChanged.class, player).init(pre, current).dispatch();
        }

        @Override
        public void onSubtitleTextUpdate(@NonNull PlayerAdapter mp, @NonNull SubtitleText subtitleText) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.v(player, "onSubtitleTextUpdate", SubtitleText.dump(subtitleText));

            player.mDispatcher.obtain(InfoSubtitleTextUpdate.class, player).init(subtitleText).dispatch();
        }

        @Override
        public void onSubtitleCacheHint(@NonNull PlayerAdapter mp, long cacheSize) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            L.v(player, "onSubtitleCacheHint", cacheSize);

            player.mDispatcher.obtain(InfoSubtitleCacheUpdate.class, player).init(cacheSize).dispatch();
        }

        @Override
        public void onFrameInfoUpdate(@NonNull PlayerAdapter mp, int frameType, long pts, long clockTime) {
            final AVPlayer player = mPlayerRef.get();
            if (player == null) return;

            // L.v(player, "onFrameInfoUpdate", frameType, pts, clockTime);

            player.mDispatcher.obtain(InfoFrameInfoUpdate.class, player).init(frameType, pts, clockTime).dispatch();
        }
    }

    private void moveToErrorState(PlayerException e) {
        L.e(this, "moveToErrorState", e);
        recordProgress();
        mPlayerException = e;
        setState(STATE_ERROR);
        mDispatcher.obtain(StateError.class, this).init(e).dispatch();
    }

    @Override
    public String getType() {
        return mPlayerType;
    }

    @Override
    public void addPlayerListener(@NonNull Dispatcher.EventListener listener) {
        mDispatcher.addEventListener(listener);
    }

    @Override
    public void removePlayerListener(@NonNull Dispatcher.EventListener listener) {
        mDispatcher.removeEventListener(listener);
    }

    @Override
    public void removeAllPlayerListener() {
        mDispatcher.removeAllEventListener();
    }

    @Override
    public void setSurface(@Nullable Surface surface) {
        if (checkIsRelease("setSurface")) return;

        L.d(this, "setSurface", mSurface, surface);
        mDispatcher.obtain(ActionSetSurface.class, this).init(surface).dispatch();
        mSurface = surface;
        mPlayer.setSurface(surface);
    }

    @Nullable
    @Override
    public Surface getSurface() {
        return mSurface;
    }

    @Override
    public void setVideoScalingMode(@ScalingMode int scalingMode) throws IllegalArgumentException {
        if (checkIsRelease("setVideoScalingMode")) return;

        Asserts.checkOneOf(scalingMode, SCALING_MODE_DEFAULT, SCALING_MODE_ASPECT_FIT, SCALING_MODE_ASPECT_FILL);

        if (mVideoScalingMode != scalingMode) {
            L.d(this, "setVideoScalingMode",
                    Player.mapScalingMode(mVideoScalingMode), Player.mapScalingMode(scalingMode));
            this.mVideoScalingMode = scalingMode;
            mPlayer.setVideoScalingMode(scalingMode);
        }
    }

    @Override
    public int getVideoScalingMode() {
        return mVideoScalingMode;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (checkIsRelease("setVolume")) return;

        mVolume[0] = leftVolume;
        mVolume[1] = rightVolume;
        mPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public float[] getVolume() {
        if (checkIsRelease("getVolume")) return mVolume;

        return mPlayer.getVolume();
    }

    @Override
    public void setMuted(boolean muted) {
        if (checkIsRelease("setMute")) return;

        mPlayer.setMuted(muted);
    }

    @Override
    public boolean isMuted() {
        if (checkIsRelease("isMuted")) return false;

        return mPlayer.isMuted();
    }

    @Override
    public void prepare(@NonNull MediaSource source) throws IllegalStateException {
        if (checkIsRelease("prepare")) return;

        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);

        L.d(this, "prepare", MediaSource.dump(source), isStartWhenPrepared());

        mDispatcher.obtain(ActionPrepare.class, this).init(source).dispatch();

        mMediaSource = source;
        setState(STATE_PREPARING);

        try {
            handleSourceSet(source);
        } catch (IllegalStateException | IOException e) {
            moveToErrorState(new PlayerException(PlayerException.CODE_ERROR_ACTION, "setDataSource", e));
            return;
        }

        try {
            mPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            moveToErrorState(new PlayerException(PlayerException.CODE_ERROR_ACTION, "prepareAsync", e));
            return;
        }

        mDispatcher.obtain(StatePreparing.class, this).dispatch();
    }

    private void handleSourceSet(MediaSource source) throws IOException {
        mPlayer.setDataSource(source);

        // set start time inner
        if (mStartTime <= 0) {
            long recordPosition = ProgressRecorder.getProgress(source.getSyncProgressId());
            if (recordPosition > 0) {
                L.d(this, "handleSourceSet", "restore start time", recordPosition);
                mStartTime = recordPosition;
                mPlayer.setStartTime(recordPosition);
            }
        } else {
            mPlayer.setStartTime(mStartTime);
        }
    }

    @Nullable
    @Override
    public MediaSource getDataSource() {
        return mMediaSource;
    }

    @Override
    public void setStartWhenPrepared(boolean startWhenPrepared) {
        final boolean mStartWhenPrepared = isStartWhenPrepared();
        if (mStartWhenPrepared != startWhenPrepared) {
            L.d(this, "setStartWhenPrepared", mStartWhenPrepared, startWhenPrepared);
            mPlayer.setStartWhenPrepared(startWhenPrepared);
            if (isPrepared() && startWhenPrepared) {
                start();
            }
        }
    }

    @Override
    public boolean isStartWhenPrepared() {
        return mPlayer.isStartWhenPrepared();
    }

    @Nullable
    @Override
    public Track getCurrentTrack(@TrackType int trackType) {
        if (checkIsRelease("getCurrentTrack")) return null;

        Asserts.checkOneOf(trackType, TRACK_TYPE_VIDEO, TRACK_TYPE_AUDIO);
        return mPlayer.getCurrentTrack(trackType);
    }

    @Nullable
    @Override
    public Track getPendingTrack(@TrackType int trackType) {
        if (checkIsRelease("getPendingTrack")) return null;

        Asserts.checkOneOf(trackType, TRACK_TYPE_VIDEO, TRACK_TYPE_AUDIO);
        return mPlayer.getPendingTrack(trackType);
    }

    @Nullable
    @Override
    public Track getSelectedTrack(@TrackType int trackType) {
        if (checkIsRelease("getSelectedTrack")) return null;

        Asserts.checkOneOf(trackType, TRACK_TYPE_VIDEO, TRACK_TYPE_AUDIO);
        return mPlayer.getSelectedTrack(trackType);
    }

    @Nullable
    @Override
    public List<Track> getTracks(@TrackType int trackType) {
        if (checkIsRelease("getTracks")) return null;

        Asserts.checkOneOf(trackType, TRACK_TYPE_VIDEO, TRACK_TYPE_AUDIO);
        return mPlayer.getTracks(trackType);
    }

    @Override
    public void selectTrack(@Nullable Track track) {
        if (track == null) return; // TODO AUTO
        selectTrack(track.getTrackType(), track);
    }

    @Override
    public void selectTrack(@TrackType int trackType, @Nullable Track track)
            throws UnsupportedOperationException {
        if (checkIsRelease("selectTrack")) return;

        Asserts.checkOneOf(trackType, TRACK_TYPE_VIDEO, TRACK_TYPE_AUDIO);
        final Track selected = getSelectedTrack(trackType);
        final Track target = track;
        L.d(this, "selectTrack", mapTrackType(trackType), "selected: " +
                Track.dump(selected), "target: " + Track.dump(target));
        if (target == null || Objects.equals(target, selected)) return;
        mPlayer.selectTrack(trackType, target);
    }

    @Nullable
    @Override
    public List<Subtitle> getSubtitles() {
        return mPlayer.getSubtitles();
    }

    @Override
    public void selectSubtitle(@Nullable Subtitle subtitle) {
        final Subtitle selected = mPlayer.getSelectedSubtitle();
        L.d(this, "selectSubtitle", "selected:" + Subtitle.dump(selected),
                "target:" + Subtitle.dump(subtitle));
        if (subtitle == null || Objects.equals(selected, subtitle)) return;
        mPlayer.selectSubtitle(subtitle);
    }

    @Nullable
    @Override
    public Subtitle getSelectedSubtitle() {
        return mPlayer.getSelectedSubtitle();
    }

    @Nullable
    @Override
    public Subtitle getPendingSubtitle() {
        return mPlayer.getPendingSubtitle();
    }

    @Nullable
    @Override
    public Subtitle getCurrentSubtitle() {
        return mPlayer.getCurrentSubtitle();
    }

    @Override
    public boolean isSupportSmoothTrackSwitching(@TrackType int trackType) {
        return mPlayer.isSupportSmoothTrackSwitching(trackType);
    }

    @Override
    public void setStartTime(long startTime) throws IllegalStateException {
        if (checkIsRelease("setStartTime")) return;

        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);

        L.d(this, "setStartTime", startTime);
        if (startTime >= 0) {
            mStartTime = startTime;
        }
    }

    @Override
    public long getStartTime() {
        return mStartTime;
    }

    @Override
    public void seekTo(long seekTo) {
        if (checkIsRelease("seekTo")) return;

        Asserts.checkState(getState(), Player.STATE_PREPARED, Player.STATE_STARTED,
                Player.STATE_PAUSED, Player.STATE_COMPLETED);
        final long from = getCurrentPosition();
        final long duration = getDuration();
        seekTo = Math.min(seekTo, duration);
        L.d(this, "seekTo", from, seekTo, duration, mSeekable ? "seekable" : "not seekable");
        if (!mSeekable) return;

        mDispatcher.obtain(ActionSeekTo.class, this).init(from, seekTo).dispatch();
        try {
            mPlayer.seekTo(seekTo);
        } catch (IllegalStateException e) {
            moveToErrorState(new PlayerException(PlayerException.CODE_ERROR_ACTION, "seekTo", e));
            return;
        }
        mDispatcher.obtain(InfoSeekingStart.class, this).init(from, seekTo).dispatch();
    }

    @Override
    public void start() {
        if (checkIsRelease("start")) return;

        Asserts.checkState(getState(), Player.STATE_PREPARED, Player.STATE_STARTED,
                Player.STATE_PAUSED, Player.STATE_COMPLETED);

        if (isPlaying()) return;
        L.d(this, "start");

        mDispatcher.obtain(ActionStart.class, this).dispatch();
        try {
            mPlayer.start();
        } catch (IllegalStateException e) {
            moveToErrorState(new PlayerException(PlayerException.CODE_ERROR_ACTION, "start", e));
            return;
        }
        setState(STATE_STARTED);
        mDispatcher.obtain(StateStarted.class, this).dispatch();
    }

    @Override
    public void pause() throws IllegalStateException {
        if (checkIsRelease("pause")) return;

        Asserts.checkState(getState(), Player.STATE_PREPARED, Player.STATE_STARTED,
                Player.STATE_PAUSED, Player.STATE_COMPLETED);

        if (isPaused()) return;
        L.d(this, "pause");

        mDispatcher.obtain(ActionPause.class, this).dispatch();
        recordProgress();
        try {
            mPlayer.pause();
        } catch (IllegalStateException e) {
            moveToErrorState(new PlayerException(PlayerException.CODE_ERROR_ACTION, "pause", e));
            return;
        }
        setState(STATE_PAUSED);
        mDispatcher.obtain(StatePaused.class, this).dispatch();
    }

    /*@Override
    public void stop() throws IllegalStateException {
        if (checkIsRelease("stop")) return;

        Asserts.checkState(getState(), Player.STATE_PREPARING, Player.STATE_PREPARED,
                Player.STATE_STARTED, Player.STATE_PAUSED, Player.STATE_COMPLETED, Player.STATE_STOPPED);

        L.d(this, "stop");

        mDispatcher.obtain(ActionStop.class, this).dispatch();
        recordProgress();
        try {
            mPlayer.stop();
        } catch (IllegalStateException e) {
            moveToErrorState(new PlayerException(PlayerException.CODE_ERROR_ACTION, "stop", e));
            return;
        }
        setState(STATE_STOPPED);
        mDispatcher.obtain(StateStopped.class, this).dispatch();
    }*/

    @Override
    public void reset() {
        if (checkIsRelease("reset")) return;

        L.d(this, "reset");
        recordProgress();
        mPlayer.reset();
        resetInner();
        setState(STATE_IDLE);
    }

    private void resetInner() {
        mSurface = null;
        mMediaSource = null;
        mStartTime = 0;
        mBufferPercentage = 0;
        mIsBuffering = false;
        mBufferIndex = -1;
        mSeekable = true;
        mVideoSampleAspectRatio = 0;
        mLooping = false;
        clearExtras();
    }

    @Override
    public void release() {
        if (checkIsRelease("release")) return;

        L.d(this, "release");
        mDispatcher.obtain(ActionRelease.class, this).dispatch();
        recordProgress();
        resetInner();
        mPlayer.setListener(null);
        mPlayer.release();
        mPlayer = null;
        setState(STATE_RELEASED);
        mDispatcher.obtain(StateReleased.class, this).dispatch();
        mDispatcher.release();
    }

    @Override
    public long getDuration() {
        if (checkIsRelease("getDuration")) return 0L;

        final int state = getState();
        switch (state) {
            case STATE_PREPARED:
            case STATE_STARTED:
            case STATE_PAUSED:
            case STATE_COMPLETED:
            case STATE_STOPPED:
                return mPlayer.getDuration();
            default:
                return 0;
        }
    }

    @Override
    public long getCurrentPosition() {
        if (checkIsRelease("getCurrentPosition")) return 0L;

        final int state = getState();
        switch (state) {
            case STATE_IDLE:
            case STATE_PREPARING:
                return mStartTime > 0 ? mStartTime : 0;
            case STATE_PREPARED:
                return mPlayer.getCurrentPosition();
            case STATE_ERROR:
                final MediaSource mediaSource = mMediaSource;
                if (mediaSource != null) {
                    return ProgressRecorder.getProgress(mediaSource.getSyncProgressId());
                } else {
                    return 0L;
                }
        }
        return mPlayer.getCurrentPosition();
    }

    @IntRange(from = 0, to = 100)
    @Override
    public int getBufferedPercentage() {
        if (checkIsRelease("getBufferPercentage")) return 0;

        return mPlayer.getBufferedPercentage();
    }

    @Override
    public long getBufferedDuration() {
        if (checkIsRelease("getBufferedDuration")) return 0;

        return mPlayer.getBufferedDuration();
    }

    @Override
    public int getVideoWidth() {
        if (checkIsRelease("getVideoWidth")) return 0;

        return mPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        if (checkIsRelease("getVideoHeight")) return 0;

        return mPlayer.getVideoHeight();
    }

    @Override
    public float getVideoSampleAspectRatio() {
        return mVideoSampleAspectRatio;
    }

    @Override
    public void setLooping(boolean looping) {
        if (isLooping() == looping) return;

        L.d(this, "setLooping", isLooping(), looping);
        mLooping = looping;
        mDispatcher.obtain(ActionSetLooping.class, this).init(looping).dispatch();
    }

    @Override
    public boolean isLooping() {
        return mLooping;
    }

    @Override
    public void setSpeed(float speed) {
        if (checkIsRelease("setSpeed")) return;
        if (speed == getSpeed()) return;

        L.d(this, "setSpeed", getSpeed(), speed);
        mDispatcher.obtain(ActionSetSpeed.class, this).init(speed).dispatch();
        mPlayer.setSpeed(speed);
    }

    @Override
    public void setAudioPitch(float audioPitch) {
        if (checkIsRelease("setAudioPitch")) return;
        if (audioPitch == getAudioPitch()) return;

        L.d(this, "setAudioPitch", getAudioPitch(), audioPitch);
        mPlayer.setAudioPitch(audioPitch);
    }

    @Override
    public float getSpeed() {
        if (checkIsRelease("getSpeed")) return -1;
        return mPlayer.getSpeed();
    }

    @Override
    public float getAudioPitch() {
        if (checkIsRelease("getAudioPitch")) return -1;
        return mPlayer.getAudioPitch();
    }

    @Override
    public void setAudioSessionId(int audioSessionId) {
        if (checkIsRelease("setAudioSessionId")) return;

        L.d(this, "setAudioPitch", getAudioSessionId(), audioSessionId);
        mPlayer.setAudioSessionId(audioSessionId);
    }

    @Override
    public int getAudioSessionId() {
        if (checkIsRelease("getAudioSessionId")) return -1;

        return mPlayer.getAudioSessionId();
    }

    @Override
    public void setSuperResolutionEnabled(boolean enabled) {

        if (checkIsRelease("setSuperResolutionEnabled")) return;
        L.d(this, "setSuperResolutionEnabled", isSuperResolutionEnabled(), enabled);
        mPlayer.setSuperResolutionEnabled(enabled);
    }

    @Override
    public boolean isSuperResolutionEnabled() {
        if (checkIsRelease("isSuperResolutionEnabled")) return false;

        return mPlayer.isSuperResolutionEnabled();
    }

    @Override
    public void setSubtitleEnabled(boolean enabled) {
        if (checkIsRelease("setSubtitleEnabled")) return;

        mPlayer.setSubtitleEnabled(enabled);
    }

    @Override
    public boolean isSubtitleEnabled() {
        if (checkIsRelease("isSubtitleEnabled")) return false;

        return mPlayer.isSubtitleEnabled();
    }

    @Override
    public int getVideoDecoderType() {
        if (checkIsRelease("getVideoDecoderType")) return Player.DECODER_TYPE_UNKNOWN;
        return mPlayer.getVideoDecoderType();
    }

    @Override
    public int getVideoCodecId() {
        if (checkIsRelease("getVideoCodecId")) return Player.CODEC_ID_UNKNOWN;
        return mPlayer.getVideoCodecId();
    }

    @Override
    public boolean isBuffering() {
        return mIsBuffering;
    }

    @PlayerState
    @Override
    public int getState() {
        return mState;
    }

    private void setState(@PlayerState int newState) {
        int state;
        synchronized (this) {
            state = this.mState;
            this.mState = newState;
        }
        L.d(this, "setState", mapState(state), mapState(newState));
    }

    @Override
    public boolean isIDLE() {
        synchronized (this) {
            return mState == STATE_IDLE;
        }
    }

    @Override
    public boolean isPreparing() {
        synchronized (this) {
            return mState == STATE_PREPARING;
        }
    }

    @Override
    public boolean isPrepared() {
        synchronized (this) {
            return mState == STATE_PREPARED;
        }
    }

    @Override
    public boolean isPlaying() {
        synchronized (this) {
            return mState == STATE_STARTED;
        }
    }

    @Override
    public boolean isPaused() {
        synchronized (this) {
            return mState == STATE_PAUSED;
        }
    }

    @Override
    public boolean isCompleted() {
        synchronized (this) {
            return mState == STATE_COMPLETED;
        }
    }

    @Override
    public boolean isStopped() {
        synchronized (this) {
            return mState == STATE_STOPPED;
        }
    }

    @Override
    public boolean isReleased() {
        synchronized (this) {
            return mState == STATE_RELEASED;
        }
    }

    @Override
    public boolean isError() {
        synchronized (this) {
            return mState == STATE_ERROR;
        }
    }

    @Override
    public boolean isInPlaybackState() {
        switch (mState) {
            case STATE_PREPARED:
            case STATE_STARTED:
            case STATE_PAUSED:
            case STATE_COMPLETED:
                return true;
        }
        return false;
    }

    @Nullable
    @Override
    public PlayerException getPlayerException() {
        return mPlayerException;
    }

    private void notifyProgressUpdate(long currentPosition, long duration) {
        currentPosition = Math.max(currentPosition, 0);
        duration = Math.max(duration, 0);
        L.v(this, "notifyProgressUpdate", duration, currentPosition);
        mDispatcher.obtain(InfoProgressUpdate.class, this).init(currentPosition, duration).dispatch();
    }

    private boolean checkIsRelease(final String func) {
        if (isReleased()) {
            L.e(this, func, "already released!");
            return true;
        }
        return false;
    }

    @Override
    public String dump() {
        String playerInfo = mPlayer == null ? null : mPlayer.dump();
        return String.format("%s %s %s", L.obj2String(this), mapState(mState), playerInfo);
    }

    private void recordProgress() {
        final MediaSource mediaSource = mMediaSource;
        if (mediaSource == null) return;
        if (isInPlaybackState() && !isCompleted() || isError()) {
            long position = getCurrentPosition();
            long duration = getDuration();
            if (position > 1000 && duration > 0 && position < duration - 1000) {
                ProgressRecorder.recordProgress(mediaSource.getSyncProgressId(), position);
            }
        }
    }

    private void clearProgress() {
        final MediaSource mediaSource = mMediaSource;
        if (mediaSource == null) return;
        ProgressRecorder.removeProgress(mediaSource.getSyncProgressId());
    }
}
