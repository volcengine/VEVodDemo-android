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

package com.bytedance.volc.voddemo.ui.video.scene.feedvideo;

import static com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene.SCENE_DETAIL;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoPageView;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoSceneView;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.ui.video.data.remote.api.GetFeedStreamApi;
import com.bytedance.volc.voddemo.ui.video.data.remote.GetFeedStream;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.scene.VideoActivity;
import com.bytedance.volc.voddemo.ui.video.scene.detail.DetailVideoFragment;

import java.util.List;


public class FeedVideoFragment extends BaseFragment implements FeedVideoPageView.DetailPageNavigator {

    private GetFeedStreamApi mRemoteApi;
    private String mAccount;
    private final Book<VideoItem> mBook = new Book<>(10);
    private FeedVideoSceneView mSceneView;

    private DetailVideoFragment.DetailVideoSceneEventListener mListener;

    public FeedVideoFragment() {
        // Required empty public constructor
    }

    public static FeedVideoFragment newInstance() {
        return new FeedVideoFragment();
    }

    @Override
    public boolean onBackPressed() {
        if (mSceneView.onBackPressed()) {
            return true;
        }
        return super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new GetFeedStream();
        mAccount = VideoSettings.stringValue(VideoSettings.FEED_VIDEO_SCENE_ACCOUNT_ID);
    }

    @Override
    protected void initBackPressedHandler() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_feed_video_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSceneView = view.findViewById(R.id.shortVideoSceneView);
        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.setOnRefreshListener(this::refresh);
        mSceneView.setOnLoadMoreListener(this::loadMore);
        mSceneView.setDetailPageNavigator(this);
        refresh();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemoteApi.cancel();
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        mSceneView.showRefreshing();
        mRemoteApi.getFeedStream(mAccount, 0, mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
            @Override
            public void onSuccess(Page<VideoItem> page) {
                L.d(this, "refresh", "success");
                if (getActivity() == null) return;
                List<VideoItem> videoItems = mBook.firstPage(page);
                VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_FEED), null);
                VideoItem.syncProgress(videoItems, true);
                mSceneView.dismissRefreshing();
                if (!videoItems.isEmpty()) {
                    mSceneView.pageView().setItems(videoItems);
                }
            }

            @Override
            public void onError(Exception e) {
                L.d(this, "refresh", e, "error");
                if (getActivity() == null) return;
                mSceneView.dismissRefreshing();
                Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMore() {
        if (mBook.hasMore()) {
            if (mSceneView.isLoadingMore()) return;
            mSceneView.showLoadingMore();
            L.d(this, "loadMore", "start", mBook.nextPageIndex(), mBook.pageSize());
            mRemoteApi.getFeedStream(mAccount, mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
                @Override
                public void onSuccess(Page<VideoItem> page) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    List<VideoItem> videoItems = mBook.addPage(page);
                    VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_FEED), null);
                    VideoItem.syncProgress(videoItems, true);
                    mSceneView.dismissLoadingMore();
                    mSceneView.pageView().appendItems(videoItems);
                }

                @Override
                public void onError(Exception e) {
                    L.d(this, "loadMore", e, "error", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    mSceneView.dismissLoadingMore();
                    Toast.makeText(getActivity(), e.getMessage() + "", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mBook.end();
            mSceneView.finishLoadingMore();
            L.d(this, "loadMore", "end");
        }
    }

    public void setDetailSceneEventListener(DetailVideoFragment.DetailVideoSceneEventListener listener) {
        mListener = listener;
    }

    @Override
    public void enterDetail(FeedVideoViewHolder holder) {
        final String pageType = VideoSettings.stringValue(VideoSettings.DETAIL_VIDEO_SCENE_FRAGMENT_OR_ACTIVITY);
        if (TextUtils.equals(pageType, "Activity")) {
            final VideoView videoView = holder.getSharedVideoView();
            if (videoView == null) return;

            final MediaSource source = videoView.getDataSource();
            if (source == null) return;

            final PlaybackController controller = videoView.controller();

            boolean continuesPlayback = false;
            if (controller != null) {
                continuesPlayback = controller.player() != null;
                controller.unbindPlayer();
            }
            final Bundle bundle = DetailVideoFragment.createBundle(source, continuesPlayback);
            VideoActivity.intentInto(getActivity(), SCENE_DETAIL, bundle);
        } else {
            final FragmentActivity activity = requireActivity();
            final DetailVideoFragment detail = DetailVideoFragment.newInstance();
            detail.setFeedVideoViewHolder(holder);
            detail.getLifecycle().addObserver(mDetailLifeCycle);
            activity.getSupportFragmentManager()
                    .beginTransaction()
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
}
