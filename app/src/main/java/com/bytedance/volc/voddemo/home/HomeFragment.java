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
package com.bytedance.volc.voddemo.home;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bytedance.volc.voddemo.R;
import com.bytedance.volc.voddemo.base.BaseAdapter;
import com.bytedance.volc.voddemo.base.ShellActivity;
import com.bytedance.volc.voddemo.utils.GridDecoration;
import com.bytedance.volc.voddemo.utils.WeakHandler;
import java.util.ArrayList;
import java.util.List;

import static com.bytedance.volc.voddemo.data.VideoItem.VIDEO_TYPE_SMALL;

public class HomeFragment extends Fragment {

    private static final int ITEM_CLICK = 0;
    private static final int SMALL = 0;
    private static final int GRID_DEFAULT_SPAN_COUNT = 2;

    private boolean mClickable = true;
    private final WeakHandler.IHandler mHandlerListener = msg -> {
        if (msg.what == ITEM_CLICK) {
            mClickable = true;
        }
    };
    private final WeakHandler mWeakHandler = new WeakHandler(mHandlerListener);

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater,
            @Nullable final ViewGroup container,
            @Nullable final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        GridLayoutManager layoutManager = new GridLayoutManager(getContext(),
                GRID_DEFAULT_SPAN_COUNT);
        recyclerView.setLayoutManager(layoutManager);
        int horizontalDividerSize = getResources().getDimensionPixelSize(R.dimen.qb_px_30);
        int verticalDividerSize = getResources().getDimensionPixelSize(R.dimen.qb_px_30);
        recyclerView.addItemDecoration(
                new GridDecoration(horizontalDividerSize, verticalDividerSize,
                        layoutManager.getSpanCount()));

        List<ActionHolder> holders = new ArrayList<>();
        holders.add(new ActionHolder(R.string.small_video, R.drawable.ic_small_video, SMALL));
        BaseAdapter<ActionHolder> adapter = new BaseAdapter<ActionHolder>(holders) {

            @Override
            public int getLayoutId(final int viewType) {
                return R.layout.list_item_fragment_home;
            }

            @SuppressLint("UseCompatLoadingForDrawables")
            @Override
            public void onBindViewHolder(final ViewHolder holder, final ActionHolder data,
                    final int position) {
                TextView tvTitle = holder.getView(R.id.tvTitle);
                tvTitle.setText(getString(data.name));
                ImageView imageView = holder.getView(R.id.imageIcon);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    imageView.setImageDrawable(
                            getResources().getDrawable(data.drawable, requireContext().getTheme()));
                } else {
                    imageView.setImageDrawable(getResources().getDrawable(data.drawable));
                }

                holder.getView(R.id.card).setOnClickListener(v -> {
                    mWeakHandler.sendMessageDelayed(mWeakHandler.obtainMessage(ITEM_CLICK),
                            1000);
                    if (mClickable) {
                        mClickable = false;
                        switch (data.action) {
                            case SMALL:
                                ShellActivity.startNewIntent(getActivity(), VIDEO_TYPE_SMALL);
                                break;
                            default:
                                String msg = getString(R.string.not_implementation);
                                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                                mClickable = false;
                                break;
                        }
                    }
                });
            }
        };
        recyclerView.setAdapter(adapter);
    }

    static class ActionHolder {
        @StringRes
        int name;
        @DrawableRes
        int drawable;
        int action;

        public ActionHolder(final int name, final int drawable, final int action) {
            this.name = name;
            this.drawable = drawable;
            this.action = action;
        }
    }
}
