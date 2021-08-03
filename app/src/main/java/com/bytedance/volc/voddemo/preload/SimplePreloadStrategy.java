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

import com.bytedance.volc.voddemo.VodApp;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.ArrayList;
import java.util.List;

public class SimplePreloadStrategy extends BasePreloadStrategy {
    private static final String SimplePreloadStrategy = "SimplePreloadStrategy";

    private final List<VideoItem> mVideoItems = new ArrayList<>();

    @Override
    public void videoListUpdate(final List<VideoItem> videoItems) {
        if (videoItems != null) {
            mVideoItems.clear();
            mVideoItems.addAll(videoItems);
        }
    }

    @Override
    public void currentVideoChanged(final VideoItem videoItem) {
        final int position = mVideoItems.indexOf(videoItem);
        if (position + 1 < mVideoItems.size()) {
            final VideoItem next = mVideoItems.get(position + 1);
            startVideoPreload(next.getVid(), next.getAuthToken());
        }
    }

    @Override
    public void bufferingUpdate(final int duration, final int buffer, final int playbackTime) {
    }

    private void startVideoPreload(String vid, String auth) {
        TTVideoEngineLog.d(TAG, "startVideoPreload vid " + vid + ", auth " + auth);
        if (!VodApp.getClientSettings().enablePreload()) {
            return;
        }

        BasePreloadStrategy.startPreloadByVid(vid, auth, PreloadStrategy.START_PLAY_RESOLUTION,
                PreloadStrategy.PRELOAD_SIZE);
    }
}
