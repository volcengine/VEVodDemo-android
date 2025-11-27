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

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.bytedance.volc.voddemo.mock.MockInit;


public class App extends Application {

    private static final String APP_ID = "";
    private static final String LICENSE_URI = "";
    private static final String APP_NAME = "VOLCVodDemo";
    private static final String APP_CHANNEL = "VOLCVodDemoAndroid";

    @Override
    public void onCreate() {
        super.onCreate();

        if (TextUtils.isEmpty(APP_ID) || TextUtils.isEmpty(LICENSE_URI)) {
            throw new IllegalArgumentException("请联系火山引擎商务获取体验 Demo AppId 与 License. 联系方式：https://www.volcengine.com/product/vod");
        }

        VodDemoApi.initVodSDK(this,
                APP_ID,
                APP_NAME,
                APP_CHANNEL,
                getAppVersionName(this),
                LICENSE_URI
        );

        /**
         * Mock 短剧/短视频广告逻辑，在正式项目中不要调用该方法
         */
        MockInit.initMockADSDK(this);
    }

    private static String getAppVersionName(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("App", "get versionName failed", e);
            return "";
        }
    }
}
