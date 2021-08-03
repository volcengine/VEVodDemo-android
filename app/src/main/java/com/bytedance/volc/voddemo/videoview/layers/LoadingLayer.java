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

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.os.Message;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.utils.WeakHandler;
import java.util.Arrays;
import java.util.List;

public class LoadingLayer extends BaseVideoLayer implements WeakHandler.IHandler {

    private static final int SHOW_LOADING_DELAY = 10;

    private static final int SHOW_LOADING_DELAY_TIME = 600;

    private static final int LOADING_ANIM_TIME = 800;

    private static final String OBJECT_ANIMATOR_PROPERTY_NAME = "rotation";

    private View mLoadingView;
    private ProgressBar mLoading;
    private ObjectAnimator mLoadingAnimator;
    private final WeakHandler mWeakHandler = new WeakHandler(this);

    @Override
    public Pair<View, RelativeLayout.LayoutParams> onCreateView(@NonNull final Context context,
            @NonNull final LayoutInflater inflater) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        mLoadingView = LayoutInflater.from(context).inflate(R.layout.layer_loading, null);
        mLoading = mLoadingView.findViewById(R.id.loading);
        mLoading.setIndeterminateDrawable(
                ContextCompat.getDrawable(context, R.drawable.loading));
        mLoading.setIndeterminate(true);
        UIUtils.setViewVisibility(mLoadingView, View.GONE);
        return Pair.create(mLoadingView, params);
    }

    @Override
    protected View getLayerView(final Context context, @NonNull final LayoutInflater inflater) {
        return null;
    }

    @Override
    public int getZIndex() {
        return ILayer.LOADING_Z_INDEX;
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return Arrays.asList(
                IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_RELEASE,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_START,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_END,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PREPARED,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR,
                IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE);
    }

    @Override
    public boolean handleVideoEvent(@NonNull final IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_START:
                showLoading(true);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_END:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_RELEASE:
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_ERROR:
                showLoading(false);
                mWeakHandler.removeMessages(SHOW_LOADING_DELAY);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PREPARED:
                mWeakHandler.removeMessages(SHOW_LOADING_DELAY);
                break;
        }
        return true;
    }

    @Override
    public void handleMsg(final Message msg) {
        if (msg.what == SHOW_LOADING_DELAY) {
            showLoading(true);
        }
    }

    private void showLoading(boolean show) {
        if (show) {
            UIUtils.setViewVisibility(mLoadingView, View.VISIBLE);
            getLoadingAnimator().start();
        } else {
            UIUtils.setViewVisibility(mLoadingView, View.GONE);
            getLoadingAnimator().cancel();
        }
    }

    private ObjectAnimator getLoadingAnimator() {
        if (mLoadingAnimator == null) {
            mLoadingAnimator = ObjectAnimator.ofFloat(mLoading, OBJECT_ANIMATOR_PROPERTY_NAME,
                    0.0f, 360f);
            mLoadingAnimator.setDuration(LOADING_ANIM_TIME);
            mLoadingAnimator.setInterpolator(new DecelerateInterpolator());
            mLoadingAnimator.setRepeatCount(-1);
            mLoadingAnimator.setRepeatMode(ValueAnimator.RESTART);
        }
        return mLoadingAnimator;
    }
}
