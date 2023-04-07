/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/3/24
 */

package com.bytedance.playerkit.player.volcengine;

import android.text.TextUtils;
import android.util.LruCache;

import androidx.annotation.Nullable;

import com.ss.ttvideoengine.model.IVideoModel;
import com.ss.ttvideoengine.model.VideoModel;

import org.json.JSONObject;

public class VolcVideoModelCache {
    /**
     * NOTE: {@link LruCache} is thread safe
     */
    private static final LruCache<String, IVideoModel> CACHE = new LruCache<String, IVideoModel>(100);

    private static boolean sEnabled;

    public static void setEnabled(boolean enabled) {
        sEnabled = enabled;
    }

    public static void resize(int maxSize) {
        CACHE.resize(maxSize);
    }

    public static void cache(String jsonModel) {
        acquire(jsonModel);
    }

    @Nullable
    static IVideoModel acquire(String jsonModel) {
        IVideoModel videoModel = get(jsonModel);
        if (videoModel != null) {
            return videoModel;
        }
        videoModel = Factory.create(jsonModel);
        if (videoModel != null) {
            put(jsonModel, videoModel);
        }
        return videoModel;
    }

    private static IVideoModel get(String key) {
        if (!sEnabled) {
            return null;
        }
        if (TextUtils.isEmpty(key)) return null;
        return CACHE.get(key);
    }

    private static IVideoModel put(String key, IVideoModel value) {
        if (TextUtils.isEmpty(key)) return value;
        if (value == null) return null;
        return CACHE.put(key, value);
    }

    public static void trim(int maxSize) {
        CACHE.trimToSize(maxSize);
    }

    public static void clear() {
        CACHE.evictAll();
    }

    static class Factory {

        @Nullable
        static IVideoModel create(String jsonModel) {
            if (TextUtils.isEmpty(jsonModel)) return null;
            try {
                VideoModel model = new VideoModel();
                model.extractFields(new JSONObject(jsonModel));
                return model;
            } catch (Throwable e) {
                return null;
            }
        }
    }
}
