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
package com.bytedance.volc.voddemo.data.remote;

import com.bytedance.volc.voddemo.data.remote.api.GeneralApi;
import com.bytedance.volc.voddemo.data.remote.api.DramaApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppServer {
    private static final String BASE_URL = "https://vevod-demo-server.volcvod.com";

    private final GeneralApi mGeneralApi;

    private final DramaApi mDramaApi;

    private AppServer() {
        mGeneralApi = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(HttpClient.defaultClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeneralApi.class);

        mDramaApi = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(HttpClient.defaultClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(DramaApi.class);
    }

    private static class Holder {
        private static final AppServer sInstance = new AppServer();
    }

    public static GeneralApi generalApi() {
        return Holder.sInstance.mGeneralApi;
    }

    public static DramaApi dramaApi() {
        return Holder.sInstance.mDramaApi;
    }


}
