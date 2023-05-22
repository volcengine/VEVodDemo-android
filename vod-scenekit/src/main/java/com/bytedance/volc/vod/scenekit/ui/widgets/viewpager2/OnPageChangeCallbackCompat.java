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
 * Create Date : 2023/5/21
 */

package com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2;

import android.util.SparseIntArray;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.utils.L;

import java.lang.ref.WeakReference;

public abstract class OnPageChangeCallbackCompat extends ViewPager2.OnPageChangeCallback {

    public static final int RETRY_COUNT = 10;

    public WeakReference<ViewPager2> mViewPagerRef;
    private final SparseIntArray mPageSelectedTryInvokeCounts = new SparseIntArray();

    public OnPageChangeCallbackCompat(ViewPager2 viewPager) {
        this.mViewPagerRef = new WeakReference<>(viewPager);
    }

    @Override
    public final void onPageSelected(int position) {
        final ViewPager2 viewPager = mViewPagerRef.get();
        if (viewPager == null) return;

        View view = null;
        final RecyclerView recyclerView = (RecyclerView) viewPager.getChildAt(0);
        if (recyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                view = layoutManager.findViewByPosition(position);
            }
        }
        int retryCountAtPos = mPageSelectedTryInvokeCounts.get(position);
        if (view == null && retryCountAtPos < RETRY_COUNT) {
            mPageSelectedTryInvokeCounts.put(position, ++retryCountAtPos);
            L.i(this, "onPageSelected", viewPager, position, "retry", retryCountAtPos);
            viewPager.post(() -> {
                onPageSelected(position);
            });
            return;
        }
        if (retryCountAtPos > 0) {
            mPageSelectedTryInvokeCounts.removeAt(position);
        }
        onPageSelected(position, viewPager);
    }

    public void onPageSelected(int position, ViewPager2 pager) {
    }
}
