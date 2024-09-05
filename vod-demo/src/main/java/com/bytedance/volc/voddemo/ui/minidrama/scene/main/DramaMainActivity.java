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

package com.bytedance.volc.voddemo.ui.minidrama.scene.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.ui.base.BaseActivity;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.ViewPager2Helper;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.impl.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DramaMainActivity extends BaseActivity {

    public static void intentInto(Activity activity) {
        Intent intent = new Intent(activity, DramaMainActivity.class);
        activity.startActivity(intent);
    }

    private ViewPager2 mViewPager;
    private DramaMainAdapter mAdapter;
    private TabLayout mTabLayout;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vevod_mini_drama_main_activity);
        // Prevent user take screenshot in your app
        if (VideoSettings.booleanValue(VideoSettings.DRAMA_VIDEO_PREVENT_SCREEN_SHOT)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setOnTouchListener((v, event) -> mTabLayout.dispatchTouchEvent(event));

        mTabLayout = findViewById(R.id.tabLayout);
        ((ViewGroup.MarginLayoutParams) mTabLayout.getLayoutParams()).topMargin = UIUtils.getStatusBarHeight(this);
        ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams()).topMargin = UIUtils.getStatusBarHeight(this);

        mViewPager = findViewById(R.id.viewPager);
        ViewPager2Helper.setup(mViewPager);
        mViewPager.setOffscreenPageLimit(1);
        mAdapter = new DramaMainAdapter(this);
        mViewPager.setAdapter(mAdapter);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                setCurrentItemTheme(position);
            }
        });
        new TabLayoutMediator(mTabLayout, mViewPager, (tab, position) -> tab.setText(mAdapter.getCurrentItemTitle(position))).attach();

        setCurrentItem(0);
    }

    private void setCurrentItem(int position) {
        mViewPager.setCurrentItem(position);
        setCurrentItemTheme(position);
    }

    private void setCurrentItemTheme(int position) {
        if (position == 0) {
            setActionBarTheme(
                    true,
                    true,
                    null,
                    Color.TRANSPARENT,
                    getResources().getColor(android.R.color.black));
            UIUtils.setSystemBarTheme(
                    this,
                    Color.TRANSPARENT,
                    true,
                    true,
                    Color.BLACK,
                    false,
                    false
            );
            mTabLayout.setTabTextColors(AppCompatResources.getColorStateList(this, R.color.vevod_mini_drama_main_tab_text_color_light));
            mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.vevod_mini_drama_main_tab_indicator_light));
        } else {
            setActionBarTheme(
                    true,
                    true,
                    null,
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
            mTabLayout.setTabTextColors(AppCompatResources.getColorStateList(this, R.color.vevod_mini_drama_main_tab_text_color_dark));
            mTabLayout.setSelectedTabIndicatorColor(getResources().getColor(R.color.vevod_mini_drama_main_tab_indicator_dark));
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