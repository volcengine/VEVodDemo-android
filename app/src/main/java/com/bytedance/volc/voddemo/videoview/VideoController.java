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
 * Create Date : 2021/6/11
 */
package com.bytedance.volc.voddemo.videoview;

import android.view.Surface;

public interface VideoController {

    int getDuration();

    void setSurface(Surface surface);

    void pause();

    void play();

    void release();

    void seekTo(int msec);

    boolean isPlaying();

    boolean isPaused();

    boolean isLooping();

    String getCover();

    int getVideoWidth();

    int getVideoHeight();

    int getCurrentPlaybackTime();
}
