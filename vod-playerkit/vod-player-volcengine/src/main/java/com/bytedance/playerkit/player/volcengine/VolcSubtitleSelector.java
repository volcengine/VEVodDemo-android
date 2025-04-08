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
 * Create Date : 2023/6/14
 */

package com.bytedance.playerkit.player.volcengine;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.SubtitleSelector;

import java.util.Arrays;
import java.util.List;

/**
 * <a href="https://www.volcengine.com/docs/4/1186356">Language IDs</a>
 */
public class VolcSubtitleSelector implements SubtitleSelector {

    @NonNull
    @Override
    public Subtitle selectSubtitle(@NonNull MediaSource mediaSource, @NonNull List<Subtitle> subtitles) {
        // 1. 按照偏好语言优先级返回语言
        final List<Integer> preferredLanguageIds = VolcConfig.get(mediaSource).subtitleLanguageIds;
        if (preferredLanguageIds != null && !preferredLanguageIds.isEmpty()) {
            for (int languageId : preferredLanguageIds) {
                for (Subtitle subtitle : subtitles) {
                    if (subtitle.getLanguageId() == languageId) {
                        return subtitle;
                    }
                }
            }
        }
        // 2. 若未命中，兜底返回第 0 个
        return subtitles.get(0);
    }
}
