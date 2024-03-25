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

import com.bytedance.playerkit.utils.L;
import com.moczul.ok2curl.CurlInterceptor;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class HttpClient {
    private static OkHttpClient sDefaultClient;
    public synchronized static OkHttpClient defaultClient() {
        if (sDefaultClient != null) {
            return sDefaultClient;
        }
        HttpLoggingInterceptor httpLog = new HttpLoggingInterceptor(s -> L.v("OKHttp", "httpLog", s));
        httpLog.setLevel(HttpLoggingInterceptor.Level.BASIC);
        CurlInterceptor curlLog = new CurlInterceptor(s -> L.log("OKHttp", "curlLog", s));
        sDefaultClient = new OkHttpClient
                .Builder()
                .addInterceptor(httpLog)
                .addInterceptor(curlLog)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        return sDefaultClient;
    }
}
