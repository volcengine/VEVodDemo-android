/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/6/25
 */

package com.bytedance.volc.vod.scenekit.strategy;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.player.volcengine.VolcSubtitleSelector;
import com.bytedance.volc.vod.scenekit.R;

import java.util.Arrays;
import java.util.List;

public class VideoSubtitle {
    public static final int LANGUAGE_ID_CN = 1;  // 简体中文
    public static final int LANGUAGE_ID_US = 2;  // 英语

    @Nullable
    public static String subtitle2String(Subtitle subtitle) {
        switch (subtitle.getLanguageId()) {
            case LANGUAGE_ID_CN:
                return VolcPlayerInit.config().context.getString(R.string.vevod_subtitle_language_cn);
            case LANGUAGE_ID_US:
                return VolcPlayerInit.config().context.getString(R.string.vevod_subtitle_language_english);
        }
        return null;
    }

    public static List<Integer> createLanguageIds() {
        return VolcSubtitleSelector.DEFAULT_LANGUAGE_IDS;
    }
}
