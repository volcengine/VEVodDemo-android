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

package com.bytedance.playerkit.utils;

import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


public class ExtraObject {

    protected final Map<String, Object> mExtras = Collections.synchronizedMap(new LinkedHashMap<>());

    public <T> T getExtra(@NonNull String key, @NonNull Class<T> clazz) {
        Object extra = mExtras.get(key);
        if (extra != null) {
            if (clazz.isInstance(extra)) {
                return (T) extra;
            }
            throw new ClassCastException(extra.getClass() + " can't be cast to + " + clazz);
        }
        return null;
    }

    public void putExtra(@NonNull String key, @Nullable Object extra) {
        if (extra == null) {
            mExtras.remove(key);
        } else {
            if (extra instanceof Serializable || extra instanceof Parcelable) {
                mExtras.put(key, extra);
            } else {
                throw new IllegalArgumentException("Unsupported type " + extra.getClass());
            }
        }
    }

    public void clearExtras() {
        mExtras.clear();
    }
}
