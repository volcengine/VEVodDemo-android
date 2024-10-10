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

package com.bytedance.volc.voddemo.ui.ad.api;

import android.os.Handler;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;

import java.util.ArrayList;
import java.util.List;

public final class AdLoadStrategy {
    private final int mPrefetchAdMaxCount;
    private final List<Ad> mAds = new ArrayList<>();
    private final Handler mHandler = new Handler();
    public final Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            schedule();
        }
    };
    private final AdLoader mAdLoader;
    private boolean mStarted;
    private boolean mLoading;

    public AdLoadStrategy(int maxCount,
                          AdLoader.Factory factory) {
        this.mPrefetchAdMaxCount = maxCount;
        this.mAdLoader = factory.create();
    }

    @Nullable
    public synchronized Ad remove() {
        Ad ad = null;
        if (!mAds.isEmpty()) {
            ad = mAds.remove(0);
        }
        L.d(this, "remove", ad);
        postSchedule();
        return ad;
    }

    public synchronized void start() {
        if (mStarted) return;
        mStarted = true;
        L.d(this, "start");
        mHandler.post(mRunnable);
    }

    public synchronized boolean isStarted() {
        return mStarted;
    }

    public synchronized void stop() {
        if (!mStarted) return;
        mStarted = false;
        L.d(this, "stop");
        mHandler.removeCallbacks(mRunnable);
    }

    private synchronized void postSchedule() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.post(mRunnable);
    }

    private synchronized void schedule() {
        if (!mStarted) return;

        if (mAds.size() < mPrefetchAdMaxCount) {
            mHandler.removeCallbacks(mRunnable);
            if (mLoading) {
                return;
            }
            mLoading = true;
            final int count = Math.min(mPrefetchAdMaxCount - mAds.size(), mAdLoader.maxLoadNum());
            L.d(this, "schedule", "load", count, "total", mAds.size(), "max", mPrefetchAdMaxCount);
            mAdLoader.load(AdLoader.TYPE_PRELOAD, count, new AdLoader.Callback() {
                @Override
                public void onSuccess(List<Ad> ads) {
                    mLoading = false;
                    mAds.addAll(ads);
                    L.d(this, "schedule", "add", ads.size());
                    postSchedule();
                }

                @Override
                public void onError(Exception e) {
                    mLoading = false;
                    L.d(this, "schedule", "error", e);
                    postSchedule();
                }
            });
        } else {
            L.d(this, "schedule", "end", "total", mAds.size(), "max", mPrefetchAdMaxCount);
        }
    }
}
