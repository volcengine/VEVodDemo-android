/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/9/5
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.detail;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.impl.R;

public class DramaDetailVideoActivity extends AppCompatActivity {

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vevod_mini_drama_detail_video_activity);

        // Prevent user take screenshot in your app
        if (VideoSettings.booleanValue(VideoSettings.DRAMA_VIDEO_PREVENT_SCREEN_SHOT)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = UIUtils.getStatusBarHeight(this);

        setActionBarTheme(
                true,
                true,
                " ",
                Color.TRANSPARENT,
                getResources().getColor(android.R.color.white));

        UIUtils.setSystemBarTheme(
                this,
                Color.TRANSPARENT,
                false,
                true,
                Color.BLACK,
                false,
                false
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .add(R.id.container, DramaDetailVideoFragment.class, null, DramaDetailVideoFragment.TAG)
                    .commit();
        }
    }

    private void setActionBarTheme(boolean showActionBar,
                                   boolean immersiveStatusBar,
                                   String title,
                                   int bgColor,
                                   int textColor) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (showActionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setNavigationIcon(ResourcesCompat.getDrawable(
                    getResources(),
                    R.drawable.vevod_actionbar_back,
                    getTheme()));
            toolbar.setTitleTextColor(textColor);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(textColor);
            }
            actionBar.setTitle(title);
        } else {
            actionBar.hide();
        }
    }
}