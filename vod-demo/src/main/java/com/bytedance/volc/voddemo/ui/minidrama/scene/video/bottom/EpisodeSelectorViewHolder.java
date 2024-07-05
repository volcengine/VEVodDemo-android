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
 * Create Date : 2024/7/8
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.video.bottom;

import android.view.View;
import android.widget.TextView;

import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.impl.R;

import java.util.Locale;

public class EpisodeSelectorViewHolder {
    public final View mSelectEpisodeView;
    public final TextView mSelectEpisodeDesc;

    public EpisodeSelectorViewHolder(View view) {
        mSelectEpisodeView = view.findViewById(R.id.bottomBarCardSelectEpisode);
        mSelectEpisodeDesc = view.findViewById(R.id.selectEpisodeDesc);
    }

    public void bind(DramaInfo drama) {
        mSelectEpisodeDesc.setText(String.format(Locale.getDefault(), mSelectEpisodeDesc.getResources().getString(R.string.vevod_mini_drama_video_detail_select_episode_number_desc), drama.totalEpisodeNumber));
    }
}
