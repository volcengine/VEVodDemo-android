/*
 * Copyright 2021 bytedance
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
 * Create Date : 2021/2/25
 */
package com.bytedance.volc.voddemo.base;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseAdapter<T> extends RecyclerView.Adapter<BaseAdapter.ViewHolder> {
    private final List<T> mDatas = new ArrayList();

    public BaseAdapter(List<T> datas) {
        if (datas != null) {
            mDatas.addAll(datas);
        }
    }

    public abstract int getLayoutId(int viewType);

    public abstract void onBindViewHolder(ViewHolder holder, T data, int position);

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(getLayoutId(viewType), parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        onBindViewHolder(holder, mDatas.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public T getItem(int position) {
        return mDatas.get(position);
    }

    public void setData(List<T> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
    }

    public void replaceItem(int index, T item) {
        mDatas.set(index, item);
    }

    public void replaceAll(List<T> datas) {
        mDatas.clear();
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public void addAll(List<T> datas) {
        mDatas.addAll(datas);
        notifyDataSetChanged();
    }

    public List<T> getAll() {
        return mDatas;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View mItemView;
        private final SparseArray<View> mViews = new SparseArray<>();

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public <T extends View> T getView(@IdRes int resourceId) {
            View view = mViews.get(resourceId);
            if (view == null) {
                T t = mItemView.findViewById(resourceId);
                mViews.put(resourceId, t);
                view = t;
            }
            return (T) view;
        }
    }
}



