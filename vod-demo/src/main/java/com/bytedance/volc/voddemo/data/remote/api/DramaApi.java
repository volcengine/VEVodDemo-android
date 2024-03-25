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

package com.bytedance.volc.voddemo.data.remote.api;

import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramaEpisodeRequest;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramaEpisodeResponse;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramasRequest;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramasResponse;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetEpisodeFeedStreamRequest;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetEpisodeFeedStreamResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DramaApi {
    @POST("/api/drama/episode/v1/getEpisodeFeedStreamWithPlayAuthToken")
    Call<GetEpisodeFeedStreamResponse> getEpisodeFeedStreamWithPlayAuthToken(@Body GetEpisodeFeedStreamRequest request);

    @POST("/api/drama/episode/v1/getEpisodeFeedStreamWithVideoModel")
    Call<GetEpisodeFeedStreamResponse> GetEpisodeFeedStreamWithVideoModel(@Body GetEpisodeFeedStreamRequest request);

    @POST("/api/drama/episode/v1/getDramaEpisodeWithPlayAuthToken")
    Call<GetDramaEpisodeResponse> getDramaEpisodeWithPlayAuthToken(@Body GetDramaEpisodeRequest request);

    @POST("/api/drama/episode/v1/getDramaEpisodeWithVideoModel")
    Call<GetDramaEpisodeResponse> getDramaEpisodeWithVideoModel(@Body GetDramaEpisodeRequest request);

    @POST("/api/drama/v1/listDrama")
    Call<GetDramasResponse> getDramas(@Body GetDramasRequest request);
}
