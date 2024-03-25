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
 * Create Date : 2024/3/28
 */

package com.bytedance.volc.voddemo.ui.minidrama.widgets;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.volc.vod.scenekit.ui.base.BaseBottomDialogFragment;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.utils.AdaptiveSpacingItemDecoration;

import java.util.Locale;

public class DramaEpisodeSelectDialogFragment extends BaseBottomDialogFragment {

    public static final String ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK = "action_drama_episode_select_dialog_click";
    public static final String EXTRA_EPISODE_VIDEO = "extra_episode_video";
    public static final String EXTRA_EPISODE_INDEX = "extra_episode_index";

    public static DramaEpisodeSelectDialogFragment newInstance(EpisodeVideo episodeVideo) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_EPISODE_VIDEO, episodeVideo);
        DramaEpisodeSelectDialogFragment fragment = new DramaEpisodeSelectDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private EpisodeVideo mEpisode;
    private TextView mTitleView;
    private TextView mDescView;
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mEpisode = (EpisodeVideo) bundle.getSerializable(EXTRA_EPISODE_VIDEO);
        }
        mAdapter = new ItemAdapter() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                ViewHolder holder = super.onCreateViewHolder(parent, viewType);
                holder.itemView.setOnClickListener(v -> {
                    onEpisodeNumberItemClick(holder);
                });
                return holder;
            }
        };
    }

    private void onEpisodeNumberItemClick(ItemAdapter.ViewHolder holder) {
        int position = holder.getAbsoluteAdapterPosition();
        if (position < 0) return;

        Intent intent = new Intent(ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK);
        intent.putExtra(EXTRA_EPISODE_INDEX, position);
        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);

        mAdapter.setPlayingIndex(position);
        holder.itemView.postDelayed(this::dismiss, 100); // delay 100MS would make selection changing be visible
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_mini_drama_episode_select_dialog_fragment;
    }

    @Override
    public int getTheme() {
        return R.style.VEVodAppTheme_BottomSheetDialog_EpisodeSelector;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.setMinimumHeight(UIUtils.getScreenHeight(requireActivity()) / 5 * 3);

        mTitleView = view.findViewById(R.id.title);
        mDescView = view.findViewById(R.id.desc);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 6));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new AdaptiveSpacingItemDecoration((int) UIUtils.dip2Px(requireActivity(), 12), false));

        int itemCount = 0;
        int playingIndex = 0;
        String title = null;
        String desc = null;
        if (mEpisode != null && mEpisode.episodeInfo != null) {
            playingIndex = mEpisode.episodeInfo.episodeNumber - 1;
            if (mEpisode.episodeInfo.dramaInfo != null) {
                itemCount = mEpisode.episodeInfo.dramaInfo.totalEpisodeNumber;
                title = mEpisode.episodeInfo.dramaInfo.dramaTitle;
                desc = String.format(Locale.getDefault(), getString(R.string.vevod_mini_drama_episode_select_dialog_title_desc), itemCount);
            }
        }

        mTitleView.setText(title);
        mDescView.setText(desc);
        mAdapter.setItemCount(itemCount);
        mAdapter.setPlayingIndex(playingIndex);

        view.findViewById(R.id.close).setOnClickListener(v -> dismiss());
    }

    public void setPlayingIndex(int playingIndex) {
        if (mAdapter != null) {
            mAdapter.setPlayingIndex(playingIndex);
        }
    }

    private static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        private int mPlayingIndex;
        private int mItemCount;

        public void setItemCount(int itemCount) {
            mItemCount = itemCount;
            notifyDataSetChanged();
        }

        public void setPlayingIndex(int playIndex) {
            mPlayingIndex = playIndex;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_mini_drama_episode_select_dialog_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position, mPlayingIndex);
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView indexView;
            public final ImageView playingView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                indexView = itemView.findViewById(R.id.indexView);
                playingView = itemView.findViewById(R.id.playingView);
            }

            public void bind(int position, int playingIndex) {
                indexView.setText(String.valueOf(position + 1));
                playingView.setVisibility(position == playingIndex ? View.VISIBLE : View.GONE);
                itemView.setSelected(position == playingIndex);
            }
        }
    }
}
