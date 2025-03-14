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

package com.bytedance.volc.voddemo.ui.minidrama.scene.detail;

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

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.ui.base.BaseBottomDialogFragment;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.data.remote.model.drama.DramaInfo;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.data.business.model.DramaItem;
import com.bytedance.volc.voddemo.utils.AdaptiveSpacingItemDecoration;

import java.util.List;
import java.util.Locale;

public class DramaEpisodeSelectDialogFragment extends BaseBottomDialogFragment {

    public static final String ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK = "action_drama_episode_select_dialog_click";
    public static final String EXTRA_ITEM = "extra_video_item";
    public static final String EXTRA_DRAMA_ITEM = "extra_drama_item";

    public static DramaEpisodeSelectDialogFragment newInstance(DramaItem dramaItem) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_DRAMA_ITEM, dramaItem);
        DramaEpisodeSelectDialogFragment fragment = new DramaEpisodeSelectDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private DramaItem mDramaItem;
    private TextView mTitleView;
    private TextView mDescView;
    private RecyclerView mRecyclerView;
    private ItemAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mDramaItem = (DramaItem) bundle.getSerializable(EXTRA_DRAMA_ITEM);
        }
        if (mDramaItem == null
                || mDramaItem.episodeVideoItems == null
                || mDramaItem.episodeVideoItems.isEmpty()) {
            dismiss();
            return;
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
        VideoItem videoItem = (VideoItem) holder.itemView.getTag();
        if (videoItem == null) return;

        Intent intent = new Intent(ACTION_DRAMA_EPISODE_SELECT_DIALOG_EPISODE_NUMBER_ITEM_CLICK);
        intent.putExtra(EXTRA_ITEM, videoItem);
        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
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
        view.findViewById(R.id.actionBarClose).setOnClickListener(v -> dismiss());

        mTitleView = view.findViewById(R.id.title);
        mDescView = view.findViewById(R.id.desc);

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 6));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(new AdaptiveSpacingItemDecoration((int) UIUtils.dip2Px(requireActivity(), 12), false));

        setDramaItem(mDramaItem);
    }

    public void setDramaItem(DramaItem dramaItem) {
        if (dramaItem == null) {
            return;
        }
        mDramaItem = dramaItem;

        if (mAdapter != null) {
            mAdapter.setDramaItem(mDramaItem);
        }

        DramaInfo dramaInfo = mDramaItem.dramaInfo;
        if (dramaInfo != null) {
            mTitleView.setText(dramaInfo.dramaTitle);
            mDescView.setText(String.format(Locale.getDefault(), getString(R.string.vevod_mini_drama_episode_select_dialog_title_desc), dramaInfo.totalEpisodeNumber));
        }
    }

    private static class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

        private DramaItem mDramaItem;

        public void setDramaItem(DramaItem dramaItem) {
            this.mDramaItem = dramaItem;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_mini_drama_episode_select_dialog_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position, mDramaItem);
        }

        @Override
        public int getItemCount() {
            if (mDramaItem != null && mDramaItem.dramaInfo != null) {
                return mDramaItem.dramaInfo.totalEpisodeNumber;
            }
            return 0;
        }


        static class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView indexView;
            public final ImageView playingView;
            public final ImageView lockView;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                indexView = itemView.findViewById(R.id.indexView);
                playingView = itemView.findViewById(R.id.playingView);
                lockView = itemView.findViewById(R.id.lockView);
            }

            public void bind(int position, DramaItem dramaItem) {
                if (dramaItem == null) return;
                if (dramaItem.episodeVideoItems == null) return;

                final int itemIndex = adapterPosition2ItemIndex(position, dramaItem.episodeVideoItems);
                if (itemIndex >= dramaItem.episodeVideoItems.size() || itemIndex < 0) {
                    return;
                }
                final Item item = dramaItem.episodeVideoItems.get(itemIndex);
                if (!(item instanceof VideoItem)) return;
                if (!(dramaItem.currentItem instanceof VideoItem)) return;

                final int episodeNumber = EpisodeVideo.getEpisodeNumber(EpisodeVideo.get(item));
                final int currentEpisodeNumber = EpisodeVideo.getEpisodeNumber(EpisodeVideo.get(dramaItem.currentItem));

                indexView.setText(String.valueOf(episodeNumber));
                playingView.setVisibility(episodeNumber == currentEpisodeNumber ? View.VISIBLE : View.GONE);
                itemView.setSelected(episodeNumber == currentEpisodeNumber);
                lockView.setVisibility(EpisodeVideo.isLocked((VideoItem) item) ? View.VISIBLE : View.GONE);

                itemView.setTag(item);
            }
        }
    }

    private static int episodeNumber2AdapterPosition(int episodeNumber) {
        return episodeNumber - 1;
    }

    private static int adapterPosition2EpisodeNumber(int adapterPosition) {
        return adapterPosition + 1;
    }

    private static int adapterPosition2ItemIndex(int adapterPosition, List<Item> items) {
        int episodeNumber = adapterPosition2EpisodeNumber(adapterPosition);
        for (int i = 0; i < items.size(); i++) {
            Item item = items.get(i);
            if (item instanceof VideoItem) {
                if (episodeNumber == EpisodeVideo.getEpisodeNumber(EpisodeVideo.get(item))) {
                    return i;
                }
            }
        }
        return -1;
    }
}
