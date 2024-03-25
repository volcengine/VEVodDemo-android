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

package com.bytedance.volc.voddemo.ui.minidrama.data.remote.api;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;

public interface GetDramaDetailApi {

    void getEpisodeVideoItems(String account, int pageIndex, int pageSize, String dramaId, Integer orderType, RemoteApi.Callback<Page<VideoItem>> callback);

    void cancel();

}
