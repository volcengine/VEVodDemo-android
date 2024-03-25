/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/4/16
 */

package com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.viewpager2.widget.ViewPager2;

/**
 * 解决 ViewPager2 内包含相同滑动方向子 View 滑动冲突问题。
 */
public class ViewPager2ChildFixSameDirectionScrollLayout extends FrameLayout {
    private int mTouchSlop;
    private int mPagingTouchSlop;
    private float mInitX;
    private float mInitY;
    private ViewPager2 mParentPager;
    private View mTargetView;

    public ViewPager2ChildFixSameDirectionScrollLayout(Context context) {
        super(context);
        init(context);
    }

    public ViewPager2ChildFixSameDirectionScrollLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mPagingTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
        mParentPager = findParentViewPager();
        mTargetView = getChildCount() > 0 ? getChildAt(0) : null;
    }

    public void setTargetView(View view) {
        this.mTargetView = view;
    }

    private ViewPager2 findParentViewPager() {
        View v = (View) getParent();
        while (v != null && !(v instanceof ViewPager2)) {
            v = (View) v.getParent();
        }
        return (ViewPager2) v;
    }

    private boolean canChildScroll(int orientation, float delta) {
        int direction = (int) -Math.signum(delta);
        if (orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
            return mTargetView != null && mTargetView.canScrollHorizontally(direction);
        } else if (orientation == ViewPager2.ORIENTATION_VERTICAL) {
            return mTargetView != null && mTargetView.canScrollVertically(direction);
        } else {
            throw new IllegalArgumentException("orientation is not found");
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent e) {
        handleInterceptTouchEvent(e);
        return super.onInterceptTouchEvent(e);
    }

    private void handleInterceptTouchEvent(MotionEvent e) {
        if (mParentPager == null) return;

        int viewpagerOrientation = mParentPager.getOrientation();

        if (!canChildScroll(viewpagerOrientation, -1f) && !canChildScroll(viewpagerOrientation, 1f)) {
            return;
        }

        if (e.getAction() == MotionEvent.ACTION_DOWN) {
            mInitX = e.getX();
            mInitY = e.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        } else if (e.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = e.getX() - mInitX;
            float dy = e.getY() - mInitY;
            float absDx = Math.abs(dx);
            float absDy = Math.abs(dy);
            boolean isVpHorizontal = viewpagerOrientation == ViewPager2.ORIENTATION_HORIZONTAL;

            if (isVpHorizontal) {
                if (absDy > mTouchSlop) {
                    if (absDy > absDx * 0.5) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    if (absDx > mPagingTouchSlop) {
                        if (!canChildScroll(viewpagerOrientation, dx)) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }
            } else {
                if (absDx > mTouchSlop) {
                    if (absDx > absDy * 0.5) {
                        getParent().requestDisallowInterceptTouchEvent(false);
                    }
                } else {
                    if (absDy > mPagingTouchSlop) {
                        if (!canChildScroll(viewpagerOrientation, dy)) {
                            getParent().requestDisallowInterceptTouchEvent(false);
                        }
                    }
                }
            }
        }
    }
}



