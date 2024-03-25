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


import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.*;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.EXTRA_OUTPUT;
import static com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodeSelectDialogFragment.ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK;
import static com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodeSelectDialogFragment.EXTRA_EPISODE_INDEX;
import static com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodeSelectDialogFragment.newInstance;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;
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
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.GetDramaDetail;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetDramaDetailApi;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.DramaDetailVideoInput;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.DramaDetailVideoOutput;
import com.bytedance.volc.voddemo.ui.minidrama.widgets.DramaEpisodeSelectDialogFragment;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class DramaDetailVideoFragment extends BaseFragment {
    public static final String TAG = "DramaDetailVideoFragment";
    public static final int RESULT_CODE_EXIT = 100;
    private VideoItem mVideoItem;
    private DramaInfo mDrama;
    private int mEpisodeNumber;
    private boolean mContinuesPlayback;
    private GetDramaDetailApi mRemoteApi;
    private String mAccount;
    private final Book<VideoItem> mBook = new Book<>(10);
    private ShortVideoSceneView mSceneView;
    private View mSelectEpisodeView;
    private TextView mSelectEpisodeDesc;
    private DramaEpisodeSelectDialogFragment mDialogFragment;

    private boolean mRegistered;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.equals(action, ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK)) {
                final int episodeIndex = intent.getIntExtra(EXTRA_EPISODE_INDEX, 0);
                if (episodeIndex >= 0) {
                    onSelectDramaEpisodeItemClicked(episodeIndex);
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

        VideoView videoView = (VideoView) mSceneView.pageView().getCurrentItemVideoView();
        if (videoView != null) {
            final VideoLayerHost layerHost = videoView.layerHost();
            if (layerHost != null && layerHost.onBackPressed()) {
                return true;
            }
            if (mContinuesPlayback) {
                currentItem = VideoItem.get(videoView.getDataSource());
                final PlaybackController controller = videoView.controller();
                if (controller != null) {
                    controller.unbindPlayer();
                }
            }
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_OUTPUT, new DramaDetailVideoOutput(
                mDrama,
                mVideoItem,
                currentItem,
                mSceneView.pageView().getItems(),
                mContinuesPlayback
        ));
        requireActivity().setResult(RESULT_CODE_EXIT, intent);
        return super.onBackPressed();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRemoteApi = new GetDramaDetail();
        mAccount = VideoSettings.stringValue(VideoSettings.DRAMA_VIDEO_SCENE_ACCOUNT_ID);

        final DramaDetailVideoInput input = parseInput();

        if (input != null) {
            mVideoItem = input.currenVideoItem;
            mDrama = input.drama;
            if (mDrama == null && mVideoItem != null) {
                final EpisodeVideo episode = (EpisodeVideo) EpisodeVideo.get(mVideoItem);
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
                EpisodeVideo episodeVideo = (EpisodeVideo) EpisodeVideo.get(videoItem);
                if (episodeVideo != null && episodeVideo.episodeInfo != null) {
                    setActionBarTitle(String.format(getString(R.string.vevod_mini_drama_detail_video_episode_number_desc), episodeVideo.episodeInfo.episodeNumber));
                    setEpisodeSelectDialogPlayingIndex(episodeVideo.episodeInfo.episodeNumber - 1);
                }
            }
        });
        mSceneView.setOnRefreshListener(this::refresh);
        mSceneView.setOnLoadMoreListener(this::loadMore);

        mSelectEpisodeView = view.findViewById(R.id.selectEpisodeView);
        mSelectEpisodeDesc = view.findViewById(R.id.selectEpisodeDesc);

        initSelector();
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mRegistered) {
            LocalBroadcastManager.getInstance(requireActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK));
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

    private void onPlayerStateCompleted(Event event) {
        VideoItem videoItem = mSceneView.pageView().getCurrentItemModel();
        if (videoItem == null) return;
        EpisodeVideo episodeVideo = (EpisodeVideo) EpisodeVideo.get(videoItem);
        if (episodeVideo == null) return;
        if (episodeVideo.episodeInfo == null) return;
        if (episodeVideo.episodeInfo.dramaInfo == null) return;
        if (episodeVideo.episodeInfo.episodeNumber < episodeVideo.episodeInfo.dramaInfo.totalEpisodeNumber) {
            // play next recommend
            final Player player = event.owner(Player.class);
            if (player != null && !player.isLooping()) {
                final int currentPosition = mSceneView.pageView().getCurrentItem();
                final int nextPosition = currentPosition + 1;
                if (nextPosition < mSceneView.pageView().getItemCount()) {
                    mSceneView.pageView().setCurrentItem(nextPosition, true);
                }
            }
        } else {
            // TODO goto next recommend video detail page
        }
    }

    private void setEpisodeSelectDialogPlayingIndex(int playingIndex) {
        if (mDialogFragment != null && mDialogFragment.isResumed()) {
            mDialogFragment.setPlayingIndex(playingIndex);
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

    private void initSelector() {
        if (mDrama == null) return;
        mSelectEpisodeDesc.setText(String.format(Locale.getDefault(), getString(R.string.vevod_mini_drama_video_detail_select_episode_number_desc), mDrama.totalEpisodeNumber));
        mSelectEpisodeView.setOnClickListener(v -> {
            VideoItem item = mSceneView.pageView().getCurrentItemModel();
            EpisodeVideo episodeVideo = (EpisodeVideo) EpisodeVideo.get(item);
            if (episodeVideo == null) return;

            mDialogFragment = newInstance(episodeVideo);
            mDialogFragment.show(getChildFragmentManager(), DramaEpisodeSelectDialogFragment.class.getName());
        });
    }

    private void initData() {
        if (mVideoItem == null) {
            refresh();
            return;
        }

        VideoItem.tag(mVideoItem, PlayScene.map(PlayScene.SCENE_SHORT), null);
        mSceneView.pageView().setItems(Collections.singletonList(mVideoItem));

        refresh();
    }

    private void refresh() {
        if (mDrama == null) return;

        L.d(this, "refresh", "start", 0, mBook.pageSize());
        mSceneView.showRefreshing();
        mRemoteApi.getEpisodeVideoItems(mAccount, 0, mBook.pageSize(), mDrama.dramaId, null, new RemoteApi.Callback<Page<VideoItem>>() {
            @Override
            public void onSuccess(Page<VideoItem> page) {
                L.d(this, "refresh", "success");
                if (getActivity() == null) return;
                List<VideoItem> videoItems = mBook.firstPage(page);
                VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
                VideoItem.syncProgress(videoItems, true);
                mSceneView.dismissRefreshing();
                mSceneView.pageView().setItems(videoItems);
                if (mVideoItem == null && mEpisodeNumber > 1
                        && mEpisodeNumber < mSceneView.pageView().getItemCount()) {
                    mSceneView.pageView().post(() -> mSceneView.pageView().setCurrentItem(mEpisodeNumber - 1, // TODO calculate currentItem index by videoItems
                            false));
                }
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
            mRemoteApi.getEpisodeVideoItems(mAccount, mBook.nextPageIndex(), mBook.pageSize(), mDrama.dramaId, null, new RemoteApi.Callback<Page<VideoItem>>() {
                @Override
                public void onSuccess(Page<VideoItem> page) {
                    L.d(this, "loadMore", "success", mBook.nextPageIndex());
                    if (getActivity() == null) return;
                    List<VideoItem> videoItems = mBook.addPage(page);
                    VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
                    VideoItem.syncProgress(videoItems, true);
                    mSceneView.dismissLoadingMore();
                    mSceneView.pageView().appendItems(videoItems);
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
