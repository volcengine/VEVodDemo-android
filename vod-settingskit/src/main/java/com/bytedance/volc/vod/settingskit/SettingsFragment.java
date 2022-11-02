/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.settingskit;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SettingsFragment extends Fragment {

    private Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private List<SettingItem> mItems;

    public static final String EXTRA_SETTINGS_KEY = "EXTRA_SETTINGS_KEY";

    public static Fragment newInstance(String settingsKey) {
        Fragment fragment = new SettingsFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_SETTINGS_KEY, settingsKey);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(0, 100, 0, "重置");
        menuItem.setVisible(true);
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 100) {
            showResetOptionsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String key = requireArguments().getString("EXTRA_SETTINGS_KEY");
        mItems = Settings.get(key);
        mAdapter = new Adapter();
        if (mItems != null) {
            mAdapter.setItems(mItems);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        mRecyclerView = new RecyclerView(container.getContext());
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setBackgroundColor(Color.parseColor("#F7F8FA"));
        return mRecyclerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private void showResetOptionsDialog() {
        new AlertDialog.Builder(requireActivity())
                .setTitle("重置所有选项?")
                .setCancelable(true)
                .setPositiveButton("重置", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resetOptions();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void resetOptions() {
        for (SettingItem settingItem : mItems) {
            if (settingItem.type == SettingItem.TYPE_OPTION) {
                settingItem.option.userValues().saveValue(settingItem.option, null);
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    private static class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private final List<SettingItem> mItems = new ArrayList<>();

        public void setItems(List<SettingItem> items) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return ItemViewHolder.create(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            final SettingItem item = mItems.get(position);
            ((ItemViewHolder) holder).bind(item, position);
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (item.type == SettingItem.TYPE_OPTION && item.option != null) {
                        showOptionItemResetDialog(v.getContext(), item, holder);
                        return true;
                    }
                    return false;
                }
            });
        }

        private void showOptionItemResetDialog(Context context, SettingItem settingItem, RecyclerView.ViewHolder holder) {
            new AlertDialog.Builder(context)
                    .setTitle("重置 \"" + settingItem.option.title + "\" ?")
                    .setCancelable(true)
                    .setPositiveButton("重置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settingItem.option.userValues().saveValue(settingItem.option, null);
                            notifyItemChanged(holder.getAbsoluteAdapterPosition());
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            SettingItem item = mItems.get(position);
            if (item.type == SettingItem.TYPE_OPTION) {
                return item.option.type;
            } else {
                return item.type;
            }
        }

        abstract static class ItemViewHolder extends RecyclerView.ViewHolder {

            public ItemViewHolder(@NonNull View itemView) {
                super(itemView);
            }

            public static RecyclerView.ViewHolder create(ViewGroup parent, int viewType) {
                switch (viewType) {
                    case SettingItem.TYPE_CATEGORY_TITLE:
                        return CategoryTitleHolder.create(parent);
                    case Option.TYPE_RATIO_BUTTON:
                        return RatioButtonViewHolder.create(parent);
                    case Option.TYPE_SELECTABLE_ITEMS:
                        return SelectableItemsViewHolder.create(parent);
                    case Option.TYPE_EDITABLE_TEXT:
                        return EditableTextViewHolder.create(parent);
                    case SettingItem.TYPE_COPYABLE_TEXT:
                        return CopyableTextViewHolder.create(parent);
                    case SettingItem.TYPE_CLICKABLE_ITEM:
                        return ClickableViewHolder.create(parent);
                    default:
                        throw new IllegalArgumentException("Unsupported viewType " + viewType);
                }
            }

            abstract void bind(SettingItem item, int position);

            static void bindText(TextView textView, SettingItem item) {
                if (item.getter != null) {
                    if (item.getter.directGetter == null) {
                        item.getter.asyncGetter.get(textView::setText);
                        textView.setText(null);
                    } else {
                        textView.setText((String) item.getter.directGetter.get());
                    }
                } else {
                    textView.setText(null);
                }
            }
        }

        static class CategoryTitleHolder extends ItemViewHolder {
            private final TextView categoryTitle;

            public CategoryTitleHolder(@NonNull View itemView) {
                super(itemView);
                categoryTitle = (TextView) itemView;
            }

            @Override
            void bind(SettingItem item, int position) {
                categoryTitle.setText(item.category);
                if (position == 0) {
                    ((ViewGroup.MarginLayoutParams) categoryTitle.getLayoutParams()).topMargin = (int) Utils.dip2Px(itemView.getContext(), 10);
                } else {
                    ((ViewGroup.MarginLayoutParams) categoryTitle.getLayoutParams()).topMargin = (int) Utils.dip2Px(itemView.getContext(), 24);
                }
            }

            static CategoryTitleHolder create(ViewGroup parent) {
                return new CategoryTitleHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_item_category_title, parent, false));
            }
        }

        static class RatioButtonViewHolder extends ItemViewHolder {
            private final TextView titleView;
            private final SwitchCompat switchView;

            public RatioButtonViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.itemTitle);
                switchView = itemView.findViewById(R.id.switchView);
            }

            @Override
            void bind(SettingItem item, int position) {
                Boolean value = item.option.value(Boolean.class);
                switchView.setOnCheckedChangeListener(null);
                if (value == null) {
                    switchView.setChecked(false);
                    switchView.setEnabled(false);
                } else {
                    switchView.setChecked(value);
                    switchView.setEnabled(true);
                }
                switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        item.option.userValues().saveValue(item.option, isChecked);
                    }
                });

                titleView.setText(item.option.title);
            }

            static RatioButtonViewHolder create(ViewGroup parent) {
                return new RatioButtonViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_item_ratio_button, parent, false));
            }
        }

        static class SelectableItemsViewHolder extends ItemViewHolder {
            private final TextView titleView;
            private final TextView valueView;

            public SelectableItemsViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.itemTitle);
                valueView = itemView.findViewById(R.id.valueView);
            }

            @Override
            void bind(SettingItem item, int position) {
                titleView.setText(item.option.title);
                valueView.setText(item.mapper.toString(item.option.value()));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showSelectableItemsDialog(v.getContext(), item, SelectableItemsViewHolder.this);
                    }
                });
            }

            public static void showSelectableItemsDialog(Context context, SettingItem settingItem, RecyclerView.ViewHolder holder) {
                Option option = settingItem.option;
                int index = option.candidates.indexOf(option.value());
                String[] items = new String[option.candidates.size()];
                for (int i = 0; i < items.length; i++) {
                    items[i] = settingItem.mapper.toString(option.candidates.get(i));
                }
                new AlertDialog.Builder(context)
                        .setTitle(option.title)
                        .setCancelable(true)
                        .setSingleChoiceItems(items, index, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Object o = option.candidates.get(which);
                                option.userValues().saveValue(option, o);
                                RecyclerView.Adapter<?> adapter = holder.getBindingAdapter();
                                if (adapter != null) {
                                    adapter.notifyItemChanged(holder.getAbsoluteAdapterPosition());
                                }
                                dialog.cancel();
                            }
                        })
                        .show();
            }

            static SelectableItemsViewHolder create(ViewGroup parent) {
                return new SelectableItemsViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_item_selectable_items, parent, false));
            }
        }

        static class EditableTextViewHolder extends ItemViewHolder {
            private final TextView titleView;
            private final TextView valueView;

            public EditableTextViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.itemTitle);
                valueView = itemView.findViewById(R.id.valueView);
            }

            @Override
            void bind(SettingItem item, int position) {
                titleView.setText(item.option.title);
                valueView.setText(item.mapper.toString(item.option.value()));
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showEditableDialog(v.getContext(), item, EditableTextViewHolder.this);
                    }
                });
            }

            public static void showEditableDialog(Context context, SettingItem item, RecyclerView.ViewHolder holder) {
                EditText editText = new EditText(context);
                editText.setText(item.mapper.toString(item.option.value()));
                FrameLayout frameLayout = new FrameLayout(context);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.leftMargin = (int) Utils.dip2Px(context, 20);
                lp.rightMargin = (int) Utils.dip2Px(context, 20);
                editText.setLayoutParams(lp);
                frameLayout.addView(editText);
                new AlertDialog.Builder(context)
                        .setCancelable(true)
                        .setTitle("编辑 \"" + item.option.title + "\"")
                        .setView(frameLayout)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Editable value = editText.getText();
                                if (!TextUtils.isEmpty(value)) {
                                    item.option.userValues().saveValue(item.option, value);
                                    RecyclerView.Adapter<?> adapter = holder.getBindingAdapter();
                                    if (adapter != null) {
                                        adapter.notifyItemChanged(holder.getAbsoluteAdapterPosition());
                                    }
                                }
                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }

            static EditableTextViewHolder create(ViewGroup parent) {
                return new EditableTextViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_item_editable_text, parent, false));
            }
        }

        static class CopyableTextViewHolder extends ItemViewHolder {

            private final TextView titleView;
            private final TextView textView;
            private final TextView copyView;

            public CopyableTextViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.itemTitle);
                textView = itemView.findViewById(R.id.text);
                copyView = itemView.findViewById(R.id.copy);
            }

            @Override
            void bind(SettingItem item, int position) {
                titleView.setText(item.title);
                bindText(textView, item);
                copyView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboardManager = (ClipboardManager) v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        CharSequence c = textView.getText();
                        if (TextUtils.isEmpty(c)) {
                            return;
                        }
                        ClipData clipData = ClipData.newPlainText(v.getContext().getPackageName(), c);
                        clipboardManager.setPrimaryClip(clipData);

                        Toast.makeText(v.getContext(), "Text copy done!", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            static CopyableTextViewHolder create(ViewGroup parent) {
                return new CopyableTextViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_item_copyable_text, parent, false));
            }
        }

        static class ClickableViewHolder extends ItemViewHolder {
            private final TextView titleView;
            private final TextView valueView;

            public ClickableViewHolder(@NonNull View itemView) {
                super(itemView);
                titleView = itemView.findViewById(R.id.itemTitle);
                valueView = itemView.findViewById(R.id.valueView);
            }

            @Override
            void bind(SettingItem item, int position) {
                titleView.setText(item.title);
                bindText(valueView, item);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.listener.onEvent(SettingItem.OnEventListener.EVENT_TYPE_CLICK, v.getContext(), item, ClickableViewHolder.this);
                    }
                });
            }

            static ClickableViewHolder create(ViewGroup parent) {
                return new ClickableViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.settings_item_clickable_item, parent, false));
            }
        }
    }
}
