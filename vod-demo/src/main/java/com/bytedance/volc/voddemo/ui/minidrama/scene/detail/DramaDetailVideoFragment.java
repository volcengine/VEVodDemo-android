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

package com.bytedance.volc.voddemo.ui.minidrama.scene.detail;


import static com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaDetailVideoActivityResultContract.EXTRA_INPUT;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaDetailVideoActivityResultContract.EXTRA_OUTPUT;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaEpisodePayDialogFragment.ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaEpisodeSelectDialogFragment.ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaEpisodeSelectDialogFragment.EXTRA_ITEM;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaVideoLayer.ACTION_DRAMA_VIDEO_LAYER_SHOW_PAY_DIALOG;
import static com.bytedance.volc.voddemo.ui.video.scene.pipvideo.PipVideoController.PipVideoConfig;

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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.ActionSetSpeed;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.CollectionUtils;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.DrawADItem;
import com.bytedance.volc.vod.scenekit.data.model.ItemType;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.utils.ItemHelper;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.vod.scenekit.ui.video.viewholder.ViewHolderAction;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.ad.api.Ad;
import com.bytedance.volc.voddemo.ui.ad.mock.MockShortVideoAdVideoView;
import com.bytedance.volc.voddemo.ui.minidrama.data.business.model.DramaItem;
import com.bytedance.volc.voddemo.ui.minidrama.data.mock.MockGetDramaDetailMultiItems;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramaDetailMultiItemsApi;
import com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaDetailVideoActivityResultContract.DramaDetailVideoInput;
import com.bytedance.volc.voddemo.ui.minidrama.scene.detail.DramaDetailVideoActivityResultContract.DramaDetailVideoOutput;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.bottom.EpisodeSelectorViewHolder;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.bottom.SpeedIndicatorViewHolder;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaVideoLayer;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder.DramaEpisodeVideoViewHolder;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder.ShortVideoDrawADItemViewHolder;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.PipVideoController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DramaDetailVideoFragment extends BaseFragment {
    public static final String TAG = "DramaDetailVideoFragment";
    public static final int RESULT_CODE_EXIT = 100;
    private List<DramaItem> mDramaItems;
    private int mInitDramaIndex;
    private int mCurrentDramaIndex;
    private boolean mContinuesPlayback;
    private GetDramaDetailMultiItemsApi mRemoteApi;
    private ShortVideoSceneView mSceneView;
    private EpisodeSelectorViewHolder mEpisodeSelector;
    private SpeedIndicatorViewHolder mSpeedIndicator;
    private MenuItem mPipActionMenuItem;
    private DramaEpisodeSelectDialogFragment mSelectDialogFragment;
    private DramaEpisodePayDialogFragment mPayDialogFragment;

    public DramaDetailVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public boolean onBackPressed() {
        if (mSceneView.pageView().onBackPressed()) {
            return true;
        }
        if (mDramaItems == null) {
            return super.onBackPressed();
        }
        final DramaItem currentDramaItem = mDramaItems.get(mCurrentDramaIndex);
        if (currentDramaItem == null) {
            return super.onBackPressed();
        }

        if (currentDramaItem.currentItem == null) {
            return super.onBackPressed();
        }
        boolean continuesPlayback = false;
        final ViewHolder viewHolder = mSceneView.pageView().getCurrentViewHolder();
        if (viewHolder == null) {
            return super.onBackPressed();
        }
        if (viewHolder instanceof DramaEpisodeVideoViewHolder) {
            if (EpisodeVideo.isLocked((VideoItem) currentDramaItem.currentItem)) {
                if (currentDramaItem.lastUnlockedItem != null) {
                    currentDramaItem.currentItem = currentDramaItem.lastUnlockedItem;
                } else {
                    currentDramaItem.currentItem = null;
                }
            } else {
                if (mContinuesPlayback) {
                    continuesPlayback = true;
                    final VideoView videoView = ((DramaEpisodeVideoViewHolder) viewHolder).videoView;
                    final PlaybackController controller = videoView != null ? videoView.controller() : null;
                    if (controller != null) {
                        controller.unbindPlayer();
                    }
                } else {
                    // For some device swipe back gesture break the Activity/Fragment lifecycle. Stop here to fix
                    // player release sequence bug.
                    viewHolder.executeAction(ViewHolderAction.ACTION_STOP);
                }
            }
        } else if (viewHolder instanceof ShortVideoDrawADItemViewHolder) {
            if (currentDramaItem.lastUnlockedItem != null) {
                currentDramaItem.currentItem = currentDramaItem.lastUnlockedItem;
            }
        }
        L.d(this, "onBackPressed", "continuesPlayback", continuesPlayback,
                DramaItem.dump(currentDramaItem), ItemHelper.dump(currentDramaItem.currentItem));
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OUTPUT, new DramaDetailVideoOutput(currentDramaItem, continuesPlayback));
        requireActivity().setResult(RESULT_CODE_EXIT, intent);
        return super.onBackPressed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mRemoteApi = new MockGetDramaDetailMultiItems();

        final DramaDetailVideoInput input = parseInput();

        if (!checkInput(input) || input == null) {
            requireActivity().onBackPressed();
            return;
        }

        mDramaItems = input.dramaItems;
        mInitDramaIndex = input.currentDramaIndex;
        mCurrentDramaIndex = input.currentDramaIndex;
        mContinuesPlayback = input.continuesPlayback;

        mPipSessionKey = UUID.randomUUID().toString();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP)) {
            inflater.inflate(R.menu.vevod_menu_short_video, menu);
            mPipActionMenuItem = menu.findItem(R.id.menu_item_pip_action);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_item_pip_action) {
            enterPip(true);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @return false for check not pass, true for check pass.
     */
    boolean checkInput(DramaDetailVideoInput input) {
        if (input == null) {
            L.w(this, "checkInput", "input = null!");
            return false;
        }
        if (input.dramaItems == null || input.dramaItems.isEmpty()) {
            L.w(this, "checkInput", "input.dramaItems is Empty!");
            return false;
        }
        if (input.currentDramaIndex < 0 || input.currentDramaIndex >= input.dramaItems.size()) {
            L.w(this, "checkInput", "input.currentDramaIndex is not valid!" + input.currentDramaIndex);
            return false;
        }
        DramaItem initDramaItem = input.dramaItems.get(input.currentDramaIndex);
        if (initDramaItem == null) {
            L.w(this, "checkInput", "dramaItem == null");
            return false;
        }
        if (initDramaItem.dramaInfo == null) {
            L.w(this, "checkInput", "initDramaItem.dramaInfo == null");
            return false;
        }
        if (initDramaItem.currentItem == null && initDramaItem.currentEpisodeNumber < 1) {
            L.w(this, "checkInput", "currentItem=null", "currentEpisodeNumber=" + initDramaItem.currentEpisodeNumber);
            return false;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemoteApi.cancel();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_mini_drama_detail_video_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSpeedIndicator = new SpeedIndicatorViewHolder(view);
        mSpeedIndicator.showSpeedIndicator(false);

        mSceneView = view.findViewById(R.id.shortVideoSceneView);
        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.pageView().setViewHolderFactory(new DetailDramaVideoViewHolderFactory());
        mSceneView.setRefreshEnabled(false);
        mSceneView.setLoadMoreEnabled(true);
        mSceneView.setOnLoadMoreListener(this::load);
        mSceneView.pageView().viewPager().registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                L.d(this, "onPageSelected", position);
                if (mDramaItems == null) return;

                final Item item = mSceneView.pageView().getItem(position);
                if (item instanceof DrawADItem) {
                    setActionBarTitle("");
                } else if (item instanceof VideoItem) {
                    final EpisodeVideo episode = EpisodeVideo.get(item);
                    final String dramaId = EpisodeVideo.getDramaId(episode);
                    DramaItem currentDrama = mDramaItems.get(mCurrentDramaIndex);
                    if (currentDrama == null) {
                        return;
                    }
                    if (TextUtils.equals(currentDrama.dramaInfo.dramaId, dramaId)) {
                        if (currentDrama.currentItem == null || !ItemHelper.comparator().compare(currentDrama.currentItem, item)) {
                            currentDrama.currentItem = item;
                            onDramaEpisodeChanged(currentDrama);
                            return;
                        }
                    }

                    for (int i = 0; i < mDramaItems.size(); i++) {
                        final DramaItem dramaItem = mDramaItems.get(i);
                        if (dramaItem.dramaInfo == null) continue; // TODO

                        if (TextUtils.equals(dramaItem.dramaInfo.dramaId, dramaId)) {
                            if (mCurrentDramaIndex != i) {
                                mCurrentDramaIndex = i;
                                currentDrama = dramaItem;
                                currentDrama.currentItem = item;
                                onDramaEpisodeChanged(currentDrama);
                                return;
                            }
                        }
                    }
                }
            }
        });

        mEpisodeSelector = new EpisodeSelectorViewHolder(view);
        mEpisodeSelector.mSelectEpisodeView.setOnClickListener(v -> {
            if (mDramaItems == null) return;
            DramaItem dramaItem = mDramaItems.get(mCurrentDramaIndex);
            showEpisodeSelectDialog(dramaItem);
        });
        initData();
    }

    private class DetailDramaVideoViewHolderFactory implements ViewHolder.Factory {
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            switch (viewType) {
                case ItemType.ITEM_TYPE_VIDEO: {
                    final DramaEpisodeVideoViewHolder viewHolder = new DramaEpisodeVideoViewHolder(
                            new FrameLayout(parent.getContext()),
                            DramaVideoLayer.Type.DETAIL,
                            mSceneView.pageView(),
                            mSpeedIndicator);
                    final VideoView videoView = viewHolder.videoView;
                    final PlaybackController controller = videoView == null ? null : videoView.controller();
                    if (controller != null) {
                        controller.addPlaybackListener(new Dispatcher.EventListener() {
                            @Override
                            public void onEvent(Event event) {
                                if (event.code() == PlayerEvent.State.COMPLETED) {
                                    onPlayerStateCompleted(event);
                                } else if (event.code() == PlayerEvent.Action.SET_SPEED) {
                                    ActionSetSpeed e = event.cast(ActionSetSpeed.class);
                                    if (mPipActionMenuItem != null) {
                                        mPipActionMenuItem.setVisible(e.speed == 1);
                                    }
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

    private void onDramaEpisodeChanged(DramaItem dramaItem) {
        if (dramaItem == null) return;
        if (dramaItem.currentItem == null) return;
        if (!(dramaItem.currentItem instanceof VideoItem)) return;

        L.d(this, "onDramaEpisodeChanged", EpisodeVideo.dump((EpisodeVideo) EpisodeVideo.get(dramaItem.currentItem)));
        final EpisodeVideo episodeVideo = EpisodeVideo.get(dramaItem.currentItem);
        setActionBarTitle(String.format(getString(R.string.vevod_mini_drama_detail_video_episode_number_desc), EpisodeVideo.getEpisodeNumber(episodeVideo)));
        mEpisodeSelector.bind(dramaItem.dramaInfo);
        syncEpisodeSelectDialog(dramaItem);

        if (EpisodeVideo.isLocked(episodeVideo)) {
            showEpisodePayDialog(episodeVideo);
        } else {
            dramaItem.lastUnlockedItem = dramaItem.currentItem;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver();
        restoreFromPip();
    }


    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver();
    }

    @Override
    public void onStop() {
        super.onStop();
        enterPip(false);
    }

    @Nullable
    private DramaDetailVideoInput parseInput() {
        Intent intent = requireActivity().getIntent();
        DramaDetailVideoInput input = (DramaDetailVideoInput) intent.getSerializableExtra(EXTRA_INPUT);
        if (input == null && getArguments() != null) {
            input = (DramaDetailVideoInput) getArguments().getSerializable(EXTRA_INPUT);
        }
        return input;
    }

    private void initData() {
        if (mDramaItems == null) return;
        DramaItem initDramaItem = mDramaItems.get(mInitDramaIndex);
        if (initDramaItem == null) return;

        if (initDramaItem.currentItem == null) {
            if (initDramaItem.dramaInfo != null) {
                load(initDramaItem);
            }
        } else {
            final List<Item> items = new ArrayList<>();
            items.add(initDramaItem.currentItem);
            setItems(items, false);
            onDramaEpisodeChanged(initDramaItem);
            load(initDramaItem);
        }
    }

    private void setItems(List<Item> items, boolean isPlay) {
        List<VideoItem> videoItems = VideoItem.findVideoItems(items);
        VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
        VideoItem.syncProgress(videoItems, true);
        mSceneView.pageView().setItems(items, isPlay);
    }

    private void appendItems(List<Item> items) {
        List<VideoItem> videoItems = VideoItem.findVideoItems(items);
        VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
        VideoItem.syncProgress(videoItems, true);
        mSceneView.pageView().appendItems(items);
    }

    private void onEpisodePayResultUnlocked(EpisodeVideo unlockedEpisode) {
        final VideoItem unlockedVideoItem = EpisodeVideo.toVideoItem(unlockedEpisode);
        VideoItem.tag(unlockedVideoItem, PlayScene.map(PlayScene.SCENE_SHORT), null);
        VideoItem.syncProgress(unlockedVideoItem, true);

        if (mDramaItems == null) return;
        final DramaItem dramaItem = mDramaItems.get(mCurrentDramaIndex);
        if (dramaItem == null) return;
        if (dramaItem.dramaInfo == null) return;
        if (dramaItem.episodeVideoItems == null) return;

        if (!TextUtils.equals(dramaItem.dramaInfo.dramaId, EpisodeVideo.getDramaId(unlockedEpisode))) {
            return;
        }

        // 1. replace unlocked videoItem in dramaItem.episodeVideoItems
        for (int i = 0; i < dramaItem.episodeVideoItems.size(); i++) {
            final Item item = dramaItem.episodeVideoItems.get(i);
            if (ItemHelper.comparator().compare(item, unlockedVideoItem)) {
                dramaItem.episodeVideoItems.set(i, unlockedVideoItem);
                break;
            }
        }

        // 2. refresh dramaItem.currentItem and dramaItem.lastUnlockedItem
        if (ItemHelper.comparator().compare(dramaItem.currentItem, unlockedVideoItem)) {
            dramaItem.currentItem = unlockedVideoItem;
            dramaItem.lastUnlockedItem = unlockedVideoItem;
        }

        // 3. replace item in adapter
        final int position = mSceneView.pageView().findItemPosition(unlockedVideoItem, ItemHelper.comparator());
        if (position >= 0) {
            mSceneView.pageView().replaceItem(position, unlockedVideoItem);
            dismissEpisodePayDialog();
        }
    }

    private void onPlayerStateCompleted(Event event) {
        final Player player = event.owner(Player.class);
        if (player != null && !player.isLooping()) {
            playNext();
        }
    }

    private void onAdVideoPlayCompleted(Ad ad) {
        // play next recommend
        playNext();
    }

    private void playNext() {
        final int currentPosition = mSceneView.pageView().getCurrentItem();
        final int nextPosition = currentPosition + 1;
        if (nextPosition < mSceneView.pageView().getItemCount()) {
            final Item videoItem = mSceneView.pageView().getCurrentItemModel();
            final Item nextItem = mSceneView.pageView().getItem(nextPosition);
            mSceneView.pageView().setCurrentItem(nextPosition, true);

            if (EpisodeVideo.isLastEpisode(EpisodeVideo.get(videoItem))) {
                Toast.makeText(requireActivity(), getString(R.string.vevod_mini_drama_play_next_drama_hint) + EpisodeVideo.getDramaTitle(EpisodeVideo.get(nextItem)), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void onSelectDramaEpisodeItemClicked(Item item) {
        final int position = mSceneView.pageView().findItemPosition(item, ItemHelper.comparator());
        if (position >= 0) {
            mSceneView.pageView().setCurrentItem(position, false);
        }
    }

    private VideoView getCurrentVideoView() {
        final ViewHolder viewHolder = mSceneView.pageView().getCurrentViewHolder();
        if (viewHolder instanceof DramaEpisodeVideoViewHolder) {
            return ((DramaEpisodeVideoViewHolder) viewHolder).videoView;
        }
        return null;
    }

    private void setActionBarTitle(String title) {
        if (!(getActivity() instanceof AppCompatActivity)) return;

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private void showEpisodeSelectDialog(DramaItem dramaItem) {
        if (mSelectDialogFragment != null && mSelectDialogFragment.isShowing()) {
            return;
        }
        L.d(this, "showEpisodeSelectDialog");
        mSelectDialogFragment = DramaEpisodeSelectDialogFragment.newInstance(dramaItem);
        mSelectDialogFragment.showNow(getChildFragmentManager(), DramaEpisodeSelectDialogFragment.class.getName());
    }

    private void syncEpisodeSelectDialog(DramaItem dramaItem) {
        if (mSelectDialogFragment != null && mSelectDialogFragment.isShowing()) {
            mSelectDialogFragment.setDramaItem(dramaItem);
        }
    }

    private void showEpisodePayDialog(EpisodeVideo episodeVideo) {
        if (episodeVideo == null) return;

        if (mPayDialogFragment != null && mPayDialogFragment.isShowing()) {
            return;
        }
        L.d(this, "showEpisodePayDialog");
        mPayDialogFragment = DramaEpisodePayDialogFragment.newInstance(episodeVideo);
        mPayDialogFragment.setCancelable(false);
        // using showNow to make sure BaseDialogFragment#isShowing is sync
        mPayDialogFragment.showNow(getChildFragmentManager(), DramaEpisodePayDialogFragment.class.getName());
    }

    private void dismissEpisodePayDialog() {
        if (mPayDialogFragment != null && mPayDialogFragment.isShowing()) {
            mPayDialogFragment.dismiss();
        }
    }

    private void setCurrentItemByEpisodeNumber(int episodeNumber) {
        final int position = EpisodeVideo.episodeNumber2VideoItemIndex(mSceneView.pageView().getItems(), episodeNumber);
        mSceneView.pageView().setCurrentItem(position, false);
    }

    private void load() {
        if (mSceneView.isLoadingMore()) {
            return;
        }
        if (mDramaItems == null) return;

        DramaItem dramaItem = null;
        int index = mCurrentDramaIndex;
        while (index < mDramaItems.size()) {
            final DramaItem item = mDramaItems.get(index);
            if (item == null) {
                return;
            }
            if (item.episodesAllLoaded) {
                index++;
            } else {
                dramaItem = item;
                break;
            }
        }
        if (dramaItem == null) {
            mSceneView.finishLoadingMore();
            L.d(this, "load", "end");
            return;
        }
        load(dramaItem);
    }

    private void load(@NonNull DramaItem dramaItem) {
        mSceneView.showLoadingMore();
        L.d(this, "load", "start", DramaItem.dump(dramaItem));
        mRemoteApi.getDramaDetail(0, -1, dramaItem.dramaInfo.dramaId, null, new RemoteApi.Callback<List<Item>>() {
            @Override
            public void onSuccess(List<Item> items) {
                L.d(this, "load", "success", DramaItem.dump(dramaItem), ItemHelper.dump(items));
                if (getActivity() == null) return;
                if (mDramaItems == null) return;

                mSceneView.dismissLoadingMore();
                dramaItem.episodeVideoItems = items;
                dramaItem.episodesAllLoaded = true;
                final DramaItem initDrama = mDramaItems.get(mInitDramaIndex);
                if (dramaItem == initDrama) {
                    // isPlay set "false" at update list, startPlayback will be triggered in onPageSelected adapter update complete
                    setItems(items, false);
                    if (dramaItem.currentEpisodeNumber >= 1) {
                        // currentEpisodeNumber >= 1 in blew scene:
                        // 1. DramaTheaterFragment -> DramaDetailVideoFragment
                        // 2. DramaRecommendVideoFragment(play complete auto jump into) -> DramaDetailVideoFragment
                        setCurrentItemByEpisodeNumber(dramaItem.currentEpisodeNumber);
                    }
                } else {
                    appendItems(items);
                }
            }

            @Override
            public void onError(Exception e) {
                L.e(this, "load", e, "error", DramaItem.dump(dramaItem));
                if (getActivity() == null) return;
                mSceneView.dismissLoadingMore();
                Toast.makeText(getActivity(), String.valueOf(e), Toast.LENGTH_LONG).show();
            }
        });
    }


    private boolean mRegistered;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) return;
            switch (action) {
                case ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK: {
                    final Item item = (Item) intent.getSerializableExtra(EXTRA_ITEM);
                    if (item != null) {
                        onSelectDramaEpisodeItemClicked(item);
                    }
                    break;
                }
                case ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED: {
                    final EpisodeVideo unlocked = (EpisodeVideo) intent.getSerializableExtra(DramaEpisodePayDialogFragment.EXTRA_EPISODE_VIDEO);
                    if (unlocked != null) {
                        onEpisodePayResultUnlocked(unlocked);
                    }
                    break;
                }
                case ACTION_DRAMA_VIDEO_LAYER_SHOW_PAY_DIALOG:
                    Item item = mSceneView.pageView().getCurrentItemModel();
                    if (!(item instanceof VideoItem)) return;
                    final VideoItem videoItem = (VideoItem) item;
                    showEpisodePayDialog(EpisodeVideo.get(videoItem));
                    break;
            }
        }
    };

    private void registerReceiver() {
        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK);
            filter.addAction(ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED);
            filter.addAction(ACTION_DRAMA_VIDEO_LAYER_SHOW_PAY_DIALOG);
            LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(mBroadcastReceiver, filter);
            mRegistered = true;
        }
    }

    private void unregisterReceiver() {
        if (mRegistered) {
            LocalBroadcastManager.getInstance(requireActivity()).unregisterReceiver(mBroadcastReceiver);
            mRegistered = false;
        }
    }

    private String mPipSessionKey;

    private void enterPip(boolean request) {
        if (!VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP)) return;

        final Activity activity = getActivity();
        if (activity == null || activity.isFinishing()) return;

        if (mSceneView.pageView().isInterceptStartPlaybackOnResume()) {
            // 用户暂停视频后，不切换小窗
            return;
        }

        if (mDramaItems == null) return;
        if (mCurrentDramaIndex < 0 || mCurrentDramaIndex >= mDramaItems.size()) return;

        final DramaItem dramaItem = mDramaItems.get(mCurrentDramaIndex);
        if (dramaItem == null) return;
        if (CollectionUtils.isEmpty(dramaItem.episodeVideoItems)) return;
        if (!(dramaItem.currentItem instanceof VideoItem)) return;

        final VideoView videoView = getCurrentVideoView();
        final List<VideoItem> videoItems = VideoItem.findNotEmptyVideoItems(dramaItem.episodeVideoItems);

        if (videoItems == null || videoItems.isEmpty()) return;

        int playIndex = -1;
        for (int i = 0; i < videoItems.size(); i++) {
            VideoItem item = videoItems.get(i);
            if (VideoItem.itemEquals(item, (VideoItem) dramaItem.currentItem)) {
                playIndex = i;
            }
        }
        if (playIndex < 0) return;

        if (request) {
            PipVideoController.instance().requestMainToPip(new PipVideoConfig(mPipSessionKey,
                    activity,
                    videoView,
                    videoItems,
                    playIndex));
        } else {
            PipVideoController.instance().mainToPip(new PipVideoConfig(mPipSessionKey,
                    activity,
                    videoView,
                    videoItems,
                    playIndex));
        }
    }

    private void restoreFromPip() {
        PipVideoController.instance().dismissPip();
        PipVideoController.MainVideoInfo mainVideoInfo = PipVideoController.instance().getMainVideoInfo();
        if (mainVideoInfo != null && TextUtils.equals(mainVideoInfo.sessionKey, mPipSessionKey)) {
            PipVideoController.instance().resetMainVideoInfo();
            VideoItem videoItem = PipVideoController.instance().getCurrentVideoItem();
            if (videoItem != null) {
                int position = mSceneView.pageView().findItemPosition(videoItem, ItemHelper.comparator());
                if (position != mSceneView.pageView().getCurrentItem()) {
                    mSceneView.pageView().setCurrentItem(position, false);
                }
            }
        }
    }
}
