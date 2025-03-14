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
 * Create Date : 2025/3/19
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.pipvideo;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import androidx.annotation.Keep;
import androidx.cardview.widget.CardView;

import com.bytedance.volc.vod.scenekit.R;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;

public class PipWindowView extends CardView {
    private final int mTouchSlop;
    private final WindowManager mWindow;
    private final WindowManager.LayoutParams mParams;

    private boolean mInitShow = true;
    private int mWindowInitWidth;
    private int mWindowInitHeight;
    private int mWindowInitX;
    private int mWindowInitY;
    private int mWindowMargin;

    private Animator mAnimator;

    private float mDownX;
    private float mDownY;
    private float mTouchX;
    private float mTouchY;

    public PipWindowView(Context context) {
        this(context, null);
    }

    public PipWindowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PipWindowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mWindow = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mParams = createPipLayoutParams();
    }

    public void setInitShow(boolean initShow) {
        this.mInitShow = initShow;
    }

    public void setWindowInitWidth(int windowInitWidth) {
        this.mWindowInitWidth = windowInitWidth;
    }

    public void setWindowInitHeight(int windowInitHeight) {
        this.mWindowInitHeight = windowInitHeight;
    }

    public void setWindowInitX(int windowInitX) {
        this.mWindowInitX = windowInitX;
    }

    public void setWindowInitY(int windowInitY) {
        this.mWindowInitY = windowInitY;
    }

    public void setWindowMargin(int windowMargin) {
        this.mWindowMargin = windowMargin;
    }

    public void requestWindowLayout() {
        mWindow.updateViewLayout(this, mParams);
    }

    private static WindowManager.LayoutParams createPipLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        // 设置窗口类型，不同的 android 版本对于不同的窗口类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  // >= 8.0
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // >= 7.0
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) { // >= 4.4
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        // 设置 Gravity
        params.gravity = Gravity.TOP | Gravity.START;
        // 设置为透明 format
        params.format = PixelFormat.TRANSLUCENT;
        // 窗口动画
        params.windowAnimations = R.style.vevod_pip_animation;
        // 窗口 flag
        params.flags = WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        return params;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mDownX = mTouchX = event.getRawX();
                mDownY = mTouchY = event.getRawY();
                cancelAnimator();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float touchX = event.getRawX();
                float touchY = event.getRawY();
                final float dx = touchX - mDownX;
                final float dy = touchY - mDownY;
                mTouchX = touchX;
                mTouchY = touchY;
                if (Math.max(Math.abs(dx), Math.abs(dy)) >= mTouchSlop) {
                    return true;
                }
                break;
            }
        }
        return super.onInterceptTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN: {
                mTouchX = event.getRawX();
                mTouchY = event.getRawY();
                cancelAnimator();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float touchX = event.getRawX();
                float touchY = event.getRawY();
                final float dx = touchX - mTouchX;
                final float dy = touchY - mTouchY;
                mTouchX = touchX;
                mTouchY = touchY;
                if (dx != 0 || dy != 0) {
                    mParams.x = (int) (mParams.x + dx + 0.5f);
                    mParams.y = (int) (mParams.y + dy + 0.5f);
                    mWindow.updateViewLayout(this, mParams);
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                mDownX = mTouchX = 0;
                mDownY = mTouchY = 0;
                animateToNearestScreenEdge();
                break;
            }
        }
        return super.onTouchEvent(event);
    }

    private void animateToNearestScreenEdge() {
        cancelAnimator();

        int screenWidth = UIUtils.getScreenWidth(getContext());
        int screenHeight = UIUtils.getScreenHeight(getContext());

        int targetX = (mParams.x + mParams.width / 2) < screenWidth / 2 ? 0 : screenWidth - mParams.width;
        targetX = Math.max(targetX, mWindowMargin);
        targetX = Math.min(targetX, screenWidth - mParams.width - mWindowMargin);

        int targetY = Math.min(Math.max(mParams.y, 0), screenHeight - mParams.height);
        targetY = Math.max(targetY, mWindowMargin);
        targetY = Math.min(targetY, screenHeight - mParams.height - mWindowMargin);

        if (mParams.x == targetX && mParams.y == targetY) return;

        int duration;
        float ratioDX = Math.abs(targetX - mParams.x) / (float) mParams.width;
        float ratioDY = Math.abs(targetY - mParams.y) / (float) mParams.height;
        if (ratioDX > ratioDY) {
            duration = (int) (ratioDX * 500);
        } else {
            duration = (int) (ratioDY * 500);
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(
                ObjectAnimator.ofInt(new Wrapper(this), "x", mParams.x, targetX),
                ObjectAnimator.ofInt(new Wrapper(this), "y", mParams.y, targetY)
        );
        animatorSet.setDuration(duration);
        animatorSet.start();
        mAnimator = animatorSet;
    }

    private void cancelAnimator() {
        if (mAnimator != null) {
            mAnimator.removeAllListeners();
            mAnimator.cancel();
            mAnimator = null;
        }
    }

    public boolean isShowing() {
        return getParent() != null;
    }

    public void show() {
        if (isShowing()) return;
        if (mInitShow) {
            mParams.x = mWindowInitX;
            mParams.y = mWindowInitY;
            mParams.width = mWindowInitWidth;
            mParams.height = mWindowInitHeight;
            mInitShow = false;
        }
        mWindow.addView(this, mParams);
    }

    public void dismiss() {
        if (!isShowing()) return;
        mWindow.removeView(this);
    }

    static class Wrapper {

        final PipWindowView mRef;

        Wrapper(PipWindowView ref) {
            this.mRef = ref;
        }

        @Keep
        public void setX(int x) {
            mRef.mParams.x = x;
            mRef.requestWindowLayout();
        }

        @Keep
        public int getX() {
            return mRef.mParams.x;
        }

        @Keep
        public void setY(int y) {
            mRef.mParams.y = y;
            mRef.requestWindowLayout();
        }

        @Keep
        public int getY() {
            return mRef.mParams.y;
        }

        @Keep
        public void setWidth(int width) {
            mRef.mParams.width = width;
            mRef.requestWindowLayout();
        }

        @Keep
        public int getWidth() {
            return mRef.mParams.width;
        }

        @Keep
        public void setHeight(int height) {
            mRef.mParams.height = height;
            mRef.requestWindowLayout();
        }

        @Keep
        public int getHeight() {
            return mRef.mParams.height;
        }
    }
}
