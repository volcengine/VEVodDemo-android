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

package com.bytedance.volc.vod.scenekit.ui.video.scene.base;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.playerkit.utils.L;


public class BaseActivity extends AppCompatActivity {

    @Override
    @CallSuper
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        L.d(this, "onCreate");
    }

    @Override
    @CallSuper
    protected void onStart() {
        super.onStart();
        L.d(this, "onStart");
    }

    @Override
    @CallSuper
    protected void onResume() {
        super.onResume();
        L.d(this, "onResume");
    }

    @Override
    @CallSuper
    protected void onPause() {
        super.onPause();
        L.d(this, "onPause");
    }

    @Override
    @CallSuper
    protected void onStop() {
        super.onStop();
        L.d(this, "onStop");
    }

    @Override
    @CallSuper
    protected void onDestroy() {
        super.onDestroy();
        L.d(this, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        L.d(this, "onBackPressed");
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        L.d(this, "finish");
    }
}
