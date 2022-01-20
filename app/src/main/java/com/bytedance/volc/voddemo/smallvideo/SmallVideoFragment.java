/*
 * Copyright 2021 bytedance
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
 * Create Date : 2021/6/10
 */
package com.bytedance.volc.voddemo.smallvideo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bytedance.volc.voddemo.VodApp;
import com.bytedance.volc.voddemo.base.BaseAdapter;
import com.bytedance.volc.voddemo.preload.PreloadManager;
import com.bytedance.volc.voddemo.preload.SimplePreloadStrategy;
import com.bytedance.volc.voddemo.settings.ClientSettings;
import com.bytedance.volc.voddemo.videoview.layers.LoadFailLayer;
import com.bytedance.volc.voddemo.videoview.layers.LoadingLayer;
import com.bytedance.volc.voddemo.videoview.DisplayMode;
import com.bytedance.volc.voddemo.videoview.layers.DebugLayer;
import com.bytedance.volc.voddemo.videoview.layers.SmallToolbarLayer;
import com.bytedance.volc.voddemo.videoview.VOLCVideoController;
import com.bytedance.volc.voddemo.videoview.VOLCVideoView;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.data.VideoItem;
import com.bytedance.volc.voddemo.data.VideoViewModel;
import com.bytedance.volc.voddemo.videoview.layers.CoverLayer;
import com.bytedance.volc.voddemo.smallvideo.pager.PagerLayoutManager;
import com.bytedance.volc.voddemo.smallvideo.pager.RecyclerViewPagerListener;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.strategy.source.StrategySource;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;
import java.util.ArrayList;
import java.util.List;

import static com.bytedance.volc.voddemo.data.VideoItem.VIDEO_TYPE_SMALL;
import static com.ss.ttvideoengine.TTVideoEngine.PLAYER_OPTION_USE_TEXTURE_RENDER;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_COMMON;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRELOAD;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRE_RENDER;

public class SmallVideoFragment extends Fragment implements RecyclerViewPagerListener {
    private static final String TAG = "SmallFragment";

    private static final int ITEMS_LIMIT = 100;

    private BaseAdapter<VideoItem> mAdapter;
    private VOLCVideoView mCurrentVideoView;

    private int mLastPosition = -1;
    private boolean mSelectFirst;
    private RecyclerView mRecyclerView;
    private PagerLayoutManager mLayoutManager;
    private VideoViewModel mVideoViewModel;

    @Override
    public void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClientSettings settings = VodApp.getClientSettings();

        if (settings.enableStrategyCommon()) {
            // VOD key step Strategy Common: enable
            TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_COMMON, STRATEGY_SCENE_SMALL_VIDEO);
        }

        if (settings.enableStrategyPreload()) {
            // VOD key step Strategy Preload 1: enable
            TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRELOAD, STRATEGY_SCENE_SMALL_VIDEO);
        } else {
            PreloadManager.getInstance().setPreloadStrategy(new SimplePreloadStrategy());
        }

        if (settings.enableStrategyPreRender()) {
            // VOD key step Strategy PreRender 1: enable
            TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRE_RENDER,
                    STRATEGY_SCENE_SMALL_VIDEO);
            // VOD key step Strategy PreRender 3: set listener
            TTVideoEngine.setEngineStrategyListener(ttVideoEngine -> {
                // VOD key step Strategy PreRender instead of cover 1: use TEXTURE_RENDER
                ttVideoEngine.setIntOption(PLAYER_OPTION_USE_TEXTURE_RENDER, 1);
                // VOD key step Strategy PreRender 4: config preRender engine
                VOLCVideoController.configEngine(ttVideoEngine);
            });
        }

        mVideoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        mAdapter = new BaseAdapter<VideoItem>(new ArrayList<>()) {
            @Override
            public int getLayoutId(final int viewType) {
                return R.layout.list_item_small_video;
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final VideoItem data,
                    final int position) {
                VOLCVideoView videoView = holder.getView(R.id.video_view);
                videoView.setVideoController(new VOLCVideoController(videoView.getContext(), data,
                        videoView));

                videoView.setDisplayMode(DisplayMode.DISPLAY_MODE_ASPECT_FIT);

                videoView.addLayer(new CoverLayer());
                videoView.addLayer(new DebugLayer());
                videoView.addLayer(new SmallToolbarLayer());
                videoView.addLayer(new LoadFailLayer());
                videoView.addLayer(new LoadingLayer());
                videoView.refreshLayers();

                if (!mSelectFirst) {
                    mSelectFirst = true;
                    onPageSelected(position, holder.itemView);
                }
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_small_video, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mLayoutManager = new PagerLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setOnViewPagerListener(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mVideoViewModel.getVideoList(VIDEO_TYPE_SMALL, ITEMS_LIMIT, videoItems -> {
            if (videoItems != null && videoItems.size() > 0) {
                mAdapter.addAll(videoItems);
                PreloadManager.getInstance().videoListUpdate(videoItems);
                setStrategySources(videoItems);
            }
        });
    }

    private void setStrategySources(final List<VideoItem> videoItems) {
        String encodeType = VodApp.getClientSettings().videoEnableH265()
                ? TTVideoEngine.CODEC_TYPE_h265 : TTVideoEngine.CODEC_TYPE_H264;
        List<StrategySource> sources = new ArrayList<>();
        for (VideoItem videoItem : videoItems) {
            StrategySource vidSource = new VidPlayAuthTokenSource.Builder()
                    .setVid(videoItem.getVid())
                    .setPlayAuthToken(videoItem.getAuthToken())
                    .setEncodeType(encodeType)
                    .build();
            sources.add(vidSource);
        }
        // VOD key step Strategy PreRender 2: set sources
        // VOD key step Strategy Preload 2: set sources
        TTVideoEngine.setStrategySources(sources);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mCurrentVideoView != null) {
            mCurrentVideoView.onResume();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mCurrentVideoView != null) {
            mCurrentVideoView.onPause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cleanUp();
    }

    @Override
    public void onInitComplete() {
    }

    @Override
    public void onPageRelease(final int position, final View view) {
        TTVideoEngineLog.d(TAG, "onPageRelease position " + position + ", view " + view);
        if (view == null) {
            return;
        }
        VOLCVideoView videoView = view.findViewById(R.id.video_view);
        videoView.release();
    }

    @Override
    public void onPageSelected(final int position, final View view) {
        TTVideoEngineLog.d(TAG, "onPageSelected position " + position);
        if (position == mLastPosition) {
            TTVideoEngineLog.d(TAG, "onPageSelected position is last position");
            return;
        }
        mLastPosition = position;

        View ItemView = view;
        final View tempView = mLayoutManager.findViewByPosition(position);
        if (tempView != null) {
            ItemView = tempView;
        }

        if (ItemView == null) {
            TTVideoEngineLog.d(TAG, "onPageSelected view is null");
            return;
        }

        VOLCVideoView videoView = ItemView.findViewById(R.id.video_view);
        if (mCurrentVideoView != null) {
            mCurrentVideoView.mute();
        }
        mCurrentVideoView = videoView;
        videoView.play();
    }

    private void cleanUp() {
        if (mCurrentVideoView == null) {
            return;
        }
        mCurrentVideoView.release();
        mCurrentVideoView = null;
    }
}
