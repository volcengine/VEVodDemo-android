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
 * Create Date : 2021/2/28
 */
package com.bytedance.volc.voddemo.videoview;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;

public class DisplayMode {
    /**
     * fitXY
     */
    public static final int DISPLAY_MODE_DEFAULT = 0;

    /**
     * The screen width is full of controls, and the height is adapted to the video ratio
     */
    public static final int DISPLAY_MODE_ASPECT_FILL_X = 1;

    /**
     * The screen height is full of controls, and the width is adapted to the video ratio
     */
    public static final int DISPLAY_MODE_ASPECT_FILL_Y = 2;
    /**
     * centerInside
     */
    public static final int DISPLAY_MODE_ASPECT_FIT = 3;
    /**
     * centerCrop
     */
    public static final int DISPLAY_MODE_ASPECT_FILL = 4;

    private static final String TAG = "DisplayMode";

    private int videoWidth;
    private int videoHeight;
    private int displayMode = DISPLAY_MODE_DEFAULT;

    private FrameLayout containerView;
    private View displayView;

    public void setVideoSize(int videoWidth, int videoHeight) {
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        apply();
    }

    public void setDisplayMode(int displayMode) {
        this.displayMode = displayMode;
        apply();
    }

    public int getDisplayMode() {
        return this.displayMode;
    }

    public void setContainerView(FrameLayout containerView) {
        this.containerView = containerView;
        apply();
    }

    public void setDisplayView(View displayView) {
        this.displayView = displayView;
        apply();
    }

    public void apply() {
        if (this.displayView == null) return;
        this.displayView.removeCallbacks(applyDisplayMode);
        this.displayView.postOnAnimation(applyDisplayMode);
    }

    private final Runnable applyDisplayMode = this::applyDisplayMode;

    private void applyDisplayMode() {
        final View containerView = this.containerView;
        if (containerView == null) {
            return;
        }

        final int containerWidth = containerView.getWidth();
        final int containerHeight = containerView.getHeight();

        final View displayView = this.displayView;
        if (displayView == null) {
            return;
        }

        final int displayMode = this.displayMode;
        final int videoWidth = this.videoWidth;
        final int videoHeight = this.videoHeight;
        if (videoWidth <= 0 || videoHeight <= 0) {
            return;
        }

        final float videoRatio = videoWidth / (float) videoHeight;
        final float containerRatio = containerWidth / (float) containerHeight;

        final int displayGravity = Gravity.CENTER;
        final int displayWidth;
        final int displayHeight;

        switch (displayMode) {
            case DISPLAY_MODE_DEFAULT:
                displayWidth = containerWidth;
                displayHeight = containerHeight;
                break;
            case DISPLAY_MODE_ASPECT_FILL_X:
                displayWidth = containerWidth;
                displayHeight = (int) (containerWidth / videoRatio);
                break;
            case DISPLAY_MODE_ASPECT_FILL_Y:
                displayWidth = (int) (containerHeight * videoRatio);
                displayHeight = containerHeight;
                break;
            case DISPLAY_MODE_ASPECT_FIT:
                if (videoRatio >= containerRatio) {
                    displayWidth = containerWidth;
                    displayHeight = (int) (containerWidth / videoRatio);
                } else {
                    displayWidth = (int) (containerHeight * videoRatio);
                    displayHeight = containerHeight;
                }
                break;
            case DISPLAY_MODE_ASPECT_FILL:
                if (videoRatio >= containerRatio) {
                    displayWidth = (int) (containerHeight * videoRatio);
                    displayHeight = containerHeight;
                } else {
                    displayWidth = containerWidth;
                    displayHeight = (int) (containerWidth / videoRatio);
                }
                break;
            default:
                throw new IllegalArgumentException("unknown displayMode = " + displayMode);
        }

        final LayoutParams displayLP = (LayoutParams) displayView.getLayoutParams();
        if (displayLP == null) {
            return;
        }

        if (displayLP.height != displayHeight
            || displayLP.width != displayWidth
            || displayLP.gravity != displayGravity) {
            displayLP.gravity = displayGravity;
            displayLP.width = displayWidth;
            displayLP.height = displayHeight;
            displayView.requestLayout();
            TTVideoEngineLog.i(TAG, displayLP.width + "," + displayLP.height);
        }
    }
}
