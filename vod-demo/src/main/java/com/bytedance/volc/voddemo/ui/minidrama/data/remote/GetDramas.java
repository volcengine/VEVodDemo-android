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

package com.bytedance.volc.voddemo.ui.minidrama.data.remote;


import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.voddemo.data.remote.AppServer;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramasRequest;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramasResponse;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramasApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GetDramas implements GetDramasApi {
    private final List<Call<?>> mCalls = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void getDramas(String account, int pageIndex, int pageSize, RemoteApi.Callback<Page<DramaInfo>> callback) {
        final RemoteApi.HandlerCallback<Page<DramaInfo>> mainCallback = new RemoteApi.HandlerCallback<>(callback);

        GetDramasRequest request = new GetDramasRequest(account, pageIndex * pageSize, pageSize);
        Call<GetDramasResponse> call = AppServer.dramaApi().getDramas(request);
        call.enqueue(new Callback<GetDramasResponse>() {
            @Override
            public void onResponse(Call<GetDramasResponse> call, Response<GetDramasResponse> response) {
                mCalls.remove(call);
                if (response.isSuccessful()) {
                    GetDramasResponse result = response.body();
                    if (result == null) {
                        mainCallback.onError(new IOException("result = null + " + response));
                        return;
                    }
                    if (result.responseMetadata != null && result.responseMetadata.error != null) {
                        mainCallback.onError(new IOException(response + "; " + result.responseMetadata.error));
                        return;
                    }
                    List<DramaInfo> dramas = result.result;
                    if (dramas == null) {
                        dramas = new ArrayList<>();
                    }
                    mainCallback.onSuccess(new Page<>(dramas, pageIndex, Page.TOTAL_INFINITY));
                } else {
                    mainCallback.onError(new IOException(response.toString()));
                }
            }

            @Override
            public void onFailure(Call<GetDramasResponse> call, Throwable t) {
                mCalls.remove(call);
                mainCallback.onError(new IOException(t));
            }
        });
        mCalls.add(call);
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
