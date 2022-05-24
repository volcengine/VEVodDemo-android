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
 * Create Date : 2021/6/8
 */
package com.bytedance.volc.voddemo.settings;

import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bytedance.applog.AppLog;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.VodApp;
import com.pandora.common.applog.AppLogWrapper;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.List;

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.settings));
        setSupportActionBar(toolbar);
        final ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayHomeAsUpEnabled(true);
            supportActionBar.setHomeButtonEnabled(true);
        }

        initSettings();
    }

    private void initSettings() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ClientSettings clientSettings = VodApp.getClientSettings();
        List<SettingItem> settingItems = clientSettings.getAll();
        SettingAdapter adapter = new SettingAdapter(settingItems);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        RelativeLayout rlDid = findViewById(R.id.rl_did);
        TextView tvDid = findViewById(R.id.tv_did);
        findViewById(R.id.bt_getDid).setOnClickListener(v -> {
            rlDid.setVisibility(View.VISIBLE);
            final String did = AppLogWrapper.getDid();
            TTVideoEngineLog.d(TAG, "did " + did);
            tvDid.setText(did);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}