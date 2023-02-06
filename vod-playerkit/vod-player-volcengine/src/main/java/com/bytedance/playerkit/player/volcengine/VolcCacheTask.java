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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.SparseArray;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.cache.CacheException;
import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.utils.L;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.IPreLoaderItemCallBackListener;
import com.ss.ttvideoengine.PreLoaderItemCallBackInfo;
import com.ss.ttvideoengine.PreloaderURLItem;
import com.ss.ttvideoengine.PreloaderVidItem;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.utils.Error;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


class VolcCacheTask implements CacheLoader.Task {

    public static class Factory implements CacheLoader.Task.Factory {

        private final Context mContext;

        public Factory(Context context) {
            this.mContext = context;
        }

        @Override
        public CacheLoader.Task create() {
            return new VolcCacheTask(mContext);
        }
    }

    private final SparseArray<Track> mSelectedTrack = new SparseArray<>();
    private final SparseArray<List<Track>> mTracks = new SparseArray<>();

    private final Context mContext;
    private final Handler mHandler;

    private MediaSource mSource;
    private TrackSelector mTrackSelector;
    private Listener mListener;

    private final List<Listener> mDepListeners = new ArrayList<>();
    private CacheKeyFactory mCacheKeyFactory;

    private int mState;

    private Error mError;

    VolcCacheTask(Context context) {
        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        setState(STATE_IDLE);
    }

    private void setState(int state) {
        synchronized (this) {
            if (mState != state) {
                L.v(this, "setState", mapState(mState), mapState(state));
            }
            mState = state;
        }
    }

    @Override
    public void setDataSource(@NonNull MediaSource source) {
        L.v(this, "setDataSource", MediaSource.dump(source));
        this.mSource = source;
    }

    @Override
    public MediaSource getDataSource() {
        return mSource;
    }

    @Override
    public void setCacheKeyFactory(CacheKeyFactory keyFactory) {
        mCacheKeyFactory = keyFactory;
    }

    @Override
    public void setTrackSelector(TrackSelector trackSelector) {
        this.mTrackSelector = trackSelector;
    }

    @Override
    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    @Override
    public void addDepListener(Listener listener) {
        this.mDepListeners.add(listener);
        synchronized (this) {
            switch (mState) {
                case STATE_STARTED:
                    notifyStartEvent();
                    break;
                case STATE_END_FINISHED:
                    notifyStartEvent();
                    notifyEndFinishEvent();
                    break;
                case STATE_END_ERROR:
                    notifyStartEvent();
                    notifyEndError(mError);
                    break;
                case STATE_END_STOPPED:
                    notifyStartEvent();
                    notifyEndStopped();
                    break;
            }
        }
    }

    private void selectTrack(@Track.TrackType int type, @Nullable Track track) {
        synchronized (this) {
            mSelectedTrack.put(type, track);
        }
    }

    @Override
    public Track getSelectedTrack(@Track.TrackType int type) {
        synchronized (this) {
            return mSelectedTrack.get(type);
        }
    }

    @Override
    public List<Track> getTracks(@Track.TrackType int type) {
        synchronized (this) {
            return mTracks.get(type);
        }
    }

    @CallSuper
    @Override
    public void start() {
        final MediaSource source = getDataSource();
        if (source == null) return;

        synchronized (this) {
            if (mState != STATE_IDLE) {
                return;
            }
        }

        L.v(this, "start", source.dump());

        setState(STATE_STARTED);

        notifyStartEvent();
        final int sourceType = source.getSourceType();
        switch (sourceType) {
            case MediaSource.SOURCE_TYPE_ID:
                preloadVid(source);
                break;
            case MediaSource.SOURCE_TYPE_URL:
                preloadUrl(source);
                break;
        }
    }


    @Override
    public void stop() {
        MediaSource source = getDataSource();
        if (source == null) return;

        synchronized (this) {
            if (isEnd()) {
                return;
            }
        }

        L.v(this, "stop", source.dump());

        final int sourceType = source.getSourceType();
        switch (sourceType) {
            case MediaSource.SOURCE_TYPE_ID:
                stopByVid(source.getMediaId());
                break;
            case MediaSource.SOURCE_TYPE_URL:
                stopByUrl(source);
                break;
        }

        setState(STATE_END_STOPPED);
    }

    @Override
    public boolean isStarted() {
        synchronized (this) {
            return mState == STATE_STARTED;
        }
    }

    @Override
    public boolean isStopped() {
        synchronized (this) {
            return mState == STATE_END_STOPPED;
        }
    }

    @Override
    public boolean isEnd() {
        synchronized (this) {
            return mState == STATE_END_STOPPED ||
                    mState == STATE_END_FINISHED ||
                    mState == STATE_END_ERROR;
        }
    }

    @Override
    public File getCacheDir() {
        return VolcPlayerInit.cacheDir(mContext);
    }

