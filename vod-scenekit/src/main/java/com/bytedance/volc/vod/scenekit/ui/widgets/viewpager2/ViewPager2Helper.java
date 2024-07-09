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

import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

public class ViewPager2Helper {

    public static void setup(ViewPager2 viewPager) {
        RecyclerView recyclerView = null;
        View view = viewPager.getChildAt(0);
        if (view instanceof RecyclerView) {
            recyclerView = (RecyclerView) view;
        }
        if (recyclerView == null) return;
        recyclerView.setScrollingTouchSlop(RecyclerView.TOUCH_SLOP_PAGING);
        recyclerView.setItemAnimator(null);
        recyclerView.setHasFixedSize(true);
    }

    @Nullable
    public static View findItemViewByPosition(ViewPager2 pager, int position) {
        final RecyclerView recyclerView = (RecyclerView) pager.getChildAt(0);
        if (recyclerView != null) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
            if (layoutManager != null) {
                return layoutManager.findViewByPosition(position);
            }
        }
        return null;
    }
}
