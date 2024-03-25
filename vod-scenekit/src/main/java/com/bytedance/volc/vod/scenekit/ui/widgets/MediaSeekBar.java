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

package com.bytedance.volc.vod.scenekit.ui.widgets;

import static com.bytedance.volc.vod.scenekit.utils.TimeUtils.time2String;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bytedance.volc.vod.scenekit.R;


public class MediaSeekBar extends RelativeLayout {

    private final TextView text1;
    private final SeekBar seekBar;
    private final TextView text2;

    private boolean mTouchSeeking;
    private long mDuration;

    private OnUserSeekListener mOnUserSeekListener;

    public interface OnUserSeekListener {
        void onUserSeekStart(long startPosition);

        void onUserSeekPeeking(long peekPosition);

        void onUserSeekStop(long startPosition, long seekToPosition);
    }

    public MediaSeekBar(Context context) {
        this(context, null);
    }

    public MediaSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MediaSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(getContext()).inflate(R.layout.vevod_media_player_seekbar, this);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        seekBar = findViewById(R.id.seekBar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            int mStartSeekProgress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                final float percent = progress / (float) seekBar.getMax();
                final long currentPosition = (int) (percent * mDuration);
                text1.setText(time2String(currentPosition));
                text2.setText(time2String(mDuration));

                if (!mTouchSeeking) return;
                if (mOnUserSeekListener != null && fromUser) {
                    mOnUserSeekListener.onUserSeekPeeking(currentPosition);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mTouchSeeking) return;
                mTouchSeeking = true;
                mStartSeekProgress = seekBar.getProgress();
                final float startSeekPercent = mStartSeekProgress / (float) seekBar.getMax();
                final long startSeekPosition = (long) (startSeekPercent * mDuration);

                if (mOnUserSeekListener != null) {
                    mOnUserSeekListener.onUserSeekStart(startSeekPosition);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (!mTouchSeeking) return;
                mTouchSeeking = false;
                final float startSeekPercent = mStartSeekProgress / (float) seekBar.getMax();
                final float currentPercent = seekBar.getProgress() / (float) seekBar.getMax();

                final long startSeekPosition = (long) (startSeekPercent * mDuration);
                final long currentPosition = (long) (currentPercent * mDuration);

                if (mOnUserSeekListener != null) {
                    mOnUserSeekListener.onUserSeekStop(startSeekPosition, currentPosition);
                }
            }
        });
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
        this.seekBar.setMax((int) Math.max(mDuration, 100));
        text2.setText(time2String(mDuration));
    }

    public void setCurrentPosition(long currentPosition) {
        if (!mTouchSeeking) {
            final int progress = (int) (currentPosition / (float) mDuration * seekBar.getMax());
            seekBar.setProgress(progress);
        }
    }

    public void setCachePercent(int cachePercent) {
        seekBar.setSecondaryProgress((int) (cachePercent * (seekBar.getMax() / 100f)));
    }

    public void setOnSeekListener(OnUserSeekListener listener) {
        this.mOnUserSeekListener = listener;
    }

    public void setSeekEnabled(boolean enabled) {
        seekBar.setEnabled(enabled);
    }

    public void setTextVisibility(boolean visibility) {
        text1.setVisibility(visibility ? VISIBLE : GONE);
        text2.setVisibility(visibility ? VISIBLE : GONE);
    }
}
