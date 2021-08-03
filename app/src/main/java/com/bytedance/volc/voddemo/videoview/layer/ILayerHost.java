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
import android.view.ViewGroup;
import com.bytedance.volc.voddemo.videoview.VideoController;

public interface ILayerHost {

    void addLayer(ILayer layer);

    void removeLayer(ILayer layer);

    ILayer getLayer(int layerType);

    void refreshLayers();

    int findPositionForLayer(ILayer layer, ViewGroup rootView);

    boolean notifyEvent(IVideoLayerEvent event);

    void execCommand(IVideoLayerCommand command);

    Context getContext();

    String getCover();

    boolean isPaused();

    VideoController getVideoController();
}
