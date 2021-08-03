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
 * Create Date : 2021/6/22
 */
package com.bytedance.volc.voddemo.preload;

import androidx.annotation.NonNull;
import com.bytedance.volc.voddemo.VodApp;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.IPreLoaderItemCallBackListener;
import com.ss.ttvideoengine.PreLoaderItemCallBackInfo;
import com.ss.ttvideoengine.PreloaderVidItem;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.model.VideoModel;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.Arrays;

// VOD key step preload : https://www.volcengine.com/docs/4/65785
public abstract class BasePreloadStrategy implements PreloadStrategy {
    public static void startPreloadByVid(@NonNull String videoId, @NonNull String playAuthToken,
            @NonNull Resolution resolution, long preloadSize) {
        TTVideoEngineLog.d(TAG, "[preload] startPreloadByVid vid = " + videoId
                                + " resolution = " + resolution
                                + " size = " + preloadSize);

        PreloaderVidItem preloadVidItem = new PreloaderVidItem(videoId, playAuthToken, resolution,
                preloadSize, VodApp.getClientSettings().videoEnableH265());

        preloadVidItem.setCallBackListener(new IPreLoaderItemCallBackListener() {
            @Override
            public void preloadItemInfo(PreLoaderItemCallBackInfo info) {
                if (info == null) {
                    return;
                }

                int key = info.getKey();
                switch (key) {
                    case PreLoaderItemCallBackInfo.KEY_IS_FETCH_END_VIDEOMODEL:
                        VideoModel videoModel = info.fetchVideoModel;
                        if (videoModel == null) {
                            return;
                        }

                        Resolution selected = select(videoModel, resolution);
                        TTVideoEngineLog.d(TAG, "[preload] preloadItemInfo videoModel fetched."
                                                + " vid = " + videoId
                                                + " resolution = " + resolution
                                                + " selected = " + selected
                                                + " all = " + Arrays.toString(
                                videoModel.getSupportResolutions()));

                        preloadVidItem.mResolution = selected;
                        break;
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_SUCCEED:
                        DataLoaderHelper.DataLoaderTaskProgressInfo cacheInfo
                                = info.preloadDataInfo;
                        if (cacheInfo != null) {
                            String cacheKey = cacheInfo.mKey;
                            String vid = cacheInfo.mVideoId;
                            String cachePath = cacheInfo.mLocalFilePath;
                            long mediaSize = cacheInfo.mMediaSize;
                            long cachedSize = cacheInfo.mCacheSizeFromZero;
                            TTVideoEngineLog.d(TAG, "[preload] preloadItemInfo result = success."
                                                    + " vid = " + vid
                                                    + " cachePath = " + cachePath
                                                    + " cacheKey = " + cacheKey
                                                    + " cachedSize = " + cachedSize
                                                    + " mediaSize = " + mediaSize);

                            break;
                        }
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_FAIL:
                        TTVideoEngineLog.d(TAG, "[preload] preloadItemInfo result = failed.");
                        break;
                    case PreLoaderItemCallBackInfo.KEY_IS_PRELOAD_END_CANCEL:
                        TTVideoEngineLog.d(TAG, "[preload] preloadItemInfo result = canceled.");
                        break;
                    default:
                        break;
                }
            }
        });

        TTVideoEngine.addTask(preloadVidItem);
    }

    public static Resolution select(VideoModel videoModel, Resolution resolution) {
        return TTVideoEngine.findDefaultResolution(videoModel, resolution);
    }
}
