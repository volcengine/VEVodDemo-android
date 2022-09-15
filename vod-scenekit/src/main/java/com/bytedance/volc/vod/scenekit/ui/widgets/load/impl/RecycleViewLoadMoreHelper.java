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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.volc.vod.scenekit.ui.widgets.load.LoadMoreAble;

public class RecycleViewLoadMoreHelper implements LoadMoreAble {
    private final RecyclerView mRecyclerView;
    private boolean mLoadingMore;
    private boolean mLoadMoreEnabled = true;
    private OnLoadMoreListener mOnLoadMoreListener;

    public RecycleViewLoadMoreHelper(RecyclerView mRecyclerView) {
        this.mRecyclerView = mRecyclerView;
        this.mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!isLoadMoreEnabled()) return;
                if (canTriggerLoadMore() && mOnLoadMoreListener != null) {
                    mOnLoadMoreListener.onLoadMore();
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

    protected boolean canTriggerLoadMore() {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        int lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
        return lastVisiblePosition + 2 >= mRecyclerView.getAdapter().getItemCount()
                && !mLoadingMore;
    }
}
