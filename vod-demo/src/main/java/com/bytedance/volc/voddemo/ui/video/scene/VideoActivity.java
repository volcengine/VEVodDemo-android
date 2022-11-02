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

package com.bytedance.volc.voddemo.ui.video.scene;

import static com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene.SCENE_DETAIL;
import static com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene.SCENE_FEED;
import static com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene.SCENE_LONG;
import static com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene.SCENE_SHORT;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bytedance.playerkit.player.volcengine.VolcPlayerStatic;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.vod.scenekit.ui.video.scene.base.BaseActivity;
import com.bytedance.volc.vod.scenekit.ui.video.scene.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.detail.DetailVideoFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.feedvideo.FeedVideoSceneView;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.video.scene.feedvideo.FeedVideoFragment;
import com.bytedance.volc.voddemo.ui.video.scene.longvideo.LongVideoFragment;
import com.bytedance.volc.voddemo.ui.video.scene.shortvideo.ShortVideoFragment;


public class VideoActivity extends BaseActivity {

    private static final String EXTRA_VIDEO_SCENE = "extra_video_scene";
    private static final String EXTRA_ARGS = "extra_args";
    private int mScene;

    public static void intentInto(Activity activity, int scene, Bundle args) {
        Intent intent = new Intent(activity, VideoActivity.class);
        intent.putExtra(EXTRA_VIDEO_SCENE, scene);
        intent.putExtra(EXTRA_ARGS, args);
        activity.startActivity(intent);
    }

    public static void intentInto(Activity activity, int scene) {
        intentInto(activity, scene, null);
    }

    @Override
    public void onBackPressed() {
        BaseFragment fragment = (BaseFragment) getSupportFragmentManager().findFragmentByTag(getTag(mScene));
        if (fragment != null && fragment.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScene = getIntent().getIntExtra(EXTRA_VIDEO_SCENE, SCENE_SHORT);
        Bundle mArgs = getIntent().getBundleExtra(EXTRA_ARGS);
        setContentView(R.layout.video_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sceneTheme(mScene);

        final String tag = getTag(mScene);
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = createFragment(mScene, mArgs);
            fm.beginTransaction().add(R.id.container, fragment, tag).commit();
        } else {
            fm.beginTransaction().attach(fragment).commit();
        }

        if (VideoSettings.booleanValue(VideoSettings.DEBUG_ENABLE_DEBUG_TOOL)){
            VolcPlayerStatic.setDebugToolContainerView(findViewById(R.id.debugTool));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (VideoSettings.booleanValue(VideoSettings.DEBUG_ENABLE_DEBUG_TOOL)) {
            VolcPlayerStatic.releaseDebugTool();
        }
    }

    private Fragment createFragment(int scene, Bundle bundle) {
        switch (scene) {
            case SCENE_SHORT:
                return ShortVideoFragment.newInstance();
            case SCENE_FEED:
                FeedVideoFragment fragment = FeedVideoFragment.newInstance();
                fragment.setFeedSceneEventListener(mFeedVideoSceneEventListener);
                return fragment;
            case SCENE_LONG:
                return LongVideoFragment.newInstance();
            case SCENE_DETAIL:
                return DetailVideoFragment.newInstance(bundle);
        }
        throw new IllegalArgumentException("unsupported " + scene);
    }

    private String getTag(int scene) {
        switch (scene) {
            case SCENE_SHORT:
                return ShortVideoFragment.class.getName();
            case SCENE_FEED:
                return FeedVideoFragment.class.getName();
            case SCENE_LONG:
                return LongVideoFragment.class.getName();
            case SCENE_DETAIL:
                return DetailVideoFragment.class.getName();
        }
        throw new IllegalArgumentException("unsupported " + scene);
    }

    private final FeedVideoSceneView.FeedVideoSceneEventListener
            mFeedVideoSceneEventListener = new FeedVideoSceneView.FeedVideoSceneEventListener() {
        @Override
        public void onEnterDetail() {
            UIUtils.setSystemBarTheme(
                    VideoActivity.this,
                    Color.BLACK,
                    false,
                    false,
                    Color.WHITE,
                    true,
                    false);
        }

        @Override
        public void onExitDetail() {
            UIUtils.setSystemBarTheme(
                    VideoActivity.this,
                    Color.WHITE,
                    true,
                    false,
                    Color.WHITE,
                    true,
                    false
            );
        }
    };

    private void sceneTheme(int scene) {
        switch (scene) {
            case SCENE_SHORT:
                setActionBarTheme(
                        true,
                        true,
                        getString(R.string.short_video),
                        Color.TRANSPARENT,
                        getResources().getColor(android.R.color.white));
                UIUtils.setSystemBarTheme(
                        this,
                        Color.TRANSPARENT,
                        false,
                        true,
                        Color.BLACK,
                        false,
                        false
                );
                break;
            case SCENE_LONG:
                setActionBarTheme(
                        true,
                        false,
                        getString(R.string.long_video),
                        getResources().getColor(android.R.color.white),
                        getResources().getColor(android.R.color.black));
                UIUtils.setSystemBarTheme(
                        this,
                        Color.WHITE,
                        true,
                        false,
                        Color.WHITE,
                        true,
                        false
                );
                break;
            case SCENE_FEED:
                setActionBarTheme(
                        true,
                        false, getString(R.string.feed_video),
                        getResources().getColor(android.R.color.white),
                        getResources().getColor(android.R.color.black));
                UIUtils.setSystemBarTheme(
                        this,
                        Color.WHITE,
                        true,
                        false,
                        Color.WHITE,
                        true,
                        false
                );
                break;
            case SCENE_DETAIL: {
                setActionBarTheme(
                        false,
                        false,
                        null,
                        0,
                        0);
                UIUtils.setSystemBarTheme(
                        this,
                        Color.BLACK,
                        false,
                        false,
                        Color.WHITE,
                        true,
                        false
                );
                break;
            }
        }
    }

    private void setActionBarTheme(boolean showActionBar,
                                   boolean immersiveStatusBar,
                                   String title,
                                   int bgColor,
                                   int textColor) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) return;
        if (showActionBar) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            toolbar.setBackgroundColor(bgColor);
            toolbar.setNavigationIcon(ResourcesCompat.getDrawable(
                    getResources(),
                    R.drawable.vevod_actionbar_back,
                    null));
            toolbar.setTitleTextColor(textColor);
            if (toolbar.getNavigationIcon() != null) {
                toolbar.getNavigationIcon().setTint(textColor);
            }
            actionBar.setTitle(title);
            if (immersiveStatusBar) {
                ((ViewGroup.MarginLayoutParams) toolbar.getLayoutParams())
                        .topMargin = UIUtils.getStatusBarHeight(this);
            } else {
                toolbar.setElevation(UIUtils.dip2Px(this, 1));
                ((ViewGroup.MarginLayoutParams) findViewById(R.id.container)
                        .getLayoutParams())
                        .topMargin = (int) UIUtils.dip2Px(this, 40);
                ((ViewGroup.MarginLayoutParams) findViewById(R.id.debugTool)
                        .getLayoutParams())
                        .topMargin = (int) UIUtils.dip2Px(this, 40);
            }
        } else {
            actionBar.hide();
        }
    }
}