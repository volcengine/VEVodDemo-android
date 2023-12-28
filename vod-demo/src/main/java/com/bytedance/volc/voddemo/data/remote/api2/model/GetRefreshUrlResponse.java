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
 * Create Date : 2024/1/13
 */

package com.bytedance.volc.voddemo.data.remote.api2.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GetRefreshUrlResponse extends BaseResponse {
    public Result result;
    public static class Result {
        public String url;
        @SerializedName("expire")
        public long expireInS;
    }
}
