/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/5/26
 */
package com.bytedance.vodplayer.example.view;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class RatioFrameLayout extends FrameLayout {

    public static final int RATIO_BY_WIDTH = 0;
    public static final int RATIO_BY_HEIGHT = 1;

    private float mRatio;

    private int mRatioBy;

    public RatioFrameLayout(@NonNull Context context) {
        super(context);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RatioFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs,
                            int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mRatio <= 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            if (mRatioBy == 0) {
                final int width = MeasureSpec.getSize(widthMeasureSpec);
                super.onMeasure(widthMeasureSpec,
                        MeasureSpec.makeMeasureSpec((int) (width / mRatio), MeasureSpec.EXACTLY));
            } else {
                final int height = MeasureSpec.getSize(heightMeasureSpec);
                super.onMeasure(
                        MeasureSpec.makeMeasureSpec((int) (height * mRatio), MeasureSpec.EXACTLY),
                        heightMeasureSpec);
            }
        }
    }

    public void setRatioBy(int ratioBy) {
        if (mRatioBy != ratioBy) {
            mRatioBy = ratioBy;
            requestLayout();
        }
    }

    public void setRatio(float ratio) {
        if (mRatio != ratio) {
            this.mRatio = ratio;
            requestLayout();
        }
    }

    public float getRatio() {
        return mRatio;
    }
}
