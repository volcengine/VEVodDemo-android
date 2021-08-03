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

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import com.bytedance.volc.voddemo.data.local.VideoItemDao;
import com.bytedance.volc.voddemo.data.local.VodDataBaseManager;
import com.bytedance.volc.voddemo.data.remote.AppServer;
import com.bytedance.volc.voddemo.data.remote.AppServerApi;
import com.bytedance.volc.voddemo.data.remote.ServerResultCallback;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoItemRepository {

    private final VideoItemDao mVideoItemDao;
    private final AppServerApi mAppServerApi;
    private final ExecutorService mExecutorService;
    private final Handler mMainHandler;

    public VideoItemRepository(final Application application) {
        mVideoItemDao = VodDataBaseManager.getInstance(application)
                .getVodDataBase()
                .videoItemDao();
        mAppServerApi = new AppServer();
        mExecutorService = Executors.newSingleThreadScheduledExecutor();
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    public void getVideoList(final int type, final int limit,
            ServerResultCallback resultCallback) {
        mAppServerApi.getFeedStreamWithPlayAuthToken(type, videoItems -> {
            if (videoItems == null) {
                mExecutorService.execute(() -> {
                    List<VideoItem> items = mVideoItemDao.getItems(type, limit);
                    mMainHandler.post(() -> resultCallback.onResult(items));
                });
                return;
            }

            mExecutorService.execute(() -> mVideoItemDao.insertItems(videoItems));
            resultCallback.onResult(videoItems);
        });
    }
}
