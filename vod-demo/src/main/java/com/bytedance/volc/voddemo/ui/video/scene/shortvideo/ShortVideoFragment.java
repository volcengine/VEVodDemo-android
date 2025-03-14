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

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.ItemType;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.layer.SimpleProgressBarLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.viewholder.ShortVideoItemViewHolder;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;
import com.bytedance.volc.vod.scenekit.utils.OrientationHelper;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.ad.api.Ad;
import com.bytedance.volc.voddemo.ui.ad.mock.MockShortVideoAdVideoView;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder.ShortVideoDrawADItemViewHolder;
import com.bytedance.volc.voddemo.ui.video.data.mock.MockGetFeedStreamMultiItems;
import com.bytedance.volc.voddemo.ui.video.data.remote.api.GetFeedStreamApi;
import com.bytedance.volc.voddemo.ui.video.scene.VideoActivity;
import com.bytedance.volc.voddemo.ui.video.scene.fullscreen.FullScreenVideoFragment;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.PipVideoController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;


public class ShortVideoFragment extends BaseFragment {
    private GetFeedStreamApi mRemoteApi;
    private final Book<Item> mBook = new Book<>(10);
    private ShortVideoSceneView mSceneView;
    private OrientationHelper mOrientationHelper;
    private boolean mFullScreen;

    public ShortVideoFragment() {
        // Required empty public constructor
    }

    public static ShortVideoFragment newInstance() {
        return new ShortVideoFragment();
    }

    @Override
    protected void initBackPressedHandler() {
    }

