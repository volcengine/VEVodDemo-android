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
 * Create Date : 2021/2/25
 */
package com.bytedance.volc.voddemo.utils;

import android.os.Handler;
import android.os.Looper;

public class TimerTaskManager {
    private Handler mHandler;
    private TaskListener mTaskListener;
    private boolean mTracking = false;

    private final Runnable mTrackingRunnable = new Runnable() {
        @Override
        public void run() {
            if (mTaskListener == null) {
                stopTracking();
                return;
            }

            final long delay = mTaskListener.onTaskExecute();
            if (delay > 0) {
                mHandler.postDelayed(this, delay);
            } else {
                stopTracking();
            }
        }
    };

    public TimerTaskManager(final Looper looper, final TaskListener taskListener) {
        mHandler = new Handler(looper);
        mTaskListener = taskListener;
    }

    public void startTracking() {
        if (mTracking) {
            return;
        }

        mTracking = true;
        mHandler.removeCallbacks(mTrackingRunnable);
        mHandler.post(mTrackingRunnable);
    }

    public void stopTracking() {
        if (!mTracking) {
            return;
        }

        mTracking = false;
        mHandler.removeCallbacks(mTrackingRunnable);
    }

    public void release() {
        mTracking = false;
        mHandler.removeCallbacksAndMessages(null);
        mHandler = null;
        mTaskListener = null;
    }

    public interface TaskListener {
        long onTaskExecute();
    }
}
