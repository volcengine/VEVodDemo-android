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
 * Create Date : 2021/2/28
 */
package com.bytedance.volc.voddemo.videoview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.VodApp;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.preload.PreloadManager;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerEvent;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;
import com.bytedance.volc.voddemo.videoview.layer.LayerRoot;
import com.bytedance.volc.voddemo.utils.TimerTaskManager;
import com.ss.ttvideoengine.utils.Error;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;

public class VOLCVideoView extends FrameLayout
        implements VideoPlayListener, TimerTaskManager.TaskListener {
    private static final String TAG = "ByteVideoView";

    private TextureView mTextureView;
    private VideoController mVideoController;
    private boolean mNeedPlayOnResume;
    private final LayerRoot mLayerRoot;
    private final DisplayMode mDisplayMode = new DisplayMode();
    private TimerTaskManager mProgressManager;

    public VOLCVideoView(@NonNull Context context) {
        this(context, null);
    }

    public VOLCVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VOLCVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VOLCVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mLayerRoot = new LayerRoot(context);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ByteVideoView,
                defStyleAttr, 0);
        initView(context, a);
    }

    private void initView(Context context, TypedArray a) {
        setBackgroundColor(ContextCompat.getColor(context, R.color.black));

        mTextureView = new TextureView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addView(mTextureView, layoutParams);

        FrameLayout.LayoutParams LayerRootLayoutParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        addView(mLayerRoot, LayerRootLayoutParams);

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                    int height) {
                if (mVideoController != null) {
                    mVideoController.setSurface(new Surface(mTextureView.getSurfaceTexture()));
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull final SurfaceTexture surface,
                    final int width,
                    final int height) {
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                if (mVideoController != null) {
                    mVideoController.setSurface(null);
                }
                return true;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull final SurfaceTexture surface) {
            }
        });

        mDisplayMode.setContainerView(this);
        mDisplayMode.setDisplayView(mTextureView);
    }

    public void setVideoController(VideoController videoController) {
        mVideoController = videoController;

        if (mLayerRoot != null) {
            mLayerRoot.setVideoController(mVideoController);
        }
    }

    public void play() {
        TTVideoEngineLog.d(TAG, "play");
        if (mVideoController == null) {
            return;
        }

        if (mTextureView != null && mTextureView.isAvailable()) {
            mVideoController.setSurface(new Surface(mTextureView.getSurfaceTexture()));
        }

        mVideoController.play();
    }

    public void pause() {
        if (mVideoController != null) {
            mVideoController.pause();
        }
    }

    public void mute() {
        if (mVideoController != null) {
            mVideoController.mute();
        }
    }

    public void release() {
        if (mProgressManager != null) {
            mProgressManager.release();
            mProgressManager = null;
        }

        if (mVideoController != null) {
            mVideoController.release();
        }
    }

    public void onResume() {
        if (mNeedPlayOnResume && mVideoController != null) {
            mVideoController.play();
        }
    }

    public void onPause() {
        if (mVideoController == null) {
            mNeedPlayOnResume = false;
            return;
        }
        mNeedPlayOnResume = mVideoController.isPlaying();
        mVideoController.pause();
    }

    public void setDisplayMode(int displayMode) {
        this.mDisplayMode.setDisplayMode(displayMode);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDisplayMode.apply();
    }

    public void addLayer(final ILayer layer) {
        mLayerRoot.addLayer(layer);
    }

    @Override
    public void onVideoSizeChanged(final int width, final int height) {
        mDisplayMode.setVideoSize(width, height);
    }

    @Override
    public void onCallPlay() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY));
    }

    @Override
    public void onPrepare() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PREPARE));
    }

    @Override
    public void onPrepared() {
        if (mVideoController != null) {
            int videoWidth = mVideoController.getVideoWidth();
            int videoHeight = mVideoController.getVideoHeight();
            mDisplayMode.setVideoSize(videoWidth, videoHeight);
        }
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PREPARED));
    }

    @Override
    public void onRenderStart() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START));
    }

    @Override
    public void onVideoPlay() {
        setKeepScreenOn(true);
        startProgressTrack();
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PLAYING));
    }

    @Override
    public void onVideoPause() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PAUSE));
    }

    @Override
    public void onBufferStart() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_START));
    }

    @Override
    public void onBufferingUpdate(final int percent) {
        notifyEvent(
                new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_UPDATE, percent));
    }

    @Override
    public void onBufferEnd() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_END));
    }

    @Override
    public void onStreamChanged(final int type) {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_STREAM_CHANGED, type));
    }

    @Override
    public void onVideoCompleted() {
        if (!mVideoController.isLooping()) {
            stopProgressTrack();
        }

        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_COMPLETE));
    }

    @Override
    public void onVideoPreRelease() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE));
    }

    @Override
    public void onVideoReleased() {
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_RELEASE));
    }

    @Override
    public void onError(final VideoItem videoItem, final Error error) {
        stopProgressTrack();
        notifyEvent(new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR, error));
    }

    @Override
    public void onFetchVideoModel(final int videoWidth, final int videoHeight) {
        mDisplayMode.setVideoSize(videoWidth, videoHeight);
    }

    @Override
    public void onVideoSeekComplete(final boolean success) {
        if (mVideoController != null && mVideoController.isPlaying()) {
            startProgressTrack();
        }
    }

    @Override
    public void onVideoSeekStart(final int msec) {
        stopProgressTrack();
    }

    private void notifyEvent(IVideoLayerEvent event) {
        if (event == null) {
            return;
        }

        mLayerRoot.notifyEvent(event);
    }

    public void refreshLayers() {
        mLayerRoot.refreshLayers();
    }

    @Override
    public long onTaskExecute() {
        if (mVideoController != null) {
            int position = mVideoController.getCurrentPlaybackTime();
            int duration = mVideoController.getDuration();
            if (position >= duration) {
                position = duration;
            }

            onProgressUpdate(position, duration);
        }
        return 500;
    }

    private void onProgressUpdate(final int position, final int duration) {
        Pair<Integer, Integer> pair = new Pair<>(position, duration);
        CommonLayerEvent progressChangeEvent = new CommonLayerEvent(
                IVideoLayerEvent.VIDEO_LAYER_EVENT_PROGRESS_CHANGE, pair);
        notifyEvent(progressChangeEvent);
    }

    private void startProgressTrack() {
        TTVideoEngineLog.d(TAG, "startProgressTrack");
        if (mProgressManager == null) {
            mProgressManager = new TimerTaskManager(Looper.getMainLooper(), this);
        }

        mProgressManager.startTracking();
    }

    private void stopProgressTrack() {
        TTVideoEngineLog.d(TAG, "stopProgressTrack");
        if (mProgressManager != null) {
            mProgressManager.stopTracking();
        }
    }
}
