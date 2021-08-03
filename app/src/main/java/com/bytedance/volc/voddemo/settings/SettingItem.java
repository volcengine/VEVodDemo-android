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

interface EnableListener {
    void onEnableChange(boolean enable);
}

public abstract class SettingItem {
    public static final int TEXT_VIEW_TYPE = 0;
    public static final int BOOL_VIEW_TYPE = 1;
    public static final int BUTTON_VIEW_TYPE = 2;
    private final int type;
    protected boolean mEnable;
    protected EnableListener mEnableListener;

    public boolean isEnable() {
        return mEnable;
    }

    public void setEnable(boolean enable) {
        mEnable = enable;
        if (mEnableListener != null) {
            mEnableListener.onEnableChange(enable);
        }
    }

    protected SettingItem(final int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setEnableListener(EnableListener enableListener) {
        mEnableListener = enableListener;
    }
}

class BoolSettingItem extends SettingItem {
    private final String text;
    private boolean defaultValue;
    private final Function1<Boolean> mFunction;

    public BoolSettingItem(final String text, final boolean defaultValue,
            Function1<Boolean> function) {
        this(text, defaultValue, true, function);
    }

    public BoolSettingItem(final String text, final boolean defaultValue, boolean enable,
            Function1<Boolean> function) {
        super(BOOL_VIEW_TYPE);
        this.text = text;
        this.defaultValue = defaultValue;
        mFunction = function;
        mEnable = enable;
    }

    public String getText() {
        return text;
    }

    public boolean isDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final boolean defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Function1<Boolean> getFunction() {
        return mFunction;
    }

    interface Function1<From> {
        void onSave(From f);
    }
}
