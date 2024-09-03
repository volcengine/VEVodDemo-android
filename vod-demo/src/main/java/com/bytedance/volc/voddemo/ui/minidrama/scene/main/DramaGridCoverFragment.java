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
 * Create Date : 2024/3/26
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.main;


import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.DramaDetailVideoInput;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.widgets.load.impl.RecycleViewLoadMoreHelper;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.data.business.model.DramaItem;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.GetDramas;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramasApi;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract;

import java.util.List;

public class DramaGridCoverFragment extends BaseFragment {
    private GetDramasApi mRemoteApi;
    private final Book<DramaInfo> mBook = new Book<>(12);
    private SwipeRefreshLayout mRefreshLayout;
    private DramaGridCoverAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecycleViewLoadMoreHelper mLoadMoreHelper;
    public ActivityResultLauncher<DramaDetailVideoInput> mDramaDetailPageLauncher = registerForActivityResult(new DramaDetailVideoActivityResultContract(), result -> {
    });

    public static DramaGridCoverFragment newInstance() {
        return new DramaGridCoverFragment();
    }

    public DramaGridCoverFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new GetDramas();
        mAdapter = new DramaGridCoverAdapter() {

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                DramaGridCoverAdapter.ViewHolder holder = super.onCreateViewHolder(parent, viewType);
                holder.itemView.setOnClickListener(v -> {
                    mDramaDetailPageLauncher.launch(new DramaDetailVideoInput(DramaItem.createByDramaInfos(mAdapter.getItems()), holder.getAbsoluteAdapterPosition(), false));
                });
                return holder;
            }
        };
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_mini_drama_grid_cover_fragment;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        ((ViewGroup.MarginLayoutParams) mRefreshLayout.getLayoutParams()).topMargin =
                (int) (UIUtils.getStatusBarHeight(requireActivity()) // status bar height
                        + UIUtils.dip2Px(requireActivity(), 44)); // tab bar height

        mRecyclerView = view.findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 3);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new DramaGridCoverItemDecoration());
        mLoadMoreHelper = new RecycleViewLoadMoreHelper(mRecyclerView);

        mRefreshLayout.setOnRefreshListener(this::refresh);
        mLoadMoreHelper.setOnLoadMoreListener(this::loadMore);
        refresh();
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        showRefreshing();
        mRemoteApi.getDramas(0, mBook.pageSize(), new RemoteApi.Callback<List<DramaInfo>>() {
            @Override
            public void onSuccess(List<DramaInfo> result) {
                L.d(this, "refresh", "success");
                if (getActivity() == null) return;
                dismissRefreshing();
                List<DramaInfo> dramas = mBook.firstPage(new Page<>(result, 0, Page.TOTAL_INFINITY));
                mAdapter.setItems(dramas);
            }

            @Override
            public void onError(Exception e) {
                L.d(this, "refresh", e, "error");
                if (getActivity() == null) return;
                dismissRefreshing();
                Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMore() {
        if (mBook.hasMore()) {
            if (isLoadingMore()) return;
            L.d(this, "loadMore", "start", mBook.nextPageIndex(), mBook.pageSize());
            showLoadingMore();
            mRemoteApi.getDramas(mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<List<DramaInfo>>() {
                @Override
                public void onSuccess(List<DramaInfo> result) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    List<DramaInfo> dramas = mBook.addPage(new Page<>(result, mBook.nextPageIndex(), Page.TOTAL_INFINITY));
                    dismissLoadingMore();
                    mAdapter.appendItems(dramas);
                }

                @Override
                public void onError(Exception e) {
                    L.d(this, "loadMore", "error", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    dismissLoadingMore();
                    Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mBook.end();
            finishLoadingMore();
            L.d(this, "loadMore", "end");
        }
    }

    private boolean isRefreshing() {
        return mRefreshLayout.isRefreshing();
    }

    private void dismissRefreshing() {
        mRefreshLayout.setRefreshing(false);
    }

    private void showRefreshing() {
        mRefreshLayout.setRefreshing(true);
    }

    private boolean isLoadingMore() {
        return mLoadMoreHelper.isLoadingMore();
    }

    private void showLoadingMore() {
        mLoadMoreHelper.showLoadingMore();
    }

    private void dismissLoadingMore() {
        mLoadMoreHelper.dismissLoadingMore();
    }

    private void finishLoadingMore() {
        mLoadMoreHelper.finishLoadingMore();
    }
}
