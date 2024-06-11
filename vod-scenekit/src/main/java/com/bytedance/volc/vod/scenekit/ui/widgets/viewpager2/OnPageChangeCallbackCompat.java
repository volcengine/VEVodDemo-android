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

/**
 * 1. 解决 ViewPager2 onPageSelected 回调了，但是通过 position 找不到 ItemView 的问题。
 * 2. 封装 onPagePeekStart 方便预渲染使用
 */
public abstract class OnPageChangeCallbackCompat extends ViewPager2.OnPageChangeCallback {

    public static final int RETRY_COUNT = 10;

    private final WeakReference<ViewPager2> mViewPagerRef;
    private final SparseIntArray mPageSelectedTryInvokeCounts = new SparseIntArray();

    private boolean mPeekStart;
    private int mPeekPosition;

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
            viewPager.postDelayed(() -> {
                onPageSelected(position);
            }, 10);
            return;
        }
        mPageSelectedTryInvokeCounts.put(position, 0);
        onPageSelected(viewPager, position);
    }

    @Override
    public final void onPageScrollStateChanged(int state) {
        final ViewPager2 viewPager = mViewPagerRef.get();
        if (viewPager == null) return;

        onPageScrollStateChanged(viewPager, state);

        if (state == ViewPager2.SCROLL_STATE_IDLE && mPeekStart) {
            mPeekStart = false;
            onPagePeekEnd(viewPager, viewPager.getCurrentItem(), mPeekPosition);
        }
    }

    @Override
    public final void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        final ViewPager2 viewPager = mViewPagerRef.get();
        if (viewPager == null) return;

        onPageScrolled(viewPager, position, positionOffset, positionOffsetPixels);

        if (!mPeekStart) {
            if (positionOffset > 0) {
                mPeekStart = true;
                mPeekPosition = positionOffset > 0.5 ? position - 1 : position + 1;
                onPagePeekStart(viewPager, position, mPeekPosition);
            }
        }
    }

    public void onPageSelected(ViewPager2 pager, int position) {
        L.d(this, "onPageSelected", pager, "position=" + pager.getCurrentItem());
    }

    public void onPageScrollStateChanged(ViewPager2 pager, int state) {
        L.d(this, "onPageScrollStateChanged", "state=" + state);
    }

    public void onPageScrolled(ViewPager2 pager, int position, float positionOffset, int positionOffsetPixels) {
        //L.v(this, "onPageScrolled", pager, "position=" + position, "positionOffset=" + positionOffset, "positionOffsetPixels=" + positionOffsetPixels);
    }

    public void onPagePeekStart(ViewPager2 pager, int position, int peekPosition) {
        L.d(this, "onPagePeekStart", pager, "position=" + position, "peekPosition=" + peekPosition);
    }

    public void onPagePeekEnd(ViewPager2 pager, int position, int peekPosition) {
        L.d(this, "onPagePeekEnd", pager, "position=" + position, "peekPosition=" + peekPosition);
    }
}
