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
 * Create Date : 2021/2/26
 */
package com.bytedance.volc.voddemo.data.remote;


import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Page;

public interface RemoteApi {

    interface Callback<T> {
        void onSuccess(T t);

        void onError(Exception e);
    }

    void getFeedStreamWithPlayAuthToken(String account, int pageIndex, int pageSize, Callback<Page<VideoItem>> callback);

    void getVideoDetailWithPlayAuthToken(String vid, Callback<VideoItem> callback);

    void cancel();
}
