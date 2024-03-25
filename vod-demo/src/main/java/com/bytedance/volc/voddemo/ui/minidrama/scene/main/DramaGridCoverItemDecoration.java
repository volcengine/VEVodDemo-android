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
 * Create Date : 2024/3/26
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.main;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.volc.vod.scenekit.utils.UIUtils;


public class DramaGridCoverItemDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);

        GridLayoutManager layoutManager = (GridLayoutManager) parent.getLayoutManager();

        final int itemCount = parent.getAdapter().getItemCount();
        final int spanCount = layoutManager.getSpanCount();

        // Grid position. Imagine all items ordered in x/y axis
        int x = position % spanCount;
        int y = position / spanCount;

        int yCount = itemCount % 3 == 0 ? itemCount / 3 : itemCount / 3 + 1;

        // Saved size
        int _16dp = (int) UIUtils.dip2Px(view.getContext(), 16);
        int _8dp = (int) UIUtils.dip2Px(view.getContext(), 8);

        // Conditions in row and column
        if (x == 0) {
            outRect.left = _16dp;
            outRect.right = 0;
        } else if (x == spanCount - 1) {
            outRect.right = _16dp;
            outRect.left = 0;
        } else {
            outRect.left = _8dp;
            outRect.right = _8dp;
        }
        if (y == 0) {
            outRect.top = _16dp;
            outRect.bottom = _16dp/2;
        } else if (y == yCount - 1) {
            outRect.top = _16dp / 2;
            outRect.bottom = _16dp;
        } else {
            outRect.top = _16dp / 2;
            outRect.bottom = _16dp / 2;
        }
    }
}
