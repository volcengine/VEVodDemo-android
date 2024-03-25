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
 * Create Date : 2024/1/8
 */

package com.bytedance.volc.voddemo.video;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.volcengine.VolcSourceRefreshStrategy.VolcUrlRefreshFetcher;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.voddemo.data.remote.AppServer;
import com.bytedance.volc.voddemo.data.remote.model.general.GetRefreshUrlRequest;
import com.bytedance.volc.voddemo.data.remote.model.general.GetRefreshUrlResponse;

import retrofit2.Call;
import retrofit2.Response;

public class AppUrlRefreshFetcher implements VolcUrlRefreshFetcher {
    private static final int ERROR_CODE_HTTP_ERROR = -1;
    private static final int ERROR_CODE_RESULT_NULL = -2;

    public static class Factory implements VolcUrlRefreshFetcher.Factory {

        @Override
        public AppUrlRefreshFetcher create() {
            return new AppUrlRefreshFetcher();
        }
    }

    private Call<GetRefreshUrlResponse> mCall;

    @Override
    public void fetch(VolcUrlRequest request, Callback callback) {
        L.v(this, "fetch",
                request.mediaId,
                request.cacheKey,
                request.url);

        final Call<GetRefreshUrlResponse> call = AppServer.generalApi().getRefreshUrl(new GetRefreshUrlRequest(request.url));
        call.enqueue(new retrofit2.Callback<GetRefreshUrlResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetRefreshUrlResponse> call,
                                   @NonNull Response<GetRefreshUrlResponse> response) {

                if (response.isSuccessful()) {
                    final GetRefreshUrlResponse ret = response.body();
                    if (ret == null || ret.result == null) {
                        notifyError(callback,
                                request,
                                ERROR_CODE_RESULT_NULL,
                                "result is empty!");
                    } else {
                        notifySuccess(callback,
                                request,
                                new VolcUrlResult(ret.result.url, ret.result.expireInS * 1000L));
                    }
                } else {
                    notifyError(callback,
                            request,
                            ERROR_CODE_HTTP_ERROR,
                            "httpCode:" + response.code() + " " + response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetRefreshUrlResponse> call, @NonNull Throwable t) {
                notifyError(callback,
                        request,
                        ERROR_CODE_HTTP_ERROR,
                        String.valueOf(t));
            }
        });
        mCall = call;
    }

    @Override
    public void cancel() {
        if (mCall != null) {
            mCall.cancel();
        }
    }

    private void notifySuccess(Callback callback,
                               VolcUrlRequest request,
                               VolcUrlResult urlInfo) {
        L.v(this, "notifySuccess",
                request.mediaId,
                request.cacheKey,
                request.url,
                urlInfo.url,
                urlInfo.expireTimeInMS);

        callback.onSuccess(urlInfo);
    }

    private void notifyError(Callback callback,
                             VolcUrlRequest request,
                             int errorCode,
                             String errorMsg) {
        L.v(this, "notifyError",
                request.mediaId,
                request.cacheKey,
                request.url,
                errorCode,
                errorMsg);

        callback.onError(errorCode, errorMsg);
    }
}
