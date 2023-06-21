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
 * Create Date : 2023/6/25
 */

package com.bytedance.playerkit.player.volcengine.utils;

import com.ss.ttvideoengine.SubInfoCallBack;
import com.ss.ttvideoengine.SubInfoSimpleCallBack;
import com.ss.ttvideoengine.utils.Error;

public class TTVideoEngineSubtitleCallbackAdapter extends SubInfoSimpleCallBack {
    private final SubInfoCallBack mCallback;

    public TTVideoEngineSubtitleCallbackAdapter(SubInfoCallBack callBack) {
        mCallback = callBack;
    }

    @Override
    public void onSubPathInfo(String subPathInfo, Error error) {
        mCallback.onSubPathInfo(subPathInfo, error);
    }

    @Override
    public void onSubInfoCallback(int code, String info) {
        mCallback.onSubInfoCallback(code, info);
    }

    @Override
    public void onSubSwitchCompleted(int success, int subId) {
        mCallback.onSubSwitchCompleted(success, subId);
    }

    @Override
    public void onSubLoadFinished(int success) {
        mCallback.onSubLoadFinished(success);
    }

    @Override
    public void onSubLoadFinished2(int success, String info) {
        mCallback.onSubLoadFinished2(success, info);
    }
}
