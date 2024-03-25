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

package com.bytedance.volc.voddemo.data.remote.model.base;

public class BaseVideoRequest {
    protected final Integer format;
    protected final Integer codec;
    protected final Integer definition;
    protected final String fileType;
    protected final Boolean needThumbs;
    protected final Boolean needBarrageMask;
    protected final Integer cdnType;
    protected final String UnionInfo;

    public BaseVideoRequest(Integer format,
                            Integer codec,
                            Integer definition,
                            String fileType,
                            Boolean needThumbs,
                            Boolean needBarrageMask,
                            Integer cdnType,
                            String unionInfo) {
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
