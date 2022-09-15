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

package com.bytedance.playerkit.player.playback;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.VideoView.VideoViewListener;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;

/**
 * {@code VideoLayer} is a holder class of layer view. See {@link VideoLayerHost} class doc
 * for details of <b>Layer System</b>.
 *
 * @see VideoLayerHost
 */
public abstract class VideoLayer extends VideoViewListener.Adapter
        implements VideoLayerHost.VideoLayerHostListener {

    private View mLayerView;
    private VideoLayerHost mLayerHost;
    private boolean mIgnoreLock;

    void bindLayerHost(VideoLayerHost layerHost) {
        if (mLayerHost == null) {
            mLayerHost = layerHost;
            layerHost.addVideoLayerHostListener(this);
            L.v(this, "onBindLayerHost", layerHost);
            onBindLayerHost(layerHost);

            final VideoView videoView = layerHost.videoView();
            if (videoView != null) {
                bindVideoView(videoView);
            }
        }
    }

    void unbindLayerHost(VideoLayerHost layerHost) {
        if (mLayerHost != null && mLayerHost == layerHost) {
            VideoView videoView = layerHost.videoView();
            unbindVideoView(videoView);
            layerHost.removeVideoLayerHostListener(this);
            L.v(this, "onUnbindLayerHost", layerHost);
            mLayerHost = null;
            onUnbindLayerHost(layerHost);
        }
    }

    void bindVideoView(VideoView videoView) {
        if (videoView != null) {
            videoView.addVideoViewListener(this);
            L.v(this, "onBindVideoView", videoView);
            onBindVideoView(videoView);

            final PlaybackController controller = videoView.controller();
            if (controller != null) {
                bindController(controller);
            }
        }
    }

    void unbindVideoView(VideoView videoView) {
        if (videoView != null) {
            final PlaybackController controller = videoView.controller();
            if (controller != null) {
                unbindController(videoView.controller());
            }
            videoView.removeVideoViewListener(this);
            L.v(this, "onUnBindVideoView", videoView);
            onUnBindVideoView(videoView);
        }
    }

    void bindController(PlaybackController controller) {
        if (controller != null) {
            L.v(this, "onBindPlaybackController", controller);
            onBindPlaybackController(controller);
        }
    }

    void unbindController(PlaybackController controller) {
        if (controller != null) {
            L.v(this, "onUnbindPlaybackController", controller);
            onUnbindPlaybackController(controller);
        }
    }

    @Override
    public final void onLayerHostAttachedToVideoView(@NonNull VideoView videoView) {
        bindVideoView(videoView);
    }

    @Override
    public final void onLayerHostDetachedFromVideoView(@NonNull VideoView videoView) {
        unbindVideoView(videoView);
    }

    @Override
    public final void onVideoViewBindController(PlaybackController controller) {
        bindController(controller);
    }

    @Override
    public final void onVideoViewUnbindController(PlaybackController controller) {
        unbindController(controller);
    }

    /**
     * <p>The {@link #mLayerView} will be created and added to {@link VideoLayerHost}'s hostView after
     * calling {@link #show()}.
     *
     * <p>Called when {@link #mLayerView} added to {@link VideoLayerHost}'s hostView.
     *
     * <p>Pair with {@link #onViewRemovedFromHostView(ViewGroup)}.
     *
     * @param hostView the host view of {@link #mLayerHost}
     * @see #show()
     * @see VideoLayerHost#addLayerView(VideoLayer)
     */
    protected void onViewAddedToHostView(ViewGroup hostView) {
    }

    /**
     * <p>The {@link #mLayerView} will be removed from {@link VideoLayerHost}'s hostView after
     * calling {@link #dismiss()}
     *
     * <p>Called when {@link #mLayerView} removed to {@link VideoLayerHost}'s hostView.
     *
     * <p>Pair with {@link #onViewAddedToHostView(ViewGroup)}.
     *
     * @param hostView the host view of {@link #mLayerHost}
     * @see #dismiss()
     * @see VideoLayerHost#removeLayerView(VideoLayer)
     */
    protected void onViewRemovedFromHostView(ViewGroup hostView) {
    }

    /**
     * <p>Called when the {@link VideoLayer} instance add to {@link VideoLayerHost} with
     * {@link VideoLayerHost#addLayer(VideoLayer)}.
     *
     * <p>Pair with {@link #onUnbindLayerHost(VideoLayerHost)}.
     *
     * @param layerHost {@link VideoLayerHost} instance
     */
    protected void onBindLayerHost(@NonNull VideoLayerHost layerHost) {
    }

    /**
     * <p>Called when the {@link VideoLayer} instance remove from {@link VideoLayerHost} with
     * {@link VideoLayerHost#removeLayer(VideoLayer)}.
     *
     * <p>Pair with {@link #onBindLayerHost(VideoLayerHost)}.
     *
     * @param layerHost {@link VideoLayerHost} instance
     */
    protected void onUnbindLayerHost(@NonNull VideoLayerHost layerHost) {
    }

    /**
     * <p>Called when the {@link VideoLayerHost} instance attached to {@link VideoView}
     *
     * <p>Pair with {@link #onUnBindVideoView(VideoView)}
     *
     * @param videoView {@link VideoView} instance
     */
    protected void onBindVideoView(@NonNull VideoView videoView) {
    }

    /**
     * <p>Called when the {@link VideoLayerHost} instance detached from {@link VideoView}
     *
     * <p>Pair with {@link #onBindVideoView(VideoView)}
     *
     * @param videoView {@link VideoView} instance
     */
    protected void onUnBindVideoView(@NonNull VideoView videoView) {
    }

    /**
     * <p>Called when {@link PlaybackController} bind {@link VideoView} with
     * {@link PlaybackController#bind(VideoView)}
     *
     * <p>Pair with {@link #onUnbindPlaybackController(PlaybackController)}
     *
     * @param controller {@link PlaybackController} instance
     */
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
    }

    /**
     * <p>Called when {@link PlaybackController} unbind {@link VideoView} with
     * {@link PlaybackController#unbind()} or {@code PlaybackController#bind(null)}
     *
     * <p>Pair with {@link #onBindPlaybackController(PlaybackController)}
     *
     * @param controller {@link PlaybackController} instance
     */
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
    }

    /**
     * <p>Called when {@link VideoLayerHost#setLocked(boolean)} is set. The default implements of
     * locking is {@link #dismiss()}.
     *
     * @param locked true locked, false unlocked.
     */
    protected void onLayerHostLockStateChanged(boolean locked) {
        if (isLocked()) {
            dismiss();
        }
    }

    /**
     * @return tag of layer
     * @see VideoLayerHost#findLayer(String)
     */
    @Nullable
    public abstract String tag();

    /**
     * Create the layer view.
     *
     * <p> Note: layer view will be added to {@link VideoLayerHost}'s hostView which is a
     * {@link android.widget.FrameLayout}. The LayoutParams of layer view must be
     * {@link android.widget.FrameLayout.LayoutParams}.
     *
     * @param parent the parent of layer view. {@link VideoLayerHost}'s hostView.
     * @return the created layer view.
     */
    @Nullable
    protected abstract View createView(@NonNull ViewGroup parent);

    @Nullable
    public final <V extends View> V getView() {
        return (V) mLayerView;
    }

    /**
     * @return {@link Context} instance
     */
    @Nullable
    public final Context context() {
        if (mLayerView != null) return mLayerView.getContext();
        if (mLayerHost != null) return mLayerHost.hostView().getContext();
        return null;
    }

    /**
     * @return {@link FragmentActivity} instance
     */
    @Nullable
    public final FragmentActivity activity() {
        Context context = context();
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        }
        return null;
    }

    /**
     * @return {@link VideoLayerHost} instance.
     */
    @Nullable
    public final VideoLayerHost layerHost() {
        return mLayerHost;
    }

    /**
     * @return {@link VideoView} instance held by {@link #mLayerHost}
     */
    @Nullable
    public final VideoView videoView() {
        return mLayerHost == null ? null : mLayerHost.videoView();
    }

    public final int playScene() {
        VideoView videoView = videoView();
        return videoView == null ? -1 : videoView.getPlayScene();
    }

    /**
     * @return {@link PlaybackController} instance held by {@link VideoView}
     */
    @Nullable
    public final PlaybackController controller() {
        VideoView videoView = videoView();
        return videoView == null ? null : videoView.controller();
    }

    /**
     * @return {@link Player} instance held by {@link PlaybackController}
     */
    @Nullable
    public final Player player() {
        PlaybackController controller = controller();
        return controller == null ? null : controller.player();
    }

    /**
     * @return {@link MediaSource} instance held by {@link VideoView}
     */
    @Nullable
    public final MediaSource dataSource() {
        VideoView videoView = videoView();
        if (videoView != null) {
            return videoView.getDataSource();
        }
        return null;
    }

    /**
     * delegate {@link VideoView#startPlayback()}
     */
    public final void startPlayback() {
        if (mLayerHost != null) {
            VideoView videoView = mLayerHost.videoView();
            if (videoView != null) {
                videoView.startPlayback();
            }
        }
    }

    /**
     * delegate {@link VideoView#startPlayback()}
     */
    public final void stopPlayback() {
        if (mLayerHost != null) {
            VideoView videoView = mLayerHost.videoView();
            if (videoView != null) {
                videoView.stopPlayback();
            }
        }
    }

    /**
     * Show layer view.
     *
     * <p> Layer view will be created by {@link #createView(ViewGroup)} and then added to
     * {@link VideoLayerHost}'s hostView.
     *
     * <p>{@link #onViewAddedToHostView(ViewGroup)} will be called when first calling.
     *
     * @see #isShowing()
     * @see #dismiss()
     * @see #hide()
     */
    @CallSuper
    public void show() {
        if (isShowing()) return;
        L.d(this, "show");

        final VideoLayerHost layerHost = mLayerHost;
        if (layerHost == null) return;

        final View layerView = createView(layerHost);

        if (isLocked()) return;

        layerHost.addLayerView(this);

        if (layerView != null && layerView.getVisibility() != View.VISIBLE) {
            layerView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Create layer view.
     *
     * <p> Layer view will be created by {@link #createView(ViewGroup)} at first calling.
     *
     * @return the layer view.
     */
    @Nullable
    public final View createView() {
        final VideoLayerHost layerHost = mLayerHost;
        if (layerHost == null) return null;

        return createView(layerHost);
    }

    private View createView(@NonNull VideoLayerHost layerHost) {
        Asserts.checkNotNull(layerHost);

        if (mLayerView == null) {
            ViewGroup hostView = layerHost.hostView();
            final long start = System.currentTimeMillis();
            mLayerView = createView(hostView);
            L.v(this, "createView", mLayerView, System.currentTimeMillis() - start);
        }
        return mLayerView;
    }

    /**
     * Dismiss the layer view.
     *
     * <p> Layer view will be removed from {@link VideoLayerHost}'s hostView if have been added
     * already.
     *
     * <p>{@link #onViewRemovedFromHostView(ViewGroup)} will be called when successful removed.
     *
     * @see #isShowing()
     * @see #hide()
     * @see #show()
     */
    @CallSuper
    public void dismiss() {
        if (!isShowing()) return;
        L.d(this, "dismiss");

        final VideoLayerHost layerHost = mLayerHost;
        if (layerHost == null) return;

        layerHost.removeLayerView(this);
    }

    /**
     * Hide the layer view.
     *
     * <p> Layer view will be simply {@code mLayerView.setVisibility(View.GONE)}
     *
     * @see #isShowing()
     * @see #dismiss()
     */
    @CallSuper
    public void hide() {
        if (!isShowing()) return;
        L.d(this, "hide");
        if (mLayerView != null && mLayerView.getVisibility() != View.GONE) {
            mLayerView.setVisibility(View.GONE);
        }
    }

    /**
     * Toggle showing state with {@link #show()} and {@link #dismiss()}
     */
    @CallSuper
    public void toggle() {
        if (isShowing()) {
            dismiss();
        } else {
            show();
        }
    }

    /**
     * @return true layerView is showing; false layerView is not showing.
     * @see #dismiss()
     * @see #show()
     * @see #hide()
     */
    public final boolean isShowing() {
        return mLayerView != null
                && mLayerView.getVisibility() == View.VISIBLE
                && mLayerHost != null
                && mLayerHost.indexOfLayerView(this) >= 0;
    }

    public void setIgnoreLock(boolean ignoreLock) {
        mIgnoreLock = ignoreLock;
    }

    /**
     * @return true ignore lock; false don't ignore lock.
     */
    public boolean isIgnoreLock() {
        return mIgnoreLock;
    }

    /**
     * @return true locked, false unlocked.
     */
    public boolean isLocked() {
        if (mLayerHost == null) return false;
        return mLayerHost.isLocked() && !isIgnoreLock();
    }

    /**
     * Delegate {@link VideoLayerHost#notifyEvent(int, Object)}
     *
     * @see VideoLayerHost#notifyEvent(int, Object)
     */
    public final void notifyEvent(int code, @Nullable Object obj) {
        L.v(this, "notifyEvent", code, obj);
        if (mLayerHost != null) {
            mLayerHost.notifyEvent(code, obj);
        }
    }

    /**
     * Handle the event send by {@link #notifyEvent(int, Object)}
     *
     * @see #notifyEvent(int, Object)
     */
    protected void handleEvent(int code, @Nullable Object obj) {
    }
}
