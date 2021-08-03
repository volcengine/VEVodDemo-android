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
 * Create Date : 2021/2/25
 */
package com.bytedance.volc.voddemo.videoview.layers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.utils.TimeUtils;
import com.bytedance.volc.voddemo.utils.UIUtils;
import com.bytedance.volc.voddemo.videoview.layer.BaseVideoLayer;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.CommonLayerEvent;
import com.bytedance.volc.voddemo.videoview.layer.ILayer;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerCommand;
import com.bytedance.volc.voddemo.videoview.layer.IVideoLayerEvent;
import com.bytedance.volc.voddemo.widget.ByteSeekBar;
import java.util.ArrayList;
import java.util.List;

public class SmallToolbarLayer extends BaseVideoLayer
        implements ByteSeekBar.OnByteSeekBarChangeListener {
    private static final int DURATION_SHOW = 30000;

    private ImageView mPlayBtn;
    private Animation mAnimation;

    private LinearLayout mLlSeek;
    private ByteSeekBar mSeekBar;
    private TextView mCurrentTv, mDurationTv;

    private float mSeekToPercent;

    private final ArrayList<Integer> mSupportEvents = new ArrayList<Integer>() {
        {
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PAUSE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PLAYING);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_PROGRESS_CHANGE);
            add(IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_UPDATE);
        }
    };

    @Override
    public int getZIndex() {
        return ILayer.SMALL_TOOLBAR_Z_INDEX;
    }

    @SuppressLint("InflateParams")
    @Override
    protected View getLayerView(final Context context, @NonNull LayoutInflater inflater) {
        return inflater.inflate(R.layout.layer_small_toolbar, null);
    }

    @Override
    protected void setupViews() {
        mPlayBtn = mLayerView.findViewById(R.id.img_play);
        UIUtils.setViewVisibility(mPlayBtn, View.GONE);
        mLayerView.setClickable(true);
        mLayerView.setFocusable(true);
        mPlayBtn.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.btn_play));

        mLlSeek = mLayerView.findViewById(R.id.ll_seek);
        mCurrentTv = mLayerView.findViewById(R.id.current_tv);
        mDurationTv = mLayerView.findViewById(R.id.duration_tv);
        mSeekBar = mLayerView.findViewById(R.id.seek_bar);
        mSeekBar.setOnByteSeekBarChangeListener(this);
        UIUtils.setViewVisibility(mLlSeek, View.GONE);

        mLayerView.setOnClickListener((v) -> {
            if (mHost.isPaused()) {
                mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_PLAY));
            } else {
                mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_PAUSE));
            }
        });
        mLayerView.setOnLongClickListener((v) -> mHost.notifyEvent(
                new CommonLayerEvent(IVideoLayerEvent.VIDEO_LAYER_EVENT_TOGGLE_DEBUG_TOOL)));
    }

    @Override
    public void refresh() {
        mCurrentTv.setText(TimeUtils.milliSecondsToTimer(0));
        int duration = mHost.getVideoController().getDuration();
        mDurationTv.setText(TimeUtils.milliSecondsToTimer(duration));
    }

    @NonNull
    @Override
    public List<Integer> getSupportEvents() {
        return mSupportEvents;
    }

    @Override
    public boolean handleVideoEvent(@NonNull IVideoLayerEvent event) {
        switch (event.getType()) {
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PAUSE:
                showBtn();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PLAY_PLAYING:
                UIUtils.setViewVisibility(mPlayBtn, View.GONE);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_RENDER_START:
                showSeekBar();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_VIDEO_PRE_RELEASE:
                UIUtils.setViewVisibility(mPlayBtn, View.GONE);
                resetSeekBar();
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_PROGRESS_CHANGE:
                // noinspection unchecked
                Pair<Integer, Integer> pair = (Pair<Integer, Integer>) event.getParam();
                updatePlayProcess(pair.first, pair.second);
                break;
            case IVideoLayerEvent.VIDEO_LAYER_EVENT_BUFFER_UPDATE:
                int percent = event.getParam(Integer.class);
                updateBufferProcess(percent);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void onProgressChanged(final ByteSeekBar seekBar, final float progress,
            final boolean fromUser, final float xVelocity) {
        if (fromUser) {
            mSeekToPercent = progress;
            int current = getSeekPos(progress);
            if (mCurrentTv != null) {
                mCurrentTv.setText(TimeUtils.milliSecondsToTimer(current));
            }
        }
    }

    @Override
    public void onStartTrackingTouch(final ByteSeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(final ByteSeekBar seekBar) {
        int seekTo = getSeekPos(mSeekToPercent);
        mHost.execCommand(new CommonLayerCommand(IVideoLayerCommand.VIDEO_HOST_CMD_SEEK, seekTo));
    }

    private void showBtn() {
        if (mAnimation == null) {
            mAnimation = new ScaleAnimation(2.0f, 1.0f, 2.0f, 1.0f, Animation.RELATIVE_TO_SELF,
                    0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            mAnimation.setDuration(160);
        }
        if (mPlayBtn != null) {
            mPlayBtn.startAnimation(mAnimation);
        }
        UIUtils.setViewVisibility(mPlayBtn, View.VISIBLE);
    }

    private void resetSeekBar() {
        mSeekBar.setProgress(0);
        mSeekBar.setSecondaryProgress(0);
        UIUtils.setViewVisibility(mLlSeek, View.GONE);
    }

    private void showSeekBar() {
        if (mHost.getVideoController().getDuration() >= DURATION_SHOW) {
            UIUtils.setViewVisibility(mLlSeek, View.VISIBLE);
        } else {
            UIUtils.setViewVisibility(mLlSeek, View.GONE);
        }
    }

    private int getSeekPos(float percent) {
        int duration = mHost.getVideoController().getDuration();
        int seekPos = 0;
        if (duration > 0) {
            seekPos = (int) (percent * duration * 1.0f / 100.0f);
        }
        return seekPos;
    }

    private void updateBufferProcess(int percent) {
        mSeekBar.setSecondaryProgress(percent);
    }

    private void updatePlayProcess(long current, long duration) {
        if (mDurationTv != null) {
            mDurationTv.setText(TimeUtils.milliSecondsToTimer(duration));
        }
        if (mCurrentTv != null) {
            mCurrentTv.setText(TimeUtils.milliSecondsToTimer(current));
        }
        if (mSeekBar != null) {
            mSeekBar.setProgress(TimeUtils.timeToPercent(current, duration));
        }
    }
}
