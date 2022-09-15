/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.voddemo.data.remote.api2.model;


public class BaseResponse {
    public ResponseMetaData responseMetadata;

    public static class ResponseMetaData {
        public String requestId;
        public String action;
        public String version;
        public String service;
        public String region;
        public ResponseError error;

        public static class ResponseError {
            public String code;
            public String message;

            @Override
            public String toString() {
                return "ResponseError{" +
                        "code='" + code + '\'' +
                        ", message='" + message + '\'' +
                        '}';
            }
        }
    }
}
