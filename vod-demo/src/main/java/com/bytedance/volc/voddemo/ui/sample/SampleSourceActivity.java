/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/3/26
 */

package com.bytedance.volc.voddemo.ui.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.utils.MD5;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.base.BaseActivity;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.impl.R;

import java.io.File;
import java.util.ArrayList;

public class SampleSourceActivity extends BaseActivity {

    public static void intentInto(Activity activity) {
        Intent intent = new Intent(activity, SampleSourceActivity.class);
        activity.startActivity(intent);
    }

    private SharedPreferences mSp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.vevod_sample_source);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(ResourcesCompat.getDrawable(
                getResources(),
                R.drawable.vevod_actionbar_back,
                null));
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.black));
        toolbar.setBackgroundColor(Color.WHITE);
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(getResources().getColor(android.R.color.black));
        }

        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("");
        }

        UIUtils.setSystemBarTheme(
                this,
                Color.WHITE,
                true,
                false,
                Color.WHITE,
                true,
                false
        );

        restore();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        save();
        super.onDestroy();
    }

    private void save() {
        final EditText editText = findViewById(R.id.input);
        final Editable editable = editText.getText();

        final String input = editable == null ? "" : editable.toString();
        final String recorded = mSp.getString("input", null);
        if (!TextUtils.equals(input, recorded)) {
            mSp.edit().putString("input", input).apply();
        }
    }

    private void restore() {
        EditText editText = findViewById(R.id.input);

        mSp = getSharedPreferences("vod_demo_media_source", Context.MODE_PRIVATE);
        String input = mSp.getString("input", null);

        if (!TextUtils.isEmpty(input)) {
            editText.setText(input);
        }
    }

    public void onBuildSceneClick(View view) {
        final EditText editText = findViewById(R.id.input);

        final Editable editable = editText.getText();
        if (TextUtils.isEmpty(editable)) {
            editText.setError("Empty!");
            return;
        }

        final String input = editable.toString();
        ArrayList<VideoItem> videoItems = createVideoItems(input.split("\n"));
        if (videoItems.isEmpty()) {
            editText.setError("Url is not illegal!");
            return;
        }
        SampleFeedVideoActivity.intentInto(this, videoItems);
    }

    private ArrayList<VideoItem> createVideoItems(String[] urls) {
        final ArrayList<VideoItem> videoItems = new ArrayList<>();
        int index = 0;
        for (String url : urls) {
            if (TextUtils.isEmpty(url)) continue;
            if (url.startsWith("/")) {
                File file = new File(url);
                if (!file.exists()) continue;
            }
            if (!url.startsWith("http") && !url.startsWith("file")) continue;

            VideoItem videoItem = VideoItem.createUrlItem(MD5.getMD5(url), url, null, 0, null, index + ": " + url);
            videoItems.add(videoItem);
            index++;
        }
        return videoItems;
    }

    public void onCleanCacheClick(View view) {
        Toast.makeText(this, "Cleaning cache...", Toast.LENGTH_SHORT).show();
        CacheLoader.Default.get().clearCache();
        Toast.makeText(this, "Clean done!", Toast.LENGTH_SHORT).show();
    }
}
