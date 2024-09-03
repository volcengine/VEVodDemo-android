/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.voddemo.ui.video.scene.longvideo;

import static com.bytedance.volc.voddemo.ui.video.scene.longvideo.LongVideoAdapter.Item;
import static com.bytedance.volc.voddemo.ui.video.scene.longvideo.LongVideoAdapter.OnItemClickListener;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.widgets.load.LoadMoreAble;
import com.bytedance.volc.vod.scenekit.ui.widgets.load.impl.RecycleViewLoadMoreHelper;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.base.BaseVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.data.remote.GetFeedStream;
import com.bytedance.volc.voddemo.ui.video.data.remote.api.GetFeedStreamApi;
import com.bytedance.volc.voddemo.ui.video.scene.VideoActivity;
import com.bytedance.volc.voddemo.ui.video.scene.detail.DetailVideoFragment;

import java.util.List;


public class LongVideoFragment extends BaseFragment {

    private GetFeedStreamApi mRemoteApi;
    private String mAccount;

    private final Book<VideoItem> mBook = new Book<>(12);
    private LongVideoAdapter mAdapter;
    private SwipeRefreshLayout mRefreshLayout;
    private RecyclerView mRecyclerView;
    private RecycleViewLoadMoreHelper mLoadMoreHelper;
    private LongVideoDataTrans mDataTrans;

    private DetailVideoFragment.DetailVideoSceneEventListener mListener;

    public static LongVideoFragment newInstance() {
        return new LongVideoFragment();
    }

    public void setDetailSceneEventListener(DetailVideoFragment.DetailVideoSceneEventListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onBackPressed() {
        final String pageType = VideoSettings.stringValue(VideoSettings.DETAIL_VIDEO_SCENE_FRAGMENT_OR_ACTIVITY);
        if (TextUtils.equals(pageType, "Activity")) {
            return super.onBackPressed();
        } else {
            DetailVideoFragment detailVideoFragment = (DetailVideoFragment) requireActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag(DetailVideoFragment.class.getName());
            if (detailVideoFragment != null) {
                if (detailVideoFragment.onBackPressed()) {
                    return true;
                }
            }
            return super.onBackPressed();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new GetFeedStream();
        mAccount = VideoSettings.stringValue(VideoSettings.LONG_VIDEO_SCENE_ACCOUNT_ID);
        mDataTrans = new LongVideoDataTrans(requireActivity());
        mAdapter = new LongVideoAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(Item item, RecyclerView.ViewHolder holder) {
                if (item.type == Item.TYPE_VIDEO_ITEM) {
                    enterDetail(item.videoItem);
                }
            }

            @Override
            public void onHeaderItemClick(VideoItem item, RecyclerView.ViewHolder holder) {
                enterDetail(item);
            }
        });
    }

    private void enterDetail(VideoItem item) {
        final MediaSource source = VideoItem.toMediaSource(item);
        final Bundle bundle = DetailVideoFragment.createBundle(source, false);

        final String pageType = VideoSettings.stringValue(VideoSettings.DETAIL_VIDEO_SCENE_FRAGMENT_OR_ACTIVITY);

        if (TextUtils.equals(pageType, "Activity")) {
            VideoActivity.intentInto(requireActivity(), PlayScene.SCENE_DETAIL, bundle);
        } else {
            FragmentActivity activity = requireActivity();
            DetailVideoFragment detail = DetailVideoFragment.newInstance(bundle);
            detail.getLifecycle().addObserver(mDetailLifeCycle);
            activity.getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(R.anim.vevod_slide_in_right, R.anim.vevod_slide_out_right,
                            R.anim.vevod_slide_in_right, R.anim.vevod_slide_out_right)
                    .addToBackStack(null)
                    .add(android.R.id.content, detail, DetailVideoFragment.class.getName())
                    .commit();
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

    @Override
    protected void initBackPressedHandler() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        FrameLayout frameLayout = new FrameLayout(requireActivity());
        mRefreshLayout = new SwipeRefreshLayout(requireActivity());
        mRecyclerView = new RecyclerView(requireActivity());
        GridLayoutManager layoutManager = new GridLayoutManager(requireActivity(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new LongVideoItemDecoration());
        mRefreshLayout.addView(mRecyclerView);
        frameLayout.addView(mRefreshLayout);
        mLoadMoreHelper = new RecycleViewLoadMoreHelper(mRecyclerView);
        return frameLayout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });
        mLoadMoreHelper.setOnLoadMoreListener(new LoadMoreAble.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadMore();
            }
        });
        refresh();
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        showRefreshing();
        mRemoteApi.getFeedStream(mAccount, 0, mBook.pageSize(), new RemoteApi.Callback<List<BaseVideo>>() {
            @Override
            public void onSuccess(List<BaseVideo> result) {
                L.d(this, "refresh", "success");
                if (getActivity() == null) return;
                List<VideoItem> videoItems = mBook.firstPage(new Page<>(BaseVideo.toVideoItems(result), 0, Page.TOTAL_INFINITY));
                VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_LONG), null);
                VideoItem.syncProgress(videoItems, true);
                dismissRefreshing();
                mDataTrans.setList(mAdapter, videoItems);
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

    private void loadMore() {
        if (mBook.hasMore()) {
            if (isLoadingMore()) return;
            L.d(this, "loadMore", "start", mBook.nextPageIndex(), mBook.pageSize());
            showLoadingMore();
            mRemoteApi.getFeedStream(mAccount, mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<List<BaseVideo>>() {
                @Override
                public void onSuccess(List<BaseVideo> result) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    List<VideoItem> videoItems = mBook.addPage(new Page<>(BaseVideo.toVideoItems(result), mBook.nextPageIndex(), Page.TOTAL_INFINITY));
                    VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_LONG), null);
                    VideoItem.syncProgress(videoItems, true);
                    dismissLoadingMore();
                    mDataTrans.append(mAdapter, videoItems);
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

}
