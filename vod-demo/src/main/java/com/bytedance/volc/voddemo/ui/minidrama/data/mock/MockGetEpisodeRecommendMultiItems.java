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
 * Create Date : 2024/3/26
 */

package com.bytedance.volc.voddemo.ui.minidrama.data.mock;

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.ui.ad.api.AdInjectStrategy;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.GetEpisodeRecommend;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetEpisodeRecommendApi;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetEpisodeRecommendMultiItemsApi;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class MockGetEpisodeRecommendMultiItems implements GetEpisodeRecommendMultiItemsApi {
    private final GetEpisodeRecommendApi mGetEpisodeRecommend = new GetEpisodeRecommend();
    private final AdInjectStrategy mAdInjectStrategy = new AdInjectStrategy();

    @Override
    public void getRecommendEpisodeVideoItems(int pageIndex, int pageSize, RemoteApi.Callback<List<Item>> callback) {
        mGetEpisodeRecommend.getRecommendEpisodeVideoItems(pageIndex, pageSize, new RemoteApi.Callback<List<EpisodeVideo>>() {
            @Override
            public void onSuccess(List<EpisodeVideo> result) {
                List<Item> items = EpisodeVideo.toItems(result);
                if (AdInjectStrategy.isEnabled() && VideoSettings.booleanValue(VideoSettings.DRAMA_RECOMMEND_ENABLE_AD)) {
                    mAdInjectStrategy.injectAd(pageIndex == 0, items);
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
        mGetEpisodeRecommend.cancel();
    }
}
