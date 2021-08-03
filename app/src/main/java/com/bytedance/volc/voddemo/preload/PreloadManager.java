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
 * Create Date : 2021/7/13
 */
package com.bytedance.volc.voddemo.preload;

import com.bytedance.volc.voddemo.data.VideoItem;
import java.util.List;

public class PreloadManager {

    private static class Holder {
        private static final PreloadManager instance = new PreloadManager();
    }

    public static PreloadManager getInstance() {
        return PreloadManager.Holder.instance;
    }

    private PreloadManager() {
    }

    private PreloadStrategy mPreloadStrategy;

    public void setPreloadStrategy(PreloadStrategy preloadStrategy) {
        mPreloadStrategy = preloadStrategy;
    }

    public void videoListUpdate(final List<VideoItem> videoItems) {
        if (mPreloadStrategy != null) {
            mPreloadStrategy.videoListUpdate(videoItems);
        }
    }

    public void currentVideoChanged(VideoItem videoItem) {
        if (mPreloadStrategy != null) {
            mPreloadStrategy.currentVideoChanged(videoItem);
        }
    }

    public void bufferingUpdate(int duration, int bufferPercent, int playbackTime) {
        if (mPreloadStrategy != null) {
            mPreloadStrategy.bufferingUpdate(duration, bufferPercent, playbackTime);
        }
    }
}
