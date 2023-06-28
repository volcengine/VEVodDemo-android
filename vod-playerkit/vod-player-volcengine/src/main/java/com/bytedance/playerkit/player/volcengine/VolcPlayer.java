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

import static com.bytedance.playerkit.player.Player.FRAME_TYPE_AUDIO;
import static com.bytedance.playerkit.player.Player.FRAME_TYPE_VIDEO;
import static com.bytedance.playerkit.player.Player.mapState;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.SubtitleText;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.volcengine.utils.TTVideoEngineListenerAdapter;
import com.bytedance.playerkit.player.volcengine.utils.TTVideoEngineSubtitleCallbackAdapter;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.CollectionUtils;
import com.bytedance.playerkit.utils.L;
import com.ss.ttm.player.PlaybackParams;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.SubDesInfoModel;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.VideoEngineInfos;
import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.source.VideoModelSource;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.Error;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

class VolcPlayer implements PlayerAdapter {
    private final Context mContext;
    private final ListenerAdapter mListenerAdapter;
    private final boolean mPreCreatePlayer;
    private boolean mPreRenderPlayer;
    private TTVideoEngine mPlayer;

    private Listener mListener;
    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;
    private boolean mStartWhenPrepared;
    private long mStartTime;
    private final PlaybackParams mPlaybackParams = new PlaybackParams();

    private MediaSource mMediaSource;
    private StrategySource mStrategySource;
    private SubDesInfoModel mSubtitleSource;

    @Player.PlayerState
    private int mState;
    private boolean mPausedWhenChangeAVTrack;
    private long mPlaybackTimeWhenChangeAVTrack;
    private boolean mBuffering;

    private final SparseArray<Track> mSelectedTrack = new SparseArray<>();
    private final SparseArray<Track> mPendingTrack = new SparseArray<>();
    private final SparseArray<Track> mCurrentTrack = new SparseArray<>();

    private Subtitle mSelectedSubtitle;
    private Subtitle mPendingSubtitle;
    private Subtitle mCurrentSubtitle;

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
        public VolcPlayer create(Looper eventLooper) {
            return new VolcPlayer(mContext, mMediaSource, false);
        }

