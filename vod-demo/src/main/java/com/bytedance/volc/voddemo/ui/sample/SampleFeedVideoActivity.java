/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/3/26
 */

package com.bytedance.volc.voddemo.ui.sample;

import static com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene.SCENE_DETAIL;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.base.BaseActivity;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoPageView;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoSceneView;
import com.bytedance.volc.voddemo.ui.video.scene.VideoActivity;
import com.bytedance.volc.voddemo.ui.video.scene.detail.DetailVideoFragment;

import java.util.ArrayList;

public class SampleFeedVideoActivity extends BaseActivity implements FeedVideoPageView.DetailPageNavigator {

    public static final String EXTRA_VIDEO_ITEMS = "extra_video_items";

    public static void intentInto(Activity activity, ArrayList<VideoItem> videoItems) {
        Intent intent = new Intent(activity, SampleFeedVideoActivity.class);
        intent.putExtra(EXTRA_VIDEO_ITEMS, videoItems);
        activity.startActivity(intent);
    }

    private FeedVideoSceneView mSceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ArrayList<VideoItem> videoItems = (ArrayList<VideoItem>) getIntent().getSerializableExtra(EXTRA_VIDEO_ITEMS);

        mSceneView = new FeedVideoSceneView(this);
        mSceneView.setRefreshEnabled(false);
        mSceneView.setLoadMoreEnabled(false);

        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.pageView().setItems(videoItems);
        mSceneView.setBackgroundColor(Color.WHITE);
        mSceneView.setDetailPageNavigator(this);
        setContentView(mSceneView);
    }

    @Override
    public void onBackPressed() {
        if (mSceneView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void enterDetail(FeedVideoViewHolder holder) {
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
        VideoActivity.intentInto(this, SCENE_DETAIL, bundle);
    }
}
