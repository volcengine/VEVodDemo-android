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

import static com.bytedance.playerkit.player.Player.STATE_STOPPED;
import static com.bytedance.playerkit.player.Player.mapState;
import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_AUDIO;
import static com.bytedance.playerkit.player.source.Track.TRACK_TYPE_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRELOAD;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRE_RENDER;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Surface;
import android.view.SurfaceHolder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
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
import com.ss.ttvideoengine.strategy.EngineStrategyListener;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.Error;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class VolcPlayer implements PlayerAdapter {
    private final Context mContext;

    private TTVideoEngine mPlayer;
    private boolean mPreRenderPlayer;

    private final ListenerAdapter mListenerAdapter;
    private PlayerAdapter.Listener mListener;

    private Surface mSurface;
    private SurfaceHolder mSurfaceHolder;

    private long mStartTime;
    private MediaSource mSource;
    private StrategySource mStrategySource;

    @Player.PlayerState
    private int mState;
    private Exception mPlayerException;

    private boolean mPausedWhenChangeAVTrack;
    private long mPlaybackTimeWhenChangeAVTrack;
    private final PlaybackParams mPlaybackParams = new PlaybackParams();

    private final SparseArray<Track> mCurrentTrack = new SparseArray<>();
    private final SparseArray<Track> mPendingTrack = new SparseArray<>();

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

    private static final List<MediaSource> sMediaSources = new ArrayList<>();

    private static MediaSource findMediaSource(StrategySource strategySource) {
        for (MediaSource mediaSource : sMediaSources) {
            if (TextUtils.equals(strategySource.vid(), mediaSource.getMediaId())) {
                return mediaSource;
            }
        }
        return null;
    }

    public static void setMediaSources(List<MediaSource> mediaSources) {
        if (mediaSources == null) return;

        sMediaSources.clear();
        sMediaSources.addAll(mediaSources);

        List<StrategySource> strategySources = Mapper.mediaSources2StrategySources(
                mediaSources,
                VolcPlayerInit.getCacheKeyFactory(),
                VolcPlayerInit.getTrackSelector(),
                TrackSelector.TYPE_PRELOAD);
        if (strategySources == null) return;
        TTVideoEngine.setStrategySources(strategySources);
    }

    public static void addMediaSources(List<MediaSource> mediaSources) {
        if (mediaSources == null) return;

        sMediaSources.addAll(mediaSources);

        List<StrategySource> strategySources = Mapper.mediaSources2StrategySources(
                mediaSources,
                VolcPlayerInit.getCacheKeyFactory(),
                VolcPlayerInit.getTrackSelector(),
                TrackSelector.TYPE_PRELOAD);
        if (strategySources == null) return;
        TTVideoEngine.addStrategySources(strategySources);
    }

    public static void renderFrame(MediaSource mediaSource, Surface surface, int[] frameInfo) {
        if (mediaSource == null) return;
        if (surface == null || !surface.isValid()) return;

        final TTVideoEngine player = get(mediaSource.getMediaId());
        if (player != null && player != StrategyManager.instance().getPlayEngine()) {
            player.setSurface(surface);
            player.forceDraw();
            frameInfo[0] = player.getVideoWidth();
            frameInfo[1] = player.getVideoHeight();
        }
    }

    public static void setShortVideoStrategyEnabled() {
        TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRELOAD, STRATEGY_SCENE_SMALL_VIDEO);
        TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRE_RENDER, STRATEGY_SCENE_SMALL_VIDEO);
    }

    public static void clearStrategy() {
        sMediaSources.clear();
        TTVideoEngine.clearAllStrategy();
    }

    private static TTVideoEngine get(String mediaId) {
        return TTVideoEngine.getPreRenderEngine(mediaId);
    }

    private static TTVideoEngine create(Context context, MediaSource mediaSource) {
        return TTVideoEngineFactory.Default.get().create(context, mediaSource);
    }

    private static void bind(TTVideoEngine player, MediaSource mediaSource) {
        player.setVideoInfoListener(new TTVideoEngineListenerAdapter() {
            @Override
            public boolean onFetchedVideoInfo(VideoModel videoModel) {
                Mapper.updateMediaSource(mediaSource, videoModel);
                @Track.TrackType
                int trackType = mediaSource.getMediaType() == MediaSource.MEDIA_TYPE_AUDIO ? TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;
                List<Track> tracks = mediaSource.getTracks(trackType);
                if (tracks != null) {
                    Track track = VolcPlayerInit.getTrackSelector().selectTrack(TrackSelector.TYPE_PRELOAD, trackType, tracks, mediaSource);
                    if (track != null) {
                        Resolution resolution = Mapper.quality2Resolution(track.getQuality());
                        if (resolution != null) {
                            player.configResolution(resolution);
                        }
                    }
                }
                return false;
            }
        });
    }

    static {
        TTVideoEngine.setEngineStrategyListener(new EngineStrategyListener() {
            @Override
            public TTVideoEngine createPreRenderEngine(StrategySource source) {
                final MediaSource mediaSource = findMediaSource(source);
                if (mediaSource == null) return null; // error

                Context context = VolcPlayerInit.getContext();
                final TTVideoEngine player = create(context, mediaSource);
                bind(player, mediaSource);
                setupSource(player, source, mediaSource.getHeaders());
                return player;
            }
        });
    }

    private VolcPlayer(final Context context, MediaSource mediaSource) {
        L.v(this, "constructor", "DEVICE_ID", TTVideoEngine.getDeviceID());
        mContext = context;

        mListenerAdapter = new ListenerAdapter(this);

        TTVideoEngine player = get(mediaSource.getMediaId());
        if (player == null) {
            player = create(mContext, mediaSource);
        } else {
            mPreRenderPlayer = true;
        }
        bind(player);
        setState(Player.STATE_IDLE);
    }

    protected void bind(TTVideoEngine player) {
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
        }
    }

    @Override
    public void setDisplay(SurfaceHolder display) {
        if (mSurfaceHolder != display) {
            mPlayer.setSurfaceHolder(display);
            mSurfaceHolder = display;
        }
    }

    @Override
    public void setVideoScalingMode(@PlayerAdapter.ScalingMode int mode) {
        final int scalingMode = Mapper.mapScalingMode(mode);
        mPlayer.setIntOption(TTVideoEngine.PLAYER_OPTION_IMAGE_LAYOUT, scalingMode);
    }

    @Override
    public void setDataSource(@NonNull MediaSource source) throws IOException {
        mSource = source;
    }

    private void selectPlayTrackInTrackInfoReady(@NonNull MediaSource source, @Track.TrackType int trackType) {
        final List<Track> tracks = source.getTracks(trackType);
        if (tracks != null && !tracks.isEmpty()) {
            if (mListener != null) {
                mListener.onTrackInfoReady(this, trackType, tracks);
            }
            Track target = VolcPlayerInit.getTrackSelector().selectTrack(TrackSelector.TYPE_PLAY, trackType, tracks, source);

            selectTrack(trackType, target);
        }
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
        if (mPlayer != null && mSource != null) {
            int contentType = mSource.getMediaProtocol();
            if (contentType == MediaSource.MEDIA_PROTOCOL_DASH) {
                return true;
            } else if (contentType == MediaSource.MEDIA_PROTOCOL_HLS) {
                IVideoModel videoModel = mPlayer.getIVideoModel();
                if (videoModel != null && videoModel.isSupportHLSSeamlessSwitch()) {
                    return VolcSettings.PLAYER_OPTION_ENABLE_HLS_SEAMLESS_SWITCH;
                }
            }
        }
        return false;
    }

    @Override
    public void selectTrack(@Track.TrackType int trackType, @Nullable Track track) throws IllegalStateException {
        if (mState == Player.STATE_RELEASED || mState == Player.STATE_ERROR) return;

        final MediaSource source = mSource;

        if (source == null) return;

        if (track == null) return; //TODO abr auto support

        final Track selected = getSelectedTrack(trackType);

        if (!Objects.equals(selected, track)) {

            setPendingTrack(trackType, track);

            if (mListener != null) {
                mListener.onTrackWillChange(this, trackType, selected, track);
            }

            if (selected == null) {
                // Select Track when playback start
                final int sourceType = source.getSourceType();
                if (sourceType == MediaSource.SOURCE_TYPE_ID) {
                    /**
                     * invoked in {@link ListenerAdapter#onFetchedVideoInfo(VideoModel)}
                     * -> {@link #selectPlayTrackInTrackInfoReady(MediaSource, int)}
                     */
                    Resolution resolution = Mapper.track2Resolution(track);
                    if (resolution == null) return;
                    mPlayer.configResolution(resolution);
                } else {
                    /**
                     * invoked in {@link #preparePlayer(MediaSource)}
                     * -> {@link #selectPlayTrackInTrackInfoReady(MediaSource, int)}
                     */
                    preparePlayer(source, track);
                }
            } else {
                // Select Track during playback
                if (isInPlaybackState()) {
                    mPausedWhenChangeAVTrack = mState == Player.STATE_PAUSED;
                    mPlaybackTimeWhenChangeAVTrack = mState == Player.STATE_COMPLETED ? 0 : mPlayer.getCurrentPlaybackTime();
                } else {
                    mPlaybackTimeWhenChangeAVTrack = mStartTime;
                }

                final int sourceType = source.getSourceType();
                switch (sourceType) {
                    case MediaSource.SOURCE_TYPE_ID: {
                        Resolution resolution = Mapper.track2Resolution(track);
                        if (resolution == null) return;

                        if (!isSupportSmoothTrackSwitching(trackType)) {
                            setState(Player.STATE_PREPARING);
                        }

                        // for vid TTVideoEngine will take care of startTime sync after
                        // resolution change

                        // mPlayer.setStartTime((int) mPlaybackTimeWhenChangeAVTrack);
                        mPlayer.configResolution(resolution);
                        break;
                    }
                    case MediaSource.SOURCE_TYPE_URL: {
                        // TODO smooth switching
                        if (mState != Player.STATE_IDLE) {
                            stop();
                        }

                        TTVideoEngine player = mPlayer;

                        bind(player);

                        player.setSurface(mSurface);

                        setState(Player.STATE_PREPARING);

                        if (mPlaybackTimeWhenChangeAVTrack > 0) {
                            player.setStartTime((int) mPlaybackTimeWhenChangeAVTrack);
                        }
                        preparePlayer(source, track);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Track getPendingTrack(@Track.TrackType int trackType) throws IllegalStateException {
        return mPendingTrack.get(trackType);
    }

    private Track removePendingTrack(@Track.TrackType int type) {
        final Track track = mPendingTrack.get(type);
        if (track != null) {
            mPendingTrack.remove(type);
        }
        return track;
    }

    private void setPendingTrack(@Track.TrackType int type, Track track) {
        mPendingTrack.put(type, track);
    }

    private void setCurrentTrack(@Track.TrackType int type, Track track) {
        this.mCurrentTrack.put(type, track);
    }

    @Override
    public Track getCurrentTrack(@Track.TrackType int trackType) throws IllegalStateException {
        return mCurrentTrack.get(trackType);
    }

    private Track getSelectedTrack(@Track.TrackType int trackType) throws IllegalStateException {
        final Track current = getCurrentTrack(trackType);
        final Track pending = getPendingTrack(trackType);
        return pending == null ? current : pending;
    }

    @Override
    public List<Track> getTracks(@Track.TrackType int trackType) throws IllegalStateException {
        return mSource.getTracks(trackType);
    }

    @Override
    public void prepareAsync() throws IllegalStateException {
        setState(Player.STATE_PREPARING);

        if (mPreRenderPlayer) {
            syncPreRenderState(mSource, mPlayer);
        } else {
            preparePlayer(mSource);
        }
    }

    private void preparePlayer(MediaSource source) {
        if (source == null) return; // TODO throw error

        @MediaSource.SourceType final int sourceType = source.getSourceType();
        @MediaSource.MediaType final int mediaType = source.getMediaType();
        @Track.TrackType final int trackType = mediaType == MediaSource.MEDIA_TYPE_AUDIO ?
                TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;

        if (source.getSourceType() == MediaSource.SOURCE_TYPE_ID) {
            StrategySource strategySource = Mapper.mediaSource2VidPlayAuthTokenSource(mSource, null);
            preparePlayer(strategySource, mSource.getHeaders());
        } else if (source.getSourceType() == MediaSource.SOURCE_TYPE_URL) {
            selectPlayTrackInTrackInfoReady(source, trackType);
        } else {
            throw new IllegalArgumentException("unsupported sourceType " + sourceType);
        }
    }

    private void preparePlayer(MediaSource mediaSource, Track track) {
        StrategySource strategySource = Mapper.mediaSource2DirectUrlSource(
                mediaSource,
                track,
                VolcPlayerInit.getCacheKeyFactory());
        preparePlayer(strategySource, mediaSource.getHeaders());
    }

    private void preparePlayer(StrategySource source, Map<String, String> headers) {
        mStrategySource = source;
        setupSource(mPlayer, source, headers);
        if (VolcSettings.PLAYER_OPTION_ENABLE_SUPER_RESOLUTION_ABILITY) {
            initSuperResolution();
        }
        mPlayer.prepare();
    }

    private static void setupSource(TTVideoEngine player, StrategySource source, Map<String, String> headers) {
        player.setStrategySource(source);
        setHeaders(player, headers);
    }

    private void syncPreRenderState(MediaSource source, TTVideoEngine player) {
        if (source == null) return;

        @MediaSource.SourceType final int sourceType = source.getSourceType();
        if (sourceType == MediaSource.SOURCE_TYPE_ID) {
            IVideoModel videoModel = player.getIVideoModel();
            if (videoModel != null) {
                Mapper.updateMediaSource(source, videoModel);
            }
            if (mListener != null) {
                mListener.onSourceInfoLoadComplete(this, SourceLoadInfo.SOURCE_INFO_PLAY_INFO_FETCHED, source);
            }
        }

        @Track.TrackType
        int trackType = source.getMediaType() == MediaSource.MEDIA_TYPE_AUDIO ? TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;

        final List<Track> tracks = source.getTracks(trackType);
        if (tracks != null && !tracks.isEmpty()) {

            if (mListener != null) {
                mListener.onTrackInfoReady(this, trackType, tracks);
            }
            if (trackType == TRACK_TYPE_VIDEO) {
                Track track = null;

                switch (sourceType) {
                    case MediaSource.SOURCE_TYPE_ID: {
                        Resolution resolution = player.getCurrentResolution();
                        if (resolution != null) {
                            track = Mapper.findTrackWithResolution(tracks, resolution);
                        }
                        break;
                    }
                    case MediaSource.SOURCE_TYPE_URL: {
                        DirectUrlSource directUrlSource = (DirectUrlSource) player.getStrategySource();
                        if (directUrlSource != null) {
                            track = Mapper.findTrackWithDirectUrlSource(source, tracks, directUrlSource, VolcPlayerInit.getCacheKeyFactory());
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
            String fileHash = VolcPlayerInit.getCacheKeyFactory().generateCacheKey(mSource, pending);
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
                mListener.onVideoSizeChanged(this, player.getVideoWidth(), player.getVideoHeight());
            }
        }
    }

    @Override
    public void start() {
        Asserts.checkState(mState, Player.STATE_PREPARED, Player.STATE_STARTED,
                Player.STATE_PAUSED, Player.STATE_COMPLETED);

        if (mState == Player.STATE_STARTED) {
            return;
        }

        mPlayer.play();
        setState(Player.STATE_STARTED);
    }

    @Override
    public boolean isPlaying() {
        return mState == Player.STATE_STARTED;
    }

    @Override
    public void pause() {
        Asserts.checkState(getState(), Player.STATE_PREPARED, Player.STATE_STARTED,
                Player.STATE_PAUSED, Player.STATE_COMPLETED);

        if (mState == Player.STATE_PAUSED) return;

        mPlayer.pause();
        setState(Player.STATE_PAUSED);
    }

    @Override
    public void stop() {
        Asserts.checkState(getState(), Player.STATE_PREPARING, Player.STATE_PREPARED,
                Player.STATE_STARTED, Player.STATE_PAUSED, Player.STATE_COMPLETED, Player.STATE_STOPPED);

        if (mState == STATE_STOPPED) return;

        mPlayer.stop();
        setState(Player.STATE_STOPPED);
    }

    @Override
    public void setStartTime(long startTime) {
        mStartTime = startTime;
        if (!mPreRenderPlayer) {
            mPlayer.setStartTime((int) startTime);
        }
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
        L.e(this, "reset", "unsupported reset method, stop instead");
        resetInner();
        mPlayer.stop();
        setState(Player.STATE_IDLE);
    }

    private void resetInner() {
        mSuperResolutionInit = false;
        mPreRenderPlayer = false;
        mSurface = null;
        mSurfaceHolder = null;
        mStartTime = 0L;
        mPausedWhenChangeAVTrack = false;
        mPlaybackTimeWhenChangeAVTrack = 0L;
        mSource = null;
        mStrategySource = null;
        mCurrentTrack.clear();
        mPendingTrack.clear();
        mPlaybackParams.setPitch(1f);
        mPlaybackParams.setSpeed(1f);
        mListener = null;
        mPlayerException = null;
    }

    @Override
    public void release() {
        if (mState == Player.STATE_RELEASED) {
            return;
        }
        mPlayer.setIsMute(true);
        resetInner();
        mPlayer.releaseAsync();
        setState(Player.STATE_RELEASED);
    }

    @Override
    public long getDuration() {
        if (isInPlaybackState()) {
            return mPlayer.getDuration();
        }
        return 0L;
    }

    @Override
    public long getCurrentPosition() {
        if (isInPlaybackState()) {
            int reason = mPlayer.getIntOption(TTVideoEngine.PLAYER_OPTION_EFFECT_NOT_USE_REASON);
            L.d(this, "getCurrentPosition", reason);
            return mPlayer.getCurrentPlaybackTime();
        }
        return 0L;
    }

    @Override
    public int getBufferedPercentage() {
        return mPlayer.getLoadedProgress();
    }

    @Override
    public int getVideoWidth() {
        return mPlayer.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
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
        if (VolcSettings.OPTION_USE_AUDIO_TRACK_VOLUME) {
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
        if (!VolcSettings.OPTION_USE_AUDIO_TRACK_VOLUME) {
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

    private boolean mSuperResolutionInit;

    /**
     * 设置是否开启超分；
     * 针对一个 TTVideoEngine 实例首次开启需要在 play 之前调用；
     * 开启后，可在播放过程中调用，动态开关。true 开启，false 关闭。
     */
    @Override
    public void setSuperResolutionEnabled(boolean enabled) {

        initSuperResolution();

        mPlayer.openTextureSR(true, enabled);

        if (isInPlaybackState() && !isPlaying()) {
            mPlayer.forceDraw();
        }
    }

    public void initSuperResolution() {
        if (mSuperResolutionInit) return;
        mSuperResolutionInit = true;

        // 必须要保障该文件夹路径是存在的，并且可读写的
        File file = new File(mContext.getFilesDir(), "bytedance/playerkit/volcplayer/bmf");
        if (!file.exists()) {
            file.mkdirs();
        }
        // 是否异步初始化超分
        mPlayer.asyncInitSR(true);
        // 设置播放过程中可动态控制关闭 or 开启超分
        mPlayer.dynamicControlSR(true);
        // 设置超分参数，第一个参数为超分算法，推荐使用 0（2 倍超分）
        mPlayer.setSRInitConfig(0, file.getAbsolutePath(), "SR", "SR");
        // 超分播放忽视分辨率限制，推荐使用
        mPlayer.ignoreSRResolutionLimit(true);
        mPlayer.openTextureSR(true, false);
    }

    @Override
    public boolean isSuperResolutionEnabled() {
        return mPlayer != null && mPlayer.isplaybackUsedSR();
    }

    @Override
    public String dump() {
        return L.obj2String(this)
                + " " + resolvePlayerDecoderType(mPlayer)
                + " " + resolvePlayerCoreType(mPlayer)
                + (mPreRenderPlayer ? " pre" : "");
    }

    private boolean isInPlaybackState() {
        switch (mState) {
            case Player.STATE_PREPARED:
            case Player.STATE_STARTED:
            case Player.STATE_PAUSED:
            case Player.STATE_COMPLETED:
                return true;
            default:
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
        mPlayerException = e;
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

            MediaSource source = player.mSource;
            if (source == null) return;

            player.setState(Player.STATE_PREPARED);

            String enginePlayerType = resolvePlayerCoreType(engine);
            L.v(player, "onPrepared", "enginePlayerType", engine, enginePlayerType);

            @MediaSource.SourceType final int sourceType = source.getSourceType();
            @MediaSource.MediaType final int mediaType = source.getMediaType();
            @Track.TrackType final int trackType = mediaType == MediaSource.MEDIA_TYPE_AUDIO ? TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;

            //TODO dash abr
            final Track current = player.getCurrentTrack(trackType);
            final Track pending = player.removePendingTrack(trackType);

            if (pending != null) {
                player.setCurrentTrack(trackType, pending);
                listener.onTrackChanged(player, trackType, current, pending);

                if (sourceType == MediaSource.SOURCE_TYPE_URL) {
                    if (player.mPlaybackTimeWhenChangeAVTrack > 0) {
                        player.seekTo(player.mPlaybackTimeWhenChangeAVTrack);
                        player.mPlaybackTimeWhenChangeAVTrack = 0;
                    }
                }
            }

            if (player.isSupportSmoothTrackSwitching(trackType)) {
                listener.onPrepared(player);
            } else {
                if (current == null) {
                    listener.onPrepared(player);
                } else {
                    if (player.mPausedWhenChangeAVTrack) {
                        player.mPausedWhenChangeAVTrack = false;
                        player.setState(Player.STATE_PAUSED);
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

            listener.onInfo(player, PlayerAdapter.Info.MEDIA_INFO_BUFFERING_START, 0);
        }

        @Override
        public void onBufferEnd(final int code) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return;
            Listener listener = player.mListener;
            if (listener == null) return;

            listener.onInfo(player, PlayerAdapter.Info.MEDIA_INFO_BUFFERING_END, 0);
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

            listener.onInfo(player, PlayerAdapter.Info.MEDIA_INFO_VIDEO_RENDERING_START, 0/*TODO*/);
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

            Track current = player.getCurrentTrack(TRACK_TYPE_VIDEO);
            Track track = player.getPendingTrack(TRACK_TYPE_VIDEO);

            final Quality quality = Mapper.resolution2Quality(resolution);

            if (track != null && Objects.equals(track.getQuality(), quality)) {
                player.removePendingTrack(TRACK_TYPE_VIDEO);
                player.setCurrentTrack(TRACK_TYPE_VIDEO, track);
                listener.onTrackChanged(player, TRACK_TYPE_VIDEO, current, track);
            }
        }

        @Override
        public boolean onFetchedVideoInfo(VideoModel videoModel) {
            VolcPlayer player = mPlayerRef.get();
            if (player == null) return false;
            Listener listener = player.mListener;
            if (listener == null) return false;

            // NOTE: fix onFetchedVideoInfo callback multi times
            if (player.isInPlaybackState()) return false;

            MediaSource source = player.mSource;
            if (source == null) return false;

            Mapper.updateMediaSource(source, videoModel);

            listener.onSourceInfoLoadComplete(player, SourceLoadInfo.SOURCE_INFO_PLAY_INFO_FETCHED, source);

            @Track.TrackType
            int trackType = source.getMediaType() == MediaSource.MEDIA_TYPE_AUDIO ? TRACK_TYPE_AUDIO : TRACK_TYPE_VIDEO;

            player.selectPlayTrackInTrackInfoReady(source, trackType);
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

            listener.onInfo(player, Info.MEDIA_INFO_AUDIO_RENDERING_START, 0);
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

    @NonNull
    private static String resolvePlayerCoreType(TTVideoEngine engine) {
        String enginePlayerType = "";
        if (engine != null) {
            if (engine.isPlayerType(TTVideoEngine.PLAYER_TYPE_EXO)) {
                enginePlayerType = "exo";
            } else if (engine.isPlayerType(TTVideoEngine.PLAYER_TYPE_OWN)) {
                enginePlayerType = "own";
            } else if (engine.isPlayerType(TTVideoEngine.PLAYER_TYPE_OS)) {
                enginePlayerType = "os";
            }
        }
        return enginePlayerType;
    }
}
