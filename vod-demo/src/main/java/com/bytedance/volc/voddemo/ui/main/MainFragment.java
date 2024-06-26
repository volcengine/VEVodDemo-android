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

package com.bytedance.volc.voddemo.ui.main;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.volc.vod.scenekit.ui.base.BaseFragment;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.scene.main.DramaMainActivity;
import com.bytedance.volc.voddemo.ui.settings.SettingsActivity;
import com.bytedance.volc.voddemo.ui.video.scene.VideoActivity;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends BaseFragment {

    public static final String EXTRA_SHOW_ACTION_BAR = "extra_show_back";

    private final List<Item> mItems = new ArrayList<>();

    private boolean mShowActionBar;

    private RecyclerView mRecyclerView;

    public static MainFragment newInstance(Bundle bundle) {
        MainFragment fragment = new MainFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle bundle = getArguments();
        if (bundle != null) {
            mShowActionBar = bundle.getBoolean(EXTRA_SHOW_ACTION_BAR, false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.vevod_main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.actionBar).setVisibility(mShowActionBar ? View.VISIBLE : View.GONE);
        View back = view.findViewById(R.id.actionBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mRecyclerView.setAdapter(new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.vevod_main_fragment_item, parent, false);
                return new RecyclerView.ViewHolder(rootView) { /* ignore */
                };
            }

            @Override
            public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
                Item item = mItems.get(position);
                TextView title = holder.itemView.findViewById(R.id.title);
                ImageView image = holder.itemView.findViewById(R.id.image);

                title.setText(item.title);
                if (item.drawable != 0) {
                    image.setImageDrawable(ResourcesCompat.getDrawable(image.getResources(), item.drawable, null));
                } else {
                    image.setImageDrawable(null);
                }
                holder.itemView.setOnClickListener(v -> {
                    switch (item.type) {
                        case Item.TYPE_PLAY_SCENE:
                            VideoActivity.intentInto(getActivity(), item.playScene);
                            break;
                        case Item.TYPE_SETTINGS:
                            SettingsActivity.intentInto(getActivity());
                            break;
                        case Item.TYPE_MINI_DRAMA:
                            DramaMainActivity.intentInto(getActivity());
                            break;
                    }
                });
            }

            @Override
            public int getItemCount() {
                return mItems.size();
            }
        });
        mItems.clear();
        mItems.addAll(createItems());
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private static List<Item> createItems() {
        final List<Item> items = new ArrayList<>();
        items.add(new Item(R.string.vevod_mini_drama, R.drawable.vevod_main_list_item_mini_drama_ic, Item.TYPE_MINI_DRAMA,
                PlayScene.SCENE_UNKNOWN));
        items.add(new Item(R.string.vevod_short_video_with_desc, R.drawable.vevod_main_scene_list_item_short_ic, Item.TYPE_PLAY_SCENE,
                PlayScene.SCENE_SHORT));
        items.add(new Item(R.string.vevod_feed_video_with_desc, R.drawable.vevod_main_scene_list_item_feed_ic, Item.TYPE_PLAY_SCENE,
                PlayScene.SCENE_FEED));
        items.add(new Item(R.string.vevod_long_video, R.drawable.vevod_main_scene_list_item_long_ic, Item.TYPE_PLAY_SCENE,
                PlayScene.SCENE_LONG));
        items.add(new Item(R.string.vevod_settings, R.drawable.vevod_main_list_item_settings, Item.TYPE_SETTINGS,
                PlayScene.SCENE_UNKNOWN));
        return items;
    }

    static class Item {
        static final int TYPE_PLAY_SCENE = 0;
        static final int TYPE_MINI_DRAMA = 1;
        static final int TYPE_SETTINGS = 2;

        @StringRes
        int title;
        @DrawableRes
        int drawable;
        int playScene;
        int type;

        Item(int name, int drawable, int type, int playScene) {
            this.title = name;
            this.drawable = drawable;
            this.type = type;
            this.playScene = playScene;
        }
    }
}