    @Override
    public boolean onBackPressed() {
        if (mSceneView.onBackPressed()) {
            return true;
        }
        // For some device swipe back gesture break the Activity/Fragment lifecycle. Stop here to fix
        // player release sequence bug.
        mSceneView.pageView().stop();
        return super.onBackPressed();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mRemoteApi = new MockGetFeedStreamMultiItems(VideoSettings.stringValue(VideoSettings.SHORT_VIDEO_SCENE_ACCOUNT_ID));
        mOrientationHelper = new OrientationHelper(requireActivity(), null);
        mOrientationHelper.setOrientationDelta(45);
        mOrientationHelper.enable();
        mPipSessionKey = UUID.randomUUID().toString();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP)) {
            inflater.inflate(R.menu.vevod_menu_short_video, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_pip_action) {
            enterPip(true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemoteApi.cancel();
        mOrientationHelper.disable();
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
        mSceneView.pageView().setViewHolderFactory(new ShortVideoAdViewHolderFactory());
        mSceneView.setOnRefreshListener(this::refresh);
        mSceneView.setOnLoadMoreListener(this::loadMore);

        refresh();
        registerBroadcast();
    }

    @Override
    public void onResume() {
        super.onResume();
        dismissPip();
    }

    @Override
    public void onStop() {
        super.onStop();
        enterPip(false);
    }

    private class ShortVideoAdViewHolderFactory implements ViewHolder.Factory {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ItemType.ITEM_TYPE_VIDEO: {
                    ShortVideoItemViewHolder viewHolder = new ShortVideoItemViewHolder(new FrameLayout(parent.getContext()));
                    PlaybackController controller = viewHolder.videoView == null ? null : viewHolder.videoView.controller();
                    if (controller != null) {
                        controller.addPlaybackListener(new Dispatcher.EventListener() {
                            @Override
                            public void onEvent(Event event) {
                                if (event.code() == PlayerEvent.State.COMPLETED) {
                                    onPlayerStateCompleted(event);
                                }
                            }
                        });
                    }
                    return viewHolder;
                }
                case ItemType.ITEM_TYPE_AD: {
                    ShortVideoDrawADItemViewHolder viewHolder = new ShortVideoDrawADItemViewHolder(new FrameLayout(parent.getContext()));
                    if (viewHolder.mockAdVideoView != null) {
                        viewHolder.mockAdVideoView.setAdVideoListener(new MockShortVideoAdVideoView.MockAdVideoListener() {
                            @Override
                            public void onAdVideoCompleted(Ad ad) {
                                playNext();
                            }
                        });
                    }
                    return viewHolder;
                }
            }
            throw new IllegalArgumentException("unsupported type!");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterBroadcast();
    }

    private void onPlayerStateCompleted(Event event) {
        final Player player = event.owner(Player.class);
        if (player != null && !player.isLooping() && VideoSettings.intValue(VideoSettings.SHORT_VIDEO_PLAYBACK_COMPLETE_ACTION) == 1 /* 1 播放下一个 */) {
            playNext();
        }
    }

    private void playNext() {
        final int currentPosition = mSceneView.pageView().getCurrentItem();
        final int nextPosition = currentPosition + 1;
        if (nextPosition < mSceneView.pageView().getItemCount()) {
            mSceneView.pageView().setCurrentItem(nextPosition, true);
        }
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        mSceneView.showRefreshing();
        mRemoteApi.getFeedStream(0, mBook.pageSize(), new RemoteApi.Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                L.d(this, "refresh", "success");
                if (getActivity() == null) return;
                mSceneView.dismissRefreshing();
                mBook.firstPage(new Page<>(items, 0, Page.TOTAL_INFINITY));

                VideoItem.tag(VideoItem.findVideoItems(items), PlayScene.map(PlayScene.SCENE_SHORT), null);
                mSceneView.pageView().setItems(items);
            }

            @Override
            public void onError(Exception e) {
                L.d(this, "refresh", e, "error");
                if (getActivity() == null) return;
                mSceneView.dismissRefreshing();
                Toast.makeText(getActivity(), String.valueOf(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadMore() {
        if (mBook.hasMore()) {
            mSceneView.showLoadingMore();
            L.d(this, "loadMore", "start", mBook.nextPageIndex(), mBook.pageSize());
            mRemoteApi.getFeedStream(mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<List<Item>>() {
                @Override
                public void onSuccess(List<Item> items) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    mSceneView.dismissLoadingMore();
                    mBook.addPage(new Page<>(items, mBook.nextPageIndex(), Page.TOTAL_INFINITY));

                    VideoItem.tag(VideoItem.findVideoItems(items), PlayScene.map(PlayScene.SCENE_SHORT), null);
                    mSceneView.pageView().appendItems(items);
                }

                @Override
                public void onError(Exception e) {
                    L.d(this, "loadMore", "error", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    mSceneView.dismissLoadingMore();
                    Toast.makeText(getActivity(), String.valueOf(e), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            mBook.end();
            mSceneView.finishLoadingMore();
            L.d(this, "loadMore", "end");
        }
    }

    private void enterFullScreen(MediaSource mediaSource) {
        final ViewHolder viewHolder = mSceneView.pageView().getCurrentViewHolder();
        if (!(viewHolder instanceof ShortVideoItemViewHolder)) {
            return;
        }
        VideoView videoView = ((ShortVideoItemViewHolder) viewHolder).videoView;
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
        mFullScreen = true;
    }

    /**
     * Sync playback states in FullScreenFragment
     */
    private void onExitFullScreen(MediaSource mediaSource, boolean continuesPlayback) {
        mFullScreen = false;
        final ViewHolder viewHolder = mSceneView.pageView().getCurrentViewHolder();
        if (!(viewHolder instanceof ShortVideoItemViewHolder)) {
            return;
        }
        VideoView videoView = ((ShortVideoItemViewHolder) viewHolder).videoView;
        if (videoView == null) return;

        if (!MediaSource.mediaEquals(videoView.getDataSource(), mediaSource)) return;

        L.d(this, "exitFullScreen", MediaSource.dump(mediaSource), continuesPlayback);

        if (continuesPlayback) {
            final PlaybackController controller = videoView.controller();
            if (controller != null) {
                controller.preparePlayback();
            }
            final Player player = videoView.player();
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

    private String mPipSessionKey;

    private void enterPip(boolean request) {
        if (!VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP)) return;

        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) {
            return;
        }
        if (mFullScreen) return;
        if (mSceneView.pageView().isInterceptStartPlaybackOnResume()) {
            // 用户暂停视频后，不切换小窗
            return;
        }
        final ViewHolder viewHolder = mSceneView.pageView().getCurrentViewHolder();
        if (!(viewHolder instanceof ShortVideoItemViewHolder)) {
            return;
        }
        VideoView videoView = ((ShortVideoItemViewHolder) viewHolder).videoView;
        if (videoView == null) return;
        VideoItem videoItem = VideoItem.get(videoView.getDataSource());
        if (videoItem == null) return;
        if (request) {
            PipVideoController.instance().requestMainToPip(new PipVideoController.PipVideoConfig(mPipSessionKey,
                    activity,
                    videoView,
                    Collections.singletonList(videoItem),
                    0));
        } else {
            PipVideoController.instance().mainToPip(new PipVideoController.PipVideoConfig(mPipSessionKey,
                    activity,
                    videoView,
                    Collections.singletonList(videoItem),
                    0));
        }
    }

    public void dismissPip() {
        PipVideoController.instance().dismissPip();
        PipVideoController.MainVideoInfo mainVideoInfo = PipVideoController.instance().getMainVideoInfo();
        if (mainVideoInfo != null && TextUtils.equals(mainVideoInfo.sessionKey, mPipSessionKey)) {
            PipVideoController.instance().resetMainVideoInfo();
        }
    }
}