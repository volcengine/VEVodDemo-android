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


import static com.bytedance.volc.voddemo.data.remote.api2.ApiManager.api2;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.api2.model.GetFeedStreamRequest;
import com.bytedance.volc.voddemo.data.remote.api2.model.GetFeedStreamResponse;
import com.bytedance.volc.voddemo.data.remote.api2.model.GetVideoDetailRequest;
import com.bytedance.volc.voddemo.data.remote.api2.model.GetVideoDetailResponse;
import com.bytedance.volc.voddemo.data.remote.api2.model.VideoDetail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class RemoteApi2 implements RemoteApi {

    private final List<Call<?>> mCalls = Collections.synchronizedList(new ArrayList<>());
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public void getFeedStreamWithPlayAuthToken(String account, int pageIndex, int pageSize, Callback<Page<VideoItem>> callback) {
        final Main<Page<VideoItem>> mainCallback = new Main<>(callback, mHandler);
        final GetFeedStreamRequest request = new GetFeedStreamRequest(
                account,
                pageIndex * pageSize,
                pageSize,
                Params.Value.format(),
                Params.Value.codec(),
                Params.Value.definition(),
                Params.Value.fileType(),
                Params.Value.needThumbs(),
                Params.Value.enableBarrageMask(),
                Params.Value.cdnType(),
                Params.Value.unionInfo()
        );
        Call<GetFeedStreamResponse> call = api2().getFeedStreamWithPlayAuthToken(request);
        call.enqueue(new retrofit2.Callback<GetFeedStreamResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetFeedStreamResponse> call, @NonNull Response<GetFeedStreamResponse> response) {
                mCalls.remove(call);
                if (response.isSuccessful()) {
                    GetFeedStreamResponse result = response.body();
                    if (result == null) {
                        mainCallback.onError(new IOException("result = null + " + response));
                        return;
                    }
                    if (result.responseMetadata != null && result.responseMetadata.error != null) {
                        mainCallback.onError(new IOException(response + "; " + result.responseMetadata.error));
                        return;
                    }
                    List<VideoDetail> details = result.result;
                    List<VideoItem> items = new ArrayList<>();
                    if (details != null) {
                        for (VideoDetail detail : details) {
                            VideoItem item = VideoDetail.toVideoItem(detail);
                            if (item != null) {
                                items.add(item);
                            }
                        }
                    }
                    mainCallback.onSuccess(new Page<>(items, pageIndex, Page.TOTAL_INFINITY));
                } else {
                    mainCallback.onError(new IOException(response.toString()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetFeedStreamResponse> call, @NonNull Throwable t) {
                mCalls.remove(call);
                mainCallback.onError(new IOException(t));
            }
        });
        mCalls.add(call);
    }

    @Override
    public void getVideoDetailWithPlayAuthToken(String vid, Callback<VideoItem> callback) {
        final Main<VideoItem> mainCallback = new Main<>(callback, mHandler);
        final GetVideoDetailRequest request = new GetVideoDetailRequest(
                vid,
                Params.Value.format(),
                Params.Value.codec(),
                Params.Value.definition(),
                Params.Value.fileType(),
                true,
                Params.Value.enableBarrageMask(),
                Params.Value.cdnType());
        Call<GetVideoDetailResponse> call = api2().getVideoDetailWithPlayAuthToken(request);
        call.enqueue(new retrofit2.Callback<GetVideoDetailResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetVideoDetailResponse> call, @NonNull Response<GetVideoDetailResponse> response) {
                mCalls.remove(call);
                if (response.isSuccessful()) {
                    GetVideoDetailResponse result = response.body();

                    if (result == null) {
                        mainCallback.onError(new IOException("result = null + " + response));
                        return;
                    }
                    if (result.responseMetadata != null && result.responseMetadata.error != null) {
                        mainCallback.onError(new IOException(response + "; " + result.responseMetadata.error));
                        return;
                    }
                    VideoDetail detail = result.result;
                    if (detail == null) {
                        mainCallback.onError(new IOException("details = null + " + response));
                        return;
                    }
                    VideoItem item = VideoDetail.toVideoItem(detail);
                    mainCallback.onSuccess(item);
                } else {
                    mainCallback.onError(new IOException(response.toString()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetVideoDetailResponse> call, @NonNull Throwable t) {
                mCalls.remove(call);
                mainCallback.onError(new IOException(t));
            }
        });
        mCalls.add(call);
    }

    @Override
    public void cancel() {
        for (Call<?> call : mCalls) {
            call.cancel();
        }
        mCalls.clear();
    }

    static class Main<T> implements Callback<T> {

        private final Callback<T> callback;
        private final Handler handler;

        Main(Callback<T> callback, Handler handler) {
            this.callback = callback;
            this.handler = handler;
        }

        @Override
        public void onSuccess(T t) {
            handler.post(() -> {
                if (callback != null) {
                    callback.onSuccess(t);
                }
            });
        }

        @Override
        public void onError(Exception e) {
            handler.post(() -> {
                if (callback != null) {
                    callback.onError(e);
                }
            });
        }
    }
}
