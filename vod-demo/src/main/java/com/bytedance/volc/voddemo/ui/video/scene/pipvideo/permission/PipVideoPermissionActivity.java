/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/3/19
 */

package com.bytedance.volc.voddemo.ui.video.scene.pipvideo.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class PipVideoPermissionActivity extends AppCompatActivity {
    private static final String EXTRA_PIP_PERMISSION_KEY = "extra_pip_permission_key";

    private PipVideoPermission mPermission;

    static void intentInto(Context context, String key) {
        Intent intent = new Intent(context, PipVideoPermissionActivity.class);
        intent.putExtra(EXTRA_PIP_PERMISSION_KEY, key);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String key = getIntent().getStringExtra(EXTRA_PIP_PERMISSION_KEY);
        mPermission = PipVideoPermission.get(key);
        if (mPermission != null) {
            mPermission.rationale(this);
        } else {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mPermission != null) {
            mPermission.onActivityResult(requestCode, resultCode, data);
            finish();
        }
    }
}
