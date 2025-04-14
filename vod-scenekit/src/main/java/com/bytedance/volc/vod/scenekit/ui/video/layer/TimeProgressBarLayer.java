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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.InfoBufferingUpdate;
import com.bytedance.playerkit.player.event.InfoProgressUpdate;
import com.bytedance.playerkit.player.event.InfoTrackChanged;
import com.bytedance.playerkit.player.event.InfoTrackWillChange;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.PlaybackEvent;
import com.bytedance.playerkit.player.playback.VideoLayerHost;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.strategy.VideoSubtitle;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.AnimateLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.QualitySelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.SpeedSelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.dialog.SubtitleSelectDialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.widgets.MediaSeekBar;
import com.bytedance.volc.vod.scenekit.utils.TimeUtils;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;

import java.util.List;

public class TimeProgressBarLayer extends AnimateLayer {

    private MediaSeekBar mSeekBar;
    private View mShadowView;

    private boolean mHalfScreenInit;
    private boolean mFullScreenInit;


    @Override
    public String tag() {
        return "time_progressbar";
    }

    public TimeProgressBarLayer() {
        setIgnoreLock(true);
    }

    @Nullable
    @Override
    protected View createView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_time_progress_bar_layer, parent, false);
        mShadowView = view.findViewById(R.id.shadow);

        mSeekBar = view.findViewById(R.id.mediaSeekBar);
        mSeekBar.setOnSeekListener(new MediaSeekBar.OnUserSeekListener() {

            @Override
            public void onUserSeekStart(long startPosition) {
                showControllerLayers();
            }

            @Override
            public void onUserSeekPeeking(long peekPosition) {
                showControllerLayers();
            }

            @Override
            public void onUserSeekStop(long startPosition, long seekToPosition) {
                final Player player = player();
                if (player == null) return;

                if (player.isInPlaybackState()) {
                    if (player.isCompleted()) {
                        player.start();
                        player.seekTo(seekToPosition);
                    } else {
                        player.seekTo(seekToPosition);
                    }
                }

                showControllerLayers();
            }
        });
        syncTheme(view);
        return view;
    }

    private void showControllerLayers() {
        VideoLayerHost layerHost = layerHost();
        if (layerHost != null) {
            GestureLayer gestureLayer = layerHost.findLayer(GestureLayer.class);
            if (gestureLayer != null) {
                gestureLayer.showController();
            }
        }
    }

    private void syncTheme(View view) {
        if (view == null) return;
        if (playScene() == PlayScene.SCENE_FULLSCREEN) {
            initFullScreen(view);
        } else {
            initHalfScreen(view);
        }
    }

    private View mFullScreen;

    private void initHalfScreen(View view) {
        if (!mHalfScreenInit) {
            mHalfScreenInit = true;
            mFullScreen = view.findViewById(R.id.fullScreen);
            mFullScreen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FullScreenLayer.toggle(videoView(), true);
                }
            });
        }

        if (mFullScreenInit) {
            if (mInteractLayout != null) mInteractLayout.setVisibility(View.GONE);
            if (mTimeContainer != null) mTimeContainer.setVisibility(View.GONE);
        }

        mFullScreen.setVisibility(View.VISIBLE);
        mSeekBar.setTextVisibility(true);
        mSeekBar.setSeekEnabled(true);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mSeekBar.getLayoutParams();
        if (lp != null) {
            lp.height = (int) UIUtils.dip2Px(context(), 44);
            lp.leftMargin = (int) UIUtils.dip2Px(context(), 10);
            lp.rightMargin = (int) UIUtils.dip2Px(context(), 10);
            mSeekBar.setLayoutParams(lp);
        }
        mShadowView.setBackground(ResourcesCompat.getDrawable(view.getResources(), R.drawable.vevod_time_progress_bar_layer_halfscreen_shadow_shape, null));
    }

    private ViewStub mInteractViewStub;
    private View mInteractLayout;

    private View mTimeContainer;
    private TextView mCurrentPosition;
    private TextView mDuration;

    private View mLikeContainer;
    private ImageView mLikeIcon;
    private TextView mLikeNum;

    private View mCommentContainer;
    private ImageView mCommentIcon;
    private TextView mCommentNum;

    private View mDanmakuContainer;
    private ImageView mDanmakuIcon;

    private View mSubtitleContainer;
    private TextView mSubtitle;

    private View mQualityContainer;
    private TextView mQuality;

    private View mSpeedContainer;
    private TextView mSpeed;

    private void initFullScreen(View view) {
        if (!mFullScreenInit) {
            mFullScreenInit = true;
            mTimeContainer = view.findViewById(R.id.timeContainer);
            mCurrentPosition = mTimeContainer.findViewById(R.id.currentPosition);
            mDuration = mTimeContainer.findViewById(R.id.duration);
            mInteractViewStub = view.findViewById(R.id.interact_stub);
            mInteractLayout = mInteractViewStub.inflate();

            mLikeContainer = mInteractLayout.findViewById(R.id.likeContainer);
            mCommentContainer = mInteractLayout.findViewById(R.id.commentContainer);
            mDanmakuContainer = mInteractLayout.findViewById(R.id.danmakuContainer);
            mSubtitleContainer = mInteractLayout.findViewById(R.id.subtitleContainer);


            mLikeContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context(), "Like is not supported yet!", Toast.LENGTH_SHORT).show();
                }
            });
            mCommentContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context(), "Comment is not supported yet!", Toast.LENGTH_SHORT).show();
                }
            });
            mDanmakuContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(context(), "Danmaku is not supported yet!", Toast.LENGTH_SHORT).show();
                }
            });
            mSubtitle = mInteractLayout.findViewById(R.id.subtitle);
            mSubtitleContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SubtitleSelectDialogLayer subtitleSelectLayer = layerHost().findLayer(SubtitleSelectDialogLayer.class);
                    if (subtitleSelectLayer != null) {
                        subtitleSelectLayer.animateShow(false);
                    }
                }
            });

            mQuality = mInteractLayout.findViewById(R.id.quality);
            mQualityContainer = view.findViewById(R.id.qualityContainer);
            mQualityContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    QualitySelectDialogLayer qualitySelectLayer = layerHost().findLayer(QualitySelectDialogLayer.class);
                    if (qualitySelectLayer != null) {
                        qualitySelectLayer.animateShow(false);
                    }
                }
            });
            mSpeed = mInteractLayout.findViewById(R.id.speed);
            mSpeedContainer = mInteractLayout.findViewById(R.id.speedContainer);
            mSpeedContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SpeedSelectDialogLayer speedSelectLayer = layerHost().findLayer(SpeedSelectDialogLayer.class);
                    if (speedSelectLayer != null) {
                        speedSelectLayer.animateShow(false);
                    }
                }
            });
        }

        if (mHalfScreenInit) {
            if (mFullScreen != null) mFullScreen.setVisibility(View.GONE);
        }
        mTimeContainer.setVisibility(View.VISIBLE);
        mInteractLayout.setVisibility(layerHost().isLocked() ? View.GONE : View.VISIBLE);
        mSeekBar.setTextVisibility(false);
        mSeekBar.setSeekEnabled(!layerHost().isLocked());

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mSeekBar.getLayoutParams();
        lp.height = (int) UIUtils.dip2Px(context(), 40);
        lp.leftMargin = (int) UIUtils.dip2Px(context(), 40);
        lp.rightMargin = (int) UIUtils.dip2Px(context(), 40);
        mSeekBar.setLayoutParams(lp);
        mShadowView.setBackground(ResourcesCompat.getDrawable(view.getResources(), R.drawable.vevod_time_progress_bar_layer_fullscreen_shadow_shape, null));
    }

    private void syncProgress() {
        final PlaybackController controller = this.controller();
        if (controller != null) {
            final Player player = controller.player();
            if (player != null) {
                if (player.isInPlaybackState()) {
                    setProgress(player.getCurrentPosition(), player.getDuration(), player.getBufferedPercentage());
                }
            }
        }
    }

    private void setProgress(long currentPosition, long duration, int bufferPercent) {
        if (mSeekBar != null) {
            if (duration >= 0) {
                mSeekBar.setDuration(duration);
            }
            if (currentPosition >= 0) {
                mSeekBar.setCurrentPosition(currentPosition);
            }
            if (bufferPercent >= 0) {
                mSeekBar.setCachePercent(bufferPercent);
            }
        }

        if (mCurrentPosition != null) {
            if (currentPosition >= 0) {
                mCurrentPosition.setText(TimeUtils.time2String(currentPosition));
            }
        }
        if (mDuration != null) {
            if (duration >= 0) {
                mDuration.setText(TimeUtils.time2String(duration));
            }
        }
    }

    private void syncQuality() {
        if (mQuality == null) return;
        final Player player = player();
        if (player != null) {
            Track selected = player.getSelectedTrack(Track.TRACK_TYPE_VIDEO);
            if (selected != null) {
                Quality quality = selected.getQuality();
                if (quality != null) {
                    mQuality.setText(quality.getQualityDesc());
                }
            }

            List<Track> tracks = player.getTracks(Track.TRACK_TYPE_VIDEO);
            if (tracks == null || tracks.size() < 2) {
                mQualityContainer.setVisibility(View.GONE);
            } else {
                mQualityContainer.setVisibility(View.VISIBLE);
            }
        } else {
            mQualityContainer.setVisibility(View.GONE);
        }
    }

    private void syncSpeed() {
        if (mSpeed == null) return;
        final Player player = player();

        if (player != null) {
            float speed = player.getSpeed();
            if (speed != 1) {
                mSpeed.setText(SpeedSelectDialogLayer.mapSpeed(context(), speed));
            } else {
                mSpeed.setText(R.string.vevod_time_progress_bar_speed);
            }
        } else {
            mSpeed.setText(R.string.vevod_time_progress_bar_speed);
        }
    }

    private void syncSubtitle() {
        if (mSubtitle == null) return;

        final Player player = player();
        if (player != null) {
            Subtitle selected = player.isSubtitleEnabled() ? player.getSelectedSubtitle() : null;
            if (selected != null) {
                mSubtitle.setText(VideoSubtitle.subtitle2String(selected));
            } else {
                mSubtitle.setText(mSubtitle.getResources().getString(R.string.vevod_time_progress_subtitle));
            }
            List<Subtitle> subtitles = player.getSubtitles();
            if (subtitles == null) {
                mSubtitleContainer.setVisibility(View.GONE);
            } else {
                mSubtitleContainer.setVisibility(View.VISIBLE);
            }
        } else {
            mSubtitleContainer.setVisibility(View.GONE);
        }
    }

    private void syncSubtitleLayer() {
        SubtitleLayer subtitleLayer = layerHost() == null ? null : layerHost().findLayer(SubtitleLayer.class);
        if (subtitleLayer != null) {
            subtitleLayer.syncWithProgressBarState();
        }
    }

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        controller.addPlaybackListener(mPlaybackListener);
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        controller.removePlaybackListener(mPlaybackListener);
        dismiss();
    }

    private final Dispatcher.EventListener mPlaybackListener = new Dispatcher.EventListener() {

        @Override
        public void onEvent(Event event) {
            switch (event.code()) {
                case PlaybackEvent.State.BIND_PLAYER: {
                    if (player() != null) {
                        sync();
                    }
                    break;
                }
                case PlayerEvent.State.STARTED: {
                    syncProgress();
                    break;
                }
                case PlayerEvent.State.PAUSED: {
                    syncProgress();
                    break;
                }
                case PlayerEvent.State.COMPLETED: {
                    syncProgress();
                    dismiss();
                    break;
                }
                case PlayerEvent.State.RELEASED: {
                    dismiss();
                    break;
                }
                case PlayerEvent.Info.PROGRESS_UPDATE: {
                    InfoProgressUpdate e = event.cast(InfoProgressUpdate.class);
                    setProgress(e.currentPosition, e.duration, -1);
                    break;
                }
                case PlayerEvent.Info.BUFFERING_UPDATE: {
                    InfoBufferingUpdate e = event.cast(InfoBufferingUpdate.class);
                    setProgress(-1, -1, e.percent);
                    break;
                }
                case PlayerEvent.Info.TRACK_WILL_CHANGE: {
                    InfoTrackWillChange e = event.cast(InfoTrackWillChange.class);
                    if (e.trackType == Track.TRACK_TYPE_VIDEO) {
                        if (mQuality != null) {
                            final Quality quality;
                            if (e.current != null) {
                                quality = e.current.getQuality();
                            } else {
                                quality = e.target.getQuality();
                            }
                            if (quality != null) {
                                mQuality.setText(quality.getQualityDesc());
                            }
                        }
                    }
                    break;
                }
                case PlayerEvent.Info.TRACK_CHANGED: {
                    InfoTrackChanged e = event.cast(InfoTrackChanged.class);
                    if (e.trackType == Track.TRACK_TYPE_VIDEO) {
                        if (mQuality != null) {
                            Quality quality = e.current.getQuality();
                            if (quality != null) {
                                mQuality.setText(quality.getQualityDesc());
                            }
                        }
                    }
                    break;
                }
            }
        }
    };

    @Override
    public void show() {
        super.show();
        sync();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        syncSubtitleLayer();
    }

    private void sync() {
        syncTheme(getView());
        syncProgress();
        syncQuality();
        syncSpeed();
        syncSubtitle();
        syncSubtitleLayer();
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        sync();
    }

    @Override
    protected void onLayerHostLockStateChanged(boolean locked) {
        sync();
    }
}
