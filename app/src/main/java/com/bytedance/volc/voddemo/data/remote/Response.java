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

import java.util.List;

public class Response {

    public static class GetFeedStreamResponse {
        private ResponseMetaData responseMetadata;
        private List<VideoDetail> result;

        public ResponseMetaData getResponseMetadata() {
            return responseMetadata;
        }

        public void setResponseMetadata(
                final ResponseMetaData responseMetadata) {
            this.responseMetadata = responseMetadata;
        }

        public List<VideoDetail> getResult() {
            return result;
        }

        public void setResult(final List<VideoDetail> result) {
            this.result = result;
        }
    }

    public static class ResponseMetaData {
        private String requestId;
        private String action;
        private String version;
        private String service;
        private String region;
        private ResponseError error;

        public String getRequestId() {
            return requestId;
        }

        public void setRequestId(final String requestId) {
            this.requestId = requestId;
        }

        public String getAction() {
            return action;
        }

        public void setAction(final String action) {
            this.action = action;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(final String version) {
            this.version = version;
        }

        public String getService() {
            return service;
        }

        public void setService(final String service) {
            this.service = service;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(final String region) {
            this.region = region;
        }

        public ResponseError getError() {
            return error;
        }

        public void setError(final ResponseError error) {
            this.error = error;
        }
    }

    public static class ResponseError {
        private String code;
        private String message;

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(final String message) {
            this.message = message;
        }
    }

    public static class VideoDetail {
        private String vid;
        private String caption;
        private double duration;
        private String coverUrl;
        private String videoModel;
        private String playAuthToken;

        public String getVid() {
            return vid;
        }

        public void setVid(final String vid) {
            this.vid = vid;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(final String caption) {
            this.caption = caption;
        }

        public double getDuration() {
            return duration;
        }

        public void setDuration(final double duration) {
            this.duration = duration;
        }

        public String getCoverUrl() {
            return coverUrl;
        }

        public void setCoverUrl(final String coverUrl) {
            this.coverUrl = coverUrl;
        }

        public String getVideoModel() {
            return videoModel;
        }

        public void setVideoModel(final String videoModel) {
            this.videoModel = videoModel;
        }

        public String getPlayAuthToken() {
            return playAuthToken;
        }

        public void setPlayAuthToken(final String playAuthToken) {
            this.playAuthToken = playAuthToken;
        }
    }
}
