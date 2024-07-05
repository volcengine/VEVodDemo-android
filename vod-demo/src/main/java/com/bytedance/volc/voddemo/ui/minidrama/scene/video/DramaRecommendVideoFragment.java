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

import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.layer.DramaGestureLayer.ACTION_DRAMA_GESTURE_LAYER_DISMISS_SPEED;
import static com.bytedance.volc.voddemo.ui.minidrama.scene.video.layer.DramaGestureLayer.ACTION_DRAMA_GESTURE_LAYER_SHOW_SPEED;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
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
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.GetEpisodeRecommend;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetEpisodeRecommendApi;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.DramaDetailVideoActivityResultContract.DramaDetailVideoInput;
import com.bytedance.volc.voddemo.ui.minidrama.scene.video.bottom.SpeedIndicatorViewHolder;

import java.util.List;

public class DramaRecommendVideoFragment extends BaseFragment {
    public static final String ACTION_PLAY_MORE_CLICK = "action_play_more_click";
    private GetEpisodeRecommendApi mRemoteApi;
    private String mAccount;
    private final Book<VideoItem> mBook = new Book<>(10);
    private ShortVideoSceneView mSceneView;
    private SpeedIndicatorViewHolder mSpeedIndicator;
    private boolean mRegistered;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (TextUtils.isEmpty(action)) return;

            switch (action) {
                case ACTION_PLAY_MORE_CLICK: {
                    onPlayMoreCardClick();
                    break;
                }
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

    public ActivityResultLauncher<DramaDetailVideoInput> mDramaDetailPageLauncher = registerForActivityResult(new DramaDetailVideoActivityResultContract(), result -> {
        if (result == null) return;
        if (!VideoItem.mediaEquals(mSceneView.pageView().getCurrentItemModel(), result.currenVideoItem)) {
            L.d(DramaRecommendVideoFragment.this, "onActivityResult", VideoItem.dump(mSceneView.pageView().getCurrentItemModel()), VideoItem.dump(result.currenVideoItem));
            mSceneView.pageView().replaceItem(mSceneView.pageView().getCurrentItem(), result.currenVideoItem);
        }
    });

    public DramaRecommendVideoFragment() { /**/ }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRemoteApi = new GetEpisodeRecommend();
        mAccount = VideoSettings.stringValue(VideoSettings.DRAMA_VIDEO_SCENE_ACCOUNT_ID);
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
        mSceneView = view.findViewById(R.id.shortVideoSceneView);
        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.pageView().setVideoViewFactory(new DramaVideoViewFactory(DramaVideoViewFactory.Type.RECOMMEND));
        mSceneView.pageView().addPlaybackListener(event -> {
            if (event.code() == PlayerEvent.State.COMPLETED) {
                onPlayerStateCompleted(event);
            }
        });
        mSceneView.setOnRefreshListener(this::refresh);
        mSceneView.setOnLoadMoreListener(this::loadMore);

        mSpeedIndicator = new SpeedIndicatorViewHolder(view);
        mSpeedIndicator.showSpeedIndicator(false);

        refresh();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_PLAY_MORE_CLICK);
            filter.addAction(ACTION_DRAMA_GESTURE_LAYER_SHOW_SPEED);
            filter.addAction(ACTION_DRAMA_GESTURE_LAYER_DISMISS_SPEED);
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
        final VideoView videoView = mSceneView.pageView().getCurrentItemVideoView();
        if (videoView == null) return;
        final PlaybackController controller = videoView.controller();
        boolean continuesPlayback = false;
        if (controller != null) {
            continuesPlayback = controller.player() != null;
            controller.unbindPlayer();
        }
        final VideoItem videoItem = mSceneView.pageView().getCurrentItemModel();
        mDramaDetailPageLauncher.launch(new DramaDetailVideoInput(videoItem, continuesPlayback));
    }

    private void onPlayerStateCompleted(Event event) {
        VideoItem videoItem = mSceneView.pageView().getCurrentItemModel();
        if (videoItem == null) return;
        EpisodeVideo episodeVideo = EpisodeVideo.get(videoItem);
        if (episodeVideo == null) return;
        if (episodeVideo.episodeInfo == null || episodeVideo.episodeInfo.dramaInfo == null) return;

        if (EpisodeVideo.isLastEpisode(episodeVideo)) {
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
            // goto detail play next
            mDramaDetailPageLauncher.launch(new DramaDetailVideoInput(episodeVideo.episodeInfo.dramaInfo, episodeVideo.episodeInfo.episodeNumber + 1, true));
        }
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        mSceneView.showRefreshing();
        mRemoteApi.getRecommendEpisodeVideoItems(mAccount, 0, mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
            @Override
            public void onSuccess(Page<VideoItem> page) {
                L.d(this, "refresh", "success");
                if (getActivity() == null) return;
                List<VideoItem> videoItems = mBook.firstPage(page);
                VideoItem.tag(videoItems, PlayScene.map(PlayScene.SCENE_SHORT), null);
                VideoItem.syncProgress(videoItems, true);
                mSceneView.dismissRefreshing();
                mSceneView.pageView().setItems(videoItems);
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
            mRemoteApi.getRecommendEpisodeVideoItems(mAccount, mBook.nextPageIndex(), mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
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
