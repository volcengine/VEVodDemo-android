/*
 * Copyright 2021 bytedance
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
 * Create Date : 2021/6/11
 */
package com.bytedance.volc.voddemo.videoview;

import android.content.Context;
import android.view.Surface;
import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.BuildConfig;
import com.bytedance.volc.voddemo.VodApp;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.preload.PreloadManager;
import com.bytedance.volc.voddemo.preload.PreloadStrategy;
import com.bytedance.volc.voddemo.settings.ClientSettings;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.SeekCompletionListener;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.VideoEngineSimpleCallback;
import com.ss.ttvideoengine.VideoInfoListener;
import com.ss.ttvideoengine.model.VideoInfo;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.Error;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.List;

import static com.bytedance.volc.voddemo.utils.ThreadUtils.runOnWorkThread;
import static com.ss.ttvideoengine.TTVideoEngine.PLAYER_OPTION_ENABLE_DATALOADER;
import static com.ss.ttvideoengine.TTVideoEngine.PLAYER_OPTION_OUTPUT_LOG;
import static com.ss.ttvideoengine.TTVideoEngine.PLAYER_OPTION_USE_VIDEOMODEL_CACHE;

public class VOLCVideoController implements VideoController, VideoInfoListener {
    private static final String TAG = "VOLCVideoController";

    private final ClientSettings mSettings = VodApp.getClientSettings();
    private final Context mContext;
    private final VideoItem mVideoItem;
    private final StrategySource mStrategySource;

    private final VideoPlayListener mVideoPlayListener;
    private TTVideoEngine mVideoEngine;
    private Surface mSurface;

    private boolean mPrepared;
    private boolean mPlayAfterSurfaceValid;

    private final SeekCompletionListener mSeekCompletionListener = new SeekCompletionListener() {
        @Override
        public void onCompletion(boolean success) {
            onSeekComplete(success);
        }
    };

    private final VideoEngineSimpleCallback mVideoEngineCallback = new VideoEngineSimpleCallback() {
        @Override
        public void onRenderStart(final TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onRenderStart");
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onRenderStart();
            }
        }

