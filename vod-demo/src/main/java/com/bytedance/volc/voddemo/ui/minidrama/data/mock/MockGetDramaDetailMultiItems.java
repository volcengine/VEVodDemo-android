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
 * Create Date : 2024/7/4
 */

package com.bytedance.volc.voddemo.ui.minidrama.data.mock;

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.utils.ItemHelper;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.ui.ad.api.AdInjectStrategy;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramaDetailApi;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramaDetailMultiItemsApi;

import java.util.List;

@Deprecated
public class MockGetDramaDetailMultiItems implements GetDramaDetailMultiItemsApi {
    private final GetDramaDetailApi mGetDramaDetail = new MockGetDramaDetail();
    private final AdInjectStrategy mAdInjectStrategy = new AdInjectStrategy();

    @Override
    public void getDramaDetail(int startIndex, int pageSize, String dramaId, Integer orderType, RemoteApi.Callback<List<Item>> callback) {
        mGetDramaDetail.getDramaDetail(startIndex, pageSize, dramaId, null, new RemoteApi.Callback<List<EpisodeVideo>>() {
            @Override
            public void onSuccess(List<EpisodeVideo> result) {
                MockAppServer.mockDramaDetailLockState(result);
                List<VideoItem> videoItems = EpisodeVideo.toVideoItems(result);
                List<Item> items = ItemHelper.toItems(videoItems);
                if (AdInjectStrategy.isEnabled() && VideoSettings.booleanValue(VideoSettings.DRAMA_DETAIL_ENABLE_AD)) {
                    mAdInjectStrategy.injectAd(false, items);
                }
                callback.onSuccess(items);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }

    @Override
    public void cancel() {
        mGetDramaDetail.cancel();
    }
}
