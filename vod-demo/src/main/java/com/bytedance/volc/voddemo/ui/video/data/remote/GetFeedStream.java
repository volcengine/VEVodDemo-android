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
package com.bytedance.volc.voddemo.ui.video.data.remote;


import static com.bytedance.volc.voddemo.data.remote.AppServer.generalApi;

import androidx.annotation.NonNull;

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.data.remote.RemoteApi.Callback;
import com.bytedance.volc.voddemo.data.remote.RemoteApi.HandlerCallback;
import com.bytedance.volc.voddemo.data.remote.model.Params;
import com.bytedance.volc.voddemo.data.remote.model.base.BaseVideo;
import com.bytedance.volc.voddemo.data.remote.model.general.GetFeedStreamRequest;
import com.bytedance.volc.voddemo.data.remote.model.general.GetFeedStreamResponse;
import com.bytedance.volc.voddemo.ui.video.data.remote.api.GetFeedStreamApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GetFeedStream implements GetFeedStreamApi {

    private final List<Call<?>> mCalls = Collections.synchronizedList(new ArrayList<>());
    private final String mAccount;
    public GetFeedStream(String account) {
        mAccount = account;
    }

    @Override
    public void getFeedStream(int pageIndex, int pageSize, Callback<List<Item>> callback) {
        final HandlerCallback<List<Item>> mainCallback = new HandlerCallback<>(callback);
        final GetFeedStreamRequest request = new GetFeedStreamRequest(
                mAccount,
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
        Call<GetFeedStreamResponse> call = createGetFeedStreamCall(request);
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
                    List<Item> items = BaseVideo.toItems(result.result);
                    mainCallback.onSuccess(items);
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

    protected Call<GetFeedStreamResponse> createGetFeedStreamCall(GetFeedStreamRequest request) {
        final int sourceType = VideoSettings.intValue(VideoSettings.COMMON_SOURCE_TYPE);
        switch (sourceType) {
            case VideoSettings.SourceType.SOURCE_TYPE_VID:
                return generalApi().getFeedStreamWithPlayAuthToken(request);
            case VideoSettings.SourceType.SOURCE_TYPE_URL:
            case VideoSettings.SourceType.SOURCE_TYPE_MODEL:
                return generalApi().getFeedVideoStreamWithVideoModel(request);
            default:
                throw new NullPointerException();
        }
    }

    @Override
    public void cancel() {
        synchronized (mCalls) {
            for (Call<?> call : mCalls) {
                call.cancel();
            }
            mCalls.clear();
        }
    }
}
