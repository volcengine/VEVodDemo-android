/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/7/26
 */

package com.bytedance.playerkit.player.volcengine;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Surface;

import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.bytedance.playerkit.player.source.MediaSource;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.VideoEngineCallback;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.strategy.prerender.PreRenderSurfaceHolder;
import com.ss.ttvideoengine.strategy.prerender.PreRenderSurfaceHolder.SurfaceListener;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.Error;

/**
 * Recommend for prerender strategy version 2.
 * <p>
 * See {@link TTSDKVodInit#initVod(VolcPlayerInitConfig)} and {@link VolcEngineStrategy#init()}.</br>
 * 1. {@link StrategyManager#setVersion(int)} with {@link StrategyManager#VERSION_2}</br>
 * 2. {@link StrategyManager#enable(int, int)} with {@link StrategyManager#STRATEGY_TYPE_PRELOAD} + {@link StrategyManager#STRATEGY_TYPE_PRE_RENDER}</br>
 * 3. {@link StrategyManager#setPreRenderSurfaceHolder(String, PreRenderSurfaceHolder)} with {@link PreRenderSurfaceHolder}.</br>
 */
public class VolcPreRenderSurfaceHolder {

    public interface PreRenderListener {
        @MainThread
        default void onPreRenderVideoSizeChanged(MediaSource mediaSource, int videoWidth, int videoHeight) {
        }

        @MainThread
        default void onPreRenderFirstFrame(MediaSource mediaSource, int videoWidth, int videoHeight) {
        }

        @MainThread
        default void onPreRenderEndError(MediaSource mediaSource, int errorCode) {
        }

        @MainThread
        default void onPreRenderEndRelease(MediaSource mediaSource) {
        }
    }

    private static Handler sHandler;
    private SurfaceListener mSurfaceListener;
    private Surface mSurface;
    private MediaSource mSource;
    private PreRenderListener mPreRenderListener;
    private final PreRenderSurfaceHolder mHolder = new PreRenderSurfaceHolder() {
        @Override
        @WorkerThread
        public Surface getSurface() {
            synchronized (this) {
                return mSurface;
            }
        }

        @Override
        @WorkerThread
        public void setSurfaceListener(SurfaceListener listener) {
            handler().post(() -> mSurfaceListener = listener);
        }

        @Override
        public void bindVideoEngine(StrategySource source, TTVideoEngine videoEngine) {
            videoEngine.addVideoEngineCallback(new VideoEngineCallback() {

                @Override
                public void onPrepared(TTVideoEngine engine) {
                    handler().post(() -> {
                        if (!TextUtils.equals(engine.getVideoID(), mSource.getMediaId())) {
                            return;
                        }
                        if (mPreRenderListener != null) {
                            mPreRenderListener.onPreRenderVideoSizeChanged(mSource,
                                    engine.getVideoWidth(),
                                    engine.getVideoHeight());
                        }
                    });
                }

                @Override
                public void onReadyForDisplay(TTVideoEngine engine) {
                    engine.removeVideoEngineCallback(this);
                    handler().post(() -> {
                        if (!TextUtils.equals(engine.getVideoID(), mSource.getMediaId())) {
                            return;
                        }
                        if (mPreRenderListener != null) {
                            mPreRenderListener.onPreRenderFirstFrame(mSource,
                                    engine.getVideoWidth(),
                                    engine.getVideoHeight());
                        }
                    });
                }

                @Override
                public void onRenderStart(TTVideoEngine engine) {
                    videoEngine.removeVideoEngineCallback(this);
                }

                @Override
                public void onError(Error error) {
                    videoEngine.removeVideoEngineCallback(this);
                    handler().post(() -> {
                        if (!TextUtils.equals(videoEngine.getVideoID(), mSource.getMediaId())) {
                            return;
                        }
                        if (mPreRenderListener != null) {
                            mPreRenderListener.onPreRenderEndError(mSource, error.code);
                        }
                    });
                }

                @Override
                public void onPlaybackStateChanged(TTVideoEngine engine, int playbackState) {
                    if (videoEngine.isReleased()) {
                        videoEngine.removeVideoEngineCallback(this);
                        handler().post(() -> {
                            if (!TextUtils.equals(videoEngine.getVideoID(), mSource.getMediaId())) {
                                return;
                            }
                            if (mPreRenderListener != null) {
                                mPreRenderListener.onPreRenderEndRelease(mSource);
                            }
                        });
                    }
                }
            });
        }
    };

    @MainThread
    public void setPreRenderListener(PreRenderListener listener) {
        this.mPreRenderListener = listener;
    }

    @MainThread
    public void onSurfaceAvailable(Surface surface, int width, int height) {
        synchronized (mHolder) {
            mSurface = surface;
        }
        final SurfaceListener listener = mSurfaceListener;
        if (listener != null) {
            listener.onSurfaceAvailable(surface, width, height);
        }
    }

    @MainThread
    public void onSurfaceDestroyed(Surface surface) {
        synchronized (mHolder) {
            mSurface = null;
        }
        final SurfaceListener listener = mSurfaceListener;
        if (listener != null) {
            listener.onSurfaceDestroyed(surface);
        }
    }

    @MainThread
    public void onVideoViewBindDataSource(MediaSource source) {
        this.mSource = source;
        if (source != null) {
            StrategyManager.instance().setPreRenderSurfaceHolder(source.getMediaId(), mHolder);
        }
    }

    private static synchronized Handler handler() {
        if (sHandler == null) {
            sHandler = new Handler(Looper.getMainLooper());
        }
        return sHandler;
    }
}
