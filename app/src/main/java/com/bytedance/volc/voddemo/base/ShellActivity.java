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
 * Create Date : 2021/6/10
 */
package com.bytedance.volc.voddemo.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.settings.SettingActivity;
import com.bytedance.volc.voddemo.smallvideo.SmallVideoFragment;

import static com.bytedance.volc.voddemo.data.VideoItem.VIDEO_TYPE_SMALL;

public class ShellActivity extends AppCompatActivity {

    final private static String ARG_VIDEO_TYPE = "pages_shell_activity_arg_video_type";

    public static void startNewIntent(Activity from, int videoType) {
        Intent intent = new Intent(from, ShellActivity.class);
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_VIDEO_TYPE, videoType);
        intent.putExtras(bundle);
        from.startActivity(intent);
    }

    private int mVideoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shell);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mVideoType = bundle.getInt(ARG_VIDEO_TYPE);
        }

        findViewById(R.id.iv_settings).setOnClickListener(v -> {
            final Intent intent = new Intent(ShellActivity.this, SettingActivity.class);
            startActivity(intent);
        });

        if (savedInstanceState == null) {
            Fragment fragment = new SmallVideoFragment();
            if (mVideoType == VIDEO_TYPE_SMALL) {
                fragment = new SmallVideoFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commitNow();
        }
    }
}
