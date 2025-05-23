/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/5/27
 */

package com.bytedance.vodplayer.example.advanced.subtitle

import android.content.Context
import com.bytedance.vodplayer.example.R
import com.ss.ttvideoengine.SubModel

enum class SubtitleLanguage(val languageId: Int, val language: Int) {
    CN(1, R.string.vevod_api_example_subtitle_language_cn),
    US(2, R.string.vevod_api_example_subtitle_language_en);

    // 更多枚举，参考：https://www.volcengine.com/docs/4/1186356

    companion object {
        fun languageDes(context: Context, subtitleItem: SubModel): String {
            entries.forEach {
                if (it.languageId == subtitleItem.languageId) {
                    return context.getString(it.language)
                }
            }
            return subtitleItem.language
        }
    }
}