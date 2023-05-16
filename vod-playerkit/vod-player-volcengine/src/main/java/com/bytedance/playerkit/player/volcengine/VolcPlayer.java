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

import static com.bytedance.playerkit.player.Player.mapState;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerException;
import com.bytedance.playerkit.player.adapter.PlayerAdapter;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.volcengine.utils.TTVideoEngineListenerAdapter;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;
import com.ss.ttm.player.PlaybackParams;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.VideoEngineInfos;
import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.Error;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

class VolcPlayer implements PlayerAdapter {
    private final Context mContext;

    private TTVideoEngine mPlayer;
    private boolean mPreRenderPlayer;

    private final ListenerAdapter mListenerAdapter;
    private Listener mListener;

    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    private boolean mStartWhenPrepared = true;
    private long mStartTime;
    private final PlaybackParams mPlaybackParams = new PlaybackParams();

    private MediaSource mMediaSource;
    private StrategySource mStrategySource;

    @Player.PlayerState
    private int mState;
    private boolean mPausedWhenChangeAVTrack;
    private long mPlaybackTimeWhenChangeAVTrack;

    private boolean mBuffering;

    private final SparseArray<Track> mSelectedTrack = new SparseArray<>();
    private final SparseArray<Track> mPendingTrack = new SparseArray<>();
    private final SparseArray<Track> mCurrentTrack = new SparseArray<>();


    public static String getDeviceId() {
        return TTVideoEngine.getDeviceID();
    }

    public static class Factory implements PlayerAdapter.Factory {
        private final Context mContext;
        private final MediaSource mMediaSource;

        public Factory(Context context, MediaSource mediaSource) {
            this.mContext = context;
            this.mMediaSource = mediaSource;
        }

        @Override
        public PlayerAdapter create(Looper eventLooper) {
            return new VolcPlayer(mContext, mMediaSource);
        }
    }

    static final class EngineParams {
        private final static WeakHashMap<TTVideoEngine, EngineParams> sPlayerParams = new WeakHashMap<>();
        boolean mPreRenderPlayer;
        boolean mSuperResolutionInitialized;
        int mVideoWidth;
        int mVideoHeight;
        Exception mPlayerException;

        synchronized static EngineParams get(TTVideoEngine engine) {
            EngineParams params = sPlayerParams.get(engine);
            if (params == null) {
                params = new EngineParams();
                sPlayerParams.put(engine, params);
            }
            return params;
        }

        synchronized static EngineParams remove(TTVideoEngine engine) {
            return sPlayerParams.remove(engine);
        }
    }

    private VolcPlayer(final Context context, MediaSource mediaSource) {
        L.d(this, "constructor", "DEVICE_ID", TTVideoEngine.getDeviceID());
        mContext = context;
        mListenerAdapter = new ListenerAdapter(this);
        TTVideoEngine player = VolcEngineStrategy.removePreRenderEngine(mediaSource);
        if (player == null) {
            player = TTVideoEngineFactory.Default.get().create(mContext, mediaSource);
            L.d(this, "constructor", "create", mPlayer, MediaSource.dump(mediaSource));
        } else {
            L.d(this, "constructor", "preRender", player, MediaSource.dump(mediaSource));
            EngineParams.get(player).mPreRenderPlayer = true;
            mPreRenderPlayer = true;
            mStartWhenPrepared = false;
        }
        bind(player);
        setState(Player.STATE_IDLE);
    }

    protected void bind(TTVideoEngine player) {
        L.d(this, "bind", player);
        player.setVideoEngineCallback(mListenerAdapter);
        player.setVideoEngineInfoListener(mListenerAdapter);
        player.setVideoInfoListener(mListenerAdapter);
        player.setPlayerEventListener(mListenerAdapter);
        mPlayer = player;
    }

    @Override
    public void setListener(Listener listener) {
        mListener = listener;
    }

    @Override
    public void setSurface(final Surface surface) {
        if (mSurface != surface) {
            mPlayer.setSurface(surface);
            mSurface = surface;
            mSurfaceHolder = null;
        } else if (surface != null) {
            refreshSurface();
        }
    }

    private void refreshSurface() {
        if (VolcConfig.get(mMediaSource).enableTextureRender) {
            if (isInPlaybackState()) {
                mPlayer.forceDraw();
            }
        } else {
            mPlayer.setSurface(null);
            mPlayer.setSurface(mSurface);
        }
    }

    @Override
    public void setDisplay(SurfaceHolder display) {
        if (mSurfaceHolder != display) {
            mPlayer.setSurfaceHolder(display);
            mSurfaceHolder = display;
            mSurface = null;
        }
    }

