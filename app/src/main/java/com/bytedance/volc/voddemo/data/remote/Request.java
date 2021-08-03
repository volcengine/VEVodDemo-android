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

public class Request {

    public static class GetFeedStreamRequest {
        private String userID;
        private Integer offset;
        private Integer pageSize;
        private Integer format;
        private Integer codec;
        private Integer definition;
        private String fileType;
        private boolean needThumbs;
        private boolean needBarrageMask;
        private Integer cdnType;

        public GetFeedStreamRequest(final String userID, final Integer offset,
                final Integer pageSize) {
            this.userID = userID;
            this.offset = offset;
            this.pageSize = pageSize;
        }

        public GetFeedStreamRequest(final String userID,
                final Integer offset,
                final Integer pageSize,
                final Integer format,
                final Integer codec,
                final Integer definition,
                final String fileType,
                final boolean needThumbs,
                final boolean needBarrageMask,
                final Integer cdnType) {
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
        }

        public String getUserID() {
            return userID;
        }

        public void setUserID(final String userID) {
            this.userID = userID;
        }

        public int getOffset() {
            return offset;
        }

        public void setOffset(final int offset) {
            this.offset = offset;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(final int pageSize) {
            this.pageSize = pageSize;
        }

        public int getFormat() {
            return format;
        }

        public void setFormat(final int format) {
            this.format = format;
        }

        public int getCodec() {
            return codec;
        }

        public void setCodec(final int codec) {
            this.codec = codec;
        }

        public int getDefinition() {
            return definition;
        }

        public void setDefinition(final int definition) {
            this.definition = definition;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(final String fileType) {
            this.fileType = fileType;
        }

        public boolean isNeedThumbs() {
            return needThumbs;
        }

        public void setNeedThumbs(final boolean needThumbs) {
            this.needThumbs = needThumbs;
        }

        public Integer getCdnType() {
            return cdnType;
        }

        public void setCdnType(Integer cdnType) {
            this.cdnType = cdnType;
        }
    }
}
