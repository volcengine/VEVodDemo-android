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
 * Create Date : 2021/6/15
 */
package com.bytedance.volc.voddemo.videoview.layer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.videoview.VideoController;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.List;
import java.util.TreeSet;

public class LayerRoot extends RelativeLayout implements ILayerHost {
    private static final String TAG = "LayerRoot";

    private VideoController mVideoController;
    private final SparseArray<ILayer> mLayerMap = new SparseArray<>();
    private final TreeSet<ILayer> mLayers = new TreeSet<>();
    private final SparseArray<TreeSet<ILayer>> mEventLayerMap = new SparseArray<>();
    private final SparseArray<View> mLayerViews = new SparseArray<>();

    public LayerRoot(final Context context) {
        super(context);
    }

    public LayerRoot(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public LayerRoot(final Context context, final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void addLayer(final ILayer layer) {
        if (layer == null) {
            return;
        }
        if (mLayerMap.get(layer.getZIndex()) != null) {
            TTVideoEngineLog.d(TAG, "layerType:" + layer.getZIndex()
                                    + " already exist, remove the old before adding new one! "
                                    + hashCode());
            return;
        }

        TTVideoEngineLog.d(TAG, "add layer:" + layer.getClass().getSimpleName()
                                + " layerType:" + layer.getZIndex() + " " + hashCode());
        mLayerMap.put(layer.getZIndex(), layer);
        mLayers.add(layer);

        List<Integer> supportEvents = layer.getSupportEvents();
        for (Integer eventType : supportEvents) {
            if (mEventLayerMap.indexOfKey(eventType) >= 0) {
                mEventLayerMap.get(eventType).add(layer);
            } else {
                TreeSet<ILayer> layers = new TreeSet<>();
                layers.add(layer);
                mEventLayerMap.put(eventType, layers);
            }
        }

        layer.onRegister(this);
        Pair<View, LayoutParams> pair = layer.onCreateView(getContext(),
                LayoutInflater.from(getContext()));
        int position = findPositionForLayer(layer, this);

        mLayerViews.put(layer.getZIndex(), pair.first);
        if (pair.second != null) {
            addView(pair.first, position, pair.second);
        } else {
            addView(pair.first, position);
        }
    }

    @Override
    public void removeLayer(final ILayer layer) {
        if (layer == null) {
            return;
        }
        if (mLayerMap.get(layer.getZIndex()) == null) {
            return;
        }
        TTVideoEngineLog.d(TAG, "removeLayer:" + layer.getClass().getSimpleName()
                                + " layerType:" + layer.getZIndex());
        mLayerMap.delete(layer.getZIndex());
        mLayers.remove(layer);
        for (int i = 0; i < mEventLayerMap.size(); i++) {
            if (mEventLayerMap.valueAt(i) != null) {
                mEventLayerMap.valueAt(i).remove(layer);
            }
        }

        View view = mLayerViews.get(layer.getZIndex());
        UIUtils.detachFromParent(view);
        mLayerViews.delete(layer.getZIndex());
        layer.onUnregister(this);
    }

    @Override
    public ILayer getLayer(final int layerType) {
        return mLayerMap.get(layerType);
    }

    @Override
    public void refreshLayers() {
        for (ILayer layer : mLayers) {
            layer.refresh();
        }
    }

    @Override
    public int findPositionForLayer(final ILayer layer, final ViewGroup rootView) {
        int result = -1;
        if (layer == null) {
            return result;
        }

        while (mLayers.contains(layer)) {
            ILayer lowerLayer = mLayers.lower(layer);
            int position;
            if (lowerLayer != null) {
                position = findPositionForChild(mLayerViews.get(lowerLayer.getZIndex()));
                if (position >= 0) {
                    result = position + 1;
                    break;
                }
            }
            ILayer higherLayer = mLayers.higher(layer);
            if (higherLayer != null) {
                position = findPositionForChild(mLayerViews.get(higherLayer.getZIndex()));
                if (position >= 0) {
                    result = position;
                    break;
                }
            }
            result = rootView.getChildCount();
            if (result >= 0) {
                break;
            }
        }

        return result;
    }

    private int findPositionForChild(View child) {
        if (child == null) {
            return -1;
        }
        int result = -1;
        for (int i = 0; i < getChildCount(); i++) {
            if (child == getChildAt(i)) {
                result = i;
                break;
            }
        }
        return result;
    }

    @Override
    public boolean notifyEvent(final IVideoLayerEvent event) {
        if (event == null) {
            return false;
        }
        TreeSet<ILayer> layers = mEventLayerMap.get(event.getType());
        if (layers == null || layers.isEmpty()) {
            return false;
        }

        boolean result = false;
        for (ILayer layer : layers) {
            if (layer.handleVideoEvent(event)) {
                result = true;
            }
        }
        return result;
    }

    public void setVideoController(final VideoController videoController) {
        mVideoController = videoController;
    }

    @Override
    public void execCommand(final IVideoLayerCommand command) {
        switch (command.getCommand()) {
            case IVideoLayerCommand.VIDEO_HOST_CMD_REPLY:
            case IVideoLayerCommand.VIDEO_HOST_CMD_PLAY:
                if (mVideoController != null) {
                    mVideoController.play();
                }
                break;
            case IVideoLayerCommand.VIDEO_HOST_CMD_PAUSE:
                if (mVideoController != null) {
                    mVideoController.pause();
                }
                break;
            case IVideoLayerCommand.VIDEO_HOST_CMD_SEEK:
                int seekTo = command.getParam(Integer.class);
                mVideoController.seekTo(seekTo);
                break;
            default:
                break;
        }
    }

    @Override
    public String getCover() {
        if (mVideoController == null) {
            return null;
        }
        return mVideoController.getCover();
    }

    @Override
    public boolean isPaused() {
        if (mVideoController != null) {
            return mVideoController.isPaused();
        }
        return false;
    }

    @Override
    public VideoController getVideoController() {
        return mVideoController;
    }
}
