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

import static com.bytedance.volc.voddemo.data.remote.AppServer.dramaApi;

import androidx.annotation.NonNull;

import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.Params;
import com.bytedance.volc.voddemo.data.remote.model.base.BaseVideo;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramaEpisodeRequest;
import com.bytedance.volc.voddemo.data.remote.model.drama.GetDramaEpisodeResponse;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramaDetailApi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

public class GetDramaDetail implements GetDramaDetailApi {
    private final List<Call<?>> mCalls = Collections.synchronizedList(new ArrayList<>());

    @Override
    public void getEpisodeVideoItems(String account, int pageIndex, int pageSize, String dramaId, Integer orderType, RemoteApi.Callback<Page<VideoItem>> callback) {
        final RemoteApi.HandlerCallback<Page<VideoItem>> mainCallback = new RemoteApi.HandlerCallback<>(callback);

        final GetDramaEpisodeRequest request = new GetDramaEpisodeRequest(
                account,
                pageIndex * pageSize,
                pageSize,
                dramaId,
                orderType,
                Params.Value.format(),
                Params.Value.codec(),
                Params.Value.definition(),
                Params.Value.fileType(),
                Params.Value.needThumbs(),
                Params.Value.enableBarrageMask(),
                Params.Value.cdnType(),
                Params.Value.unionInfo()
        );

        Call<GetDramaEpisodeResponse> call = createCall(request);
        call.enqueue(new retrofit2.Callback<GetDramaEpisodeResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetDramaEpisodeResponse> call, @NonNull Response<GetDramaEpisodeResponse> response) {
                mCalls.remove(call);
                if (response.isSuccessful()) {
                    GetDramaEpisodeResponse result = response.body();
                    if (result == null) {
                        mainCallback.onError(new IOException("result = null + " + response));
                        return;
                    }
                    if (result.responseMetadata != null && result.responseMetadata.error != null) {
                        mainCallback.onError(new IOException(response + "; " + result.responseMetadata.error));
                        return;
                    }
                    List<EpisodeVideo> details = result.result;
                    List<VideoItem> items = new ArrayList<>();
                    if (details != null) {
                        for (BaseVideo detail : details) {
                            VideoItem item = BaseVideo.toVideoItem(detail);
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
            public void onFailure(@NonNull Call<GetDramaEpisodeResponse> call, @NonNull Throwable t) {
                mCalls.remove(call);
                mainCallback.onError(new IOException(t));
            }
        });
        mCalls.add(call);
    }

    protected Call<GetDramaEpisodeResponse> createCall(GetDramaEpisodeRequest request) {
        final int sourceType = VideoSettings.intValue(VideoSettings.COMMON_SOURCE_TYPE);
        switch (sourceType) {
            case VideoSettings.SourceType.SOURCE_TYPE_VID:
                return dramaApi().getDramaEpisodeWithPlayAuthToken(request);
            case VideoSettings.SourceType.SOURCE_TYPE_URL:
            case VideoSettings.SourceType.SOURCE_TYPE_MODEL:
                return dramaApi().getDramaEpisodeWithVideoModel(request);
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
