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
 * Create Date : 2021/2/24
 */
package com.bytedance.volc.voddemo.videoview.layer;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import java.util.List;

public interface ILayer extends Comparable<ILayer> {

    int VIDEO_COVER_Z_INDEX = 300;

    int LOADING_Z_INDEX = 400;

    int SMALL_TOOLBAR_Z_INDEX = 600;

    int LOAD_FAIL_Z_INDEX = 1200;

    int DEBUG_TOOL_Z_INDEX = 1400;

    Pair<View, RelativeLayout.LayoutParams> onCreateView(@NonNull Context context, @NonNull
            LayoutInflater inflater);

    @NonNull
    List<Integer> getSupportEvents();

    void refresh();

    int getZIndex();

    void onRegister(ILayerHost host);

    void onUnregister(ILayerHost host);

    boolean handleVideoEvent(@NonNull IVideoLayerEvent event);
}
