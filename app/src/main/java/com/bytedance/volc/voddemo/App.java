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
import android.app.Application;
import android.content.Context;
import android.text.TextUtils;


public class App extends Application {

    private static final String APP_ID = "";
    private static final String LICENSE_URI = "";
    private static final String APP_NAME = "VOLCVodDemo";
    private static final String APP_CHANNEL = "VOLCVodDemoAndroid";
    private static final String APP_VERSION = BuildConfig.VERSION_NAME;

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

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
                APP_VERSION,
                LICENSE_URI
        );

        /**
         * Mock 短剧/短视频广告逻辑，在正式项目中不要调用该方法
         */
        VodDemoApi.initMockADSDK(this);
    }

    public Context context() {
        return sContext;
    }
}
