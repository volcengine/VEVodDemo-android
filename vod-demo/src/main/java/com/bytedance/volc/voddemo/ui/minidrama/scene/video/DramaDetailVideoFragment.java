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

package com.bytedance.volc.voddemo.ui.minidrama.scene.video;


import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.EXTRA_INPUT;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.EXTRA_OUTPUT;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.layer.DramaGestureLayer.ACTION_DRAMA_GESTURE_LAYER_DISMISS_SPEED;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.layer.DramaGestureLayer.ACTION_DRAMA_GESTURE_LAYER_SHOW_SPEED;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.layer.DramaVideoLayer.ACTION_DRAMA_VIDEO_LAYER_INTERCEPT_START_PLAYBACK;
import static com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodePayDialogFragment.ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED;
import static com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodeSelectDialogFragment.ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK;
import static com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodeSelectDialogFragment.EXTRA_EPISODE_INDEX;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.data.mock.MockGetDramaDetail;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramaDetailApi;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.DramaDetailVideoInput;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.DramaDetailVideoOutput;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.bottom.EpisodeSelectorViewHolder;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.bottom.SpeedIndicatorViewHolder;
import com.bytedance.volc.voddemo.ui.minidrama.utils.DramaPayUtils;
import com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodePayDialogFragment;
import com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodeSelectDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class DramaDetailVideoFragment extends BaseFragment {
    public static final String TAG = "DramaDetailVideoFragment";
    public static final int RESULT_CODE_EXIT = 100;
    private VideoItem mVideoItem;
    private DramaInfo mDrama;
    private int mEpisodeNumber;
    private boolean mContinuesPlayback;
    private VideoItem mLastUnlockedVideoItem;
    private GetDramaDetailApi mRemoteApi;
    private String mAccount;
    private ShortVideoSceneView mSceneView;
    private SpeedIndicatorViewHolder mSpeedIndicator;
    private DramaEpisodeSelectDialogFragment mSelectDialogFragment;
    private DramaEpisodePayDialogFragment mPayDialogFragment;
    private boolean mRegistered;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;

            switch (action) {
                case ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK: {
                    final int episodeIndex = intent.getIntExtra(EXTRA_EPISODE_INDEX, 0);
                    if (episodeIndex >= 0) {
                        onSelectDramaEpisodeItemClicked(episodeIndex);
                    }
                    break;
                }
                case ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED: {
                    final EpisodeVideo unlocked = (EpisodeVideo) intent.getSerializableExtra(DramaEpisodePayDialogFragment.EXTRA_EPISODE_VIDEO);
                    if (unlocked != null) {
                        VideoItem videoItem = EpisodeVideo.toVideoItem(unlocked);
                        int position = EpisodeVideo.episodeNumber2VideoItemIndex(mSceneView.pageView().getItems(), EpisodeVideo.getEpisodeNumber(unlocked));
                        if (position >= 0) {
                            mSceneView.pageView().replaceItem(position, videoItem);
                        }
                    }
                    break;
                }
                case ACTION_DRAMA_VIDEO_LAYER_INTERCEPT_START_PLAYBACK:
                    showEpisodePayDialog(EpisodeVideo.get(mSceneView.pageView().getCurrentItemModel()));
                    break;
                case ACTION_DRAMA_GESTURE_LAYER_SHOW_SPEED: {
                    mSpeedIndicator.showSpeedIndicator(true);
                    break;
                }
                case ACTION_DRAMA_GESTURE_LAYER_DISMISS_SPEED: {
                    mSpeedIndicator.showSpeedIndicator(false);
                    break;
                }
            }
        }
    };

    public DramaDetailVideoFragment() {
        // Required empty public constructor
    }

    @Override
    public boolean onBackPressed() {
        VideoItem currentItem = null;
        boolean continuesPlayback = false;

        final VideoView videoView = mSceneView.pageView().getCurrentItemVideoView();
        if (videoView != null) {
            final VideoLayerHost layerHost = videoView.layerHost();
            if (layerHost != null && layerHost.onBackPressed()) {
                return true;
            }
            currentItem = VideoItem.get(videoView.getDataSource());
            if (DramaPayUtils.isLocked(currentItem)) {
                if (mLastUnlockedVideoItem != null) {
                    currentItem = mLastUnlockedVideoItem;
                }
            } else {
                if (mContinuesPlayback) {
                    continuesPlayback = true;
                    final PlaybackController controller = videoView.controller();
                    if (controller != null) {
                        controller.unbindPlayer();
                    }
                }
            }
        }
        L.d(this, "onBackPressed", "currentItem", VideoItem.dump(currentItem));
        Intent intent = new Intent();
        intent.putExtra(EXTRA_OUTPUT, new DramaDetailVideoOutput(
                mDrama,
                mVideoItem,
                currentItem,
                mSceneView.pageView().getItems(),
                continuesPlayback
        ));
        requireActivity().setResult(RESULT_CODE_EXIT, intent);
        return super.onBackPressed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRemoteApi = new MockGetDramaDetail();
        mAccount = VideoSettings.stringValue(VideoSettings.DRAMA_VIDEO_SCENE_ACCOUNT_ID);

        final DramaDetailVideoInput input = parseInput();

        if (input != null) {
            mVideoItem = input.currenVideoItem;
            mDrama = input.drama;
            if (mDrama == null && mVideoItem != null) {
                final EpisodeVideo episode = EpisodeVideo.get(mVideoItem);
                if (episode != null && episode.episodeInfo != null) {
                    mDrama = episode.episodeInfo.dramaInfo;
                }
            }
            mEpisodeNumber = input.episodeNumber;
            mContinuesPlayback = input.continuesPlayback;
        }

        if (mDrama == null || TextUtils.isEmpty(mDrama.dramaId)) {
            requireActivity().onBackPressed();
        }
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
        mSceneView = view.findViewById(R.id.shortVideoSceneView);
        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.setRefreshEnabled(false);
        mSceneView.setLoadMoreEnabled(false);
        mSceneView.pageView().setVideoViewFactory(new DramaVideoViewFactory(DramaVideoViewFactory.Type.DETAIL));
        mSceneView.pageView().addPlaybackListener(event -> {
            if (event.code() == PlayerEvent.State.COMPLETED) {
                onPlayerStateCompleted(event);
            }
        });
        mSceneView.pageView().viewPager().registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                VideoItem videoItem = mSceneView.pageView().getItem(position);
                EpisodeVideo episodeVideo = EpisodeVideo.get(videoItem);
                setActionBarTitle(String.format(getString(R.string.vevod_mini_drama_detail_video_episode_number_desc), EpisodeVideo.getEpisodeNumber(episodeVideo)));
                setEpisodeSelectDialogPlayingIndex(EpisodeVideo.getEpisodeNumber(episodeVideo) - 1);
                if (DramaPayUtils.isLocked(episodeVideo)) {
                    showEpisodePayDialog(episodeVideo);
                } else {
                    mLastUnlockedVideoItem = videoItem;
                }
            }
        });

        EpisodeSelectorViewHolder selector = new EpisodeSelectorViewHolder(view);
        selector.bind(mDrama);
        selector.mSelectEpisodeView.setOnClickListener(v -> {
            VideoItem currentItem = mSceneView.pageView().getCurrentItemModel();
            EpisodeVideo episodeVideo = EpisodeVideo.get(currentItem);
            showEpisodeSelectDialog(mSceneView.pageView().getItems(), EpisodeVideo.getEpisodeNumber(episodeVideo) - 1);
        });

        mSpeedIndicator = new SpeedIndicatorViewHolder(view);

        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK);
            filter.addAction(ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED);
            filter.addAction(ACTION_DRAMA_GESTURE_LAYER_SHOW_SPEED);
            filter.addAction(ACTION_DRAMA_GESTURE_LAYER_DISMISS_SPEED);
            filter.addAction(ACTION_DRAMA_VIDEO_LAYER_INTERCEPT_START_PLAYBACK);
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
        if (mVideoItem == null) {
            if (mDrama != null) {
                load();
            }
        } else {
            final List<VideoItem> items = new ArrayList<>();
            items.add(mVideoItem);
            setItems(items);
            load();
        }
    }

    private void setItems(List<VideoItem> items) {
        VideoItem.tag(items, PlayScene.map(PlayScene.SCENE_SHORT), null);
        VideoItem.syncProgress(items, true);
        mSceneView.pageView().setItems(items);
    }

    private void onPlayerStateCompleted(Event event) {
        final VideoItem videoItem = mSceneView.pageView().getCurrentItemModel();
        if (EpisodeVideo.isLastEpisode(EpisodeVideo.get(videoItem))) {
            // TODO goto next recommend video detail page
        } else {
            // play next recommend
            final Player player = event.owner(Player.class);
            if (player != null && !player.isLooping()) {
                final int currentPosition = mSceneView.pageView().getCurrentItem();
                final int nextPosition = currentPosition + 1;
                if (nextPosition < mSceneView.pageView().getItemCount()) {
                    mSceneView.pageView().setCurrentItem(nextPosition, true);
                }
            }
        }
    }

    private void setEpisodeSelectDialogPlayingIndex(int playingIndex) {
        if (mSelectDialogFragment != null && mSelectDialogFragment.isShowing()) {
            mSelectDialogFragment.setPlayingIndex(playingIndex);
        }
    }

    private void onSelectDramaEpisodeItemClicked(int position) {
        mSceneView.pageView().setCurrentItem(position, false);
    }

    private void setActionBarTitle(String title) {
        if (!(getActivity() instanceof AppCompatActivity)) return;

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    private void showEpisodeSelectDialog(List<VideoItem> videoItems, int playingIndex) {
        if (mSelectDialogFragment != null && mSelectDialogFragment.isShowing()) {
            return;
        }
        L.d(this, "showEpisodeSelectDialog");
        mSelectDialogFragment = DramaEpisodeSelectDialogFragment.newInstance((ArrayList<VideoItem>) videoItems, playingIndex);
        mSelectDialogFragment.showNow(getChildFragmentManager(), DramaEpisodeSelectDialogFragment.class.getName());
    }

    private void showEpisodePayDialog(EpisodeVideo episodeVideo) {
        if (episodeVideo == null) return;

        if (mPayDialogFragment != null && mPayDialogFragment.isShowing()) {
            return;
        }
        L.d(this, "showEpisodePayDialog");
        mPayDialogFragment = DramaEpisodePayDialogFragment.newInstance(episodeVideo);
        mPayDialogFragment.setCancelable(false);
        mPayDialogFragment.showNow(getChildFragmentManager(), DramaEpisodePayDialogFragment.class.getName());
    }

    private void setCurrentItemByEpisodeNumber(int episodeNumber) {
        List<VideoItem> videoItems = mSceneView.pageView().getItems();
        int position = EpisodeVideo.episodeNumber2VideoItemIndex(videoItems, episodeNumber);
        if (0 <= position && position < videoItems.size()) {
            mSceneView.pageView().setCurrentItem(position, false);
        }
    }


    private void load() {
        L.d(this, "load", "start");
        mRemoteApi.getDramaDetail(mAccount, 0, -1, mDrama.dramaId, null, new RemoteApi.Callback<List<VideoItem>>() {
            @Override
            public void onSuccess(List<VideoItem> items) {
                L.d(this, "load", "success", items);
                if (getActivity() == null) return;
                setItems(items);
                if (mVideoItem == null && mEpisodeNumber >= 1) {
                    setCurrentItemByEpisodeNumber(mEpisodeNumber);
                }
            }

            @Override
            public void onError(Exception e) {
                L.d(this, "load", "error", e);
                if (getActivity() == null) return;
                Toast.makeText(getActivity(), String.valueOf(e), Toast.LENGTH_LONG).show();
            }
        });
    }
}
