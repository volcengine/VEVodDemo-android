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

package com.bytedance.playerkit.player.ui.utils;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;

public class GestureHelper implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {

    private final GestureDetectorCompat mGestureDetector;
    private final ScaleGestureDetector mScaleDetector;

    public GestureHelper(Context context) {
        mGestureDetector = new GestureDetectorCompat(context, this);
        mScaleDetector = new ScaleGestureDetector(context, this);
    }

    public boolean onTouchEvent(View v, MotionEvent event) {
        boolean handle = mGestureDetector.onTouchEvent(event) ||
                (mScaleDetector.onTouchEvent(event) && event.getAction() != MotionEvent.ACTION_DOWN);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            handle = onUp(event) || handle;
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            handle = onCancel(event) || handle;
        }
        return handle;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onUp(MotionEvent e) {
        return false;
    }

    public boolean onCancel(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return false;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }
}