    @Override
    public void setVideoScalingMode(@Player.ScalingMode int mode) {
        final int scalingMode = Mapper.mapScalingMode(mode);
        mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_IMAGE_LAYOUT, scalingMode);
    }

    @Override
    public void setDataSource(@NonNull MediaSource mediaSource) throws IllegalStateException {
        L.d(this, "setDataSource", MediaSource.dump(mediaSource));
        mMediaSource = mediaSource;
    }

    private static void setHeaders(TTVideoEngine player, @Nullable Map<String, String> headers) {
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key != null && value != null) {
                    // compatible with ffmpeg
                    player.setCustomHeader(key, " " + value);
                }
            }
        }
    }

    @Override
    public boolean isSupportSmoothTrackSwitching(@Track.TrackType int trackType) {
        final MediaSource source = mMediaSource;
        final TTVideoEngine player = mPlayer;
        if (player != null && source != null) {
            final int contentType = source.getMediaProtocol();
            final IVideoModel videoModel = player.getIVideoModel();
            if (videoModel != null) {
                switch (contentType) {
                    case MediaSource.MEDIA_PROTOCOL_DASH:
                        if (videoModel.isSupportBash()) {
                            return VolcConfig.get(source).enableDash;
                        }
                        break;
                    case MediaSource.MEDIA_PROTOCOL_HLS:
                        if (videoModel.isSupportHLSSeamlessSwitch()) {
                            return VolcConfig.get(source).enableHlsSeamlessSwitch;
                        }
                        break;
                    case MediaSource.MEDIA_PROTOCOL_DEFAULT:
                        if (videoModel.isSupportBash()) {
                            return VolcConfig.get(source).enableMP4SeamlessSwitch;
                        }
                }
            }
            return false;
        }
        return false;
    }

    @Override
    public void selectTrack(@Track.TrackType int trackType, @Nullable Track track) throws IllegalStateException {
        if (isInState(Player.STATE_RELEASED, Player.STATE_ERROR)) return;

        final MediaSource mediaSource = mMediaSource;

        if (mediaSource == null) return;

        if (track == null) return; //TODO abr auto support

        final Track selected = getSelectedTrack(trackType);

        if (Objects.equals(selected, track)) {
            return;
        }

        setSelectedTrack(trackType, track);

        if (isInState(Player.STATE_IDLE, Player.STATE_STOPPED)) return;

        if (selected == null) return; // TODO not possible

        setPendingTrack(trackType, track);

        if (mListener != null) {
            mListener.onTrackWillChange(this, trackType, selected, track);
        }

        // Select Track during playback
        if (isInPlaybackState()) {
            mPausedWhenChangeAVTrack = isInState(Player.STATE_PAUSED);
            mPlaybackTimeWhenChangeAVTrack = isInState(Player.STATE_COMPLETED) ? 0 : mPlayer.getCurrentPlaybackTime();
        } else {
            mPlaybackTimeWhenChangeAVTrack = mStartTime;
        }

        final int sourceType = mediaSource.getSourceType();

        if (sourceType == MediaSource.SOURCE_TYPE_ID ||
                sourceType == MediaSource.SOURCE_TYPE_MODEL ||
                (sourceType == MediaSource.SOURCE_TYPE_URL && isSupportSmoothTrackSwitching(trackType))) {

            if ((sourceType == MediaSource.SOURCE_TYPE_ID || sourceType == MediaSource.SOURCE_TYPE_MODEL) &&
                    !isSupportSmoothTrackSwitching(trackType)) {
                setState(Player.STATE_PREPARING);
            }
            final Resolution resolution = Mapper.track2Resolution(track);
            if (resolution == null) return;

            // for VID/VideoModel TTVideoEngine will take care of startTime sync after
            // resolution change

            // mPlayer.setStartTime((int) mPlaybackTimeWhenChangeAVTrack);
            mPlayer.configResolution(resolution);
        } else {
            stop();

            final TTVideoEngine player = mPlayer;
            bind(player);
            player.setSurface(mSurface);
            player.setStartTime(mPlaybackTimeWhenChangeAVTrack > 0 ? (int) mPlaybackTimeWhenChangeAVTrack : 0);
            prepareDirectUrl(mediaSource, track, !mPausedWhenChangeAVTrack);
        }
    }

    @Override
    public Track getPendingTrack(@Track.TrackType int trackType) throws IllegalStateException {
        return mPendingTrack.get(trackType);
    }

    private Track removePendingTrack(@Track.TrackType int trackType) {
        final Track track = mPendingTrack.get(trackType);
        if (track != null) {
            mPendingTrack.remove(trackType);
        }
        return track;
    }

    private void setPendingTrack(@Track.TrackType int trackType, Track track) {
        L.d(this, "setPendingTrack", Track.dump(track));
        mPendingTrack.put(trackType, track);
    }

    @Override
    public Track getCurrentTrack(@Track.TrackType int trackType) {
        return mCurrentTrack.get(trackType);
    }

    private void setCurrentTrack(@Track.TrackType int trackType, Track track) {
        L.d(this, "setCurrentTrack", Track.dump(track));
        this.mCurrentTrack.put(trackType, track);
    }

    private Track removeCurrentTrack(@Track.TrackType int trackType) {
        final Track track = mCurrentTrack.get(trackType);
        L.d(this, "removeCurrentTrack", Track.dump(track));
        if (track != null) {
            mCurrentTrack.remove(trackType);
        }
        return track;
    }

    @Override
    public Track getSelectedTrack(@Track.TrackType int trackType) {
        return mSelectedTrack.get(trackType);
    }

    private void setSelectedTrack(@Track.TrackType int trackType, Track track) {
        L.d(this, "setSelectedTrack", Track.dump(track));
        mSelectedTrack.put(trackType, track);
    }

    @Override
    public List<Track> getTracks(@Track.TrackType int trackType) {
        return mMediaSource.getTracks(trackType);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);

        final MediaSource mediaSource = mMediaSource;
        if (mediaSource == null) {
            throw new IllegalStateException("You should invoke VolcPlayer#setDataSource(MediaSource) method first!", new NullPointerException("source == null"));
        }

        if (EngineParams.get(mPlayer).mPreRenderPlayer) {
            syncPreRenderState(mediaSource);
        } else {
            prepareAsync(mediaSource);
        }
    }

    private void syncPreRenderState(@NonNull MediaSource mediaSource) {
        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);
        setState(Player.STATE_PREPARING);
        L.d(this, "syncPreRenderState", MediaSource.dump(mediaSource));

        @MediaSource.SourceType final int sourceType = mediaSource.getSourceType();
        @Track.TrackType int trackType = MediaSource.mediaType2TrackType(mediaSource);
        if (sourceType == MediaSource.SOURCE_TYPE_ID || sourceType == MediaSource.SOURCE_TYPE_MODEL) {
            IVideoModel videoModel = mPlayer.getIVideoModel();
            if (videoModel != null) {
                Mapper.updateMediaSource(mediaSource, videoModel);
            }
            if (mListener != null) {
                mListener.onMediaSourceUpdated(this, MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_PLAY_INFO_FETCHED, mediaSource);
            }
        }

        final List<Track> tracks = mediaSource.getTracks(trackType);
        if (tracks != null && !tracks.isEmpty()) {

            if (mListener != null) {
                mListener.onTrackInfoReady(this, trackType, tracks);
            }
            if (trackType == Track.TRACK_TYPE_VIDEO) {
                Track track = null;
                switch (sourceType) {
                    case MediaSource.SOURCE_TYPE_ID:
                    case MediaSource.SOURCE_TYPE_MODEL: {
                        Resolution resolution = mPlayer.getCurrentResolution();
                        if (resolution != null) {
                            track = Mapper.findTrackWithResolution(tracks, resolution);
                        }
                        break;
                    }
                    case MediaSource.SOURCE_TYPE_URL: {
                        if (mPlayer.getIVideoModel() != null) {
                            Resolution resolution = mPlayer.getCurrentResolution();
                            if (resolution != null) {
                                track = Mapper.findTrackWithResolution(tracks, resolution);
                            }
                        } else {
                            DirectUrlSource directUrlSource = (DirectUrlSource) mPlayer.getStrategySource();
                            if (directUrlSource != null) {
                                track = Mapper.findTrackWithDirectUrlSource(mediaSource, tracks, directUrlSource, VolcPlayerInit.getCacheKeyFactory());
                            }
                        }
                        break;
                    }
                }

                if (track == null) return; // TODO throw error

                setPendingTrack(trackType, track);

                if (mListener != null) {
                    mListener.onTrackWillChange(this, trackType, getCurrentTrack(trackType), track);
                }
            } else {
                throw new IllegalArgumentException("Only support video track for now. " + Track.mapTrackType(trackType));
            }
        }

        final Track current = getCurrentTrack(trackType);
        final Track pending = removePendingTrack(trackType);

        if (pending != null) {
            String fileHash = VolcPlayerInit.getCacheKeyFactory().generateCacheKey(mMediaSource, pending);
            if (fileHash != null) {
                long cachedSize = TTVideoEngine.quickGetCacheFileSize(fileHash);
                if (mListener != null) {
                    mListener.onCacheHint(this, cachedSize);
                }
            }
            setCurrentTrack(trackType, pending);
            if (mListener != null) {
                mListener.onTrackChanged(this, trackType, current, pending);
                setState(Player.STATE_PREPARED);
                mListener.onPrepared(this);
                mListener.onVideoSizeChanged(this, mPlayer.getVideoWidth(), mPlayer.getVideoHeight());
            }
        }
    }

    private void prepareAsync(@NonNull MediaSource mediaSource) {
        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);
        L.d(this, "prepareAsync", MediaSource.dump(mediaSource));

        @MediaSource.SourceType final int sourceType = mediaSource.getSourceType();
        switch (sourceType) {
            case MediaSource.SOURCE_TYPE_ID:
                prepareVid(mediaSource);
                break;
            case MediaSource.SOURCE_TYPE_MODEL:
                prepareVideoModel(mediaSource);
                break;
            case MediaSource.SOURCE_TYPE_URL:
                if (Mapper.isDirectUrlSeamlessSwitchEnabled(mediaSource)) {
                    prepareVideoModel(mediaSource);
                } else {
                    prepareDirectUrl(mediaSource);
                }
                break;
            default:
                throw new IllegalArgumentException("unsupported sourceType " + sourceType);
        }
    }

    private void prepareVid(@NonNull MediaSource mediaSource) {
        L.d(this, "prepareVid", MediaSource.dump(mediaSource));

        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);
        setState(Player.STATE_PREPARING);

        mStrategySource = Mapper.mediaSource2VidPlayAuthTokenSource(mediaSource);
        mPlayer.setStrategySource(mStrategySource);
        /** config in {@link  #prepareVid(MediaSource, Track)} which invoked in
         * {@link  TTVideoEngineListenerAdapter#onFetchedVideoInfo(VideoModel)} */
        preparePlayer(mPlayer, mStartWhenPrepared);
    }

    private void prepareDirectUrl(@NonNull MediaSource mediaSource) {
        L.d(this, "prepareDirectUrl", MediaSource.dump(mediaSource));
        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);
        setState(Player.STATE_PREPARING);

        final Track playTrack = selectPlayTrack(mediaSource);
        if (playTrack != null) {
            prepareDirectUrl(mediaSource, playTrack, isStartWhenPrepared());
        } else {
            moveToErrorState(PlayerException.CODE_TRACK_SELECT_ERROR, new Exception("Select Track return null!"));
        }
    }

    private void prepareVideoModel(@NonNull MediaSource mediaSource) {
        L.d(this, "prepareVideoModel", MediaSource.dump(mediaSource));

        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);
        setState(Player.STATE_PREPARING);

        Mapper.updateVideoModelMediaSource(mediaSource);
        final Track playTrack = selectPlayTrack(mediaSource);
        if (playTrack != null) {
            prepareVideoModel(mediaSource, playTrack, isStartWhenPrepared());
        } else {
            moveToErrorState(PlayerException.CODE_TRACK_SELECT_ERROR, new Exception("Select Track return null!"));
        }
    }

    @Nullable
    private Track selectPlayTrack(@NonNull MediaSource mediaSource) {
        @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);
        final List<Track> tracks = mediaSource.getTracks(trackType);
        if (tracks != null && !tracks.isEmpty()) {
            if (mListener != null) {
                mListener.onTrackInfoReady(this, trackType, tracks);
            }
            Track selected = getSelectedTrack(trackType);

            if (selected == null) {
                selected = VolcPlayerInit.getTrackSelector().selectTrack(TrackSelector.TYPE_PLAY, trackType, tracks, mediaSource);
                setPendingTrack(trackType, selected);
                if (mListener != null) {
                    mListener.onTrackWillChange(this, trackType, null, selected);
                }
            } else {
                setPendingTrack(trackType, selected);
            }
            return selected;
        }
        return null;
    }

    private void prepareDirectUrl(MediaSource mediaSource, Track track, boolean startWhenPrepared) {
        if (isInState(Player.STATE_IDLE, Player.STATE_STOPPED)) {
            setState(Player.STATE_PREPARING);
        }
        mStrategySource = Mapper.mediaSource2DirectUrlSource(
                mediaSource,
                track,
                VolcPlayerInit.getCacheKeyFactory());
        mPlayer.setStrategySource(mStrategySource);
        config(mContext, mPlayer, mediaSource, track);
        preparePlayer(mPlayer, startWhenPrepared);
    }

    private void prepareVideoModel(MediaSource mediaSource, @NonNull Track track, boolean startWhenPrepared) {
        mStrategySource = Mapper.mediaSource2VideoModelSource(
                mediaSource,
                track,
                VolcPlayerInit.getCacheKeyFactory()
        );
        mPlayer.setStrategySource(mStrategySource);
        config(mContext, mPlayer, mediaSource, track);
        preparePlayer(mPlayer, startWhenPrepared);
    }

    static void preparePlayer(TTVideoEngine player, boolean startWhenPrepared) {
        if (startWhenPrepared) {
            player.play();
        } else {
            player.prepare();
        }
    }

    static void config(Context context, TTVideoEngine player, MediaSource mediaSource, @Nullable Track track) {
        if (track != null) {
            player.configResolution(Mapper.track2Resolution(track));
        }

        final Map<String, String> headers = Mapper.findHeaders(mediaSource, track);
        if (headers != null) {
            setHeaders(player, headers);
        }

        VolcSuperResolutionStrategy.initSuperResolution(context, player, mediaSource, track);
    }

    @Override
    public void start() {
        Asserts.checkState(getState(), Player.STATE_PREPARED, Player.STATE_STARTED,
                Player.STATE_PAUSED, Player.STATE_COMPLETED);

        if (isInState(Player.STATE_STARTED)) {
            return;
        }

        if (isInState(Player.STATE_PREPARED)) {
            if (!mStartWhenPrepared || EngineParams.get(mPlayer).mPreRenderPlayer) {
                L.d(this, "start");
                mPlayer.play();
            }
        } else {
            L.d(this, "start");
            mPlayer.play();
        }
        setState(Player.STATE_STARTED);
    }

    @Override
    public boolean isPlaying() {
        synchronized (this) {
            return mState == Player.STATE_STARTED;
        }
    }

    @Override
    public void pause() throws IllegalStateException {
        Asserts.checkState(getState(), Player.STATE_PREPARING, Player.STATE_PREPARED, Player.STATE_STARTED,
                Player.STATE_PAUSED, Player.STATE_COMPLETED);

        if (isInState(Player.STATE_PAUSED)) return;

        mPlayer.pause();
        setState(Player.STATE_PAUSED);
    }

    @Override
    public void stop() throws IllegalStateException {
        Asserts.checkState(getState(), Player.STATE_PREPARING, Player.STATE_PREPARED,
                Player.STATE_STARTED, Player.STATE_PAUSED, Player.STATE_COMPLETED, Player.STATE_STOPPED);

        if (isInState(Player.STATE_STOPPED)) return;

        mPlayer.stop();
        mBuffering = false;
        setState(Player.STATE_STOPPED);
    }

    @Override
    public void setStartTime(long startTime) {
        mStartTime = startTime;
        if (!EngineParams.get(mPlayer).mPreRenderPlayer) {
            mPlayer.setStartTime((int) startTime);
        }
    }

    @Override
    public void setStartWhenPrepared(boolean startWhenPrepared) {
        if (mStartWhenPrepared != startWhenPrepared) {
            mStartWhenPrepared = startWhenPrepared;
        }
    }

    @Override
    public boolean isStartWhenPrepared() {
        return mStartWhenPrepared;
    }

    @Override
    public long getStartTime() {
        return mStartTime;
    }

    @Override
    public void seekTo(final long seekTo) {
        mPlayer.seekTo((int) seekTo, mListenerAdapter);
    }

    @Override
    public void reset() {
        if (isInState(Player.STATE_IDLE)) return;

        L.e(this, "reset", "unsupported reset method, stop instead");
        resetInner();
        if (mState != Player.STATE_STOPPED) {
            mPlayer.stop();
        }
        setState(Player.STATE_IDLE);
    }

    private void resetSource() {
        L.d(this, "resetSource", MediaSource.dump(mMediaSource));
        mMediaSource = null;
        mStrategySource = null;
        mCurrentTrack.clear();
        mPendingTrack.clear();
        mSelectedTrack.clear();

        mPreRenderPlayer = false;
        mPausedWhenChangeAVTrack = false;
        mPlaybackTimeWhenChangeAVTrack = 0L;
    }

    private void resetInner() {
        resetSource();

        mListener = null;
        mSurface = null;
        mSurfaceHolder = null;

        mStartWhenPrepared = true;
        mStartTime = 0L;
        mPlaybackParams.setSpeed(-1);
        mPlaybackParams.setPitch(-1);

        mVolume[0] = 1f;
        mVolume[1] = 1f;

        mBuffering = false;
        EngineParams.remove(mPlayer);
    }

    @Override
    public void release() {
        if (isInState(Player.STATE_RELEASED)) {
            return;
        }
        final MediaSource mediaSource = mMediaSource;
        resetInner();
        L.d(this, "release", mPlayer, MediaSource.dump(mediaSource));
        mPlayer.setIsMute(true);
        mPlayer.releaseAsync();
        setState(Player.STATE_RELEASED);
    }

    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            return Math.max(0, mPlayer.getDuration());
        }
        return 0L;
    }

    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            return Math.max(0, mPlayer.getCurrentPlaybackTime());
        }
        return 0L;
    }

    @Override
    public int getBufferedPercentage() {
        return mPlayer.getLoadedProgress();
    }

    @Override
    public long getBufferedDuration() {
        long videoBufferedDuration = getBufferedDuration(Track.TRACK_TYPE_VIDEO);
        long audioBufferedDuration = getBufferedDuration(Track.TRACK_TYPE_AUDIO);
        return Math.min(videoBufferedDuration, audioBufferedDuration);
    }

    @Override
    public long getBufferedDuration(@Track.TrackType int trackType) {
        switch (trackType) {
            case Track.TRACK_TYPE_AUDIO:
                return mPlayer.getLongOption(TTVideoEngine.PLAYER_OPTION_GET_AUDIO_CACHE_DURATION);
            case Track.TRACK_TYPE_VIDEO:
                return mPlayer.getLongOption(TTVideoEngine.PLAYER_OPTION_GET_VIDEO_CACHE_DURATION);
            case Track.TRACK_TYPE_UNKNOWN:
                return 0L;
            default:
                throw new IllegalArgumentException("Unsupported trackType " + trackType);
        }
    }

    @Override
    public int getVideoWidth() {
        if (isSupportSmoothTrackSwitching(Track.TRACK_TYPE_VIDEO)) {
            // Opt video TTVideoEngine#getVideoWidth/Height is not change after resolution changed
            // when using seamless video switching
            final EngineParams params = EngineParams.get(mPlayer);
            if (params.mVideoWidth > 0) {
                return params.mVideoWidth;
            }
        }
        return mPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        if (isSupportSmoothTrackSwitching(Track.TRACK_TYPE_VIDEO)) {
            // Opt video TTVideoEngine#getVideoWidth/Height is not change after resolution changed
            // when using seamless video switching
            final EngineParams params = EngineParams.get(mPlayer);
            if (params.mVideoHeight > 0) {
                return params.mVideoHeight;
            }
        }
        return mPlayer.getVideoHeight();
    }

    @Override
    public void setLooping(final boolean looping) {
        mPlayer.setLooping(looping);
    }

    @Override
    public boolean isLooping() {
        return mPlayer.isLooping();
    }

    private final float[] mVolume = new float[]{1f, 1f};

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        float left;
        float right;
        if (VolcConfig.get(mMediaSource).enableAudioTrackVolume) {
            left = leftVolume;
            right = rightVolume;
        } else {
            float maxVolume = mPlayer.getMaxVolume();
            left = maxVolume * leftVolume;
            right = maxVolume * rightVolume;
        }
        mPlayer.setVolume(left, right);
        mVolume[0] = left;
        mVolume[1] = right;
    }

    @Override
    public float[] getVolume() {
        if (!VolcConfig.get(mMediaSource).enableAudioTrackVolume && mPlayer != null) {
            float maxVolume = mPlayer.getMaxVolume();
            float volume = mPlayer.getVolume();
            if (volume >= 0 && volume <= maxVolume) {
                volume = volume / maxVolume;
            }
            mVolume[0] = volume;
            mVolume[1] = volume;
        }
        return mVolume;
    }

    @Override
    public void setMuted(boolean muted) {
        mPlayer.setIsMute(muted);
    }

    @Override
    public boolean isMuted() {
        return mPlayer.isMute();
    }

    @Override
    public void setSpeed(float speed) {
        mPlaybackParams.setSpeed(speed);
        mPlayer.setPlaybackParams(mPlaybackParams);
    }

    @Override
    public float getSpeed() {
        float speed = mPlaybackParams.getSpeed();
        return speed == -1f ? 1f : speed;
    }

    @Override
    public void setAudioPitch(float audioPitch) {
        mPlaybackParams.setPitch(audioPitch);
        mPlayer.setPlaybackParams(mPlaybackParams);
    }

    @Override
    public float getAudioPitch() {
        return mPlaybackParams.getPitch();
    }

    @Override
    public void setAudioSessionId(int audioSessionId) {
        mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_AUDIOTRACK_SESSIONID, audioSessionId);
    }

    @Override
    public int getAudioSessionId() {
        return mPlayer.getIntOption(TTVideoEngine.PLAYER_OPTION_AUDIOTRACK_SESSIONID);
    }

    /**
     * Set super resolution open/close during playback
     *
     * @param enabled true open SR, false close SR
     */
    @Override
    public void setSuperResolutionEnabled(boolean enabled) {
        VolcSuperResolutionStrategy.setEnabled(mPlayer, enabled);
    }

    @Override
    public boolean isSuperResolutionEnabled() {
        return VolcSuperResolutionStrategy.isEnabled(mPlayer);
    }

    @Override
    public String dump() {
        return L.obj2String(this)
                + " " + resolvePlayerDecoderType(mPlayer)
                + " " + VolcEditions.dumpEngineCoreType(mPlayer)
                + (mPreRenderPlayer ? " pre" : "");
    }

    @SuppressLint("SwitchIntDef")
    private boolean isInPlaybackState() {
        return isInState(Player.STATE_PREPARED,
                Player.STATE_STARTED,
                Player.STATE_PAUSED,
                Player.STATE_COMPLETED);
    }

    private boolean isInState(int... states) {
        synchronized (this) {
            for (int state : states) {
                if (mState == state) {
                    return true;
                }
            }
            return false;
        }
    }

    @Player.PlayerState
    private int getState() {
        synchronized (this) {
            return mState;
        }
    }

    private void setState(@Player.PlayerState int newState) {
        int state;
        synchronized (this) {
            state = this.mState;
            this.mState = newState;
        }
        L.d(this, "setState", mapState(state), mapState(newState));
    }

    private void moveToErrorState(int code, Exception e) {
        L.e(this, "moveToErrorState", e, code);
        EngineParams.get(mPlayer).mPlayerException = e;
        setState(Player.STATE_ERROR);
        if (mListener != null) {
            mListener.onError(this, code, String.valueOf(e));
        }
    }

    private static class ListenerAdapter extends TTVideoEngineListenerAdapter {

        private final WeakReference<VolcPlayer> mPlayerRef;

        ListenerAdapter(VolcPlayer player) {
            this.mPlayerRef = new WeakReference<>(player);
        }

        @Override
        public void onPrepared(final TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;

            Listener listener = player.mListener;
            if (listener == null) return;

            MediaSource mediaSource = player.mMediaSource;
            if (mediaSource == null) return;

            if (player.mState != Player.STATE_PREPARING) return;

            player.setState(Player.STATE_PREPARED);

            final String enginePlayerType = VolcEditions.dumpEngineCoreType(engine);
            L.d(player, "onPrepared", "enginePlayerType", engine, enginePlayerType);

            @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);

            //TODO dash abr
            final Track current = player.getCurrentTrack(trackType);
            final Track pending = player.removePendingTrack(trackType);

            if (pending == null) {
                return;
            }

            player.setCurrentTrack(trackType, pending);
            listener.onTrackChanged(player, trackType, current, pending);
            if (player.isSupportSmoothTrackSwitching(trackType)) {
                listener.onPrepared(player);
            } else {
                if (current == null) {
                    // 首次启播 prepare 完成
                    listener.onPrepared(player);
                } else {
                    // 切换清晰度 prepare 完成
                    if (player.mPausedWhenChangeAVTrack) {
                        player.mPausedWhenChangeAVTrack = false;
                        player.pause();
                    } else {
                        player.start();
                    }
                }
            }
        }

        @Override
        public void onBufferStart(final int reason, final int afterFirstFrame, final int action) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            if (VolcConfigGlobal.ENABLE_BUFFER_START_MSG_OPT) {
                if (!player.isPlaying()) {
                    L.w(player, "onBufferStart", "msg blocked", "reason",
                            "state not playing", mapState(player.getState()));
                    return;
                }
            }

            player.mBuffering = true;
            listener.onInfo(player, Info.MEDIA_INFO_BUFFERING_START, 0);
        }

        @Override
        public void onBufferEnd(final int code) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            if (!player.mBuffering) {
                L.w(player, "onBufferEnd", "msg blocked", "reason",
                        "not pair with buffering start", mapState(player.getState()));
                return;
            }
            player.mBuffering = false;
            listener.onInfo(player, Info.MEDIA_INFO_BUFFERING_END, 0);
        }

        @Override
        public void onBufferingUpdate(final TTVideoEngine engine, final int percent) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onBufferingUpdate(player, percent);
        }

        @Override
        public void onCompletion(final TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            player.setState(Player.STATE_COMPLETED);
            listener.onCompletion(player);
        }

        @Override
        public void onError(final Error error) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            player.moveToErrorState(error.code, new Exception(error.toString()));
        }

        @Override
        public void onRenderStart(final TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, Info.MEDIA_INFO_VIDEO_RENDERING_START, 0/*TODO*/);
        }

        @Override
        public void onReadyForDisplay(TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, Info.MEDIA_INFO_VIDEO_RENDERING_START_BEFORE_START, 0/*TODO*/);
        }

        @Override
        public void onVideoSizeChanged(final TTVideoEngine engine, final int width, final int height) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            EngineParams params = EngineParams.get(player.mPlayer);
            params.mVideoWidth = width;
            params.mVideoHeight = height;

            listener.onVideoSizeChanged(player, width, height);
        }

        @Override
        public void onSARChanged(int num, int den) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onSARChanged(player, num, den);
        }

        @Override
        public void onVideoStreamBitrateChanged(Resolution resolution, int bitrate) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;
            MediaSource mediaSource = player.mMediaSource;
            if (mediaSource == null) return;

            if (!player.isInPlaybackState()) return;

            @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);
            Track current = player.getCurrentTrack(trackType);
            Track track = player.getPendingTrack(trackType);

            final Quality quality = Mapper.resolution2Quality(resolution);

            L.d(player, "onVideoStreamBitrateChanged", Track.dump(current), Track.dump(track), Quality.dump(quality));
            if (track != null && Objects.equals(track.getQuality(), quality)) {
                player.removePendingTrack(Track.TRACK_TYPE_VIDEO);
                player.setCurrentTrack(Track.TRACK_TYPE_VIDEO, track);
                listener.onTrackChanged(player, Track.TRACK_TYPE_VIDEO, current, track);
            }
        }

        @Override
        public boolean onFetchedVideoInfo(VideoModel videoModel) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return false;
            Listener listener = player.mListener;
            MediaSource mediaSource = player.mMediaSource;
            if (mediaSource == null) return false;
            // NOTE: fix onFetchedVideoInfo callback multi times
            if (player.isInPlaybackState()) return false;

            TTVideoEngine mPlayer = player.mPlayer;
            if (mPlayer == null) return false;

            Mapper.updateMediaSource(mediaSource, videoModel);

            if (listener != null) {
                listener.onMediaSourceUpdated(player, MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_PLAY_INFO_FETCHED, mediaSource);
            }

            final Track playTrack = player.selectPlayTrack(mediaSource);
            if (playTrack != null) {
                config(player.mContext, mPlayer, mediaSource, playTrack);
                return false;
            } else {
                new Handler().post(() -> {
                    player.stop();
                    player.moveToErrorState(PlayerException.CODE_TRACK_SELECT_ERROR, new Exception());
                });
                return true;
            }
        }

        @Override
        public void onVideoEngineInfos(VideoEngineInfos videoEngineInfos) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            switch (videoEngineInfos.getKey()) {
                case VideoEngineInfos.USING_RENDER_SEEK_COMPLETE:
                    listener.onSeekComplete(player);
                    return;
                case VideoEngineInfos.USING_MDL_HIT_CACHE_SIZE:
                    String taskKey = videoEngineInfos.getUsingMDLPlayTaskKey(); // 使用的 key 信息
                    long hitCacheSize = videoEngineInfos.getUsingMDLHitCacheSize(); // 命中缓存文件 size
                    listener.onCacheHint(player, hitCacheSize);
                    return;
                case VideoEngineInfos.USING_MDL_CACHE_END:
                    return;
            }
        }


        @Override
        public void onAudioRenderOpened(int renderType, long start, long end) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, Info.MEDIA_INFO_AUDIO_RENDERING_START, 0);
        }

        @Override
        public void onCurrentPlaybackTimeUpdate(TTVideoEngine engine, int currentPlaybackTime) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onProgressUpdate(player, currentPlaybackTime);
        }
    }

    private static String resolvePlayerDecoderType(TTVideoEngine engine) {
        if (engine == null) return "unknown";
        int videoCodecType = engine.getIntOption(TTVideoEngine.PLAYER_OPTION_GET_VIDEO_CODEC_TYPE);
        if (videoCodecType == TTVideoEngine.PLAY_CODEC_NAME_AN_HW) {
            return "hw";
        } else {
            return "sw";
        }
    }
}
