/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/6/29
 */

package com.bytedance.volc.voddemo.ui.sample.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.volcengine.Mapper;
import com.bytedance.playerkit.utils.MD5;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class SampleSourceParser {

    public static ArrayList<VideoItem> parse(String input) {
        try {
            JSONArray array = new JSONArray(input);
            return createVideoItems(array);
        } catch (JSONException e) {
            return createVideoItems(input.split("\n"));
        }
    }

    private static ArrayList<VideoItem> createVideoItems(JSONArray jsonArray) {
        final ArrayList<VideoItem> videoItems = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.optJSONObject(i);
            VideoItem videoItem = createVideoItem(object);
            videoItems.add(videoItem);
        }
        return videoItems;
    }

    @NonNull
    private static VideoItem createVideoItem(JSONObject object) {
        int type = object.optInt("type", 1);
        String vid = object.optString("vid");
        String title = object.optString("title");
        String cover = object.optString("cover");
        long duration = object.optLong("duration");
        VideoItem videoItem;
        switch (type) {
            case VideoItem.SOURCE_TYPE_VID: {
                String playAuthToken = object.optString("playAuthToken");
                String subtitleAuthToken = object.optString("subtitleAuthToken");
                videoItem = VideoItem.createVidItem(vid, playAuthToken, subtitleAuthToken, duration, cover, title);
                break;
            }
            case VideoItem.SOURCE_TYPE_URL: {
                String httpUrl = object.optString("httpUrl");
                JSONObject subtitleModel = object.optJSONObject("subtitleModel");
                videoItem = VideoItem.createUrlItem(vid, httpUrl, null, Mapper.subtitleModel2Subtitles(subtitleModel), duration, cover, title);
                break;
            }
            case VideoItem.SOURCE_TYPE_MODEL: {
                String videoModel = object.optString("videoModel");
                String subtitleAuthToken = object.optString("subtitleAuthToken");
                videoItem = VideoItem.createVideoModelItem(vid, videoModel, subtitleAuthToken, duration, cover, title);
                break;
            }
            default:
                throw new IllegalArgumentException("supported type " + type);
        }
        return videoItem;
    }


    private static ArrayList<VideoItem> createVideoItems(String[] urls) {
        final ArrayList<VideoItem> videoItems = new ArrayList<>();
        int index = 0;
        for (String url : urls) {
            if (TextUtils.isEmpty(url)) continue;
            if (url.startsWith("/")) {
                File file = new File(url);
                if (!file.exists()) continue;
            }
            if (!url.startsWith("http") && !url.startsWith("file")) continue;

            VideoItem videoItem = VideoItem.createUrlItem(MD5.getMD5(url), url, null, null, 0, null, index + ": " + url);
            videoItems.add(videoItem);
            index++;
        }
        return videoItems;
    }
}
