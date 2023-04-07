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
 * Create Date : 2021/2/26
 */
package com.bytedance.volc.voddemo.data.remote;


import android.os.Handler;
import android.os.Looper;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Page;

public interface RemoteApi {

    class HandlerCallback<T> implements Callback<T> {

        private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

        private final Callback<T> callback;
        private final Handler handler;

        public HandlerCallback(Callback<T> callback) {
            this(callback, MAIN_HANDLER);
        }

        public HandlerCallback(RemoteApi.Callback<T> callback, Handler handler) {
            this.callback = callback;
            this.handler = handler;
        }

        @Override
        public void onSuccess(T t) {
            handler.post(() -> {
                if (callback != null) {
                    callback.onSuccess(t);
                }
            });
        }

        @Override
        public void onError(Exception e) {
            handler.post(() -> {
                if (callback != null) {
                    callback.onError(e);
                }
            });
        }
    }

    interface Callback<T> {
        void onSuccess(T t);

        void onError(Exception e);
    }

    interface GetFeedStream {
        void getFeedStream(String account, int pageIndex, int pageSize, Callback<Page<VideoItem>> callback);

        void cancel();
    }
}
