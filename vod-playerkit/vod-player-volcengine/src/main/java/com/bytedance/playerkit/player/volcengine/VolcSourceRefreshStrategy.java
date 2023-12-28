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
 * Create Date : 2024/1/5
 */

package com.bytedance.playerkit.player.volcengine;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.volcengine.VolcSourceRefreshStrategy.VolcUrlRefreshFetcher.VolcUrlRequest;
import com.bytedance.playerkit.player.volcengine.VolcSourceRefreshStrategy.VolcUrlRefreshFetcher.VolcUrlResult;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.strategy.refresh.TTVideoEngineSourceRefreshStrategy;
import com.ss.ttvideoengine.strategy.refresh.TTVideoEngineUrlFetcher;

public class VolcSourceRefreshStrategy {

    public interface VolcUrlRefreshFetcher {

        interface Factory {
            VolcUrlRefreshFetcher create();
        }

        class VolcUrlRequest {
            public final String mediaId;
            public final String cacheKey;
            public final String url;

            public VolcUrlRequest(String mediaId, String cacheKey, String url) {
                this.mediaId = mediaId;
                this.cacheKey = cacheKey;
                this.url = url;
            }
        }

        class VolcUrlResult {
            public final String url;
            public final long expireTimeInMS;

            public VolcUrlResult(String url, long expireTimeInMS) {
                this.url = url;
                this.expireTimeInMS = expireTimeInMS;
            }
        }

        void fetch(VolcUrlRequest request, Callback callback);

        void cancel();

        interface Callback {
            void onSuccess(VolcUrlResult urlInfo);

            void onError(int errorCode, String errorMsg);
        }
    }

    public static void init(VolcUrlRefreshFetcher.Factory factory) {
        if (factory == null) return;
        TTVideoEngineSourceRefreshStrategy.setUrlFetcherFactory(new TTVideoEngineUrlFetcher.Factory() {
            @Override
            public TTVideoEngineUrlFetcher create(@NonNull TTVideoEngine engine) {

                final VolcUrlRefreshFetcher fetcher = factory.create();
                if (fetcher == null) return null;

                return new TTVideoEngineUrlFetcher() {

                    @Override
                    public void fetch(@NonNull UrlRequest urlRequest, @NonNull Callback<UrlResult> callback) {

                        final VolcUrlRequest volcUrlRequest = new VolcUrlRequest(urlRequest.vid,
                                urlRequest.cacheKey,
                                urlRequest.url);

                        fetcher.fetch(volcUrlRequest, new VolcUrlRefreshFetcher.Callback() {
                            @Override
                            public void onSuccess(VolcUrlResult urlInfo) {
                                callback.onSuccess(new UrlResult(urlInfo.url, urlInfo.expireTimeInMS));
                            }

                            @Override
                            public void onError(int errorCode, String errorMsg) {
                                callback.onError(errorCode, errorMsg);
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                        fetcher.cancel();
                    }
                };
            }
        });
    }
}
