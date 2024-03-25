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

public class BasePageableRequest extends BaseVideoRequest {
    protected final String userID;
    protected final String authorId;
    protected final Integer offset;
    protected final Integer pageSize;

    public BasePageableRequest(final String userID,
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
        super(format,
                codec,
                definition,
                fileType,
                needThumbs,
                needBarrageMask,
                cdnType,
                unionInfo);

        this.userID = userID;
        this.authorId = userID;
        this.offset = offset;
        this.pageSize = pageSize;
    }
}
