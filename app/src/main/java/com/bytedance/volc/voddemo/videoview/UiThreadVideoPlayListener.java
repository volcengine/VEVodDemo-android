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
 * Create Date : 5/5/22
 */
package com.bytedance.volc.voddemo.videoview;

import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.utils.ThreadUtils;
import com.ss.ttvideoengine.utils.Error;

import static com.bytedance.volc.voddemo.utils.ThreadUtils.runOnUiThread;

public class UiThreadVideoPlayListener implements VideoPlayListener {

    private final VideoPlayListener mListener;

    public UiThreadVideoPlayListener(@NonNull VideoPlayListener listener) {
        mListener = listener;
    }

    @Override
    public void onVideoSizeChanged(final int width, final int height) {
        runOnUiThread(() -> mListener.onVideoSizeChanged(width, height));
    }

    @Override
    public void onCallPlay() {
        runOnUiThread(mListener::onCallPlay);
    }

    @Override
    public void onPrepare() {
        runOnUiThread(mListener::onPrepare);
    }

    @Override
    public void onPrepared() {
        runOnUiThread(mListener::onPrepared);
    }

    @Override
    public void onRenderStart() {
        runOnUiThread(mListener::onRenderStart);
    }

    @Override
    public void onVideoPlay() {
        runOnUiThread(mListener::onVideoPlay);
    }

    @Override
    public void onVideoPause() {
        runOnUiThread(mListener::onVideoPause);
    }

    @Override
    public void onBufferStart() {
        runOnUiThread(mListener::onBufferStart);
    }

    @Override
    public void onBufferingUpdate(final int percent) {
        runOnUiThread(() -> mListener.onBufferingUpdate(percent));
    }

    @Override
    public void onBufferEnd() {
        runOnUiThread(mListener::onBufferEnd);
    }

    @Override
    public void onStreamChanged(final int type) {
        runOnUiThread(() -> mListener.onStreamChanged(type));
    }

    @Override
    public void onVideoCompleted() {
        runOnUiThread(mListener::onVideoCompleted);
    }

    @Override
    public void onVideoPreRelease() {
        runOnUiThread(mListener::onVideoPreRelease);
    }

    @Override
    public void onVideoReleased() {
        runOnUiThread(mListener::onVideoReleased);
    }

    @Override
    public void onError(final VideoItem videoItem, final Error error) {
        runOnUiThread(() -> mListener.onError(videoItem, error));
    }

    @Override
    public void onFetchVideoModel(final int videoWidth, final int videoHeight) {
        runOnUiThread(() -> mListener.onFetchVideoModel(videoWidth, videoHeight));
    }

    @Override
    public void onVideoSeekComplete(final boolean success) {
        runOnUiThread(() -> mListener.onVideoSeekComplete(success));
    }

    @Override
    public void onVideoSeekStart(final int msec) {
        runOnUiThread(() -> mListener.onVideoSeekStart(msec));
    }

    @Override
    public void onNeedCover() {
        runOnUiThread(mListener::onNeedCover);
    }
}
