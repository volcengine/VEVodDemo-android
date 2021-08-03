/*
 * Copyright 2021 bytedance
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
 * Create Date : 2021/2/26
 */
package com.bytedance.volc.voddemo.settings;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingSpHelper {
    private final Context mContext;
    private final SharedPreferences mSharedPreferences;
    private final Map<Integer, Boolean> mCacheBools = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> mCacheIntegers = new ConcurrentHashMap<>();
    private final Map<Integer, String> mCacheStrs = new ConcurrentHashMap<>();

    public SettingSpHelper(@NonNull final Context context, @NonNull final String spName) {
        mContext = context.getApplicationContext();
        mSharedPreferences = mContext.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }

    public boolean getBool(@StringRes int res, final boolean defaultValue) {
        if (mCacheBools.containsKey(res)) {
            return mCacheBools.get(res);
        } else {
            final boolean result = mSharedPreferences.getBoolean(mContext.getString(res),
                    defaultValue);
            mCacheBools.put(res, result);
            return result;
        }
    }

    protected void setBool(@StringRes int res, boolean value) {
        mCacheBools.put(res, value);
        mSharedPreferences.edit().putBoolean(mContext.getString(res), value).apply();
    }

    public String getStr(@StringRes int res, String defaultValue) {
        if (mCacheStrs.containsKey(res)) {
            String result = mCacheStrs.get(res);
            return result == null ? defaultValue : result;
        } else {
            String result = mSharedPreferences.getString(mContext.getString(res), defaultValue);
            mCacheStrs.put(res, result);
            return result;
        }
    }

    public void setStr(@StringRes int res, String value) {
        mCacheStrs.put(res, value);
        mSharedPreferences.edit().putString(mContext.getString(res), value).apply();
    }

    public void setInt(final int res, final Integer value) {
        mCacheIntegers.put(res, value);
        mSharedPreferences.edit().putInt(mContext.getString(res), value).apply();
    }

    public int getInt(final int res, final int defaultValue) {
        if (mCacheIntegers.containsKey(res)) {
            return mCacheIntegers.get(res);
        } else {
            final int result = mSharedPreferences.getInt(mContext.getString(res), defaultValue);
            mCacheIntegers.put(res, result);
            return result;
        }
    }
}
