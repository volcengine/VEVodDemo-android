/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/4/17
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.video;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;

import java.io.Serializable;
import java.util.List;

public class DramaDetailVideoActivityResultContract extends ActivityResultContract<DramaDetailVideoActivityResultContract.DramaDetailVideoInput, DramaDetailVideoActivityResultContract.DramaDetailVideoOutput> {
    public static final String EXTRA_INPUT = "extra_input";
    public static final String EXTRA_OUTPUT = "extra_output";

    public static class DramaDetailVideoInput implements Serializable {
        public final DramaInfo drama;
        public final int episodeNumber;
        public final VideoItem currenVideoItem;
        public final boolean continuesPlayback;

        public DramaDetailVideoInput(DramaInfo drama, int episodeNumber, boolean continuesPlayback) {
            this.drama = drama;
            this.episodeNumber = episodeNumber;
            this.currenVideoItem = null;
            this.continuesPlayback = continuesPlayback;
        }

        public DramaDetailVideoInput(VideoItem currenVideoItem, boolean continuesPlayback) {
            this.drama = null;
            this.episodeNumber = 0;
            this.currenVideoItem = currenVideoItem;
            this.continuesPlayback = continuesPlayback;
        }
    }

    public static class DramaDetailVideoOutput implements Serializable {
        public final DramaInfo drama;
        public final VideoItem originalVideoItem;
        public final VideoItem currenVideoItem;

        public final List<VideoItem> videoItems;
        public final boolean continuesPlayback;

        public DramaDetailVideoOutput(DramaInfo drama, VideoItem originalVideoItem, VideoItem currenVideoItem, List<VideoItem> videoItems, boolean continuesPlayback) {
            this.drama = drama;
            this.originalVideoItem = originalVideoItem;
            this.currenVideoItem = currenVideoItem;
            this.videoItems = videoItems;
            this.continuesPlayback = continuesPlayback;
        }
    }

    @NonNull
    @Override
    public Intent createIntent(@NonNull Context context, DramaDetailVideoInput input) {
        Intent intent = new Intent(context, DramaDetailVideoActivity.class);
        intent.putExtra(EXTRA_INPUT, input);
        return intent;
    }

    @Override
    @Nullable
    public DramaDetailVideoOutput parseResult(int resultCode, @Nullable Intent intent) {
        if (intent == null) return null;
        return (DramaDetailVideoOutput) intent.getSerializableExtra(EXTRA_OUTPUT);
    }

}
