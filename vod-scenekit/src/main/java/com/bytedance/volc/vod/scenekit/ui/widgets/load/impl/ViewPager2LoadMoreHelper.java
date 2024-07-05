/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.scenekit.ui.widgets.load.impl;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.volc.vod.scenekit.ui.widgets.load.LoadMoreAble;

public class ViewPager2LoadMoreHelper implements LoadMoreAble {

    private final ViewPager2 mViewPager;
    private OnLoadMoreListener mOnLoadMoreListener;

    private boolean mLoadingMore;

    private boolean mLoadMoreEnabled = true;

    public ViewPager2LoadMoreHelper(ViewPager2 viewPager) {
        this.mViewPager = viewPager;
        this.mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (!isLoadMoreEnabled()) return;

                final RecyclerView.Adapter<?> adapter = mViewPager.getAdapter();
                if (adapter == null) return;

                final int count = adapter.getItemCount();
                if (position == count - 2 && !isLoadingMore()) {
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onLoadMore();
                    }
                }
            }
        });
    }

    @Override
    public void setLoadMoreEnabled(boolean enabled) {
        mLoadMoreEnabled = enabled;
    }

    @Override
    public boolean isLoadMoreEnabled() {
        return mLoadMoreEnabled;
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mOnLoadMoreListener = listener;
    }

    @Override
    public boolean isLoadingMore() {
        return mLoadingMore;
    }

    @Override
    public void showLoadingMore() {
        mLoadingMore = true;
    }

    @Override
    public void dismissLoadingMore() {
        mLoadingMore = false;
    }

    @Override
    public void finishLoadingMore() {
        mLoadingMore = false;
    }
}
