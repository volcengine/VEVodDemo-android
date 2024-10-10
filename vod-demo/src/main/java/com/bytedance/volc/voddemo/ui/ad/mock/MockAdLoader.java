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
 * Create Date : 2024/10/18
 */

package com.bytedance.volc.voddemo.ui.ad.mock;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.base.BaseVideo;
import com.bytedance.volc.voddemo.ui.ad.api.Ad;
import com.bytedance.volc.voddemo.ui.ad.api.AdLoader;
import com.bytedance.volc.voddemo.ui.video.data.remote.GetFeedStream;
import com.bytedance.volc.voddemo.ui.video.data.remote.api.GetFeedStreamApi;

import java.util.ArrayList;
import java.util.List;
/**
 * Mock impl of AdLoader
 */
@Deprecated
public class MockAdLoader implements AdLoader {
    public static class Factory implements AdLoader.Factory {
        private final String mCodeId;

        public Factory(String codeId) {
            this.mCodeId = codeId;
        }

        @Override
        public AdLoader create() {
            return new MockAdLoader(mCodeId);
        }
    }

    private final GetFeedStreamApi mMockADApi;
    private final List<BaseVideo> mVideos = new ArrayList<>();
    public int mEndIndex;
    private final String mCodeId;
    private boolean mCanceled;

    public MockAdLoader(String codeId) {
        this.mMockADApi = new GetFeedStream(VideoSettings.stringValue(VideoSettings.AD_VIDEO_ACCOUNT_ID));
        this.mCodeId = codeId;
    }

    @Override
    public void load(int type, int num, Callback callback) {
        if (!mVideos.isEmpty()) {
            int start = mEndIndex;
            int end = mEndIndex = start + num;
            L.d(this, "load", "start", start, "end", end);
            final List<Ad> ads = new ArrayList<>();
            for (int i = start; i < end; i++) {
                final BaseVideo video = mVideos.get(i % mVideos.size());
                ads.add(new MockAd(video.vid, type, mCodeId, video));
            }
            callback.onSuccess(ads);
        } else {
            mMockADApi.getFeedStream(0, 100, new RemoteApi.Callback<List<BaseVideo>>() {
                @Override
                public void onSuccess(List<BaseVideo> videos) {
                    if (videos == null || videos.isEmpty()) {
                        onError(new Exception("empty"));
                        return;
                    }
                    for (BaseVideo video : videos) {
                        video.vid = "ad_" + video.vid;
                    }
                    mVideos.addAll(videos);
                    load(type, num, callback);
                }

                @Override
                public void onError(Exception e) {
                    L.d(this, "onError", e);
                    callback.onError(e);
                }
            });
        }
    }

    @Override
    public int maxLoadNum() {
        return 2;
    }

    @Override
    public boolean isCanceled() {
        return mCanceled;
    }

    @Override
    public void cancel() {
        if (mCanceled) return;
        mCanceled = true;
        mMockADApi.cancel();
    }
}
