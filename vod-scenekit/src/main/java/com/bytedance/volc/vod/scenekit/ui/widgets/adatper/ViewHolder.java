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

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.utils.L;

import java.util.List;

public abstract class ViewHolder extends RecyclerView.ViewHolder {
    public interface Factory {
        @NonNull
        ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType);
    }

    public ViewHolder(@NonNull View itemView) {
        super(itemView);
        itemView.setTag(this);
    }

    public void onViewAttachedToWindow() {
        L.v(this, "onViewAttachedToWindow", getBindingAdapterPosition());
    }

    public void onViewDetachedFromWindow() {
        L.v(this, "onViewDetachedFromWindow", getBindingAdapterPosition());
    }

    public void onViewRecycled() {
        L.v(this, "onViewRecycled", getBindingAdapterPosition());
    }

    public boolean onBackPressed() {
        return false;
    }

    public abstract void bind(List<Item> items, int position);

    public abstract Item getBindingItem();

    public final void executeAction(int action) {
        executeAction(action, null);
    }

    public void executeAction(int action, @Nullable Object o) {
    }
}