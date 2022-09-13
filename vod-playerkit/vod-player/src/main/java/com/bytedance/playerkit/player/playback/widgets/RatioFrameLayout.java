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

package com.bytedance.playerkit.player.playback.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.R;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class RatioFrameLayout extends FrameLayout {

    public static final int RATIO_MODE_FIXED_WIDTH = 0;
    public static final int RATIO_MODE_FIXED_HEIGHT = 1;

    private float mRatio;

    @RatioMode
    private int mRatioMode;

    /**
     * Ratio mode of {@link RatioFrameLayout}. One of {@link #RATIO_MODE_FIXED_WIDTH} or
     * {@link #RATIO_MODE_FIXED_HEIGHT}
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            RATIO_MODE_FIXED_WIDTH,
            RATIO_MODE_FIXED_HEIGHT,
    })
    public @interface RatioMode {
    }

    public RatioFrameLayout(@NonNull Context context) {
        super(context);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RatioFrameLayout, defStyleAttr, 0);
        try {
            mRatioMode = a.getInt(R.styleable.RatioFrameLayout_ratioMode, RATIO_MODE_FIXED_WIDTH);
            mRatio = a.getFloat(R.styleable.RatioFrameLayout_ratio, 0f);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRatio <= 0 || mRatioMode < 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            if (mRatioMode == RATIO_MODE_FIXED_WIDTH) {
                final int width = MeasureSpec.getSize(widthMeasureSpec);
                final int height = (int) (width / mRatio);
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
            } else if (mRatioMode == RATIO_MODE_FIXED_HEIGHT) {
                final int height = MeasureSpec.getSize(heightMeasureSpec);
                final int width = (int) (height * mRatio);
                super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), heightMeasureSpec);
            } else {
                throw new IllegalArgumentException("unsupported ratio mode! " + mRatioMode);
            }
        }
    }

    /**
     * @param ratio width/height
     */
    public void setRatio(float ratio) {
        if (mRatio != ratio) {
            this.mRatio = ratio;
            requestLayout();
        }
    }

    public float getRatio() {
        return mRatio;
    }

    public int getRatioMode() {
        return mRatioMode;
    }

    public void setRatioMode(@RatioMode int ratioMode) {
        if (mRatioMode != ratioMode) {
            this.mRatioMode = ratioMode;
            requestLayout();
        }
    }
}
