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
 * Create Date : 2025/9/5
 */

package com.bytedance.volc.voddemo.mock;

import android.content.Context;

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.voddemo.mock.ad.MockAdLoader;
import com.bytedance.volc.voddemo.ui.ad.shortvideo.ShortVideoAdInsertStrategy;

@Deprecated
public class MockInit {

    @Deprecated
    public static void initMockADSDK(Context context) {
        if (VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_AD)
                || VideoSettings.booleanValue(VideoSettings.DRAMA_DETAIL_ENABLE_AD)
                || VideoSettings.booleanValue(VideoSettings.DRAMA_RECOMMEND_ENABLE_AD)) {

            // preload AD data for demo
            // Mock 短剧/短视频广告逻辑，在正式项目中不要调用该方法
            ShortVideoAdInsertStrategy.init(new MockAdLoader.Factory("ShortVideo"));
        }
    }
}
