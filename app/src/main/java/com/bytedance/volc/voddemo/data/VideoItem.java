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
 * Create Date : 2021/2/28
 */
package com.bytedance.volc.voddemo.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import com.bytedance.volc.voddemo.data.remote.Response;

@Entity(tableName = "video_item")
public class VideoItem {
    public static final int VIDEO_TYPE_SMALL = 0;

    @NonNull
    @PrimaryKey
    private String itemId;

    @NonNull
    private String vid;

    private int duration;

    private String title;

    private String cover;

    @NonNull
    private String authToken;

    private int type;

    public VideoItem(@NonNull final String vid, final int duration, final String title,
            final String cover, @NonNull final String authToken, final int type) {
        this.vid = vid;
        this.duration = duration;
        this.title = title;
        this.cover = cover;
        this.authToken = authToken;
        this.type = type;
        this.itemId = String.format("%s_%d", vid, type);
    }

    @NonNull
    public String getItemId() {
        return itemId;
    }

    public void setItemId(@NonNull final String itemId) {
        this.itemId = itemId;
    }

    @NonNull
    public String getVid() {
        return vid;
    }

    public void setVid(@NonNull final String vid) {
        this.vid = vid;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(final int duration) {
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(final String cover) {
        this.cover = cover;
    }

    @NonNull
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(@NonNull final String authToken) {
        this.authToken = authToken;
    }

    public int getType() {
        return type;
    }

    public void setType(final int type) {
        this.type = type;
    }

    public static VideoItem toVideoItem(Response.VideoDetail videoDetail) {
        if (videoDetail == null) {
            return null;
        }
        return new VideoItem(videoDetail.getVid(), (int) (videoDetail.getDuration() * 1000),
                videoDetail.getCaption(), videoDetail.getCoverUrl(), videoDetail.getPlayAuthToken()
                , VIDEO_TYPE_SMALL);
    }
}


