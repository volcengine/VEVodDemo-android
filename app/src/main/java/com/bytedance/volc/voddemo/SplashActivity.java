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

package com.bytedance.volc.voddemo;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    // 这里只控制提示 UI，不会取消或重新发起正在执行的进程级初始化。
    private static final long INIT_SLOW_REMINDER_MS = 10_000L;

    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Runnable mSlowInitReminder = this::showSlowInitDialog;
    private boolean mInitCompleted;
    private AlertDialog mInitDialog;
    private VodDemoApi.ReadyRequest mReadyRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fix reopen app from launcher, app restart instead resume
        // https://stackoverflow.com/questions/19545889/app-restarts-rather-than-resumes
        if (!isTaskRoot()) {
            Intent intent = getIntent();
            if (intent != null
                    && intent.hasCategory(Intent.CATEGORY_LAUNCHER)
                    && Intent.ACTION_MAIN.equals(intent.getAction())) {
                finish();
                return;
            }
        }

        initSystemBar();
        intentIntoMain();
    }

    private void initSystemBar() {
        int flags = getWindow().getDecorView().getSystemUiVisibility();
        flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        getWindow().getDecorView().setSystemUiVisibility(flags);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().setNavigationBarColor(Color.WHITE);
    }

    private void intentIntoMain() {
        if (!VodDemoApi.isVodSdkAsyncInitEnabled()) {
            VodDemoApi.intentInto(this, false);
            finish();
            return;
        }

        // 只在页面入口等待 SDK 初始化；进入点播业务后继续沿用原有同步 PlayerKit API。
        mMainHandler.postDelayed(mSlowInitReminder, INIT_SLOW_REMINDER_MS);
        // Activity 作为 LifecycleOwner 传入后，页面销毁会自动取消本次待执行跳转。
        mReadyRequest = VodDemoApi.runWhenVodSdkReady(
                this,
                () -> {
                    onInitFinished();
                    VodDemoApi.intentInto(this, false);
                    finish();
                },
                error -> {
                    onInitFinished();
                    Log.e(TAG, "Vod SDK initialization failed", error);
                    showInitFailureDialog();
                });
    }

    private void onInitFinished() {
        mInitCompleted = true;
        mMainHandler.removeCallbacks(mSlowInitReminder);
        dismissInitDialog();
    }

    private void showSlowInitDialog() {
        if (mInitCompleted || isFinishing() || isDestroyed() || mInitDialog != null) {
            return;
        }
        mInitDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.vevod_init_slow_title)
                .setMessage(R.string.vevod_init_slow_message)
                .setPositiveButton(R.string.vevod_continue_waiting, (dialog, which) -> {
                    mInitDialog = null;
                })
                .setNegativeButton(R.string.vevod_close_app, (dialog, which) -> finishAffinity())
                .setOnCancelListener(dialog -> mInitDialog = null)
                .show();
    }

    private void showInitFailureDialog() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        mInitDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.vevod_init_failed_title)
                .setMessage(R.string.vevod_init_failed_message)
                .setCancelable(false)
                .setPositiveButton(R.string.vevod_close_app, (dialog, which) -> finishAffinity())
                .show();
    }

    private void dismissInitDialog() {
        if (mInitDialog != null) {
            mInitDialog.dismiss();
            mInitDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        // 显式取消用于明确请求所有权；与 Lifecycle 自动取消重复调用也安全。
        if (mReadyRequest != null) {
            mReadyRequest.cancel();
            mReadyRequest = null;
        }
        mMainHandler.removeCallbacks(mSlowInitReminder);
        dismissInitDialog();
        super.onDestroy();
    }
}