        @Override
        public void onBufferStart(final int reason, final int afterFirstFrame, final int action) {
            TTVideoEngineLog.d(TAG, "onBufferStart reason " + reason
                                    + ", afterFirstFrame " + afterFirstFrame
                                    + ", action " + action);
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onBufferStart();
            }
        }

        @Override
        public void onBufferEnd(final int code) {
            TTVideoEngineLog.d(TAG, "onBufferEnd code " + code);
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onBufferEnd();
            }
        }

        @Override
        public void onPlaybackStateChanged(TTVideoEngine engine, int playbackState) {
            TTVideoEngineLog.d(TAG, "onPlaybackStateChanged " + playbackState);
            switch (playbackState) {
                case TTVideoEngine.PLAYBACK_STATE_PLAYING:
                    if (mVideoPlayListener != null) {
                        mVideoPlayListener.onVideoPlay();
                    }
                    break;
                case TTVideoEngine.PLAYBACK_STATE_PAUSED:
                    if (mVideoPlayListener != null) {
                        mVideoPlayListener.onVideoPause();
                    }
                default:
                    break;
            }
        }

        @Override
        public void onVideoSizeChanged(TTVideoEngine engine, int width, int height) {
            TTVideoEngineLog.d(TAG, "onVideoSizeChanged width " + width + ", height " + height);
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onVideoSizeChanged(width, height);
            }
        }

        @Override
        public void onBufferingUpdate(TTVideoEngine engine, int percent) {
            TTVideoEngineLog.d(TAG, "onBufferingUpdate percent " + percent);
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onBufferingUpdate(percent);
            }

            if (mVideoEngine != null) {
                final int currentPlaybackTime = mVideoEngine.getCurrentPlaybackTime();
                PreloadManager.getInstance()
                        .bufferingUpdate(mVideoEngine.getDuration(), percent, currentPlaybackTime);
            }
        }

        @Override
        public void onPrepare(TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onPrepare");
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onPrepare();
            }
        }

        @Override
        public void onPrepared(TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onPrepared");
            mPrepared = true;
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onPrepared();
            }
        }

        @Override
        public void onStreamChanged(TTVideoEngine engine, int type) {
            TTVideoEngineLog.d(TAG, "onStreamChanged type " + type);
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onStreamChanged(type);
            }
        }

        @Override
        public void onCompletion(TTVideoEngine engine) {
            TTVideoEngineLog.d(TAG, "onCompletion");
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onVideoCompleted();
            }
        }

        @Override
        public void onError(Error error) {
            TTVideoEngineLog.d(TAG, "onError error " + error);
            if (mVideoPlayListener != null) {
                mVideoPlayListener.onError(mVideoItem, error);
            }
        }
    };

    public VOLCVideoController(@NonNull Context context, @NonNull VideoItem mVideoItem,
            VideoPlayListener listener) {
        this.mContext = context;
        this.mVideoItem = mVideoItem;
        String encodeType = VodApp.getClientSettings().videoEnableH265()
                ? TTVideoEngine.CODEC_TYPE_h265 : TTVideoEngine.CODEC_TYPE_H264;
        this.mStrategySource = new VidPlayAuthTokenSource.Builder()
                .setVid(mVideoItem.getVid())
                .setPlayAuthToken(mVideoItem.getAuthToken())
                .setEncodeType(encodeType)
                .build();
        this.mVideoPlayListener = new UiThreadVideoPlayListener(listener);
    }

    private void initEngine() {
        if (mVideoEngine != null) {
            return;
        }

        // VOD key step Strategy PreRender 5: use preRender engine
        mVideoEngine = TTVideoEngine.getPreRenderEngine(mStrategySource);
        if (mVideoEngine != null) {
            mVideoEngineCallback.onPrepared(mVideoEngine);
        } else {
            // VOD key step play 1: init TTVideoEngine with ApplicationContext
            mVideoEngine = new TTVideoEngine(mContext.getApplicationContext(),
                    TTVideoEngine.PLAYER_TYPE_OWN);
            configEngine(mVideoEngine);
            // VOD key step play 4: set source
            // VOD key step Strategy Preload 3: set source
            mVideoEngine.setStrategySource(mStrategySource);
        }
        // VOD key step play 2: set Callback
        mVideoEngine.setVideoEngineSimpleCallback(mVideoEngineCallback);
        mVideoEngine.setVideoInfoListener(this);

        if (mVideoPlayListener != null) {
            mVideoPlayListener.onCallPlay();
        }

        PreloadManager.getInstance().currentVideoChanged(mVideoItem);
    }

    @Override
    public int getDuration() {
        if (mPrepared && mVideoEngine != null) {
            return mVideoEngine.getDuration();
        }
        return mVideoItem.getDuration();
    }

    public void play() {
        runOnWorkThread(this::doPlay);
    }

    private void doPlay() {
        initEngine();

        if (mSurface != null && mSurface.isValid()) {
            // VOD key step play 5: set surface
            mVideoEngine.setSurface(mSurface);
            // VOD key step play 6: play
            mVideoEngine.play();
        } else {
            mPlayAfterSurfaceValid = true;
        }
    }

    public void pause() {
        runOnWorkThread(this::doPause);
    }

    private void doPause() {
        if (mVideoEngine != null) {
            mVideoEngine.pause();
        }
    }

    public void release() {
        runOnWorkThread(this::doRelease);
    }

    private void doRelease() {
        if (mVideoEngine == null) {
            return;
        }
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onVideoPreRelease();
        }

        mPlayAfterSurfaceValid = false;
        mPrepared = false;
        // VOD key step play 7: release
        mVideoEngine.releaseAsync();
        mVideoEngine = null;
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onVideoReleased();
        }
    }

    @Override
    public void mute() {
        runOnWorkThread(this::doMute);
    }

    private void doMute() {
        if (mVideoEngine != null) {
            mVideoEngine.setIsMute(true);
        }
    }

    @Override
    public boolean isPlaying() {
        return mPrepared && mVideoEngine.getPlaybackState() == TTVideoEngine.PLAYBACK_STATE_PLAYING;
    }

    @Override
    public boolean isPaused() {
        return mPrepared && mVideoEngine.getPlaybackState() == TTVideoEngine.PLAYBACK_STATE_PAUSED;
    }

    @Override
    public boolean isLooping() {
        if (mVideoEngine == null) {
            return false;
        }
        return mVideoEngine.isLooping();
    }

    @Override
    public String getCover() {

        return mVideoItem.getCover();
    }

    @Override
    public int getVideoWidth() {
        if (mVideoEngine == null) {
            return 0;
        }
        return mVideoEngine.getVideoWidth();
    }

    @Override
    public int getVideoHeight() {
        if (mVideoEngine == null) {
            return 0;
        }
        return mVideoEngine.getVideoHeight();
    }

    @Override
    public int getCurrentPlaybackTime() {
        if (mVideoEngine == null) {
            return 0;
        }
        return mVideoEngine.getCurrentPlaybackTime();
    }

    public void setSurface(Surface surface) {
        runOnWorkThread(() -> doSetSurface(surface));
    }

    private void doSetSurface(Surface surface) {
        mSurface = surface;
        if (mSurface == null || !mSurface.isValid()) {
            mSurface = null;
            if (mVideoEngine != null) {
                mVideoEngine.setSurface(null);
            }
            return;
        }

        if (mVideoEngine != null) {
            mVideoEngine.setSurface(mSurface);
            if (mPlayAfterSurfaceValid) {
                mVideoEngine.play();
                mPlayAfterSurfaceValid = false;
            }
        } else {
            // VOD key step Strategy PreRender instead of cover 2: call forceDraw when surface valid
            TTVideoEngine preRenderEngine = TTVideoEngine.getPreRenderEngine(mStrategySource);
            if (preRenderEngine != null) {
                preRenderEngine.setSurface(surface);
                preRenderEngine.forceDraw();
            } else {
                // VOD key step Strategy PreRender instead of cover 3: use cover when no PreRender
                mVideoPlayListener.onNeedCover();
            }
        }
    }

    public TTVideoEngine getTTVideoEngine() {
        return mVideoEngine;
    }

    @Override
    public boolean onFetchedVideoInfo(final VideoModel videoModel) {
        if (videoModel == null) {
            return false;
        }

        final List<VideoInfo> videoInfoList = videoModel.getVideoInfoList();
        if (videoInfoList != null && videoInfoList.size() > 0) {
            final VideoInfo videoInfo = videoInfoList.get(0);
            if (videoInfo != null) {
                int width = videoInfo.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VWIDTH);
                int height = videoInfo.getValueInt(VideoInfo.VALUE_VIDEO_INFO_VHEIGHT);
                if (width > 0 && height > 0) {
                    mVideoPlayListener.onFetchVideoModel(width, height);
                }
            }
        }

        return false;
    }

    public void seekTo(int msec) {
        runOnWorkThread(() -> doSeekTo(msec));
    }

    private void doSeekTo(int msec) {
        if (mVideoEngine == null) {
            return;
        }

        mVideoEngine.seekTo(msec, mSeekCompletionListener);

        if (mVideoPlayListener != null) {
            mVideoPlayListener.onVideoSeekStart(msec);
        }
    }

    private void onSeekComplete(final boolean success) {
        TTVideoEngineLog.d(TAG, "seek_complete:" + (success ? "done" : "fail"));
        if (mVideoPlayListener != null) {
            mVideoPlayListener.onVideoSeekComplete(success);
        }
    }

    public static void configEngine(TTVideoEngine engine) {
        ClientSettings settings = VodApp.getClientSettings();
        // VOD key step play 3: use mdl
        engine.setIntOption(PLAYER_OPTION_ENABLE_DATALOADER, 1);

        // VOD key step play 8: other feature
        // use videomodel cache
        engine.setIntOption(PLAYER_OPTION_USE_VIDEOMODEL_CACHE, 1);
        // use video hardware
        if (settings.enableManualVideoHW()) {
            engine.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABEL_HARDWARE_DECODE,
                    settings.enableVideoHW() ? 1 : 0);
        }
        // Loop Playback
        engine.setLooping(true);
        if (BuildConfig.DEBUG) {
            // open debug log
            engine.setIntOption(PLAYER_OPTION_OUTPUT_LOG, 1);
        }
        // enable key message upload：default is enable
        engine.setReportLogEnable(settings.engineEnableUploadLog());
        DataLoaderHelper.getDataLoader().setReportLogEnable(settings.mdlEnableUploadLog());
        // set resolution
        engine.configResolution(PreloadStrategy.START_PLAY_RESOLUTION);
    }
}
