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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.settingskit;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class Option {
    public static final int STRATEGY_IMMEDIATELY = 0;
    public static final int STRATEGY_RESTART_APP = 1;

    public static final int VALUE_FROM_DEFAULT = 0;
    public static final int VALUE_FROM_REMOTE = 1;
    public static final int VALUE_FROM_USER = 2;

    public static final int TYPE_RATIO_BUTTON = 1;
    public static final int TYPE_SELECTABLE_ITEMS = 2;
    public static final int TYPE_EDITABLE_TEXT = 3;

    public final int type;
    public final String category;
    public final String title;
    public final String key;
    public final int strategy;
    public final Class<?> clazz;
    public final Object defaultValue;
    public final List<Object> candidates;

    private Object value;
    private int valueFrom;

    private Options.UserValues mUserValues;
    private Options.RemoteValues mRemoteValues;

    public Option(int type, String category, String key, String title, int strategy, Class<?> clazz, Object defaultValue, List<Object> candidates) {
        this.type = type;
        this.category = category;
        this.key = key;
        this.title = title;
        this.strategy = strategy;
        this.clazz = clazz;
        this.defaultValue = defaultValue;
        this.candidates = candidates;
    }

    public void setup(Options.UserValues userValues, Options.RemoteValues remoteValues) {
        this.mUserValues = userValues;
        this.mRemoteValues = remoteValues;
    }

    public Options.UserValues userValues() {
        return mUserValues;
    }

    public Options.RemoteValues remoteValues() {
        return mRemoteValues;
    }

    public Object userValue() {
        if (mUserValues != null) {
            return mUserValues.getValue(this);
        }
        return null;
    }

    public Object remoteValue() {
        if (mRemoteValues != null) {
            return mRemoteValues.getValue(this);
        }
        return null;
    }

    public Object value() {
        if (value == null || strategy == STRATEGY_IMMEDIATELY) {
            Object userValue = userValue();
            if (userValue != null) {
                value = userValue;
                valueFrom = Option.VALUE_FROM_USER;
                return value;
            }
            Object remoteValue = remoteValue();
            if (remoteValue != null) {
                value = remoteValue;
                valueFrom = VALUE_FROM_REMOTE;
                return value;
            }
            value = defaultValue;
            valueFrom = VALUE_FROM_DEFAULT;
        }
        return value;
    }

    public <T> T value(Class<T> clazz) {
        if (this.clazz != clazz) {
            throw new IllegalArgumentException(this.clazz + " is not compare with " + clazz);
        }
        if (value == null || strategy == STRATEGY_IMMEDIATELY) {
            T userValue = userValue(clazz);
            if (userValue != null) {
                value = userValue;
                valueFrom = Option.VALUE_FROM_USER;
                return clazz.cast(value);
            }
            T remoteValue = remoteValue(clazz);
            if (remoteValue != null) {
                value = remoteValue;
                valueFrom = VALUE_FROM_REMOTE;
                return clazz.cast(value);
            }
            value = defaultValue;
            valueFrom = VALUE_FROM_DEFAULT;
            return clazz.cast(value);
        }
        return clazz.cast(value);
    }

    public <T> T userValue(Class<T> clazz) {
        if (this.clazz != clazz) {
            throw new IllegalArgumentException(this.clazz + " is not compare with " + clazz);
        }
        return clazz.cast(userValue());
    }

    public <T> T remoteValue(Class<T> clazz) {
        if (this.clazz != clazz) {
            throw new IllegalArgumentException(this.clazz + " is not compare with " + clazz);
        }
        return clazz.cast(remoteValue());
    }

    public final int valueFrom() {
        return valueFrom;
    }

    public int intValue() {
        return value(Integer.class);
    }

    public boolean booleanValue() {
        return value(Boolean.class);
    }

    public long longValue() {
        return value(Long.class);
    }

    public float floatValue() {
        return value(Float.class);
    }

    @Nullable
    public String stringValue() {
        return value(String.class);
    }

    @Nullable
    public JSONObject jsonObjectValue() {
        return value(JSONObject.class);
    }

    @Nullable
    public JSONArray jsonArrayValue() {
        return value(JSONArray.class);
    }

    public static String obj2String(Object o, Class<?> clazz) {
        final String valueStr;
        if (clazz == Integer.class ||
                clazz == Long.class ||
                clazz == Float.class ||
                clazz == Double.class ||
                clazz == Boolean.class ||
                clazz == String.class ||
                clazz == JSONObject.class ||
                clazz == JSONArray.class) {
            valueStr = String.valueOf(o);
        } else {
            valueStr = new Gson().toJson(o);
        }
        return valueStr;
    }

    public static Object string2Obj(String value, Class<?> clazz) {
        if (value == null) return null;
        if (clazz == Integer.class) {
            return Integer.parseInt(value);
        } else if (clazz == Long.class) {
            return Long.parseLong(value);
        } else if (clazz == Float.class) {
            return Float.parseFloat(value);
        } else if (clazz == Double.class) {
            return Double.parseDouble(value);
        } else if (clazz == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (clazz == String.class) {
            return value;
        } else if (clazz == JSONObject.class) {
            try {
                return new JSONObject(value);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else if (clazz == JSONArray.class) {
            try {
                return new JSONArray(value);
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return new Gson().fromJson(value, clazz);
        }
    }
}
