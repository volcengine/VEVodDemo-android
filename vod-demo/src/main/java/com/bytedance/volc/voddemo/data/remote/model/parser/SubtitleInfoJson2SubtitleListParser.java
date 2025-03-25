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
 * Create Date : 2025/3/12
 */

package com.bytedance.volc.voddemo.data.remote.model.parser;

import android.text.TextUtils;

import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.utils.Parser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://www.volcengine.com/docs/4/70518">SubtitleInfoList</a>
 */
public class SubtitleInfoJson2SubtitleListParser implements Parser<List<Subtitle>> {

    private final String mSubtitleInfoJson;

    public SubtitleInfoJson2SubtitleListParser(String subtitleInfoJson) {
        this.mSubtitleInfoJson = subtitleInfoJson;
    }

    @Override
    public List<Subtitle> parse() throws JSONException {
        if (TextUtils.isEmpty(mSubtitleInfoJson)) {
            return null;
        }
        JSONArray jsonArray = new JSONArray(mSubtitleInfoJson);
        if (jsonArray.length() <= 0) {
            return null;
        }
        List<Subtitle> subtitles = null;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject == null) continue;
            if (subtitles == null) {
                subtitles = new ArrayList<>();
            }

            String format = jsonObject.optString("Format");
            String url = jsonObject.optString("SubtitleUrl");
            String cacheKey = VolcPlayerInit.config().cacheKeyFactory.generateCacheKey(url);
            String language = jsonObject.optString("Language");
            int languageId = jsonObject.optInt("LanguageId");
            int subtitleId = jsonObject.optInt("SubtitleId");

            Subtitle subtitle = new Subtitle();
            subtitle.setUrl(url);
            subtitle.setCacheKey(cacheKey);
            subtitle.setLanguageId(languageId);
            subtitle.setFormat(format);
            subtitle.setLanguage(language);
            subtitle.setSubtitleId(subtitleId);
            subtitles.add(subtitle);
        }
        return subtitles;
    }

    public List<Subtitle> safeParse() {
        try {
            return parse();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
