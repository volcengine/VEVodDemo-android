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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.voddemo.ui.video.scene.longvideo;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.volc.vod.scenekit.utils.UIUtils;


public class LongVideoItemDecoration extends RecyclerView.ItemDecoration {
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final int position = parent.getChildAdapterPosition(view);
        final RecyclerView.Adapter<?> adapter = parent.getAdapter();
        final int type = adapter.getItemViewType(position);
        if (type == LongVideoAdapter.Item.TYPE_HEADER_BANNER) {
            outRect.set(0, 0, 0, 0);
        } else if (type == LongVideoAdapter.Item.TYPE_GROUP_TITLE) {
            outRect.set(0, (int) UIUtils.dip2Px(view.getContext(), 6), 0, 0);
        } else if (type == LongVideoAdapter.Item.TYPE_VIDEO_ITEM) {
            int titlePosition = -1;
            for (int i = position - 1; i >= 0; i--) {
                int viewType = adapter.getItemViewType(i);
                if (viewType == LongVideoAdapter.Item.TYPE_GROUP_TITLE) {
                    titlePosition = i;
                    break;
                }
            }
            int relativePosition = position - titlePosition - 1;
            final int left;
            final int right;
            if (relativePosition % 2 == 0) {
                left = 0;
                right = (int) UIUtils.dip2Px(view.getContext(), 3) / 2;
            } else {
                left = (int) UIUtils.dip2Px(view.getContext(), 3) / 2;
                right = 0;
            }
            outRect.set(left, 0, right, (int) UIUtils.dip2Px(view.getContext(), 14));
        } else {
            outRect.set(0, 0, 0, 0);
        }
    }
}
