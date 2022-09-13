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

package com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.volc.vod.scenekit.ui.video.scene.detail.DetailVideoFragment;
import com.bytedance.volc.vod.scenekit.ui.widgets.load.LoadMoreAble;
import com.bytedance.volc.vod.scenekit.ui.widgets.load.RefreshAble;
import com.bytedance.volc.vod.scenekit.ui.widgets.load.impl.RecycleViewLoadMoreHelper;

public class FeedVideoSceneView extends FrameLayout implements FeedVideoPageView.DetailPageNavigator, RefreshAble, LoadMoreAble {
    private final FeedVideoPageView mPageView;
    private final SwipeRefreshLayout mRefreshLayout;

    private final RecycleViewLoadMoreHelper mLoadMoreHelper;

    private RefreshAble.OnRefreshListener mRefreshListener;
    private LoadMoreAble.OnLoadMoreListener mLoadMoreListener;

    private FeedVideoSceneEventListener mListener;

    public interface FeedVideoSceneEventListener {
        void onEnterDetail();

        void onExitDetail();
    }

    public FeedVideoSceneView(@NonNull Context context) {
        this(context, null);
    }

    public FeedVideoSceneView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FeedVideoSceneView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mPageView = new FeedVideoPageView(context);

        mRefreshLayout = new SwipeRefreshLayout(context);
        mRefreshLayout.setOnRefreshListener(() -> {
            if (mRefreshListener != null) {
                mRefreshListener.onRefresh();
            }
        });
        mRefreshLayout.addView(mPageView, new SwipeRefreshLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mRefreshLayout, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mPageView.setDetailPageNavigator(this);
        mLoadMoreHelper = new RecycleViewLoadMoreHelper(mPageView.recyclerView());
        mLoadMoreHelper.setOnLoadMoreListener(() -> {
            if (mLoadMoreListener != null) {
                mLoadMoreListener.onLoadMore();
            }
        });
    }

    public void setEventListener(FeedVideoSceneEventListener listener) {
        this.mListener = listener;
    }

    @Override
    public void setLoadMoreEnabled(boolean enabled) {
        mLoadMoreHelper.setLoadMoreEnabled(enabled);
    }

    @Override
    public boolean isLoadMoreEnabled() {
        return mLoadMoreHelper.isLoadMoreEnabled();
    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.mLoadMoreListener = listener;
    }

    @Override
    public boolean isLoadingMore() {
        return mLoadMoreHelper.isLoadingMore();
    }

    @Override
    public void showLoadingMore() {
        mLoadMoreHelper.showLoadingMore();
    }

    @Override
    public void dismissLoadingMore() {
        mLoadMoreHelper.dismissLoadingMore();
    }

    @Override
    public void finishLoadingMore() {
        mLoadMoreHelper.finishLoadingMore();
    }

    @Override
    public void setRefreshEnabled(boolean enabled) {
        mRefreshLayout.setEnabled(enabled);
    }

    @Override
    public boolean isRefreshEnabled() {
        return mRefreshLayout.isEnabled();
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        this.mRefreshListener = listener;
    }

    @Override
    public boolean isRefreshing() {
        return mRefreshLayout.isRefreshing();
    }

    @Override
    public void showRefreshing() {
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    public void dismissRefreshing() {
        mRefreshLayout.setRefreshing(false);
    }

    public FeedVideoPageView pageView() {
        return mPageView;
    }

    public boolean onBackPressed() {
        return mPageView.onBackPressed();
    }

    @Override
    public boolean isDetail() {
        DetailVideoFragment detailVideoFragment = (DetailVideoFragment) ((FragmentActivity) getContext())
                .getSupportFragmentManager()
                .findFragmentByTag(DetailVideoFragment.class.getSimpleName());
        return detailVideoFragment != null;
    }

    @Override
    public void enterDetail(FeedVideoViewHolder holder) {
        FragmentActivity activity = (FragmentActivity) getContext();
        DetailVideoFragment detail = DetailVideoFragment.newInstance();
        detail.setFeedVideoViewHolder(holder);
        detail.getLifecycle().addObserver(mDetailLifeCycle);
        activity.getSupportFragmentManager()
                .beginTransaction()
                .addToBackStack(null)
                .add(android.R.id.content, detail, DetailVideoFragment.class.getName())
                .commit();
    }

    @Override
    public void exitDetail() {
        if (isDetail()) {
            FragmentActivity activity = (FragmentActivity) getContext();
            activity.getSupportFragmentManager().popBackStack();
        }
    }

    final LifecycleEventObserver mDetailLifeCycle = new LifecycleEventObserver() {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            switch (event) {
                case ON_CREATE:
                    if (mListener != null) {
                        mListener.onEnterDetail();
                    }
                    break;
                case ON_DESTROY:
                    if (mListener != null) {
                        mListener.onExitDetail();
                    }
                    break;
            }
        }
    };
}
