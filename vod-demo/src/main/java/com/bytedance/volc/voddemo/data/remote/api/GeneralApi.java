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

import com.bytedance.volc.voddemo.data.remote.model.general.GetFeedStreamRequest;
import com.bytedance.volc.voddemo.data.remote.model.general.GetFeedStreamResponse;
import com.bytedance.volc.voddemo.data.remote.model.general.GetRefreshUrlRequest;
import com.bytedance.volc.voddemo.data.remote.model.general.GetRefreshUrlResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface GeneralApi {
    @POST("/api/general/v1/getFeedStreamWithPlayAuthToken")
    Call<GetFeedStreamResponse> getFeedStreamWithPlayAuthToken(@Body GetFeedStreamRequest request);

    @POST("/api/general/v1/getFeedStreamWithVideoModel")
    Call<GetFeedStreamResponse> getFeedVideoStreamWithVideoModel(@Body GetFeedStreamRequest request);

    @POST("/api/cdn/v1/refreshUrl")
    Call<GetRefreshUrlResponse> getRefreshUrl(@Body GetRefreshUrlRequest request);
}
