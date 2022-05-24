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
package com.bytedance.volc.voddemo;

import android.annotation.SuppressLint;
import android.app.Application;
import com.bytedance.volc.voddemo.settings.ClientSettings;
import com.pandora.common.env.Env;
import com.pandora.common.env.config.Config;
import com.pandora.ttlicense2.LicenseManager;
import com.ss.mediakit.medialoader.AVMDLLog;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;

public class VodApp extends Application {
    private static final String APP_ID = "229234";
    private static final String APP_NAME = "VOLCVodDemo";
    private static final String APP_CHANNEL = "VOLCVodDemoAndroid";

    @SuppressLint("StaticFieldLeak")
    private static ClientSettings sClientSettings;

    public static ClientSettings getClientSettings() {
        return sClientSettings;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sClientSettings = new ClientSettings(this);
        initVodSDK();
    }

    private void initVodSDK() {
        // VOD key step init 1: open debug log
        if (BuildConfig.DEBUG) {
            TTVideoEngineLog.turnOn(TTVideoEngineLog.LOG_DEBUG, 1);
            AVMDLLog.turnOn(AVMDLLog.LOG_DEBUG, 1);
            LicenseManager.turnOnLogcat(true);
        }

        // VOD key step init 2: init
        Env.init(new Config.Builder()
                .setApplicationContext(getApplicationContext())
                .setAppID(APP_ID)
                .setAppName(APP_NAME)
                // 合法版本号应大于、等于 2 个分隔符，如："1.3.2"
                .setAppVersion(BuildConfig.VERSION_NAME)
                .setAppChannel(APP_CHANNEL)
                // 将 license 文件拷贝到 app 的 assets 文件夹中，并设置 LicenseUri
                // 下面 LicenseUri 对应工程中 assets 路径为：assets/license/vod.lic
                .setLicenseUri("assets:///license2/volc_vod_demo_license2.lic")
                .build());
    }
}
