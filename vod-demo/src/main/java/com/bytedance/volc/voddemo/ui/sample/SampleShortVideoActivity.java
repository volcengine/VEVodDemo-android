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
 * Create Date : 2023/6/30
 */

package com.bytedance.volc.voddemo.ui.sample;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.base.BaseActivity;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;

import java.util.ArrayList;

public class SampleShortVideoActivity extends BaseActivity {

    public static final String EXTRA_VIDEO_ITEMS = "extra_video_items";

    public static void intentInto(Activity activity, ArrayList<VideoItem> videoItems) {
        Intent intent = new Intent(activity, SampleShortVideoActivity.class);
        intent.putExtra(EXTRA_VIDEO_ITEMS, videoItems);
        activity.startActivity(intent);
    }

    private ShortVideoSceneView mSceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ArrayList<VideoItem> videoItems = (ArrayList<VideoItem>) getIntent().getSerializableExtra(EXTRA_VIDEO_ITEMS);

        mSceneView = new ShortVideoSceneView(this);
        mSceneView.setRefreshEnabled(false);
        mSceneView.setLoadMoreEnabled(false);

        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.pageView().setItems(videoItems);
        setContentView(mSceneView);

        UIUtils.setSystemBarTheme(
                this,
                Color.TRANSPARENT,
                false,
                true,
                Color.BLACK,
                false,
                false
        );
    }
}
