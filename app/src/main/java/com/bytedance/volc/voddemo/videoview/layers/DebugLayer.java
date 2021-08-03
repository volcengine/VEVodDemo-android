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
 * Create Date : 2021/2/25
 */
package com.bytedance.volc.voddemo.videoview.layers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerEvent;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;
import com.bytedance.volc.voddemo.videoview.VOLCVideoController;
import com.bytedance.volc.voddemo.videoview.VideoController;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.debug.DebugTools;
import java.util.Arrays;
import java.util.List;

public class DebugLayer extends BaseVideoLayer {
    private boolean mShow;
    private final DebugTools mTools = new DebugTools();

    @Override
    public int getZIndex() {
        return ILayer.DEBUG_TOOL_Z_INDEX;
    }

    @Override
    public boolean handleVideoEvent(@NonNull final IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_TOGGLE_DEBUG_TOOL:
                toggleDebugView();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY:
                showDebugView();
                break;
            default:
                break;
        }
        return true;
    }

    @SuppressLint("InflateParams")
    @Override
    protected View getLayerView(final Context context, @NonNull final LayoutInflater inflater) {
        return inflater.inflate(R.layout.layer_debug_tool, null);
    }

    @Override
    protected void setupViews() {
        super.setupViews();
        DebugTools.DEBUG = true;
        LinearLayout debugHub = mLayerView.findViewById(R.id.debug_hub);
        mTools.setInfoHudView(debugHub);

        mLayerView.setOnLongClickListener(v -> mHost.notifyEvent(
                new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_TOGGLE_DEBUG_TOOL)));

        mLayerView.setOnClickListener(v -> mHost.notifyEvent(
                new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_VIEW_CLICK)));
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return Arrays.asList(IVideoLayerEvent.VIDEO_LAYER_EVENT_TOGGLE_DEBUG_TOOL,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_CALL_PLAY);
    }

    private void toggleDebugView() {
        mShow = !mShow;
        if (mShow) {
            showDebugView();
        } else {
            hideDebugView();
        }
    }

    private void showDebugView() {
        if (!mShow) {
            return;
        }

        UIUtils.setViewVisibility(mLayerView, View.VISIBLE);
        final VideoController videoController = mHost.getVideoController();
        if (videoController instanceof VOLCVideoController) {
            TTVideoEngine engine = ((VOLCVideoController) videoController).getTTVideoEngine();
            if (engine != null) {
                mTools.setVideoEngine(engine);
                mTools.start();
            }
        }
    }

    private void hideDebugView() {
        UIUtils.setViewVisibility(mLayerView, View.GONE);
        mTools.stop();
    }
}
