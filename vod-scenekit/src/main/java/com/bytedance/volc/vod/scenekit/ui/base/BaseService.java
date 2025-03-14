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
 * Create Date : 2025/3/17
 */

package com.bytedance.volc.vod.scenekit.ui.base;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;

public class BaseService extends Service {

    public BaseService() {
        super();
        L.d(this, "construction");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        L.d(this, "onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        L.d(this, "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        L.d(this, "onStartCommand", intent, flags, startId);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        L.d(this, "onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        L.d(this, "onConfigurationChanged", newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        L.d(this, "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        L.d(this, "onLowMemory", level);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        L.d(this, "onUnbind", intent);
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        L.d(this, "onRebind", intent);
        super.onRebind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        L.d(this, "onTaskRemoved", rootIntent);
    }
}
