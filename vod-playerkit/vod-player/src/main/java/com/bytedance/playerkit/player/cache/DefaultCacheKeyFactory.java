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
 * Create Date : 2025/3/25
 */

package com.bytedance.playerkit.player.cache;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.MD5;

import java.net.MalformedURLException;
import java.net.URL;

public class DefaultCacheKeyFactory implements CacheKeyFactory {

    @Override
    public String generateCacheKey(@NonNull MediaSource source, @NonNull Track track) {
        if (!TextUtils.isEmpty(track.getFileHash())) {
            return track.getFileHash();
        }
        if (!TextUtils.isEmpty(track.getFileId())) {
            return track.getFileId();
        }
        String fileHash = generateCacheKey(track.getUrl());
        track.setFileHash(fileHash);
        return fileHash;
    }

    @Override
    public String generateCacheKey(@NonNull MediaSource source, @NonNull Subtitle subtitle) {
        if (!TextUtils.isEmpty(subtitle.getCacheKey())) {
            return subtitle.getCacheKey();
        }
        String cacheKey = generateCacheKey(subtitle.getUrl());
        subtitle.setCacheKey(cacheKey);
        return cacheKey;
    }

    @Override
    public String generateCacheKey(@NonNull String url) {
        String path;
        try {
            URL u = new URL(url);
            path = u.getPath();
        } catch (MalformedURLException e) {
            path = url;
        }
        return MD5.getMD5(path);
    }
}
