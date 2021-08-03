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
 * Create Date : 2021/7/20
 */
package com.bytedance.volc.voddemo.smallvideo.pager;

import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;

public class VOLCPagerSnapHelper extends PagerSnapHelper {
    public static final String TAG = "VOLCPagerSnapHelper";

    private RecyclerViewPagerListener mRecyclerViewPagerListener;

    @Override
    public int findTargetSnapPosition(final RecyclerView.LayoutManager layoutManager,
            final int velocityX, final int velocityY) {

        int targetSnapPosition = super.findTargetSnapPosition(layoutManager, velocityX,
                velocityY);
        TTVideoEngineLog.d(TAG, "findTargetSnapPosition " + targetSnapPosition);
        if (mRecyclerViewPagerListener != null) {
            mRecyclerViewPagerListener.onPageSelected(targetSnapPosition, null);
        }
        return targetSnapPosition;
    }

    public void setCallback(final RecyclerViewPagerListener recyclerViewPagerListener) {
        mRecyclerViewPagerListener = recyclerViewPagerListener;
    }
}