        public VolcPlayer preCreate(Looper eventLooper) {
            return new VolcPlayer(mContext, mMediaSource, true);
        }
    }

    static final class EngineParams {
        private final static WeakHashMap<TTVideoEngine, EngineParams> sPlayerParams = new WeakHashMap<>();

        VolcPlayer mPreCreatedPlayerInstance;
        boolean mPreRenderPlayer;
        boolean mSuperResolutionInitialized;
        boolean mSubtitleEnabled;
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

    private VolcPlayer(final Context context, MediaSource mediaSource, boolean preCreate) {
        L.d(this, "constructor", "DEVICE_ID");
        this.mContext = context;
        this.mListenerAdapter = new ListenerAdapter(this);
        this.mPreCreatePlayer = preCreate;
        VolcPlayerInit.getConfigUpdater().updateVolcConfig(mediaSource);
        TTVideoEngine player;
        if (preCreate) {
            player = TTVideoEngineFactory.Default.get().create(mContext, mediaSource);
            EngineParams.get(player).mPreCreatedPlayerInstance = this;
            mStartWhenPrepared = false;
            L.d(this, "constructor", "preCreate", mPlayer, MediaSource.dump(mediaSource));
        } else {
            player = VolcEngineStrategy.removePreRenderEngine(mediaSource);
            if (player == null) {
                player = TTVideoEngineFactory.Default.get().create(mContext, mediaSource);
                mStartWhenPrepared = true;
                L.d(this, "constructor", "create", mPlayer, MediaSource.dump(mediaSource));
            } else {
                EngineParams.get(player).mPreRenderPlayer = true;
                mPreRenderPlayer = true;
                mStartWhenPrepared = false;
                L.d(this, "constructor", "preRender", player, MediaSource.dump(mediaSource));
            }
        }
        bind(player);
        setState(Player.STATE_IDLE);
    }

    TTVideoEngine getTTVideoEngine() {
        return mPlayer;
    }

    protected void bind(TTVideoEngine player) {
        L.d(this, "bind", player);
        player.setVideoEngineCallback(mListenerAdapter);
        player.setVideoEngineInfoListener(mListenerAdapter);
        player.setVideoInfoListener(mListenerAdapter);
        player.setPlayerEventListener(mListenerAdapter);
        player.setSubInfoCallBack(new TTVideoEngineSubtitleCallbackAdapter(mListenerAdapter));
        mPlayer = player;
    }

    protected void unbind() {
        final TTVideoEngine player = mPlayer;
        if (player == null) return;
        player.setVideoEngineCallback(null);
        player.setVideoEngineInfoListener(null);
        player.setVideoInfoListener(null);
        player.setPlayerEventListener(null);
        player.setSubInfoCallBack(null);
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

    @Override
    public MediaSource getDataSource() {
        return mMediaSource;
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
        synchronized (this) {
            return mPendingTrack.get(trackType);
        }
    }

    private void setPendingTrack(@Track.TrackType int trackType, Track track) {
        L.d(this, "setPendingTrack", Track.dump(track));
        synchronized (this) {
            mPendingTrack.put(trackType, track);
        }
    }

    @Override
    public Track getCurrentTrack(@Track.TrackType int trackType) {
        synchronized (this) {
            return mCurrentTrack.get(trackType);
        }
    }

    private void setCurrentTrack(@Track.TrackType int trackType, Track track) {
        L.d(this, "setCurrentTrack", Track.dump(track));
        synchronized (this) {
            this.mCurrentTrack.put(trackType, track);
        }
    }

    @Override
    public Track getSelectedTrack(@Track.TrackType int trackType) {
        synchronized (this) {
            return mSelectedTrack.get(trackType);
        }
    }

    private void setSelectedTrack(@Track.TrackType int trackType, Track track) {
        L.d(this, "setSelectedTrack", Track.dump(track));
        synchronized (this) {
            mSelectedTrack.put(trackType, track);
        }
    }

    @Override
    public List<Track> getTracks(@Track.TrackType int trackType) {
        return mMediaSource.getTracks(trackType);
    }

    @Override
    public List<Subtitle> getSubtitles() {
        return mMediaSource.getSubtitles();
    }

    @Override
    public void selectSubtitle(@Nullable Subtitle subtitle) {
        if (subtitle == null) return;
        final Subtitle selected = getSelectedSubtitle();
        final Subtitle current = getCurrentSubtitle();
        if (selected != subtitle) {
            setSelectedSubtitle(subtitle);
            setPendingSubtitle(subtitle);
            if (mListener != null) {
                mListener.onSubtitleWillChange(this, current, subtitle);
            }
            mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_SWITCH_SUB_ID, subtitle.getSubtitleId());
        }
    }

    public void setSelectedSubtitle(Subtitle subtitle) {
        L.d(this, "setSelectedSubtitle", Subtitle.dump(subtitle));
        synchronized (this) {
            mSelectedSubtitle = subtitle;
        }
    }

    @Override
    public Subtitle getSelectedSubtitle() {
        synchronized (this) {
            return mSelectedSubtitle;
        }
    }


    public void setPendingSubtitle(Subtitle subtitle) {
        L.d(this, "setPendingSubtitle", Subtitle.dump(subtitle));
        synchronized (this) {
            mPendingSubtitle = subtitle;
        }
    }


    @Override
    public Subtitle getPendingSubtitle() {
        synchronized (this) {
            return mPendingSubtitle;
        }
    }

    public void setCurrentSubtitle(Subtitle subtitle) {
        L.d(this, "setCurrentSubtitle", Subtitle.dump(subtitle));
        synchronized (this) {
            mCurrentSubtitle = subtitle;
        }
    }

    @Override
    public Subtitle getCurrentSubtitle() {
        synchronized (this) {
            return mCurrentSubtitle;
        }
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
        L.d(this, "syncPreRenderState", MediaSource.dump(mediaSource));
        Asserts.checkState(getState(), Player.STATE_IDLE, Player.STATE_STOPPED);
        setState(Player.STATE_PREPARING);

        final VolcPlayer prePlayer = EngineParams.get(mPlayer).mPreCreatedPlayerInstance;
        final VolcPlayer player = this;

        if (prePlayer == null) return;

        final PlaybackParams playbackParams = prePlayer.mPlaybackParams;
        mPlaybackParams.setSpeed(playbackParams.getSpeed())
                .setPitch(playbackParams.getPitch())
                .setAudioFallbackMode(playbackParams.getAudioFallbackMode());

        mStrategySource = prePlayer.mStrategySource;

        final VolcPlayerEventRecorder recorder = (VolcPlayerEventRecorder) prePlayer.mListener;
        if (recorder == null) return;
        prePlayer.setListener(null);

        final Listener listener = mListener;
        if (listener == null) return;
        recorder.notifyEvents(new Listener() {

            @Override
            public void onPrepared(@NonNull PlayerAdapter mp) {
                setState(Player.STATE_PREPARED);

                listener.onPrepared(player);
            }

            @Override
            public void onCompletion(@NonNull PlayerAdapter mp) { /**/ }

            @Override
            public void onError(@NonNull PlayerAdapter mp, int code, @NonNull String msg) { /**/ }

            @Override
            public void onSeekComplete(@NonNull PlayerAdapter mp) { /**/ }

            @Override
            public void onVideoSizeChanged(@NonNull PlayerAdapter mp, int width, int height) {
                listener.onVideoSizeChanged(player, width, height);
            }

            @Override
            public void onSARChanged(@NonNull PlayerAdapter mp, int num, int den) {
                listener.onSARChanged(player, num, den);
            }

            @Override
            public void onBufferingUpdate(@NonNull PlayerAdapter mp, int percent) { /**/ }

            @Override
            public void onProgressUpdate(@NonNull PlayerAdapter mp, long position) { /**/ }

            @Override
            public void onInfo(@NonNull PlayerAdapter mp, int what, @Nullable Object extra) {
                switch (what) {
                    case Info.MEDIA_INFO_BUFFERING_START:
                    case Info.MEDIA_INFO_BUFFERING_END:
                        break;
                    default:
                        listener.onInfo(player, what, extra);
                        break;
                }
            }

            @Override
            public void onCacheHint(PlayerAdapter mp, long cacheSize) {
                listener.onCacheHint(player, cacheSize);
            }

            @Override
            public void onMediaSourceUpdateStart(PlayerAdapter mp, int type, MediaSource mediaSource1) {
                listener.onMediaSourceUpdateStart(player, type, mediaSource);
            }

            @Override
            public void onMediaSourceUpdated(PlayerAdapter mp, int type, MediaSource mediaSource1) {
                if (type == MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_PLAY_INFO_FETCHED) {
                    Mapper.updateMediaSource(mMediaSource, mediaSource); // TODO
                }
                listener.onMediaSourceUpdated(player, type, mediaSource);
            }

            @Override
            public void onMediaSourceUpdateError(PlayerAdapter mp, int type, PlayerException e) {
                listener.onMediaSourceUpdated(player, type, mediaSource);
            }

            @Override
            public void onTrackInfoReady(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @NonNull List<Track> tracks) {
                mediaSource.setTracks(tracks);

                listener.onTrackInfoReady(player, trackType, tracks);
            }

            @Override
            public void onTrackWillChange(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @Nullable Track current, @NonNull Track target) {
                setSelectedTrack(trackType, target);
                setPendingTrack(trackType, target);

                listener.onTrackWillChange(player, trackType, current, target);
            }

            @Override
            public void onTrackChanged(@NonNull PlayerAdapter mp, @Track.TrackType int trackType, @Nullable Track pre, @NonNull Track current) {
                setPendingTrack(trackType, null);
                setCurrentTrack(trackType, current);

                listener.onTrackChanged(player, trackType, pre, current);
            }


            @Override
            public void onSubtitleStateChanged(@NonNull PlayerAdapter mp, boolean enabled) {
                listener.onSubtitleStateChanged(player, enabled);
            }

            @Override
            public void onSubtitleInfoReady(@NonNull PlayerAdapter mp, List<Subtitle> subtitles) {
                mediaSource.setSubtitles(subtitles);

                listener.onSubtitleInfoReady(player, subtitles);
            }

            @Override
            public void onSubtitleFileLoadFinish(@NonNull PlayerAdapter mp, int success, String info) {
                listener.onSubtitleFileLoadFinish(player, success, info);
            }

            @Override
            public void onSubtitleWillChange(@NonNull PlayerAdapter mp, Subtitle current, Subtitle target) {
                setSelectedSubtitle(target);
                setPendingSubtitle(target);

                listener.onSubtitleWillChange(player, current, target);
            }

            @Override
            public void onSubtitleChanged(@NonNull PlayerAdapter mp, Subtitle pre, Subtitle current) {
                setPendingSubtitle(null);
                setCurrentSubtitle(current);

                listener.onSubtitleChanged(player, pre, current);
            }

            @Override
            public void onSubtitleTextUpdate(@NonNull PlayerAdapter mp, @NonNull SubtitleText subtitleText) {
                listener.onSubtitleTextUpdate(player, subtitleText);
            }

            @Override
            public void onFrameInfoUpdate(@NonNull PlayerAdapter mp, int frameType, long pts, long clockTime) { /**/}
        });
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

        final VidPlayAuthTokenSource vidSource = Mapper.mediaSource2VidPlayAuthTokenSource(mediaSource);
        if (vidSource == null) {
            moveToErrorState(PlayerException.CODE_SOURCE_SET_ERROR, new NullPointerException("vidSource is null!"));
            return;
        }
        mStrategySource = vidSource;
        mPlayer.setStrategySource(mStrategySource);

        final VolcConfig volcConfig = VolcConfig.get(mediaSource);
        if (volcConfig.enableSubtitle) {
            mPlayer.setSubAuthToken(mediaSource.getSubtitleAuthToken());
        }

        if (VolcQualityStrategy.isEnableStartupABR(volcConfig)) {
            VolcQualityStrategy.init(mPlayer, mediaSource, new VolcQualityStrategy.Listener() {
                @Override
                public void onStartupTrackSelected(VolcQualityStrategy.StartupTrackResult result) {
                    @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);
                    final List<Track> tracks = VolcPlayer.this.getTracks(trackType);
                    Track selected = result.track;
                    if (selected == null) {
                        selected = VolcPlayerInit.getTrackSelector().selectTrack(TrackSelector.TYPE_PLAY,
                                trackType,
                                tracks,
                                mediaSource);
                    }
                    VolcPlayer.this.config(mediaSource, selected);
                    VolcPlayer.this.setSelectedTrack(trackType, selected);
                    VolcPlayer.this.setPendingTrack(trackType, selected);
                    if (mListener != null) {
                        mListener.onTrackWillChange(VolcPlayer.this, trackType, null, selected);
                    }
                }
            });
        }
        /**
         * config in {@link  #prepareVid(MediaSource, Track)} which invoked in
         * {@link  TTVideoEngineListenerAdapter#onFetchedVideoInfo(VideoModel)}
         */
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
        @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);
        final List<Track> tracks = mediaSource.getTracks(trackType);
        if (CollectionUtils.isEmpty(tracks)) {
            // TODO error
            return;
        }

        final VideoModelSource videoModelSource = Mapper.mediaSource2VideoModelSource(mediaSource, VolcPlayerInit.getCacheKeyFactory());
        if (videoModelSource == null) {
            // TODO error
            return;
        }

        mStrategySource = videoModelSource;
        mPlayer.setStrategySource(mStrategySource);

        final VolcConfig volcConfig = VolcConfig.get(mediaSource);
        if (volcConfig.enableSubtitle) {
            mPlayer.setSubAuthToken(mediaSource.getSubtitleAuthToken());
            setSubtitleIds(videoModelSource.videoModel());
        }

        if (VolcQualityStrategy.isEnableStartupABR(volcConfig)) {
            if (mListener != null) {
                mListener.onTrackInfoReady(this, trackType, tracks);
            }
            VolcQualityStrategy.init(mPlayer, mediaSource, new VolcQualityStrategy.Listener() {
                @Override
                public void onStartupTrackSelected(VolcQualityStrategy.StartupTrackResult result) {
                    Track selected = result.track;
                    if (selected == null) {
                        selected = VolcPlayerInit.getTrackSelector().selectTrack(TrackSelector.TYPE_PLAY, trackType, tracks, mediaSource);
                    }
                    VolcPlayer.this.setSelectedTrack(trackType, selected);
                    VolcPlayer.this.setPendingTrack(trackType, selected);
                    if (mListener != null) {
                        mListener.onTrackWillChange(VolcPlayer.this, trackType, null, selected);
                    }
                    VolcPlayer.this.config(mediaSource, selected);
                }
            });
        } else {
            final Track playTrack = selectPlayTrack(mediaSource);
            if (playTrack == null) {
                moveToErrorState(PlayerException.CODE_TRACK_SELECT_ERROR, new Exception("Select Track return null!"));
                return;
            }
            config(mediaSource, playTrack);
        }

        preparePlayer(mPlayer, isStartWhenPrepared());
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
                setSelectedTrack(trackType, selected);
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

    @Nullable
    private Subtitle selectPlaySubtitle(MediaSource mediaSource) {
        final List<Subtitle> subtitles = mediaSource.getSubtitles();
        if (subtitles != null && !subtitles.isEmpty()) {
            if (mListener != null) {
                mListener.onSubtitleInfoReady(this, subtitles);
            }
        }

        Subtitle subtitle = getSelectedSubtitle();
        if (subtitle != null) return subtitle;

        if (subtitles != null && !subtitles.isEmpty()) {
            subtitle = VolcPlayerInit.getSubtitleSelector().selectSubtitle(mediaSource, mediaSource.getSubtitles());
        }
        return subtitle;
    }

    private void prepareDirectUrl(MediaSource mediaSource, Track track, boolean startWhenPrepared) {
        if (isInState(Player.STATE_IDLE, Player.STATE_STOPPED)) {
            setState(Player.STATE_PREPARING);
        }

        final VolcConfig volcConfig = VolcConfig.get(mediaSource);

        final DirectUrlSource directUrlSource = Mapper.mediaSource2DirectUrlSource(
                mediaSource,
                track,
                VolcPlayerInit.getCacheKeyFactory());

        if (directUrlSource == null) {
            // TODO error
            return;
        }

        mStrategySource = directUrlSource;
        mPlayer.setStrategySource(mStrategySource);

        if (volcConfig.enableSubtitle) {
            mSubtitleSource = Mapper.subtitles2SubtitleSource(mediaSource.getSubtitles());
            if (mSubtitleSource != null) {
                mPlayer.setSubDesInfoModel(mSubtitleSource);
                final Subtitle subtitle = selectPlaySubtitle(mediaSource);
                if (subtitle != null) {
                    selectSubtitle(subtitle);
                }
            }
        }

        config(mediaSource, track);

        preparePlayer(mPlayer, startWhenPrepared);
    }

    private void setSubtitleIds(IVideoModel videoModel) {
        final VolcConfig volcConfig = VolcConfig.get(mMediaSource);
        if (volcConfig.enableSubtitle &&
                volcConfig.subtitleLanguageIds != null) {
            final String subtitleIds = Mapper.subtitleList2SubtitleIds(
                    Mapper.findSubInfoListWithLanguageIds(
                            Mapper.findSubInfoList(videoModel),
                            volcConfig.subtitleLanguageIds));
            if (!TextUtils.isEmpty(subtitleIds)) {
                mPlayer.setStringOption(TTVideoEngine.PLAYER_OPTION_SUB_IDS, subtitleIds);
            }
        }
    }

    void preparePlayer(TTVideoEngine player, boolean startWhenPrepared) {
        if (mPreCreatePlayer) return;

        L.d(this, "preparePlayer", mStartWhenPrepared);

        if (startWhenPrepared) {
            player.play();
        } else {
            player.prepare();
        }
    }

    private void config(MediaSource mediaSource, @Nullable Track track) {
        if (track != null) {
            mPlayer.configResolution(Mapper.track2Resolution(track));
        }

        final Map<String, String> headers = Mapper.findHeaders(mediaSource, track);
        if (headers != null) {
            setHeaders(mPlayer, headers);
        }

        VolcSuperResolutionStrategy.initSuperResolution(mContext, mPlayer, mediaSource, track);
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
            mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_START_AUTOMATICALLY, startWhenPrepared ? 1 : 0);
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
        mSubtitleSource = null;
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
        if (isInState(Player.STATE_RELEASED)) return;
        L.d(this, "release", mPlayer, MediaSource.dump(mMediaSource));
        resetInner();
        mPlayer.setIsMute(true);
        mPlayer.releaseAsync();
        unbind();
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
        if (!VolcConfig.get(mMediaSource).enableAudioTrackVolume) {
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
    public void setSubtitleEnabled(boolean enabled) {
        if (VolcConfig.get(mMediaSource).enableSubtitle) {
            if (EngineParams.get(mPlayer).mSubtitleEnabled != enabled) {
                EngineParams.get(mPlayer).mSubtitleEnabled = enabled;
                mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_OPEN_SUB, enabled ? 1 : 0);
                if (mListener != null) {
                    mListener.onSubtitleStateChanged(this, enabled);
                }
            }
        }
    }

    @Override
    public boolean isSubtitleEnabled() {
        return VolcConfig.get(mMediaSource).enableSubtitle && EngineParams.get(mPlayer).mSubtitleEnabled;
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
            final Track pending = player.getPendingTrack(trackType);
            player.setPendingTrack(trackType, null);

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
                if (!player.isPlaying() && afterFirstFrame == 1) {
                    L.w(player, "onBufferStart", "msg blocked", "reason",
                            "state not playing", mapState(player.getState()));
                    return;
                }
            }

            player.mBuffering = true;
            listener.onInfo(player, Info.MEDIA_INFO_BUFFERING_START, new Object[]{reason, afterFirstFrame, action});
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
            listener.onInfo(player, Info.MEDIA_INFO_BUFFERING_END, code);
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

            listener.onInfo(player, Info.MEDIA_INFO_VIDEO_RENDERING_START, null);
        }

        @Override
        public void onReadyForDisplay(TTVideoEngine engine) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, Info.MEDIA_INFO_VIDEO_RENDERING_START_BEFORE_START, null);
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
                player.setPendingTrack(Track.TRACK_TYPE_VIDEO, null);
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

            // select start play subtitle
            player.setSubtitleIds(videoModel);

            final VolcConfig volcConfig = VolcConfig.get(mediaSource);
            if (VolcQualityStrategy.isEnableStartupABR(volcConfig)) {
                @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);
                final List<Track> tracks = mediaSource.getTracks(trackType);
                if (tracks != null && !tracks.isEmpty()) {
                    if (listener != null) {
                        listener.onTrackInfoReady(player, trackType, tracks);
                    }
                }
            } else {
                // select start play video/audio track
                final Track playTrack = player.selectPlayTrack(mediaSource);
                if (playTrack != null) {
                    player.config(mediaSource, playTrack);
                    return false;
                } else {
                    new Handler().post(() -> {
                        player.stop();
                        player.moveToErrorState(PlayerException.CODE_TRACK_SELECT_ERROR, new Exception());
                    });
                    return true;
                }
            }
            return false;
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

            listener.onInfo(player, Info.MEDIA_INFO_AUDIO_RENDERING_START, null);
        }

        @Override
        public void onCurrentPlaybackTimeUpdate(TTVideoEngine engine, int currentPlaybackTime) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onProgressUpdate(player, currentPlaybackTime);
        }

        @Override
        public void onSubPathInfo(String subPathInfo, Error error) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            final Listener listener = player.mListener;
            if (listener == null) return;

            final MediaSource mediaSource = player.mMediaSource;
            if (mediaSource == null) return;

            L.d(player, "onSubPathInfo", subPathInfo, error);

            if (error != null) {
                listener.onMediaSourceUpdateError(player, MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_SUBTITLE_INFO_FETCHED, new PlayerException(error.code, error.toString()));
                return;
            }

            // TODO
            if (!TextUtils.isEmpty(subPathInfo)) {
                JSONObject subtitleModel;
                try {
                    subtitleModel = new JSONObject(subPathInfo);
                } catch (JSONException e) {
                    listener.onMediaSourceUpdateError(player, MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_SUBTITLE_INFO_FETCHED, new PlayerException(PlayerException.CODE_SUBTITLE_PARSE_ERROR, "subtitleModel parse error", e));
                    return;
                }
                List<Subtitle> subtitles = Mapper.subtitleModel2Subtitles(subtitleModel);
                if (subtitles != null && !subtitles.isEmpty()) {
                    mediaSource.setSubtitles(subtitles);
                    listener.onMediaSourceUpdated(player, MediaSourceUpdateReason.MEDIA_SOURCE_UPDATE_REASON_SUBTITLE_INFO_FETCHED, mediaSource);

                    final Subtitle playSubtitle = player.selectPlaySubtitle(mediaSource);
                    if (playSubtitle != null) {
                        player.selectSubtitle(playSubtitle);
                    }
                }
            }
        }

        @Override
        public void onSubInfoCallback(int code, String info) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            L.v(player, "onSubInfoCallback", code, info);
            SubtitleText subtitleText = Mapper.mapSubtitleFrameInfo2SubtitleText(info);
            if (subtitleText != null) {
                listener.onSubtitleTextUpdate(player, subtitleText);
            }
        }

        @Override
        public void onSubSwitchCompleted(int success, int subId) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;
            MediaSource mediaSource = player.mMediaSource;
            if (mediaSource == null) return;

            L.d(player, "onSubSwitchCompleted", success, subId);

            final Subtitle subtitle = mediaSource.getSubtitle(subId);
            final Subtitle pending = player.getPendingSubtitle();
            final Subtitle current = player.getCurrentSubtitle();
            if (pending == subtitle) {
                player.setPendingSubtitle(null);
                player.setCurrentSubtitle(subtitle);
                listener.onSubtitleChanged(player, current, subtitle);
            }
        }

        @Override
        public void onSubLoadFinished2(int success, String info) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            L.d(player, "onSubLoadFinished2", success, info);

            final Subtitle selected = player.getSelectedSubtitle();
            final Subtitle current = player.getCurrentSubtitle();
            if (current == null) {
                player.setPendingSubtitle(null);
                player.setCurrentSubtitle(selected);
                listener.onSubtitleChanged(player, null, selected);
            }

            listener.onSubtitleFileLoadFinish(player, success, info);
        }

        @Override
        public void onFrameAboutToBeRendered(TTVideoEngine engine, int type, long pts, long wallClockTime, Map<Integer, String> frameData) {
            final VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            L.v(player, "onFrameAboutToBeRendered", type, pts, wallClockTime);
            listener.onFrameInfoUpdate(player, type == 0 ? FRAME_TYPE_VIDEO : FRAME_TYPE_AUDIO, pts, wallClockTime);
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
