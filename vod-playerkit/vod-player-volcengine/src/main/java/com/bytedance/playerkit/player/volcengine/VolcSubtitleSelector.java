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
 * <a href="https://www.volcengine.com/docs/4/70518#%E5%AD%97%E5%B9%95%E8%AF%AD%E8%A8%80">Language IDs</a>
 */
public class VolcSubtitleSelector implements SubtitleSelector {
    public static final List<Integer> DEFAULT_LANGUAGE_IDS = Arrays.asList(5, 1, 2);

    @NonNull
    @Override
    public Subtitle selectSubtitle(@NonNull MediaSource mediaSource, @NonNull List<Subtitle> subtitles) {
        for (int languageId : DEFAULT_LANGUAGE_IDS) {
            for (Subtitle subtitle : subtitles) {
                if (subtitle.getLanguageId() == languageId) {
                    return subtitle;
                }
            }
        }
        return subtitles.get(0);
    }
}
