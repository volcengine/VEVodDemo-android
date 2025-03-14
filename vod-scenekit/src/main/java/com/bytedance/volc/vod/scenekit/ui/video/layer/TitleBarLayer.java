/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/11/2
 */

package com.bytedance.volc.vod.scenekit.ui.video.layer;


import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.MoreDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;


public class TitleBarLayer extends AnimateLayer {
    public static final String ACTION_VIDEO_LAYER_TOGGLE_PIP_MODE = "TITLE_BAR_TOGGLE_PIP_MODE";

    private View mBack;
    private TextView mTitle;
    private View mTitleBar;
    private ViewGroup mActions;

    @Override
    public String tag() {
        return "title_bar";
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_title_bar_layer, parent, false);
        mBack = view.findViewById(R.id.back);
        View search = view.findViewById(R.id.search);
        View cast = view.findViewById(R.id.cast);
        View pip = view.findViewById(R.id.pip);
        View more = view.findViewById(R.id.more);

        mActions = view.findViewById(R.id.actions);
        mTitle = view.findViewById(R.id.title);
        mTitleBar = view.findViewById(R.id.titleBar);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = activity();
                if (activity != null) {
                    activity.onBackPressed();
                }
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity(), "Search is not supported yet!", Toast.LENGTH_SHORT).show();
            }
        });

        cast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(activity(), "Cast is not supported yet!", Toast.LENGTH_SHORT).show();
            }
        });

        if (VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP)) {
            pip.setVisibility(View.VISIBLE);
            pip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(ACTION_VIDEO_LAYER_TOGGLE_PIP_MODE);
                    LocalBroadcastManager.getInstance(v.getContext()).sendBroadcast(intent);
                }
            });
        } else {
            pip.setVisibility(View.GONE);
        }
        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (playScene() != PlayScene.SCENE_FULLSCREEN) {
                    Toast.makeText(context(), "More is only supported in fullscreen for now!",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                VideoLayerHost layerHost = layerHost();
                if (layerHost == null) return;
                MoreDialogLayer layer = layerHost.findLayer(MoreDialogLayer.class);
                if (layer != null) {
                    layer.animateShow(false);
                }
            }
        });
        return view;
    }

    @Override
    public void show() {
        if (!checkShow()) {
            return;
        }
        super.show();
        mTitle.setText(resolveTitle());
        applyTheme();
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        applyTheme();
        if (!checkShow()) {
            dismiss();
        }
    }

    private boolean checkShow() {
        switch (playScene()) {
            case PlayScene.SCENE_FEED:
            case PlayScene.SCENE_FULLSCREEN:
            case PlayScene.SCENE_DETAIL:
            case PlayScene.SCENE_UNKNOWN:
                return true;
            default:
                return false;
        }
    }

    private String resolveTitle() {
        VideoView videoView = videoView();
        if (videoView != null) {
            MediaSource mediaSource = videoView.getDataSource();
            if (mediaSource != null) {
                VideoItem videoItem = VideoItem.get(mediaSource);
                if (videoItem != null) {
                    return videoItem.getTitle();
                }
            }
        }
        return null;
    }

    public void applyTheme() {
        if (playScene() == PlayScene.SCENE_FULLSCREEN) {
            applyFullScreenTheme();
        } else if (playScene() == PlayScene.SCENE_FEED) {
            applyFeedTheme();
        } else {
            applyDetailTheme();
        }
    }

    private void applyFullScreenTheme() {
        setTitleBarLeftRightMargin(44);
        if (getView() != null) {
            mTitle.setVisibility(View.VISIBLE);
            mBack.setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.VISIBLE);
            mActions.setVisibility(View.VISIBLE);
            for (int i = 0; i < mActions.getChildCount(); i++) {
                View view = mActions.getChildAt(i);
                if (view.getId() == R.id.pip) {
                    view.setVisibility(VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP) ? View.VISIBLE : VideoView.GONE);
                } else {
                    view.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void applyDetailTheme() {
        setTitleBarLeftRightMargin(0);
        if (getView() != null) {
            mTitle.setVisibility(View.GONE);
            mBack.setVisibility(View.VISIBLE);
            mTitle.setVisibility(View.GONE);
            mActions.setVisibility(View.VISIBLE);
            for (int i = 0; i < mActions.getChildCount(); i++) {
                View view = mActions.getChildAt(i);
                if (view.getId() == R.id.pip) {
                    view.setVisibility(VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP) ? View.VISIBLE : VideoView.GONE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    private void applyFeedTheme() {
        setTitleBarLeftRightMargin(0);
        if (getView() != null) {
            mTitle.setVisibility(View.GONE);
            mBack.setVisibility(View.GONE);
            mTitle.setVisibility(View.GONE);
            mActions.setVisibility(View.VISIBLE);
            for (int i = 0; i < mActions.getChildCount(); i++) {
                View view = mActions.getChildAt(i);
                if (view.getId() == R.id.pip) {
                    view.setVisibility(VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_PIP) ? View.VISIBLE : VideoView.GONE);
                } else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    private void setTitleBarLeftRightMargin(int marginDp) {
        if (mTitleBar == null) return;

        ViewGroup.MarginLayoutParams titleBarLP = (ViewGroup.MarginLayoutParams) mTitleBar.getLayoutParams();
        if (titleBarLP != null) {
            int margin = (int) UIUtils.dip2Px(context(), marginDp);
            titleBarLP.leftMargin = margin;
            titleBarLP.rightMargin = margin;
            mTitleBar.setLayoutParams(titleBarLP);
        }
    }
}
