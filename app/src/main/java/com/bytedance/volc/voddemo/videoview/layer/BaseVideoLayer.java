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
 * Create Date : 2021/6/8
 */
package com.bytedance.volc.voddemo.videoview.layer;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;

public abstract class BaseVideoLayer implements ILayer {

    protected View mLayerView;
    protected ILayerHost mHost;
    protected Context mContext;

    @Override
    public void onRegister(ILayerHost host) {
        mHost = host;
    }

    @Override
    public void onUnregister(ILayerHost host) {
        if (mLayerView != null) {
            mLayerView.setOnClickListener(null);
        }
        mHost = null;
    }

    @Override
    public Pair<View, RelativeLayout.LayoutParams> onCreateView(@NonNull Context context, @NonNull
            LayoutInflater inflater) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContext = context;
        if (mLayerView == null) {
            mLayerView = getLayerView(context, inflater);
            if (mLayerView != null) {
                setupViews();
            }
        }

        refresh();
        return new Pair<>(mLayerView, params);
    }

    protected abstract View getLayerView(final Context context, @NonNull LayoutInflater inflater);

    @Override
    public void refresh() {
    }

    protected void setupViews() {
    }

    @Override
    public int compareTo(@NotNull ILayer another) {
        return Integer.compare(getZIndex(), another.getZIndex());
    }
}
