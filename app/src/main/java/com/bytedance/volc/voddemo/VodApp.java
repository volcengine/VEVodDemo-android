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
import android.content.Context;
import android.widget.Toast;
import com.bytedance.volc.voddemo.settings.ClientSettings;
import com.pandora.common.Constants;
import com.pandora.common.env.Env;
import com.pandora.ttlicense2.LicenseManager;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class VodApp extends Application {
    private static final String APP_ID = "";
    private static final String APP_NAME = "VOLCVodDemo";
    private static final String APP_CHANNEL = "VOLCVodDemoAndroid";
    private static final String APP_REGION = Constants.APPLog.APP_REGION_CHINA;
    private static final String APP_VERSION = BuildConfig.VERSION_NAME;

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
        // VOD key step init 1: init env
        initEnv();
        // VOD key step init 2: init License
        initLicense();
        // VOD key step init 3: init Engine
        initEngine();
        // VOD key step init 4: init MDL
        initMDL();
        // VOD key step init 5: open debug log
        openDebugLog();
    }

    private void openDebugLog() {
        if (BuildConfig.DEBUG) {
            LicenseManager.turnOnLogcat(true);
            TTVideoEngineLog.turnOn(TTVideoEngineLog.LOG_DEBUG, 1);
        }
    }

    private void initEnv() {
        Env.setupSDKEnv(new Env.SdkContextEnv() {
            @Override
            public Context getApplicationContext() {
                return VodApp.this;
            }

            @Override
            public Thread.UncaughtExceptionHandler getUncaughtExceptionHandler() {
                return (t, e) -> {
                };
            }

            @Override
            public String getAppID() {
                return APP_ID;
            }

            @Override
            public String getAppName() {
                return APP_NAME;
            }

            @Override
            public String getAppRegion() {
                return Constants.APPLog.APP_REGION_CHINA;
            }
        });
    }

    private void initLicense() {
        LicenseManager.init(this);
        String assetsLicenseUri = "assets:///license2/volc_vod_demo_license2.lic";
        LicenseManager.getInstance().addLicense(assetsLicenseUri, null);
    }

    private void initEngine() {
        Map<String, Object> appInfoMap = new HashMap<>();
        appInfoMap.put("appname", APP_NAME);
        appInfoMap.put("appid", APP_ID);
        appInfoMap.put("appchannel", APP_CHANNEL);
        appInfoMap.put("region", APP_REGION);
        appInfoMap.put("appversion", APP_VERSION);
        TTVideoEngine.setAppInfo(getApplicationContext(), appInfoMap);
        TTVideoEngine.initAppLog();
    }

    private void initMDL() {
        File videoCacheDir = new File(this.getCacheDir(), "video_cache");
        if (!videoCacheDir.exists()) {
            boolean result = videoCacheDir.mkdirs();
            if (!result) {
                Toast.makeText(this, getString(R.string.invalid_cache_path), Toast.LENGTH_SHORT)
                        .show();
            }
        }

        TTVideoEngine.setStringValue(DataLoaderHelper.DATALOADER_KEY_STRING_CACHEDIR,
                videoCacheDir.getAbsolutePath());
        TTVideoEngine.setIntValue(DataLoaderHelper.DATALOADER_KEY_INT_MAXCACHESIZE,
                300 * 1024 * 1024); // 300MB
        try {
            // start MDL
            TTVideoEngine.startDataLoader(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
