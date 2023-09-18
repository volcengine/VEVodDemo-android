/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.voddemo;

import android.app.Activity;
import android.content.Context;

import com.bytedance.volc.voddemo.ui.main.MainActivity;

public class VodDemoApi {

    public static void initVodSDK(Context context,
                                  String appId,
                                  String appName,
                                  String appChannel,
                                  String appVersion,
                                  String licenseUri) {
        VodSDK.init(context, appId, appName, appChannel, appVersion, licenseUri);
    }

    public static void intentInto(Activity activity, boolean showActionBar) {
        MainActivity.intentInto(activity, showActionBar);
        //SampleVideoActivity.intentInto(activity);
    }
}
