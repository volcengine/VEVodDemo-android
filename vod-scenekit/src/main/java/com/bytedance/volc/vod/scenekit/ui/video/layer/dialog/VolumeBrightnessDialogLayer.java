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
 * Create Date : 2021/12/3
 */

package com.bytedance.volc.vod.scenekit.ui.video.layer.dialog;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.core.math.MathUtils;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.playback.PlaybackController;
import com.bytedance.playerkit.player.playback.VideoLayerHost;

import com.bytedance.volc.vod.scenekit.ui.video.layer.GestureLayer;
import com.bytedance.volc.vod.scenekit.ui.video.layer.Layers;
import com.bytedance.volc.vod.scenekit.ui.video.layer.base.DialogLayer;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.volc.vod.scenekit.R;

import java.lang.ref.WeakReference;


public class VolumeBrightnessDialogLayer extends DialogLayer {
    public static final int TYPE_VOLUME = 0;
    public static final int TYPE_BRIGHTNESS = 1;

    private SeekBar mSeekBar;
    private View mSeekBarContainer;
    private int mType;

    private VolumeReceiver mVolume;

    @Override
    public String tag() {
        return "volume_brightness";
    }

    @Override
    protected View createDialogView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_volume_brightness_layer, parent, false);
        mSeekBarContainer = view.findViewById(R.id.seekBarContainer);
        mSeekBar = view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    animateShow(true);
                    if (mType == TYPE_VOLUME) {
                        setVolumeByProgress(progress);
                    } else {
                        setBrightnessByProgress(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                animateDismiss();
            }
        });
        mSeekBar.setMax(100);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateDismiss();
            }
        });

        setAnimateDismissListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                GestureLayer layer = layerHost().findLayer(GestureLayer.class);
                if (layer != null) {
                    layer.showController();
                }
            }
        });
        return view;
    }

    @Override
    protected int backPressedPriority() {
        return Layers.BackPriority.VOLUME_BRIGHTNESS_DIALOG_BACK_PRIORITY;
    }

    @Override
    public void onVideoViewPlaySceneChanged(int fromScene, int toScene) {
        if (playScene() != PlayScene.SCENE_FULLSCREEN) {
            dismiss();
        }
    }

    @Override
    protected void onBindLayerHost(@NonNull VideoLayerHost layerHost) {
        super.onBindLayerHost(layerHost);
        if (mVolume == null) {
            mVolume = new VolumeReceiver(this);
        }
        mVolume.register(activity());
    }

    @Override
    protected void onUnbindLayerHost(@NonNull VideoLayerHost layerHost) {
        super.onUnbindLayerHost(layerHost);
        if (mVolume != null) {
            mVolume.unregister(activity());
        }
    }

    @Override
    protected void onBindPlaybackController(@NonNull PlaybackController controller) {
        controller.addPlaybackListener(mPlaybackListener);
    }

    @Override
    protected void onUnbindPlaybackController(@NonNull PlaybackController controller) {
        controller.removePlaybackListener(mPlaybackListener);
    }

    private final Dispatcher.EventListener mPlaybackListener = event -> {
        switch (event.code()) {
            case PlayerEvent.State.RELEASED:
                dismiss();
                break;
        }
    };

    public void setBrightnessByProgress(int progress) {
        Activity activity = activity();
        if (activity == null) return;

        Window window = activity.getWindow();
        if (window == null) return;

        WindowManager.LayoutParams params = window.getAttributes();
        if (params == null) return;

        L.v(this, "setBrightnessByProgress", progress);

        params.screenBrightness = mapProgress2Brightness(progress);
        window.setAttributes(params);

        if (mSeekBar != null) {
            mSeekBar.setProgress(progress);
        }
    }

    public int getBrightByProgress() {
        Activity activity = activity();
        if (activity == null) return 0;

        Window window = activity.getWindow();
        if (window == null) return 0;

        WindowManager.LayoutParams params = window.getAttributes();
        if (params == null) return 0;

        float brightness = params.screenBrightness == -1 ? UIUtils.getSystemBrightness(activity) : params.screenBrightness;

        return Math.max(mapBrightness2Progress(brightness), 0);
    }

    public void setVolumeByProgress(int progress) {
        L.v(this, "setVolumeByProgress", progress);
        final Player player = player();
        if (player != null) {
            float volume = mapProgress2Volume(progress);
            player.setVolume(volume, volume);
        }
    }

    public int getVolumeByProgress() {
        final Player player = player();
        if (player != null) {
            float[] volume = player.getVolume();
            float left = volume[0];

            return Math.max(mapVolume2Progress(left), 0);
        }
        return 0;
    }

    public void setType(int type) {
        mType = type;
    }

    @Override
    public void show() {
        super.show();
        if (mType == TYPE_VOLUME) {
            syncVolume();
        } else {
            syncBrightness();
        }
    }

    private void syncVolume() {
        createView();

        final Player player = player();
        if (player != null) {
            float[] volume = player.getVolume();
            float left = volume[0];
            mSeekBar.setProgress(mapVolume2Progress(left));
        } else {
            mSeekBar.setProgress(50);
        }

        if (mSeekBarContainer == null) return;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSeekBarContainer.getLayoutParams();
        if (lp != null) {
            lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            lp.leftMargin = 0;
            lp.rightMargin = (int) UIUtils.dip2Px(context(), 64);
            mSeekBarContainer.setLayoutParams(lp);
        }
    }

    private void syncBrightness() {
        createView();

        Activity activity = activity();
        if (activity == null) return;

        Window window = activity.getWindow();
        if (window == null) return;

        WindowManager.LayoutParams params = window.getAttributes();
        if (params == null) return;

        float brightness = params.screenBrightness == -1 ? UIUtils.getSystemBrightness(activity) : params.screenBrightness;
        mSeekBar.setProgress(MathUtils.clamp(mapBrightness2Progress(brightness), 0, 100));

        if (mSeekBarContainer == null) return;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSeekBarContainer.getLayoutParams();
        if (lp != null) {
            lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            lp.leftMargin = (int) UIUtils.dip2Px(context(), 64);
            lp.rightMargin = 0;
            mSeekBarContainer.setLayoutParams(lp);
        }
    }

    public static int mapVolume2Progress(float volume) {
        return (int) (volume * 100);
    }

    public static float mapProgress2Volume(int progress) {
        return progress / 100f;
    }

    public static int mapBrightness2Progress(float brightness) {
        return (int) (brightness * 100);
    }

    public static float mapProgress2Brightness(int progress) {
        return progress / 100f;
    }

    private static class VolumeReceiver extends BroadcastReceiver {

        public static String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
        public static String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";

        private final WeakReference<VolumeBrightnessDialogLayer> mRef;

        private boolean mRegistered;

        void register(Context context) {
            if (mRegistered) return;

            mRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(VOLUME_CHANGED_ACTION);
            context.getApplicationContext().registerReceiver(this, filter);
        }

        void unregister(Context context) {
            if (!mRegistered) return;
            mRegistered = false;
            context.getApplicationContext().unregisterReceiver(this);
        }

        VolumeReceiver(VolumeBrightnessDialogLayer layer) {
            mRef = new WeakReference<>(layer);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final int volumeStreamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1);
            if (TextUtils.equals(action, VOLUME_CHANGED_ACTION) && volumeStreamType == AudioManager.STREAM_MUSIC) {
                VolumeBrightnessDialogLayer layer = mRef.get();
                if (layer != null) {
                    layer.syncVolume();
                }
            }
        }
    }
}
