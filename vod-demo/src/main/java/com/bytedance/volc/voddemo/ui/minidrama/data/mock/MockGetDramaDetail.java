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

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.GetDramaDetail;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramaDetailApi;

import java.util.List;

@Deprecated
public class MockGetDramaDetail implements GetDramaDetailApi {
    private final GetDramaDetail mGetDramaDetail = new GetDramaDetail();

    @Override
    public void getDramaDetail(String account, int startIndex, int pageSize, String dramaId, Integer orderType, RemoteApi.Callback<List<VideoItem>> callback) {
        mGetDramaDetail.getDramaDetail(account, startIndex, pageSize, dramaId, null, new RemoteApi.Callback<List<VideoItem>>() {
            @Override
            public void onSuccess(List<VideoItem> videoItems) {
                List<EpisodeVideo> episodes = EpisodeVideo.videoItems2EpisodeVideos(videoItems);
                MockAppServer.mockDramaDetailLockState(episodes);
                callback.onSuccess(EpisodeVideo.toVideoItems(episodes));
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
