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
 * Create Date : 2021/6/10
 */
package com.bytedance.volc.voddemo.smallvideo.pager;

import android.content.Context;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.ss.ttvideoengine.utils.TTVideoEngineLog;

public class PagerLayoutManager extends LinearLayoutManager implements PagerSelectListener {

    private VOLCPagerSnapHelper mPagerSnapHelper;
    private RecyclerViewPagerListener mRecyclerViewPagerListener;
    private RecyclerView mRecyclerView;
    private int mCurrentPosition;

    private final RecyclerView.OnChildAttachStateChangeListener mChildAttachStateChangeListener =
            new RecyclerView.OnChildAttachStateChangeListener() {
                @Override
                public void onChildViewAttachedToWindow(@NonNull View view) {
                    if (mRecyclerViewPagerListener != null && getChildCount() == 1) {
                        mRecyclerViewPagerListener.onInitComplete();
                    }
                }

                @Override
                public void onChildViewDetachedFromWindow(@NonNull View view) {
                    if (mRecyclerViewPagerListener != null) {
                        mRecyclerViewPagerListener.onPageRelease(getPosition(view), view);
                    }
                }
            };

    public PagerLayoutManager(final Context context, final int orientation,
            final boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mPagerSnapHelper = new VOLCPagerSnapHelper();
        mPagerSnapHelper.setCallback(this);
    }

    @Override
    public void onAttachedToWindow(RecyclerView view) {
        if (view == null) {
            throw new IllegalArgumentException("The attach RecycleView must not null!!");
        }
        super.onAttachedToWindow(view);

        if (mPagerSnapHelper == null) {
            mPagerSnapHelper = new VOLCPagerSnapHelper();
        }
        mRecyclerView = view;
        mPagerSnapHelper.attachToRecyclerView(mRecyclerView);
        mRecyclerView.addOnChildAttachStateChangeListener(mChildAttachStateChangeListener);
    }

    @Override
    public void onDetachedFromWindow(RecyclerView view, RecyclerView.Recycler recycler) {
        super.onDetachedFromWindow(view, recycler);
        if (mRecyclerView != null) {
            mRecyclerView.removeOnChildAttachStateChangeListener(mChildAttachStateChangeListener);
        }
    }

    public void setOnViewPagerListener(RecyclerViewPagerListener listener) {
        this.mRecyclerViewPagerListener = listener;
    }

    @Override
    public void onScrollStateChanged(int state) {
        if (state == RecyclerView.SCROLL_STATE_IDLE) {
            View view = mPagerSnapHelper.findSnapView(this);
            int position = 0;
            if (view != null) {
                position = getPosition(view);
            }

            onPageSelected(position, view);
        }
    }

    @Override
    public void onPageSelected(final int position, final View view) {
        if (mCurrentPosition == position) {
            return;
        }

        mCurrentPosition = position;
        if (mRecyclerViewPagerListener != null) {
            mRecyclerViewPagerListener.onPageSelected(position, view);
        }
    }
}
