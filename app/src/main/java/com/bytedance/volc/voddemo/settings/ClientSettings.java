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
import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.R;
import java.util.ArrayList;
import java.util.List;

public class ClientSettings {
    private static final String TAG = "ClientSettings";

    private static final String CLIENT_SETTING_SP_NAME = "client_settings";
    private final Context mContext;
    private final SettingSpHelper mSettingSpHelper;
    private BoolSettingItem mHwSetting;

    public ClientSettings(@NonNull Context context) {
        mContext = context;
        mSettingSpHelper = new SettingSpHelper(context, CLIENT_SETTING_SP_NAME);
    }

    public List<SettingItem> getAll() {
        List<SettingItem> settings = new ArrayList<>();

        settings.add(new TitleSettingItem(mContext.getString(R.string.setting_engine_strategy)));
        settings.add(new BoolSettingItem(mContext.getString(R.string.enable_common_strategy),
                getBool(R.string.enable_common_strategy_key, true), f -> {
            setBool(R.string.enable_common_strategy_key, f);
        }));
        settings.add(new BoolSettingItem(mContext.getString(R.string.enable_preload_strategy),
                getBool(R.string.enable_preload_strategy_key, true), f -> {
            setBool(R.string.enable_preload_strategy_key, f);
        }));
        settings.add(new BoolSettingItem(mContext.getString(R.string.enable_pre_render_strategy),
                getBool(R.string.enable_pre_render_strategy_key, true), f -> {
            setBool(R.string.enable_pre_render_strategy_key, f);
        }));

        settings.add(new TitleSettingItem(mContext.getString(R.string.engine_setting)));
        settings.add(new BoolSettingItem(
                mContext.getString(R.string.set_enable_manual_video_hardware_decode),
                getBool(R.string.set_enable_manual_video_hardware_decode_key, false), aBoolean -> {
            setBool(R.string.set_enable_manual_video_hardware_decode_key, aBoolean);
            if (mHwSetting != null) {
                mHwSetting.setEnable(aBoolean);
            }
        }));

        mHwSetting = new BoolSettingItem(mContext.getString(R.string.set_video_hardware_decode),
                getBool(R.string.set_video_hardware_decode_key, false), false,
                aBoolean -> setBool(R.string.set_video_hardware_decode_key, aBoolean));
        settings.add(mHwSetting);

        settings.add(new BoolSettingItem(mContext.getString(R.string.set_video_enable_H265),
                getBool(R.string.set_video_enable_H265_key, true),
                aBoolean -> setBool(R.string.set_video_enable_H265_key, aBoolean)));

        settings.add(new BoolSettingItem(mContext.getString(R.string.set_enable_preload),
                getBool(R.string.set_enable_preload_key, true),
                aBoolean -> setBool(R.string.set_enable_preload_key, aBoolean)));

        settings.add(new BoolSettingItem(mContext.getString(R.string.set_engine_enable_uploadlog),
                getBool(R.string.set_engine_enable_uploadlog_key, true), aBoolean -> {
            setBool(R.string.set_engine_enable_uploadlog_key, aBoolean);
        }));

        settings.add(new BoolSettingItem(mContext.getString(R.string.set_mdl_enable_uploadlog),
                getBool(R.string.set_mdl_enable_uploadlog_key, true), aBoolean -> {
            setBool(R.string.set_mdl_enable_uploadlog_key, aBoolean);
        }));

        return settings;
    }

    public void setBool(final int res, boolean value) {
        mSettingSpHelper.setBool(res, value);
    }

    public boolean getBool(final int res, final boolean defaultValue) {
        return mSettingSpHelper.getBool(res, defaultValue);
    }

    public boolean videoEnableH265() {
        return getBool(R.string.set_video_enable_H265_key, true);
    }

    public boolean engineEnableUploadLog() {
        return getBool(R.string.set_engine_enable_uploadlog_key, true);
    }

    public boolean mdlEnableUploadLog() {
        return getBool(R.string.set_mdl_enable_uploadlog_key, true);
    }

    public boolean enablePreload() {
        return getBool(R.string.set_enable_preload_key, true);
    }

    public boolean enableManualVideoHW() {
        return getBool(R.string.set_enable_manual_video_hardware_decode_key, false);
    }

    public boolean enableVideoHW() {
        return getBool(R.string.set_video_hardware_decode_key, false);
    }

    public boolean enableStrategyCommon() {
        return getBool(R.string.enable_common_strategy_key, true);
    }

    public boolean enableStrategyPreload() {
        return getBool(R.string.enable_preload_strategy_key, true);
    }

    public boolean enableStrategyPreRender() {
        return getBool(R.string.enable_pre_render_strategy_key, true);
    }
}

