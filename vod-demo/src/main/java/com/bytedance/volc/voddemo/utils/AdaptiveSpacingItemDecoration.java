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
 * Create Date : 2024/3/27
 */

package com.bytedance.volc.voddemo.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

public class AdaptiveSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private static final int NO_SPACING = 0;
    private final int mSize;
    private final boolean mEdgeEnabled;

    public AdaptiveSpacingItemDecoration(int size, boolean edgeEnabled) {
        this.mSize = size;
        this.mEdgeEnabled = edgeEnabled;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        // Separate layout type
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            makeGridSpacing(
                    outRect,
                    parent.getChildAdapterPosition(view),
                    state.getItemCount(),
                    ((GridLayoutManager) layoutManager).getOrientation(),
                    ((GridLayoutManager) layoutManager).getSpanCount(),
                    ((GridLayoutManager) layoutManager).getReverseLayout()
            );
        } else if (layoutManager instanceof LinearLayoutManager) {
            int linearOrientation = ((LinearLayoutManager) layoutManager).getOrientation();
            // Flag whether item positioning is reversed (more like flipped) or not. So, if normally item is
            // written from left to right (horizontally), then it will be right to left (whatever item index is)
            // and if item is written from top to bottom (vertically), then it will be from bottom to top.
            boolean isReversed = ((LinearLayoutManager) layoutManager).getReverseLayout() ^ ((LinearLayoutManager) layoutManager).getStackFromEnd();
            makeLinearSpacing(
                    outRect,
                    parent.getChildAdapterPosition(view),
                    state.getItemCount(),
                    linearOrientation,
                    isReversed
            );
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            makeGridSpacing(
                    outRect,
                    parent.getChildAdapterPosition(view),
                    state.getItemCount(),
                    ((StaggeredGridLayoutManager) layoutManager).getOrientation(),
                    ((StaggeredGridLayoutManager) layoutManager).getSpanCount(),
                    ((StaggeredGridLayoutManager) layoutManager).getReverseLayout()
            );
        }
    }

    private void makeLinearSpacing(
            Rect outRect,
            int position,
            int itemCount,
            int orientation,
            boolean isReversed
    ) {
        // Basic item positioning
        boolean isLastPosition = position == (itemCount - 1);
        boolean isFirstPosition = position == 0;
        // Saved size
        int sizeBasedOnEdge = mEdgeEnabled ? mSize : NO_SPACING;
        int sizeBasedOnFirstPosition = isFirstPosition ? sizeBasedOnEdge : mSize;
        int sizeBasedOnLastPosition = isLastPosition ? sizeBasedOnEdge : NO_SPACING;
        switch (orientation) {
            case RecyclerView.HORIZONTAL:
                outRect.left = isReversed ? sizeBasedOnLastPosition : sizeBasedOnFirstPosition;
                outRect.top = sizeBasedOnEdge;
                outRect.right = isReversed ? sizeBasedOnFirstPosition : sizeBasedOnLastPosition;
                outRect.bottom = sizeBasedOnEdge;
                break;
            case RecyclerView.VERTICAL:
                outRect.left = sizeBasedOnEdge;
                outRect.top = isReversed ? sizeBasedOnLastPosition : sizeBasedOnFirstPosition;
                outRect.right = sizeBasedOnEdge;
                outRect.bottom = isReversed ? sizeBasedOnFirstPosition : sizeBasedOnLastPosition;
                break;
        }
    }

    private void makeGridSpacing(
            Rect outRect,
            int position,
            int itemCount,
            int orientation,
            int spanCount,
            boolean isReversed
    ) {
        // Basic item positioning
        boolean isLastPosition = position == (itemCount - 1);
        int sizeBasedOnEdge = mEdgeEnabled ? mSize : NO_SPACING;
        int sizeBasedOnLastPosition = isLastPosition ? sizeBasedOnEdge : mSize;
        // Opposite of spanCount (find layout depth)
        int subsideCount = itemCount % spanCount == 0 ? itemCount / spanCount : (itemCount / spanCount) + 1;
        // Grid position. Imagine all items ordered in x/y axis
        int xAxis = orientation == RecyclerView.HORIZONTAL ? position / spanCount : position % spanCount;
        int yAxis = orientation == RecyclerView.HORIZONTAL ? position % spanCount : position / spanCount;
        // Conditions in row and column
        boolean isFirstColumn = xAxis == 0;
        boolean isFirstRow = yAxis == 0;
        boolean isLastColumn = orientation == RecyclerView.HORIZONTAL ? xAxis == subsideCount - 1 : xAxis == spanCount - 1;
        boolean isLastRow = orientation == RecyclerView.HORIZONTAL ? yAxis == spanCount - 1 : yAxis == subsideCount - 1;
        // Saved size
        int sizeBasedOnFirstColumn = isFirstColumn ? sizeBasedOnEdge : NO_SPACING;
        int sizeBasedOnLastColumn = !isLastColumn ? sizeBasedOnLastPosition : sizeBasedOnEdge;
        int sizeBasedOnFirstRow = isFirstRow ? sizeBasedOnEdge : NO_SPACING;
        int sizeBasedOnLastRow = !isLastRow ? mSize : sizeBasedOnEdge;
        switch (orientation) {
            case RecyclerView.HORIZONTAL: // Row fixed. Number of rows is spanCount
                outRect.left = isReversed ? sizeBasedOnLastColumn : sizeBasedOnFirstColumn;
                outRect.top = mEdgeEnabled ? mSize * (spanCount - yAxis) / spanCount : mSize * yAxis / spanCount;
                outRect.right = isReversed ? sizeBasedOnFirstColumn : sizeBasedOnLastColumn;
                outRect.bottom = mEdgeEnabled ? mSize * (yAxis + 1) / spanCount : mSize * (spanCount - (yAxis + 1)) / spanCount;
                break;
            case RecyclerView.VERTICAL: // Column fixed. Number of columns is spanCount
                outRect.left = mEdgeEnabled ? mSize * (spanCount - xAxis) / spanCount : mSize * xAxis / spanCount;
                outRect.top = isReversed ? sizeBasedOnLastRow : sizeBasedOnFirstRow;
                outRect.right = mEdgeEnabled ? mSize * (xAxis + 1) / spanCount : mSize * (spanCount - (xAxis + 1)) / spanCount;
                outRect.bottom = isReversed ? sizeBasedOnFirstRow : sizeBasedOnLastRow;
                break;
        }
    }


}



