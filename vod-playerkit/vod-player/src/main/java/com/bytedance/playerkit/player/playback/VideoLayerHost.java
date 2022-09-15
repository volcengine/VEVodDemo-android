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
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Host of {@link VideoLayer}.
 *
 * <p> It's recommend to use PlayerKit's <b>Layer System</b> to implements ui views in VideoView.
 *
 * <p><b>Layer System</b> main classes:
 * <ul>
 *   <li>{@link VideoView}. Render video content</li>
 *   <li>{@link VideoLayerHost}. Manager layer views' attach/detach state.</li>
 *   <li>{@link VideoLayer}. Helps you implement single responsibility, modular and reusable
 *   layer view.</li>
 * </ul>
 *
 * <p>From view hierarchy perspective:
 * <ul>
 *   <li>{@code VideoLayerHost} is not a view but a view holder. It holds a FrameLayout instance
 *   {@link #mHostView}.</li>
 *   <li>{@link VideoView} is the parent view of {@link #mHostView}.</li>
 *   <li>{@link #mHostView} is the parent view of  {@link VideoLayer}'s layer view. The index
 *   and z-index of layer view is as same as the order of calling {@link #addLayer(VideoLayer)}.
 *   Therefore You should be careful with the order of calling {@link #addLayer(VideoLayer)}.
 *   </li>
 * </ul>
 *
 * <p>{@code VideoLayerHost} is a manager class of {@link VideoLayer}. The {@link VideoLayer}
 * provider basic {@link VideoLayer#show()} and {@link VideoLayer#hide()} method to control
 * visibility of the layer view.
 * <ul>
 *   <li>Calling {@link VideoLayer#show()} to add it's view created by
 *   {@link VideoLayer#createView(ViewGroup)} to {@link VideoLayerHost#mHostView}.
 *   After view is created, the view will be held by {@link VideoLayer} to avoid unnecessary
 *   recreate.</li>
 *   <li>Calling {@link VideoLayer#hide()} to remove it's view from
 *   {@link VideoLayerHost#mHostView} if the view have been added.</li>
 *   <li>Calling {@link VideoLayer#dismiss()} to make view gone by calling
 *   {@code View#setVisibility(View.GONE)} if the view have been added and is visible.</li>
 * </ul>
 *
 * <p> {@code VideoLayerHost} is a connector between {@link VideoLayer} and {@link VideoView}.
 * {@link VideoLayer} can get {@link Player} and {@link PlaybackController} instance with
 * {@link VideoView}. Your implement of {@link VideoLayer} can add/remove listeners to
 * {@link PlaybackController} in {@link VideoLayer#onBindPlaybackController(PlaybackController)}/
 * {@link VideoLayer#onUnbindPlaybackController(PlaybackController)} to listen the action/state
 * changes of {@link Player} and {@link PlaybackController} instance.
 *
 * @see VideoView
 * @see VideoLayer
 */
public class VideoLayerHost {

    private final List<VideoLayer> mLayers = new CopyOnWriteArrayList<>();

    private final List<VideoLayerHostListener> mListeners = new CopyOnWriteArrayList<>();
    private final SparseArray<BackPressedHandler> mHandlers = new SparseArray<>();
    private final FrameLayout mHostView;
    private VideoView mVideoView;

    private boolean mLocked;

    public interface VideoLayerHostListener {
        /**
         * Callbacks when {@link VideoLayerHost} instance is attached to VideoView.
         *
         * @param videoView {@link VideoView} attached into
         * @see VideoLayerHost#attachToVideoView(VideoView)
         */
        void onLayerHostAttachedToVideoView(@NonNull VideoView videoView);

        /**
         * Callbacks when {@link VideoLayerHost} instance is detached from a VideoView.
         *
         * @param videoView {@link VideoView} detached from
         * @see VideoLayerHost#detachFromVideoView()
         */
        void onLayerHostDetachedFromVideoView(@NonNull VideoView videoView);
    }

    public VideoLayerHost(Context context) {
        mHostView = new FrameLayout(context);
    }

    void addVideoLayerHostListener(VideoLayerHostListener listener) {
        if (listener != null && !mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    void removeVideoLayerHostListener(VideoLayerHostListener listener) {
        if (listener != null) {
            mListeners.remove(listener);
        }
    }

    /**
     * Attaching into a {@link VideoView} will add the {@link #mHostView} to {@link VideoView}.
     * <p>
     * The {@link VideoLayer} attached will receive the event
     * {@link VideoLayerHostListener#onLayerHostAttachedToVideoView(VideoView)}
     *
     * @param videoView The {@link VideoView} instance to be attached into.
     */
    public void attachToVideoView(@NonNull VideoView videoView) {
        if (mVideoView == null) {
            mVideoView = videoView;
            mVideoView.bindLayerHost(this);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mHostView.getLayoutParams();
            if (lp == null) {
                lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.gravity = Gravity.CENTER;
            }
            videoView.addView(mHostView, lp);
            L.v(this, "onLayerHostAttachedToVideoView", videoView);
            for (VideoLayerHostListener listener : mListeners) {
                listener.onLayerHostAttachedToVideoView(videoView);
            }
        }
    }

    /**
     * Detach from {@link VideoView} will remove the {@link #mHostView} from {@link VideoView}.
     * All the {@link VideoLayer} will be invisible after calling this method.
     * <p>
     * The {@link VideoLayer} attached will receive the event
     * {@link VideoLayerHostListener#onLayerHostDetachedFromVideoView(VideoView)}
     */
    public void detachFromVideoView() {
        if (mVideoView != null) {
            mVideoView.unbindLayerHost(this);
            mVideoView.removeView(mHostView);
            final VideoView toDetach = mVideoView;
            mVideoView = null;
            L.v(this, "onLayerHostDetachedFromVideoView", toDetach);
            for (VideoLayerHostListener listener : mListeners) {
                listener.onLayerHostDetachedFromVideoView(toDetach);
            }
        }
    }

    /**
     * @return The {@link VideoView} instance of being attached into.
     */
    public VideoView videoView() {
        return mVideoView;
    }

    /**
     * Adds the {@link VideoLayer} to host.
     * <p>
     * The index and z-index of layer view is as same as the order of calling
     * {@code #addLayer(VideoLayer)}. Therefore You should be careful with the order of calling
     * {@code addLayer(VideoLayer)}.
     * eg. If you want the CoverLayer to be showed blow the LoadingLayer. You should add CoverLayer
     * first then add LoadingLayer.
     *
     * @param layer The {@link VideoLayer} instance to be added
     */
    public final void addLayer(VideoLayer layer) {
        if (layer != null && !mLayers.contains(layer)) {
            mLayers.add(layer);
            layer.bindLayerHost(this);
        }
    }

    /**
     * Find layer by index. Useful when you want to loop the layers in host.
     *
     * @param index index of layer
     * @return the {@link VideoLayer} instance in index.
     * @throws IndexOutOfBoundsException is index out of range.
     */
    @Nullable
    public final VideoLayer findLayer(int index) {
        return mLayers.get(index);
    }

    /**
     * Find layer by tag.
     *
     * @param tag tag of layer
     * @return the {@link VideoLayer} instance with tag
     */
    public final VideoLayer findLayer(String tag) {
        for (VideoLayer layer : mLayers) {
            if (layer != null && TextUtils.equals(layer.tag(), tag)) {
                return layer;
            }
        }
        return null;
    }

    /**
     * Find layers by layer class.
     *
     * @param layerClazz the implementation class of {@link VideoLayer}
     * @param <T>        cast to requested type simply.
     * @return the {@link VideoLayer} instance of class
     */
    public final <T extends VideoLayer> T findLayer(Class<T> layerClazz) {
        for (VideoLayer layer : mLayers) {
            if (layer != null && layerClazz.isInstance(layer)) {
                return (T) layer;
            }
        }
        return null;
    }

    /**
     * @return the index of {@link VideoLayer}
     */
    public final int indexOfLayer(VideoLayer layer) {
        if (layer != null) {
            return mLayers.indexOf(layer);
        }
        return -1;
    }

    /**
     * @return The added {@link VideoLayer} count.
     */
    public final int layerSize() {
        return mLayers.size();
    }

    /**
     * Remove {@link VideoLayer} instance from host by layer class
     *
     * @param layerClazz class of layer
     * @return the removed {@link VideoLayer} instance
     */
    public final VideoLayer removeLayer(Class<? extends VideoLayer> layerClazz) {
        for (VideoLayer layer : mLayers) {
            if (layerClazz.isInstance(layer)) {
                mLayers.remove(layer);
                return layer;
            }
        }
        return null;
    }

    /**
     * Remove {@link VideoLayer} instance form host
     */
    public final void removeLayer(VideoLayer layer) {
        if (layer != null) {
            if (mLayers.contains(layer)) {
                removeLayerView(layer);
                layer.unbindLayerHost(this);
            }
        }
    }

    /**
     * Remove {@link VideoLayer} instance from host by layer index
     *
     * @param index index of layer
     * @return the removed {@link VideoLayer} instance
     */
    public final VideoLayer removeLayer(int index) {
        final VideoLayer layer = mLayers.remove(index);
        if (layer != null) {
            layer.unbindLayerHost(this);
        }
        return layer;
    }

    /**
     * Remove {@link VideoLayer} instance from host by layer tag
     *
     * @param tag tag of layer
     * @return the removed {@link VideoLayer} instance
     */
    public final VideoLayer removeLayer(String tag) {
        for (VideoLayer layer : mLayers) {
            if (TextUtils.equals(layer.tag(), tag)) {
                mLayers.remove(layer);
                return layer;
            }
        }
        return null;
    }

    /**
     * Remove all {@link VideoLayer} instances from host.
     */
    public final void removeAllLayers() {
        for (VideoLayer layer : mLayers) {
            mLayers.remove(layer);
            layer.unbindLayerHost(this);
        }
    }

    /**
     * A {@link VideoLayer} instance can send it's event to others layers. {@link VideoLayer}
     * instance should override the {@link VideoLayer#handleEvent(int, Object)} to receive the event.
     *
     * @param code event code
     * @param obj  event obj
     */
    public void notifyEvent(int code, @Nullable Object obj) {
        for (VideoLayer layer : mLayers) {
            layer.handleEvent(code, obj);
        }
    }

    /**
     * Lock the {@link VideoLayer} in host. This method will not lock layers, it only tells layers
     * the lock/unlock state by {@link VideoLayer#onLayerHostLockStateChanged(boolean)}.
     * <p>
     * The default implements of {@link VideoLayer#onLayerHostLockStateChanged(boolean)} is dismiss.
     * You can override the {@link VideoLayer#onLayerHostLockStateChanged(boolean)} to implements
     * the lock/unlock behavior.
     *
     * @param locked true for locked, false for unlocked
     */
    public void setLocked(boolean locked) {
        if (mLocked != locked) {
            mLocked = locked;
            for (VideoLayer layer : mLayers) {
                layer.onLayerHostLockStateChanged(locked);
            }
        }
    }

    /**
     * @return true for locked, false for unlocked
     */
    public boolean isLocked() {
        return mLocked;
    }

    /**
     * Back press handler of {@link VideoLayer}. If your subclass of {@link VideoLayer} want to
     * handle back press. The subclass should:
     * <ol>
     *   <li>implement the {@link BackPressedHandler} interface, Handle back press event in
     *   {@link BackPressedHandler#onBackPressed()}.</li>
     *   <li>Calling {@link #registerBackPressedHandler(BackPressedHandler, int)} and
     *   {@link #unregisterBackPressedHandler(BackPressedHandler)} to register and unregister
     *   the back press handle in {@link VideoLayer#onBindLayerHost(VideoLayerHost)} and
     *   {@link VideoLayer#onUnbindLayerHost(VideoLayerHost)}.</li>
     *   <li>override Activity/Fragment onBackPressed method with
     *   <pre>
     *   {@code
     *     public class ExampleActivity {
     *       VideoView videoView;
     *
     *       public void onBackPressed() {
     *         if (videoView.layerHost().onBackPressed()) {
     *           return;
     *         }
     *         super.onBackPressed();
     *       }
     *     }
     *   } </pre></li>
     * </ol>
     */
    public interface BackPressedHandler {
        boolean onBackPressed();
    }

    /**
     * {@link VideoLayer} register the back press handler. See {@link BackPressedHandler} java doc
     * for usage.
     *
     * @param handler  {@link BackPressedHandler} instance
     * @param priority back press priority, Higher priority will be execute first.
     * @see BackPressedHandler
     */
    public void registerBackPressedHandler(BackPressedHandler handler, int priority) {
        mHandlers.put(priority, handler);
    }


    /**
     * {@link VideoLayer} unregister the back press handler. See {@link BackPressedHandler} java doc
     * for usage.
     *
     * @param handler {@link BackPressedHandler} instance
     */
    public void unregisterBackPressedHandler(BackPressedHandler handler) {
        for (int i = mHandlers.size() - 1; i >= 0; i--) {
            if (mHandlers.get(i) == handler) {
                mHandlers.remove(i);
            }
        }
    }

    /**
     * Intercept the Fragment/Activity onBackPressed() method.
     *
     * <pre>
     *   {@code
     *     public class ExampleActivity {
     *       VideoView videoView;
     *
     *       public void onBackPressed() {
     *         if (videoView.layerHost().onBackPressed()) {
     *           return;
     *         }
     *         super.onBackPressed();
     *       }
     *     }
     *   } </pre>
     *
     * @return true if event is intercepted.
     */
    public boolean onBackPressed() {
        for (int i = mHandlers.size() - 1; i >= 0; i--) {
            final BackPressedHandler handler = mHandlers.get(mHandlers.keyAt(i));
            if (handler != null && handler.onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return Parent view of {@link VideoLayer}'s layer view.
     */
    public FrameLayout hostView() {
        return mHostView;
    }

    final int indexOfLayerView(@NonNull VideoLayer layer) {
        Asserts.checkNotNull(layer);

        final ViewGroup hostView = mHostView;

        View layerView = layer.getView();
        if (layerView == null) return -1;

        return hostView.indexOfChild(layerView);
    }


    final void addLayerView(@NonNull VideoLayer layer) {
        Asserts.checkNotNull(layer);

        final ViewGroup hostView = mHostView;

        View layerView = layer.getView();
        if (layerView == null) return;

        if (layerView.getParent() == null) {
            final int layerIndex = indexOfLayer(layer);
            final int layerViewIndex = calViewIndex(hostView, layerIndex);
            L.v(this, "addLayerView", layer, hostView, "layerIndex = " + layerIndex, "viewIndex = " + layerViewIndex);
            hostView.addView(layerView, layerViewIndex);
            layer.onViewAddedToHostView(hostView);
        }
    }

    private int calViewIndex(@NonNull ViewGroup hostView, int layerIndex) {
        int preLayerIndex = layerIndex - 1;
        int preLayerViewIndex = -1;
        for (int i = preLayerIndex; i >= 0; i--) {
            final VideoLayer layer = findLayer(i);
            final View layerView = layer.getView();
            if (layerView != null) {
                int viewIndex = hostView.indexOfChild(layerView);
                if (viewIndex >= 0) {
                    preLayerViewIndex = viewIndex;
                    break;
                }
            }
        }
        return preLayerViewIndex < 0 ? 0 : preLayerViewIndex + 1;
    }


    final void removeLayerView(@NonNull VideoLayer layer) {
        Asserts.checkNotNull(layer);

        final ViewGroup hostView = mHostView;
        final View layerView = layer.getView();

        if (layerView == null) return;

        final int layerIndex = mLayers.indexOf(layer);
        final int layerViewIndex = hostView.indexOfChild(layerView);
        if (layerViewIndex >= 0) {
            L.v(this, "removeLayerView", layer, hostView, "layerIndex = " + layerIndex, "viewIndex = " + layerViewIndex);
            hostView.removeView(layerView);
            layer.onViewRemovedFromHostView(hostView);
        }
    }
}
