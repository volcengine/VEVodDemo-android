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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.player.volcengine.utils;

import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.DataLoaderListener;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.utils.DataLoaderCDNLog;
import com.ss.ttvideoengine.utils.Error;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class DataLoaderListenerAdapter implements DataLoaderListener {

    @Override
    public String apiStringForFetchVideoModel(Map<String, String> param, String videoId, Resolution resolution) {
        return null;
    }

    @Override
    public String authStringForFetchVideoModel(String videoId, Resolution resolution) {
        return null;
    }

    @Override
    public void dataLoaderError(String videoId, int errorType, Error error) {

    }

    @Override
    public void onNotify(int what, long code, long parameter, String info) {

    }

    @Override
    public void onLogInfo(int what, String logType, JSONObject log) {

    }

    @Override
    public void onTaskProgress(DataLoaderHelper.DataLoaderTaskProgressInfo progressInfo) {

    }

    @Override
    public void onNotifyCDNLog(DataLoaderCDNLog log) {

    }

    @Override
    public void onNotifyCDNLog(JSONObject log) {

    }

    @Override
    public String getCheckSumInfo(String fileKey) {
        return null;
    }

    @Override
    public boolean loadLibrary(String name) {
        return false;
    }

    @Override
    public void onLogInfoToMonitor(int what, String logType, JSONObject log) {

    }

    @Override
    public void onLoadProgress(DataLoaderHelper.DataLoaderTaskLoadProgress loadProgress) {

    }

    @Override
    public HashMap<String, String> getCustomHttpHeaders(String url) {
        return null;
    }
}
