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
 * Create Date : 2023/6/12
 */

package com.bytedance.playerkit.player.source;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

public class Subtitle implements Serializable {
    private int index;
    private int subtitleId;
    private String language;
    private int languageId;
    private String url;
    private String cacheKey;
    private long expire;
    private String format;
    private String subtitleDesc;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSubtitleId() {
        return subtitleId;
    }

    public void setSubtitleId(int subtitleId) {
        this.subtitleId = subtitleId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getLanguageId() {
        return languageId;
    }

    public void setLanguageId(int languageId) {
        this.languageId = languageId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCacheKey() {
        return cacheKey;
    }

    public void setCacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
    }

    public long getExpire() {
        return expire;
    }

    public void setExpire(long expire) {
        this.expire = expire;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getSubtitleDesc() {
        return subtitleDesc;
    }

    public void setSubtitleDesc(String subtitleDesc) {
        this.subtitleDesc = subtitleDesc;
    }

    public String dump() {
        return String.format(Locale.getDefault(), "%s %s", L.obj2String(this), TextUtils.isEmpty(subtitleDesc) ? language : subtitleDesc);
    }

    @Nullable
    public static String dump(@Nullable Subtitle subtitle) {
        if (!L.ENABLE_LOG) return null;
        if (subtitle == null) return null;

        return subtitle.dump();
    }

    @Nullable
    public static String dump(List<Subtitle> subtitles) {
        if (!L.ENABLE_LOG) return null;
        if (subtitles == null) return null;

        StringBuilder sb = new StringBuilder();
        for (Subtitle subtitle : subtitles) {
            sb.append(subtitle.dump()).append(", ");
        }
        return sb.toString();
    }
}
