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
 * Create Date : 2021/6/10
 */
package com.bytedance.volc.voddemo.data.remote;

import com.bytedance.volc.voddemo.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class AppServerManager {

    private static final String BASE_URL = "http://vod-app-server.snssdk.com";
    private final AppService mAppService;

    private static class StaticHolder {
        private static final AppServerManager instance = new AppServerManager();
    }

    public static AppServerManager getInstance() {
        return AppServerManager.StaticHolder.instance;
    }

    private AppServerManager() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BASIC
                : HttpLoggingInterceptor.Level.NONE);

        mAppService = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(new OkHttpClient.Builder().addInterceptor(logging).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(AppService.class);
    }

    public Call<Response.GetFeedStreamResponse> getFeedStreamWithPlayAuthToken(
            @Body Request.GetFeedStreamRequest request) {
        if (mAppService != null) {
            return mAppService.getFeedStreamWithPlayAuthToken(request);
        }

        return null;
    }

    public interface AppService {

        @POST("/api/general/v1/getFeedStreamWithPlayAuthToken")
        Call<Response.GetFeedStreamResponse> getFeedStreamWithPlayAuthToken(
                @Body Request.GetFeedStreamRequest request);
    }
}
