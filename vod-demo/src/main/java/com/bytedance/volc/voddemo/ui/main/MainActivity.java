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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.voddemo.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.vod.scenekit.ui.video.scene.base.BaseActivity;

public class MainActivity extends BaseActivity {

    public static void intentInto(Activity activity, boolean showActionBar) {
        Intent intent = new Intent(activity, MainActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean(MainFragment.EXTRA_SHOW_ACTION_BAR, showActionBar);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    private boolean mShowActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mShowActionBar = getIntent().getBooleanExtra(MainFragment.EXTRA_SHOW_ACTION_BAR, false);

        UIUtils.setSystemBarTheme(
                this,
                Color.TRANSPARENT,
                true,
                true,
                Color.WHITE,
                true,
                false
        );

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, MainFragment.newInstance(getIntent().getExtras()))
                    .commit();
        }
    }


    @Override
    public void onBackPressed() {
        if (mShowActionBar) {
            super.onBackPressed();
        } else {
            final Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}