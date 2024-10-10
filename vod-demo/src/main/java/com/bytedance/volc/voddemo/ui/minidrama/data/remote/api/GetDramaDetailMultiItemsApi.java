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
 * Create Date : 2024/10/26
 */

package com.bytedance.volc.voddemo.ui.minidrama.data.remote.api;

import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;

import java.util.List;

public interface GetDramaDetailMultiItemsApi {

    void getDramaDetail(int startIndex, int pageSize, String dramaId, Integer orderType, RemoteApi.Callback<List<Item>> callback);

    void cancel();

}
