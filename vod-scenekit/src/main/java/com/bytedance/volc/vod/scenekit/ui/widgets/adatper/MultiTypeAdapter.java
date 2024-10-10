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
 * Create Date : 2024/10/14
 */

package com.bytedance.volc.vod.scenekit.ui.widgets.adatper;

import android.annotation.SuppressLint;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.AdapterListUpdateCallback;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MultiTypeAdapter extends RecyclerView.Adapter<ViewHolder> {
    private final ViewHolder.Factory mFactory;
    private final ArrayList<Item> mItems = new ArrayList<>();

    public MultiTypeAdapter(ViewHolder.Factory factory) {
        this.mFactory = factory;
    }

    public void setItems(List<Item> items, Comparator<Item> comparator) {
        final DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return mItems.size();
            }

            @Override
            public int getNewListSize() {
                return items.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Item oldOne = mItems.get(oldItemPosition);
                Item newOne = items.get(newItemPosition);
                return comparator.compare(oldOne, newOne);
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Item oldOne = mItems.get(oldItemPosition);
                Item newOne = items.get(newItemPosition);
                return Objects.equals(oldOne, newOne);
            }

            @Override
            public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                return new Object();
            }
        }, false);

        diffResult.dispatchUpdatesTo(new AdapterListUpdateCallback(this));
        mItems.clear();
        mItems.addAll(items);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void appendItems(List<Item> items) {
        if (items != null && !items.isEmpty()) {
            int count = mItems.size();
            mItems.addAll(items);
            if (count > 0) {
                notifyItemRangeInserted(count, mItems.size());
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public void deleteItem(int position) {
        if (position >= 0 && position < mItems.size()) {
            mItems.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void deleteItems(int position, int count) {
        if (position >= 0 && position < mItems.size()) {
            List<Item> items = new ArrayList<>(mItems);
            final int toIndex = Math.min(position + count + 1, items.size());
            items.removeAll(items.subList(position, toIndex));
            notifyItemRangeRemoved(position, toIndex - position); // TODO
        }
    }

    public void replaceItem(int position, Item item) {
        if (0 <= position && position < mItems.size()) {
            mItems.set(position, item);
            notifyItemChanged(position, new Object() /*Prevent Adapter calling onCreateViewHolder}*/);
        }
    }

    public void replaceItems(int position, List<Item> items) {
        if (0 <= position && position < mItems.size()) {
            for (int i = 0; i < items.size(); i++) {
                mItems.set(position + i, items.get(i));
            }
            notifyItemRangeChanged(position, items.size(), new Object() /*Prevent Adapter calling onCreateViewHolder}*/);
        }
    }

    public void insertItem(int position, Item item) {
        if (0 <= position && item != null) {
            if (position < mItems.size()) {
                mItems.add(position, item);
            } else if (position == mItems.size()) {
                mItems.add(item);
            }
            notifyItemInserted(position);
        }
    }

    public void insertItems(int position, List<Item> items) {
        if (0 <= position && position < mItems.size() && items != null && !items.isEmpty()) {
            mItems.addAll(position, items);
            notifyItemRangeInserted(position, items.size());
        }
    }

    public int findPosition(Item item, Comparator<Item> comparator) {
        for (int i = 0; i < mItems.size(); i++) {
            Item item2 = mItems.get(i);
            if (comparator.compare(item, item2)) {
                return i;
            }
        }
        return -1;
    }

    public List<Item> getItems() {
        return mItems;
    }

    @Nullable
    public Item getItem(int position) {
        if (position < mItems.size()) {
            return mItems.get(position);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        final Item item = mItems.get(position);
        return item.itemType();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return mFactory.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mItems, position);
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        holder.onViewAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        holder.onViewDetachedFromWindow();
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        holder.onViewRecycled();
    }
}
