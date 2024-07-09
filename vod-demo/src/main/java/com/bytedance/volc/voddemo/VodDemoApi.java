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

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.voddemo.ui.ad.api.AdInjectStrategy;
import com.bytedance.volc.voddemo.ui.ad.api.AdLoader;
import com.bytedance.volc.voddemo.ui.ad.mock.MockAdLoader;
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

    /**
     * @deprecated mock AD SDK
     */
    @Deprecated
    public static void initMockADSDK(Context context) {
        if (VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_AD)
                || VideoSettings.booleanValue(VideoSettings.DRAMA_DETAIL_ENABLE_AD)
                || VideoSettings.booleanValue(VideoSettings.DRAMA_RECOMMEND_ENABLE_AD)) {

            // preload AD data for demo
            // Mock 短剧/短视频广告逻辑，在正式项目中不要调用该方法
            AdLoader.Factory mockAdLoaderFactory = new MockAdLoader.Factory("ShortVideo");

            AdInjectStrategy.init(mockAdLoaderFactory);
        }
    }

    public static void intentInto(Activity activity, boolean showActionBar) {
        MainActivity.intentInto(activity, showActionBar);
        //SampleVideoActivity.intentInto(activity);
    }
}
