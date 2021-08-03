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
package com.bytedance.volc.voddemo.videoview;

import com.bytedance.volc.voddemo.data.VideoItem;
import com.ss.ttvideoengine.utils.Error;

public interface VideoPlayListener {

    void onVideoSizeChanged(int width, int height);

    void onCallPlay();

    void onPrepare();

    void onPrepared();

    void onRenderStart();

    void onVideoPlay();

    void onVideoPause();

    void onBufferStart();

    void onBufferingUpdate(int percent);

    void onBufferEnd();

    void onStreamChanged(int type);

    void onVideoCompleted();

    void onVideoPreRelease();

    void onVideoReleased();

    void onError(VideoItem videoItem, Error error);

    void onFetchVideoModel(final int videoWidth, final int videoHeight);

    void onVideoSeekComplete(boolean success);

    void onVideoSeekStart(int msec);
}
