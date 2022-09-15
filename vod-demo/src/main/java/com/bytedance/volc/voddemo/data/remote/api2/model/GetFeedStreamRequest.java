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


public class GetFeedStreamRequest {
    private final String userID;
    private final Integer offset;
    private final Integer pageSize;
    private final Integer format;
    private final Integer codec;
    private final Integer definition;
    private final String fileType;
    private final Boolean needThumbs;
    private final Boolean needBarrageMask;
    private final Integer cdnType;
    private final String UnionInfo;

    public GetFeedStreamRequest(final String userID,
                                final Integer offset,
                                final Integer pageSize,
                                final Integer format,
                                final Integer codec,
                                final Integer definition,
                                final String fileType,
                                final Boolean needThumbs,
                                final Boolean needBarrageMask,
                                final Integer cdnType,
                                final String unionInfo) {
        this.userID = userID;
        this.offset = offset;
        this.pageSize = pageSize;
        this.format = format;
        this.codec = codec;
        this.definition = definition;
        this.fileType = fileType;
        this.needThumbs = needThumbs;
        this.needBarrageMask = needBarrageMask;
        this.cdnType = cdnType;
        this.UnionInfo = unionInfo;
    }
}
