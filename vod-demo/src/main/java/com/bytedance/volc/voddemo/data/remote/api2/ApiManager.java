/*
 * Copyright 2021 bytedance
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
 * Create Date : 2021/2/28
 */
package com.bytedance.volc.voddemo.data.remote.api2;

import com.bytedance.volc.voddemo.data.remote.api2.model.GetFeedStreamRequest;
import com.bytedance.volc.voddemo.data.remote.api2.model.GetFeedStreamResponse;
import com.bytedance.volc.voddemo.data.remote.api2.model.GetVideoDetailRequest;
import com.bytedance.volc.voddemo.data.remote.api2.model.GetVideoDetailResponse;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class ApiManager {
    private static final String BASE_URL = "https://vevod-demo-server.volcvod.com";
    private final Api2 api2;

    private ApiManager() {
        api2 = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api2.class);
    }

    private static class Holder {
        private static final ApiManager sInstance = new ApiManager();
    }

    public static Api2 api2() {
        return Holder.sInstance.api2;
    }

    public interface Api2 {
        @POST("/api/general/v1/getFeedStream")
        Call<GetFeedStreamResponse> getFeedStream(@Body GetFeedStreamRequest request);

        @POST("/api/general/v1/getFeedStreamWithPlayAuthToken")
        Call<GetFeedStreamResponse> getFeedStreamWithPlayAuthToken(@Body GetFeedStreamRequest request);

        @POST("/api/general/v1/getVideoDetailWithLocal")
        Call<GetVideoDetailResponse> getVideoDetailWithLocal(@Body GetVideoDetailRequest request);

        @POST("/api/general/v1/getVideoDetailWithPlayAuthToken")
        Call<GetVideoDetailResponse> getVideoDetailWithPlayAuthToken(@Body GetVideoDetailRequest request);
    }
}
