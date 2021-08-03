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
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.ss.ttvideoengine.utils.Error;
import java.util.Arrays;
import java.util.List;

public class LoadFailLayer extends BaseVideoLayer {
    @Override
    public int getZIndex() {
        return ILayer.LOAD_FAIL_Z_INDEX;
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return Arrays.asList(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR);
    }

    @Override
    protected void setupViews() {
        UIUtils.setViewVisibility(mLayerView, View.GONE);
        mLayerView.findViewById(R.id.tv_click_to_retry).setOnClickListener(v -> {
            dismissView();
            mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_REPLY));
        });

        mLayerView.setOnClickListener(
                v -> mLayerView.findViewById(R.id.errorMsg).setVisibility(View.VISIBLE));
    }

    @SuppressLint("InflateParams")
    @Override
    protected View getLayerView(final Context context, @NonNull final LayoutInflater inflater) {
        return inflater.inflate(R.layout.layer_load_fail, null);
    }

    @Override
    public void refresh() {
        dismissView();
    }

    @Override
    public boolean handleVideoEvent(@NonNull final IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR:
                final Error param = event.getParam(Error.class);
                showFailedView(param);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_START:
                dismissView();
                break;
            default:
                break;
        }
        return false;
    }

    private void showFailedView(Error error) {
        final TextView tvErrorMsg = mLayerView.findViewById(R.id.errorMsg);
        tvErrorMsg.setText(error.toString());
        mLayerView.setVisibility(View.GONE);
        UIUtils.setViewVisibility(mLayerView, View.VISIBLE);
    }

    private void dismissView() {
        final TextView tvErrorMsg = mLayerView.findViewById(R.id.errorMsg);
        tvErrorMsg.setText(null);
        mLayerView.setVisibility(View.GONE);
        UIUtils.setViewVisibility(mLayerView, View.GONE);
    }
}
