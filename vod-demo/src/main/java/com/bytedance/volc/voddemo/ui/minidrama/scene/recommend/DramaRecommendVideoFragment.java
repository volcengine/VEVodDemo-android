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
 * Create Date : 2024/9/5
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.recommend;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.data.model.ItemType;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.data.utils.ItemHelper;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoPageView;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.ad.api.Ad;
import com.bytedance.volc.voddemo.ui.ad.mock.MockShortVideoAdVideoView;
import com.bytedance.volc.voddemo.ui.minidrama.data.business.model.DramaItem;
import com.bytedance.volc.voddemo.ui.minidrama.data.mock.MockGetEpisodeRecommendMultiItems;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetEpisodeRecommendMultiItemsApi;
import com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaDetailVideoActivityResultContract;
import com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaDetailVideoActivityResultContract.DramaDetailVideoInput;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.bottom.SpeedIndicatorViewHolder;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaVideoLayer;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder.DramaEpisodeVideoViewHolder;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder.ShortVideoDrawADItemViewHolder;

import java.util.List;

public class DramaRecommendVideoFragment extends BaseFragment {
    public static final String ACTION_PLAY_MORE_CLICK = "action_play_more_click";
    private GetEpisodeRecommendMultiItemsApi mRemoteApi;
    private final Book<Item> mBook = new Book<>(10);
    private ShortVideoSceneView mSceneView;
    private SpeedIndicatorViewHolder mSpeedIndicator;
    private boolean mRegistered;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case ACTION_PLAY_MORE_CLICK: {
                    onPlayMoreCardClick();
                    break;
                }
            }
        }
    };

    public ActivityResultLauncher<DramaDetailVideoInput> mDramaDetailPageLauncher = registerForActivityResult(new DramaDetailVideoActivityResultContract(), result -> {
        if (result == null) return;
        if (result.currentDramaItem == null) return;
        if (result.currentDramaItem.currentItem == null) return;


        // 1. Find targetDramaIndex and targetDramaCurrentVideoItem first
        int targetDramaIndex = -1;
        Item targetDramaCurrentVideoItem = null;
        final Item targetDramaTargetVideoItem = result.currentDramaItem.currentItem;
        for (int i = 0; i < mSceneView.pageView().getItemCount(); i++) {
            final Item item = mSceneView.pageView().getItem(i);
            if (TextUtils.equals(EpisodeVideo.getDramaId(EpisodeVideo.get(item)),
                    EpisodeVideo.getDramaId(EpisodeVideo.get(targetDramaTargetVideoItem)))) {
                targetDramaIndex = i;
                targetDramaCurrentVideoItem = item;
                break;
            }
        }
        if (targetDramaIndex == -1 || targetDramaCurrentVideoItem == null) {
            L.d(DramaRecommendVideoFragment.this, "onActivityResult", "can't find target drama! return", DramaItem.dump(result.currentDramaItem));
            return;
        }

        // 2. Replace target drama current videoItem
        final int currentDramaIndex = mSceneView.pageView().getCurrentItem();
        if (!ItemHelper.comparator().compare(targetDramaCurrentVideoItem, targetDramaTargetVideoItem)) {
            L.d(DramaRecommendVideoFragment.this, "onActivityResult",
                    "replace target drama current item",
                    "currentDramaIndex=" + currentDramaIndex,
                    "targetDramaIndex=" + targetDramaIndex,
                    "targetDramaCurrentVideoItem=" + ItemHelper.dump(targetDramaCurrentVideoItem),
                    "targetDramaTargetVideoItem=" + ItemHelper.dump(targetDramaTargetVideoItem));
            mSceneView.pageView().stop();
            /**
             * onActivityResult current fragment lifeCycle state is STARTED, replaceItem can't start playback.
             * see {@link ShortVideoPageView#togglePlayback(int)}
             * so we need to setInterceptStartPlaybackOnResume(false), to startPlayback onResume.
             * see {@link ShortVideoPageView#onResume()}
             */
            mSceneView.pageView().replaceItem(targetDramaIndex, targetDramaTargetVideoItem);
            mSceneView.pageView().setInterceptStartPlaybackOnResume(false);
        }

        // 3. ViewPager switch to target drama
        if (targetDramaIndex != currentDramaIndex) {
            L.d(DramaRecommendVideoFragment.this, "onActivityResult", "setCurrentItem", currentDramaIndex, targetDramaIndex);
            mSceneView.pageView().stop();
            mSceneView.pageView().setCurrentItem(targetDramaIndex, false);
        }
    });

    public DramaRecommendVideoFragment() { /**/ }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new MockGetEpisodeRecommendMultiItems();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemoteApi.cancel();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_mini_drama_recommend_video_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpeedIndicator = new SpeedIndicatorViewHolder(view);
        mSpeedIndicator.showSpeedIndicator(false);

        mSceneView = view.findViewById(R.id.shortVideoSceneView);
        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.pageView().setViewHolderFactory(new RecommendDramaVideoViewHolderFactory());
        mSceneView.setOnRefreshListener(this::refresh);
        mSceneView.setOnLoadMoreListener(this::loadMore);
        refresh();
    }

    private class RecommendDramaVideoViewHolderFactory implements ViewHolder.Factory {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ItemType.ITEM_TYPE_VIDEO: {
                    final DramaEpisodeVideoViewHolder viewHolder = new DramaEpisodeVideoViewHolder(
                            new FrameLayout(parent.getContext()),
                            DramaVideoLayer.Type.RECOMMEND,
                            mSceneView.pageView(),
                            mSpeedIndicator);
                    final VideoView videoView = viewHolder.videoView;
                    final PlaybackController controller = videoView == null ? null : videoView.controller();
                    if (controller != null) {
                        controller.addPlaybackListener(new Dispatcher.EventListener() {
                            @Override
                            public void onEvent(Event event) {
                                if (event.code() == PlayerEvent.State.COMPLETED) {
                                    onDramaPlayCompleted(event);
                                }
                            }
                        });
                    }
                    return viewHolder;
                }
                case ItemType.ITEM_TYPE_AD: {
                    ShortVideoDrawADItemViewHolder viewHolder = new ShortVideoDrawADItemViewHolder(
                            new FrameLayout(parent.getContext()));
                    if (viewHolder.mockAdVideoView != null) {
                        viewHolder.mockAdVideoView.setAdVideoListener(new MockShortVideoAdVideoView.MockAdVideoListener() {
                            @Override
                            public void onAdVideoCompleted(Ad ad) {
                                onAdVideoPlayCompleted(ad);
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
    public void onResume() {
        super.onResume();
        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PLAY_MORE_CLICK);
            LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(mBroadcastReceiver, filter);
            mRegistered = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mRegistered) {
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mBroadcastReceiver);
            mRegistered = false;
        }
    }

    private void onPlayMoreCardClick() {
        final List<DramaItem> dramaItems = DramaItem.createByEpisodeVideoItems(
                ItemHelper.toItems(VideoItem.findVideoItems(mSceneView.pageView().getItems())));
        final int currentDramaIndex = DramaItem.findDramaItemPosition(dramaItems,
                mSceneView.pageView().getCurrentItemModel());
        if (currentDramaIndex < 0) return;

        final ViewHolder viewHolder = mSceneView.pageView().getCurrentViewHolder();
        VideoView videoView = null;
        if (viewHolder instanceof DramaEpisodeVideoViewHolder) {
            videoView = ((DramaEpisodeVideoViewHolder) viewHolder).videoView;
        }
        if (videoView == null) return;
        final PlaybackController controller = videoView.controller();
        boolean continuesPlayback = false;
        if (controller != null) {
            continuesPlayback = controller.player() != null;
            controller.unbindPlayer();
        }
        mDramaDetailPageLauncher.launch(new DramaDetailVideoInput(
                dramaItems,
                currentDramaIndex,
                continuesPlayback));
    }


    private void onAdVideoPlayCompleted(Ad ad) {
        playNext();
    }

    private void onDramaPlayCompleted(Event event) {
        final Item item = mSceneView.pageView().getCurrentItemModel();
        if (!(item instanceof VideoItem)) return;
        final VideoItem videoItem = (VideoItem) item;
        final EpisodeVideo episodeVideo = EpisodeVideo.get(videoItem);
        if (episodeVideo == null) return;
        if (episodeVideo.episodeInfo == null || episodeVideo.episodeInfo.dramaInfo == null) return;
        if (EpisodeVideo.isLastEpisode(episodeVideo)) {
            // play next recommend
            final Player player = event.owner(Player.class);
            if (player != null && !player.isLooping()) {
                playNext();
            }
        } else {
            final List<DramaItem> dramaItems = DramaItem.createByEpisodeVideoItems(
                    ItemHelper.toItems(VideoItem.findVideoItems(mSceneView.pageView().getItems())));
            final int currentDramaIndex = DramaItem.findDramaItemPosition(dramaItems,
                    mSceneView.pageView().getCurrentItemModel());

            if (dramaItems == null) return;
            if (currentDramaIndex < 0) return;

            final DramaItem currentDramaItem = dramaItems.get(currentDramaIndex);
            if (currentDramaItem == null) return;

            currentDramaItem.currentEpisodeNumber = EpisodeVideo.getEpisodeNumber(episodeVideo) + 1;
            currentDramaItem.currentItem = null;
            mDramaDetailPageLauncher.launch(new DramaDetailVideoInput(dramaItems, currentDramaIndex, true));
        }
    }

    private void playNext() {
        final int currentPosition = mSceneView.pageView().getCurrentItem();
        final int nextPosition = currentPosition + 1;
        final int total = mSceneView.pageView().getItemCount();
        if (nextPosition < total) {
            L.d(this, "playNext", "current", currentPosition, "next", nextPosition, "total", total);
            mSceneView.pageView().setCurrentItem(nextPosition, true);
        } else {
            L.d(this, "playNext", "current", currentPosition, "total", total, "end");
        }
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        mSceneView.showRefreshing();
        mRemoteApi.getRecommendEpisodeVideoItems(0, mBook.pageSize(), new RemoteApi.Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                L.d(this, "refresh", "success", ItemHelper.dump(items));
                if (getActivity() == null) return;

                List<VideoItem> videoItems = VideoItem.findVideoItems(items);
                VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
                VideoItem.syncProgress(videoItems, true);

                mBook.firstPage(new Page<>(items, 0, Page.TOTAL_INFINITY));
                mSceneView.dismissRefreshing();
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
            mRemoteApi.getRecommendEpisodeVideoItems(mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<List<Item>>() {
                @Override
                public void onSuccess(List<Item> items) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex(), ItemHelper.dump(items));
                    if (getActivity() == null) return;

                    List<VideoItem> videoItems = VideoItem.findVideoItems(items);
                    VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
                    VideoItem.syncProgress(videoItems, true);

                    mBook.addPage(new Page<>(items, mBook.nextPageIndex(), Page.TOTAL_INFINITY));
                    mSceneView.dismissLoadingMore();
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
}
