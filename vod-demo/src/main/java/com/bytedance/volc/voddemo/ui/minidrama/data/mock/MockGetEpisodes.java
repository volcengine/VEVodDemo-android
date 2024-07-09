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
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetEpisodesApi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Deprecated
public class MockGetEpisodes implements GetEpisodesApi {
    private final GetDramaDetail mGetDramaDetail = new GetDramaDetail();

    @Override
    public void getEpisodeVideosByIds(String account, String dramaId, List<Integer> episodeNumbers, RemoteApi.Callback<List<EpisodeVideo>> callback) {
        if (episodeNumbers.isEmpty()) return;
        List<Integer> sortedEpisodeNumbers = new ArrayList<>(episodeNumbers);
        Collections.sort(sortedEpisodeNumbers);
        int startIndex = Math.max(sortedEpisodeNumbers.get(0) - 1, 0);
        int endIndex = Math.max(sortedEpisodeNumbers.get(sortedEpisodeNumbers.size() - 1) - 1, 0);
        int pageSize = endIndex - startIndex + 1;
        mGetDramaDetail.getDramaDetail(account, startIndex, pageSize, dramaId, null, new RemoteApi.Callback<List<VideoItem>>() {
            @Override
            public void onSuccess(List<VideoItem> videoItems) {
                List<EpisodeVideo> episodes = new ArrayList<>();
                for (int episodeNumber : episodeNumbers) {
                    for (VideoItem item : videoItems) {
                        EpisodeVideo episode = EpisodeVideo.get(item);
                        if (EpisodeVideo.getEpisodeNumber(episode) == episodeNumber) {
                            episodes.add(episode);
                        }
                    }
                }
                MockAppServer.mockDramaDetailLockState(episodes);
                callback.onSuccess(episodes);
            }

            @Override
            public void onError(Exception e) {
                callback.onError(e);
            }
        });
    }
}