    private void preloadVid(MediaSource source) {
        final VidPlayAuthTokenSource vidSource = Mapper.mediaSource2VidPlayAuthTokenSource(source, null);
        final long preloadSize = resolvePreloadSize(source, null);
        final PreloaderVidItem preloadItem = new PreloaderVidItem(vidSource, preloadSize);

        L.v(VolcCacheTask.this, "preloadVid", source.getMediaId(), "start", preloadSize);
        preloadItem.setCallBackListener(new IPreLoaderItemCallBackListener() {
            @Override
            public void preloadItemInfo(PreLoaderItemCallBackInfo info) {
                if (info == null) return;
                final int key = info.getKey();
                switch (key) {
                    case PreLoaderItemCallBackInfo.KEY_IS_FETCH_END_VIDEOMODEL: {
                        VideoModel videoModel = info.fetchVideoModel;
                        if (videoModel == null) return;
                        Mapper.updateMediaSource(source, videoModel);
                        Track target = resolvePreloadTrack(source);
                        if (target != null) {
                            preloadItem.mResolution = Mapper.track2Resolution(target);
                            long preloadSize = resolvePreloadSize(source, target);
                            preloadItem.mPreloadSize = preloadSize;

                            L.v(VolcCacheTask.this, "preloadVid",
                                    source.getMediaId(),
                                    "fetchedPlayInfo",
                                    "selected", Track.dump(target),
                                    "all", Track.dump(source.getTracks()),
                                    "preloadSize", preloadSize);
                        }
                        break;
                    }
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_SUCCEED: {
                        DataLoaderHelper.DataLoaderTaskProgressInfo cacheInfo = info.preloadDataInfo;
                        if (cacheInfo != null) {
                            Track selected = VolcCacheTask.this.getSelectedTrack(Track.TRACK_TYPE_VIDEO);
                            L.v(VolcCacheTask.this, "preloadVid",
                                    source.getMediaId(),
                                    "success",
                                    "selected", Track.dump(selected),
                                    "cacheKey", cacheInfo.mKey,
                                    "preloadSize", preloadItem.mPreloadSize,
                                    "mediaSize", cacheInfo.mMediaSize,
                                    "cachedSize", cacheInfo.mCacheSizeFromZero,
                                    "path", cacheInfo.mLocalFilePath);

                            VolcCacheTask.this.setState(STATE_END_FINISHED);
                            VolcCacheTask.this.notifyEndFinishEvent();
                        }
                        break;
                    }
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_FAIL: {
                        Track selected = VolcCacheTask.this.getSelectedTrack(Track.TRACK_TYPE_VIDEO);
                        L.v(VolcCacheTask.this, "preloadVid",
                                source.getMediaId(),
                                "error",
                                "selected", Track.dump(selected));

                        VolcCacheTask.this.setState(STATE_END_ERROR);
                        VolcCacheTask.this.notifyEndError(info.preloadError);
                        break;
                    }
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_CANCEL: {
                        Track selected = VolcCacheTask.this.getSelectedTrack(Track.TRACK_TYPE_VIDEO);
                        L.v(VolcCacheTask.this, "preloadVid",
                                source.getMediaId(),
                                "cancel",
                                "selected", Track.dump(selected));

                        VolcCacheTask.this.setState(STATE_END_STOPPED);
                        VolcCacheTask.this.notifyEndStopped();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        });
        DataLoaderHelper.getDataLoader().addTask(preloadItem);
    }

    private void preloadUrl(MediaSource source) {
        final Track target = resolvePreloadTrack(source);
        if (target == null) return;

        DirectUrlSource directUrlSource = Mapper.mediaSource2DirectUrlSource(source, target, mCacheKeyFactory);
        final long preloadSize = resolvePreloadSize(source, target);

        final PreloaderURLItem preloadItem = new PreloaderURLItem(directUrlSource, preloadSize);

        final Map<String, String> headers = target.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (!TextUtils.isEmpty(key) && TextUtils.isEmpty(value)) {
                    preloadItem.setCustomHeader(key, value);
                }
            }
        }

        L.v(VolcCacheTask.this, "preloadUrl", source.getMediaId(), "start",
                "selected", Track.dump(target),
                "all", Track.dump(source.getTracks()),
                "preloadSize", preloadSize,
                "url", target.getUrl());

        preloadItem.setCallBackListener(new IPreLoaderItemCallBackListener() {
            @Override
            public void preloadItemInfo(PreLoaderItemCallBackInfo info) {
                if (info == null) return;
                final int key = info.getKey();
                switch (key) {
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_SUCCEED: {
                        DataLoaderHelper.DataLoaderTaskProgressInfo cacheInfo = info.preloadDataInfo;
                        if (cacheInfo != null) {
                            String cacheKey = cacheInfo.mKey;
                            String preloadTaskKey = cacheInfo.mKey;
                            String videoId = cacheInfo.mVideoId;
                            String cachePath = cacheInfo.mLocalFilePath;
                            long mediaSize = cacheInfo.mMediaSize;
                            long cachedSize = cacheInfo.mCacheSizeFromZero;

                            Track selected = getSelectedTrack(Track.TRACK_TYPE_VIDEO);
                            L.v(VolcCacheTask.this, "preloadUrl",
                                    source.getMediaId(),
                                    "success",
                                    "selected", Track.dump(selected),
                                    "cacheKey", cacheKey,
                                    "preloadSize", preloadSize,
                                    "mediaSize", mediaSize,
                                    "cachedSize", cachedSize,
                                    "path", cachePath);

                            setState(STATE_END_FINISHED);

                            notifyEndFinishEvent();
                        }
                        break;
                    }
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_FAIL: {
                        Track selected = getSelectedTrack(Track.TRACK_TYPE_VIDEO);
                        L.v(VolcCacheTask.this, "preloadUrl",
                                source.getMediaId(),
                                "error",
                                "selected", Track.dump(selected));

                        setState(STATE_END_ERROR);
                        notifyEndError(info.preloadError);
                        break;
                    }
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_CANCEL: {
                        Track selected = getSelectedTrack(Track.TRACK_TYPE_VIDEO);
                        L.v(VolcCacheTask.this, "preloadUrl",
                                source.getMediaId(),
                                "cancel",
                                "selected", Track.dump(selected));

                        setState(STATE_END_STOPPED);
                        notifyEndStopped();
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        });

        DataLoaderHelper.getDataLoader().addTask(preloadItem);
    }


    private void notifyStartEvent() {
        notify(() -> {
            if (mListener != null) {
                mListener.onStart(VolcCacheTask.this);
            }
            for (Listener listener : mDepListeners) {
                listener.onStart(VolcCacheTask.this);
            }
        });
    }

    private void notifyEndFinishEvent() {
        notify(() -> {
            if (mListener != null) {
                mListener.onFinished(VolcCacheTask.this);
            }

            for (Listener listener : mDepListeners) {
                listener.onFinished(VolcCacheTask.this);
            }
        });
    }

    private void notifyEndError(Error error) {
        mError = error;
        notify(() -> {

            final CacheException e = error != null ? new CacheException(error.code, error.toString())
                    : new CacheException(0, "error is null!");

            if (mListener != null) {
                mListener.onError(VolcCacheTask.this, e);
            }

            for (Listener listener : mDepListeners) {
                listener.onError(VolcCacheTask.this, e);
            }
        });
    }


    private void notifyEndStopped() {
        notify(() -> {
            if (mListener != null) {
                mListener.onStopped(VolcCacheTask.this);
            }

            for (Listener listener : mDepListeners) {
                listener.onStopped(VolcCacheTask.this);
            }
        });
    }

    public void notify(Runnable runnable) {
        if (Looper.myLooper() == mHandler.getLooper()) {
            runnable.run();
        } else {
            mHandler.post(runnable);
        }
    }

    private static long resolvePreloadSize(@Nullable MediaSource source, @Nullable Track target) {
        long preloadSize = target != null ? target.getPreloadSize() : 0;
        if (preloadSize > 0) {
            return preloadSize;
        }
        Long l = source != null ? source.getExtra(CacheLoader.Task.EXTRA_PRELOAD_SIZE_IN_BYTES, Long.class) : null;
        preloadSize = l != null ? l : 0;
        if (preloadSize > 0) {
            return preloadSize;
        }
        return DEFAULT_PRELOAD_SIZE;
    }

    @Nullable
    private Track resolvePreloadTrack(MediaSource source) {
        if (mListener != null) {
            mListener.onDataSourcePrepared(this, source);
        }
        List<Track> tracks = source.getTracks(Track.TRACK_TYPE_VIDEO);
        if (tracks == null || tracks.isEmpty()) {
            return null;
        }
        //TODO auto quality
        Track target = getSelectedTrack(Track.TRACK_TYPE_VIDEO);
        if (target == null) {
            if (mTrackSelector != null) {
                target = mTrackSelector.selectTrack(TrackSelector.TYPE_PRELOAD, Track.TRACK_TYPE_VIDEO, tracks, source);
            }
            if (target == null) {
                target = tracks.get(0);
            }
            selectTrack(Track.TRACK_TYPE_VIDEO, target);
        }
        return target;
    }

    private void stopByVid(String id) {
        DataLoaderHelper.getDataLoader().cancelTaskByVideoId(id);
    }

    private void stopByUrl(MediaSource source) {
        final Track track = getSelectedTrack(Track.TRACK_TYPE_VIDEO);
        if (track != null) {
            String cacheKey = mCacheKeyFactory.generateCacheKey(source, track);
            DataLoaderHelper.getDataLoader().cancelTask(cacheKey);
        }
    }
}
