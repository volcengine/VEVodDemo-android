/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/10/11
 */

package com.bytedance.volc.voddemo.ui.video.data.mock;

import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.utils.ItemHelper;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.base.BaseVideo;
import com.bytedance.volc.voddemo.ui.ad.api.AdInjectStrategy;
import com.bytedance.volc.voddemo.ui.video.data.remote.GetFeedStream;
import com.bytedance.volc.voddemo.ui.video.data.remote.api.GetFeedStreamMultiItemsApi;

import java.util.List;

public class MockGetFeedStreamMultiItems implements GetFeedStreamMultiItemsApi {
    private final GetFeedStream mGetFeedStream;
    private final AdInjectStrategy mAdInjectStrategy = new AdInjectStrategy();

    public MockGetFeedStreamMultiItems(String account) {
        mGetFeedStream = new GetFeedStream(account);
    }

    @Override
    public void getFeedStream(int pageIndex, int pageSize, RemoteApi.Callback<List<Item>> callback) {
//        mAdLoadStrategy.start();
        mGetFeedStream.getFeedStream(pageIndex, pageSize, new RemoteApi.Callback<List<BaseVideo>>() {
            @Override
            public void onSuccess(List<BaseVideo> videos) {
                Asserts.checkMainThread();
                final List<VideoItem> videoItems = BaseVideo.toVideoItems(videos);
                final List<Item> items = ItemHelper.toItems(videoItems);
                if (AdInjectStrategy.isEnabled() && VideoSettings.booleanValue(VideoSettings.SHORT_VIDEO_ENABLE_AD)) {
                    mAdInjectStrategy.injectAd(pageIndex == 0, items);
                }
                callback.onSuccess(items);
            }

            @Override
            public void onError(Exception e) {
                Asserts.checkMainThread();
                callback.onError(e);
            }
        });
    }

    @Override
    public void cancel() {
        mGetFeedStream.cancel();
    }
}
