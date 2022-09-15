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

package com.bytedance.volc.vod.settingskit;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

public class SettingItem {
    public static final int TYPE_CATEGORY_TITLE = Integer.MAX_VALUE;

    public static final int TYPE_OPTION = 10000;
    public static final int TYPE_COPYABLE_TEXT = 10001;
    public static final int TYPE_CLICKABLE_ITEM = 10002;

    public final int type;
    public String category;
    public String title;
    public Getter getter;
    public Option option;
    public ValueMapper mapper;
    public OnEventListener listener;

    public static SettingItem createCategoryItem(String category) {
        SettingItem item = new SettingItem(SettingItem.TYPE_CATEGORY_TITLE);
        item.category = category;
        return item;
    }

    public static SettingItem createOptionItem(String category, Option option) {
        return createOptionItem(category, option, null);
    }

    public static SettingItem createOptionItem(String category, Option option, ValueMapper mapper) {
        SettingItem item = new SettingItem(SettingItem.TYPE_OPTION);
        item.category = category;
        item.option = option;
        item.mapper = mapper != null ? mapper : ValueMapper.DEFAULT;
        return item;
    }

    public static SettingItem createCopyableTextItem(String category, String title, Getter getter) {
        SettingItem item = new SettingItem(SettingItem.TYPE_COPYABLE_TEXT);
        item.category = category;
        item.title = title;
        item.getter = getter;
        return item;
    }

    public static SettingItem createClickableItem(String category, String title, Getter getter, OnEventListener listener) {
        SettingItem item = new SettingItem(SettingItem.TYPE_CLICKABLE_ITEM);
        item.category = category;
        item.title = title;
        item.getter = getter;
        item.listener = listener;
        return item;
    }

    public static SettingItem createEditableItem(String category, String title, Getter getter) {
        SettingItem item = new SettingItem(SettingItem.TYPE_COPYABLE_TEXT);
        item.category = category;
        item.title = title;
        item.getter = getter;
        return item;
    }

    public interface ValueMapper {

        ValueMapper DEFAULT = String::valueOf;

        String toString(Object value);
    }

    public static class Getter {
        public final DirectGetter directGetter;
        public final AsyncGetter asyncGetter;

        public Getter(DirectGetter directGetter) {
            this.directGetter = directGetter;
            this.asyncGetter = null;
        }

        public Getter(AsyncGetter asyncGetter) {
            this.directGetter = null;
            this.asyncGetter = asyncGetter;
        }

        public interface DirectGetter {
            String get();
        }

        public interface AsyncGetter {
            void get(Setter setter);
        }
    }

    public interface Setter {
        void set(String text);
    }

    public interface OnEventListener {
        int EVENT_TYPE_CLICK = 0;

        void onEvent(int eventType, Context context, SettingItem settingItem, RecyclerView.ViewHolder holder);
    }

    public SettingItem(int type) {
        this.type = type;
    }
}
