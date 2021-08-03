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

import android.widget.TextView;
import androidx.appcompat.widget.SwitchCompat;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.base.BaseAdapter;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.List;

import static com.bytedance.volc.voddemo.settings.SettingItem.BOOL_VIEW_TYPE;
import static com.bytedance.volc.voddemo.settings.SettingItem.BUTTON_VIEW_TYPE;

public class SettingAdapter extends BaseAdapter<SettingItem> {

    public SettingAdapter(final List<SettingItem> datas) {
        super(datas);
    }

    @Override
    public int getItemViewType(final int position) {
        SettingItem item = getItem(position);
        return item.getType();
    }

    @Override
    public int getLayoutId(final int viewType) {
        if (viewType == BOOL_VIEW_TYPE) {
            return R.layout.list_item_setting_bool;
        } else if (viewType == BUTTON_VIEW_TYPE) {
            return R.layout.list_item_setting_button;
        }

        return R.layout.list_item_setting_title;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final SettingItem data,
            final int position) {

        if (data instanceof BoolSettingItem) {
            BoolSettingItem settingItem = (BoolSettingItem) data;
            ((TextView) holder.getView(R.id.txt_test_text)).setText(settingItem.getText());
            SwitchCompat switchCompat = holder.getView(R.id.test_switcher);
            switchCompat.setEnabled(settingItem.isEnable());
            switchCompat.setOnCheckedChangeListener((buttonView, isChecked) -> {
                settingItem.setDefaultValue(isChecked);
                settingItem.getFunction().onSave(isChecked);
            });

            settingItem.setEnableListener(enable -> {
                switchCompat.setChecked(false);
                switchCompat.setEnabled(enable);
            });
            switchCompat.setChecked(settingItem.isDefaultValue());
        }
    }
}
