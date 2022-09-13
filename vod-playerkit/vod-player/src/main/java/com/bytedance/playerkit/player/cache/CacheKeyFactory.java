/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.player.cache;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.MD5;


public interface CacheKeyFactory {

    CacheKeyFactory DEFAULT = new CacheKeyFactory() {
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
        public String generateCacheKey(@NonNull String url) {
            return MD5.getMD5(url);
        }
    };

    String generateCacheKey(@NonNull MediaSource source, @NonNull Track track);

    String generateCacheKey(@NonNull String url);
}
