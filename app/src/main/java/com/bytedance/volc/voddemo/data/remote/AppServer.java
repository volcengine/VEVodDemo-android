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

import com.bytedance.volc.voddemo.data.VideoItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;
import retrofit2.Callback;

public class AppServer implements AppServerApi {
    private static final String TAG = "AppServer";
    
    private static final String ACCOUNT = "small-video";

    @Override
    public void getFeedStreamWithPlayAuthToken(final int VideoType, ServerResultCallback callback) {
        Request.GetFeedStreamRequest request = new Request.GetFeedStreamRequest(
                ACCOUNT, 0, 100);

        AppServerManager.getInstance().getFeedStreamWithPlayAuthToken(request).enqueue(
                new Callback<Response.GetFeedStreamResponse>() {
                    @Override
                    public void onResponse(final Call<Response.GetFeedStreamResponse> call,
                            @NotNull
                            final retrofit2.Response<Response.GetFeedStreamResponse> response) {
                        Response.GetFeedStreamResponse feedStreamResponse = response.body();
                        if (feedStreamResponse == null) {
                            callback.onResult(null);
                            return;
                        }

                        List<VideoItem> videoItems = new ArrayList<>();
                        List<Response.VideoDetail> videoDetails = feedStreamResponse.getResult();
                        for (Response.VideoDetail videoDetail : videoDetails) {
                            videoItems.add(VideoItem.toVideoItem(videoDetail));
                        }
                        callback.onResult(videoItems);
                    }

                    @Override
                    public void onFailure(final Call<Response.GetFeedStreamResponse> call,
                            final Throwable t) {
                        callback.onResult(null);
                    }
                });
    }
}
