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

package com.bytedance.volc.voddemo.ui.video.scene.shortvideo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.layer.SimpleProgressBarLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.vod.scenekit.utils.OrientationHelper;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.api2.GetFeedStreamApi;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.scene.VideoActivity;
import com.bytedance.volc.voddemo.ui.video.scene.fullscreen.FullScreenVideoFragment;

import java.util.List;


public class ShortVideoFragment extends BaseFragment {

    private RemoteApi.GetFeedStream mRemoteApi;
    private String mAccount;

    private final Book<VideoItem> mBook = new Book<>(10);
    private ShortVideoSceneView mSceneView;
    private OrientationHelper mOrientationHelper;

    public ShortVideoFragment() {
        // Required empty public constructor
    }

    public static ShortVideoFragment newInstance() {
        ShortVideoFragment fragment = new ShortVideoFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new GetFeedStreamApi();
        mAccount = VideoSettings.stringValue(VideoSettings.SHORT_VIDEO_SCENE_ACCOUNT_ID);
        mOrientationHelper = new OrientationHelper(requireActivity(), null);
        mOrientationHelper.setOrientationDelta(45);
        mOrientationHelper.enable();
    }

    @Override
    protected void initBackPressedHandler() {
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_short_video_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSceneView = (ShortVideoSceneView) view;
        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.setOnRefreshListener(this::refresh);
        mSceneView.setOnLoadMoreListener(this::loadMore);
        refresh();
        registerBroadcast();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBroadcast();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemoteApi.cancel();
        mOrientationHelper.disable();
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
                VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
                mSceneView.dismissRefreshing();
                mSceneView.pageView().setItems(videoItems);
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
            mSceneView.showLoadingMore();
            L.d(this, "loadMore", "start", mBook.nextPageIndex(), mBook.pageSize());
            mRemoteApi.getFeedStream(mAccount, mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
                @Override
                public void onSuccess(Page<VideoItem> page) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    List<VideoItem> videoItems = mBook.addPage(page);
                    VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
                    mSceneView.dismissLoadingMore();
                    mSceneView.pageView().appendItems(videoItems);
                }

                @Override
                public void onError(Exception e) {
                    L.d(this, "loadMore", "error", mBook.nextPageIndex());
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


    private void enterFullScreen(MediaSource mediaSource) {
        VideoView videoView = (VideoView) mSceneView.pageView().getCurrentItemView();
        if (videoView == null) return;

        if (!MediaSource.mediaEquals(videoView.getDataSource(), mediaSource)) return;

        L.d(this, "enterFullScreen", MediaSource.dump(mediaSource));

        final PlaybackController controller = videoView.controller();

        boolean continuesPlayback = false;
        if (controller != null) {
            continuesPlayback = controller.player() != null;
            controller.unbindPlayer();
        }

        VideoActivity.intentInto(requireActivity(),
                PlayScene.SCENE_FULLSCREEN,
                FullScreenVideoFragment.createBundle(mediaSource, continuesPlayback, mOrientationHelper.getOrientation()));
    }

    /**
     * Sync playback states in FullScreenFragment
     */
    private void onExitFullScreen(MediaSource mediaSource, boolean continuesPlayback) {
        VideoView videoView = (VideoView) mSceneView.pageView().getCurrentItemView();
        if (videoView == null) return;

        if (!MediaSource.mediaEquals(videoView.getDataSource(), mediaSource)) return;

        L.d(this, "exitFullScreen", MediaSource.dump(mediaSource), continuesPlayback);

        if (continuesPlayback) {
            final PlaybackController controller = videoView.controller();
            if (controller != null) {
                controller.preparePlayback();
            }
            final Player player = controller.player();
            if (player != null && (player.isPaused() || (!player.isLooping() && player.isCompleted()))) {
                mSceneView.pageView().setInterceptStartPlaybackOnResume(true);
            }
        }
    }

    private BroadcastReceiver mBroadcastReceiver;
    private void registerBroadcast() {
        if (mBroadcastReceiver != null) return;
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action == null) return;

                switch (action) {
                    case SimpleProgressBarLayer.ACTION_ENTER_FULLSCREEN: {
                        MediaSource mediaSource = (MediaSource) intent.getSerializableExtra(SimpleProgressBarLayer.EXTRA_MEDIA_SOURCE);
                        if (mediaSource == null) return;
                        enterFullScreen(mediaSource);
                        break;
                    }
                    case FullScreenVideoFragment.ACTION_USER_EXIT_FULLSCREEN: {
                        MediaSource mediaSource = (MediaSource) intent.getSerializableExtra(FullScreenVideoFragment.EXTRA_MEDIA_SOURCE);
                        boolean continuesPlayback = intent.getBooleanExtra(FullScreenVideoFragment.EXTRA_CONTINUES_PLAYBACK, false);
                        if (mediaSource == null) return;
                        onExitFullScreen(mediaSource, continuesPlayback);
                        break;
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(SimpleProgressBarLayer.ACTION_ENTER_FULLSCREEN);
        filter.addAction(FullScreenVideoFragment.ACTION_USER_EXIT_FULLSCREEN);
        LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(mBroadcastReceiver, filter);
    }

    private void unregisterBroadcast() {
        if (mBroadcastReceiver == null) return;

        LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mBroadcastReceiver);
        mBroadcastReceiver = null;
    }
}