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
 * Create Date : 2024/9/5
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.detail;

import android.content.Context;
import android.content.Intent;

import androidx.activity.result.contract.ActivityResultContract;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.volc.voddemo.ui.minidrama.data.business.model.DramaItem;

import java.io.Serializable;
import java.util.List;

public class DramaDetailVideoActivityResultContract extends ActivityResultContract<DramaDetailVideoActivityResultContract.DramaDetailVideoInput, DramaDetailVideoActivityResultContract.DramaDetailVideoOutput> {
    public static final String EXTRA_INPUT = "extra_input";
    public static final String EXTRA_OUTPUT = "extra_output";

    public static class DramaDetailVideoInput implements Serializable {
        public final List<DramaItem> dramaItems;
        public final int currentDramaIndex;
        public final boolean continuesPlayback;

        public DramaDetailVideoInput(List<DramaItem> dramaItems, int currentDramaIndex, boolean continuesPlayback) {
            this.dramaItems = dramaItems;
            this.currentDramaIndex = currentDramaIndex;
            this.continuesPlayback = continuesPlayback;
        }
    }

    public static class DramaDetailVideoOutput implements Serializable {
        public DramaItem currentDramaItem;
        public final boolean continuesPlayback;

        public DramaDetailVideoOutput( DramaItem currentDramaItem, boolean continuesPlayback) {
            this.currentDramaItem = currentDramaItem;
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
