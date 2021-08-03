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
 * Create Date : 2021/6/22
 */
package com.bytedance.volc.voddemo.preload;

import com.bytedance.volc.voddemo.data.VideoItem;
import com.ss.ttvideoengine.Resolution;
import java.util.List;

public interface PreloadStrategy {
    String TAG = "PreloadStrategy";

    Resolution START_PLAY_RESOLUTION = Resolution.High;
    long PRELOAD_SIZE = 300 * 1024;

    void videoListUpdate(List<VideoItem> videoItems);

    void currentVideoChanged(VideoItem videoItem);

    void bufferingUpdate(int duration, int buffer, int playbackTime);
}
