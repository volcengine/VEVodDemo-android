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


public class GetVideoDetailRequest {
    private final String vid;
    private final Integer format;
    private final Integer codec;
    private final Integer definition;
    private final String fileType;
    private final Boolean needOriginal;
    private final Boolean needBarrageMask;
    private final Integer cdnType;

    public GetVideoDetailRequest(
            final String vid,
            final Integer format,
            final Integer codec,
            final Integer definition,
            final String fileType,
            final Boolean needOriginal,
            final Boolean needBarrageMask,
            final Integer cdnType) {
        this.vid = vid;
        this.format = format;
        this.codec = codec;
        this.definition = definition;
        this.fileType = fileType;
        this.needOriginal = needOriginal;
        this.needBarrageMask = needBarrageMask;
        this.cdnType = cdnType;
    }
}
