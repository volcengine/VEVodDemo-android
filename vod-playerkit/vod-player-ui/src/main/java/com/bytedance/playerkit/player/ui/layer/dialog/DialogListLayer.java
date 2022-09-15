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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.player.ui.layer.dialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.ui.R;
import com.bytedance.playerkit.player.ui.layer.base.DialogLayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public abstract class DialogListLayer<T> extends DialogLayer {

    private final Adapter<T> mAdapter;
    private CharSequence mTitle;

    public DialogListLayer() {
        this.mAdapter = new Adapter<>();
    }

    public void setTitle(CharSequence title) {
        this.mTitle = title;
    }

    protected Adapter<T> adapter() {
        return mAdapter;
    }

    @Nullable
    @Override
    protected View createDialogView(@NonNull ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_list_layer, parent, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(parent.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(mAdapter);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateDismiss();
            }
        });

        View listPanel = view.findViewById(R.id.listPanel);
        listPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do nothing
            }
        });

        listPanel.setBackground(new DialogBackgroundDrawable(parent.getContext()));

        TextView title = view.findViewById(R.id.title);
        if (title != null) {
            title.setText(mTitle);
        }
        return view;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, RecyclerView.ViewHolder holder);
    }

    public static class Item<T> {
        public final T obj;
        public final String text;

        public Item(T obj, String text) {
            this.obj = obj;
            this.text = text;
        }
    }

    public static class Adapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<Item<T>> mItems = new ArrayList<>();

        private OnItemClickListener mListener;
        private Item<T> mSelected;

        public void setOnItemClickListener(OnItemClickListener listener) {
            this.mListener = listener;
        }

        public void setList(List<Item<T>> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        public void setSelected(Item<T> item) {
            if (mSelected != item) {
                mSelected = item;
                notifyDataSetChanged();
            }
        }

        public Item<T> findItem(T obj) {
            for (Item<T> item : mItems) {
                if (Objects.equals(obj, item.obj)) return item;
            }
            return null;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_list_layer_item, parent, false);
            RecyclerView.ViewHolder holder = new RecyclerView.ViewHolder(item) {
            };
            holder.itemView.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onItemClick(holder.getBindingAdapterPosition(), holder);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Item<T> item = mItems.get(position);
            TextView text = holder.itemView.findViewById(R.id.text);
            text.setText(item.text);
            holder.itemView.setSelected(Objects.equals(mSelected, item));
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public Item<T> getItem(int position) {
            return mItems.get(position);
        }
    }

}
