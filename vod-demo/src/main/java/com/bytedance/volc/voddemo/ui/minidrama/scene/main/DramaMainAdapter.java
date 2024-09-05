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
 * Create Date : 2024/9/5
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.main;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.scene.theater.DramaTheaterFragment;
import com.bytedance.volc.voddemo.ui.minidrama.scene.recommend.DramaRecommendVideoFragment;

public class DramaMainAdapter extends FragmentStateAdapter {
    private Context mContext;

    public DramaMainAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.mContext = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new DramaTheaterFragment();
        } else {
            return new DramaRecommendVideoFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public String getCurrentItemTitle(int position) {
        if (position == 0) {
            return mContext.getString(R.string.vevod_mini_drama_main_tab_title_theater);
        } else {
            return mContext.getString(R.string.vevod_mini_drama_main_tab_title_recommend);
        }
    }
}
