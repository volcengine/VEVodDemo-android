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

package com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.utils.ItemHelper;
import com.bytedance.volc.vod.scenekit.ui.video.scene.PlayScene;
import com.bytedance.volc.vod.scenekit.ui.video.viewholder.ViewHolderAction;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Comparator;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.MultiTypeAdapter;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.OnPageChangeCallbackCompat;
import com.bytedance.volc.vod.scenekit.ui.widgets.viewpager2.ViewPager2Helper;

import java.util.List;

public class ShortVideoPageView extends FrameLayout implements LifecycleEventObserver, Dispatcher.EventListener {
    private final ViewPager2 mViewPager;
    private final MultiTypeAdapter mShortVideoAdapter;
    private Lifecycle mLifeCycle;
    private ViewHolder.Factory mViewHolderFactory;
    private boolean mInterceptStartPlaybackOnResume;
    private ViewHolder mCurrentHolder;

    public ShortVideoPageView(@NonNull Context context) {
        this(context, null);
    }

    public ShortVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ShortVideoPageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mViewPager = new ViewPager2(context);
        ViewPager2Helper.setup(mViewPager);
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setOrientation(ViewPager2.ORIENTATION_VERTICAL);
        mShortVideoAdapter = new MultiTypeAdapter(new ShortVideoViewHolderFactory() {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                if (mViewHolderFactory != null) {
                    return mViewHolderFactory.onCreateViewHolder(parent, viewType);
                }
                return super.onCreateViewHolder(parent, viewType);
            }
        });
        mViewPager.setAdapter(mShortVideoAdapter);
        mViewPager.registerOnPageChangeCallback(new OnPageChangeCallbackCompat(mViewPager) {
            @Override
            public void onPageSelected(ViewPager2 pager, int position) {
                super.onPageSelected(pager, position);
                togglePlayback(position);
            }

            @Override
            public void onPagePeekStart(ViewPager2 pager, int position, int peekPosition) {
                super.onPagePeekStart(pager, position, peekPosition);
                final ViewHolder holder = findItemViewHolderByPosition(pager, peekPosition);
                if (holder == null) return;
                holder.executeAction(ViewHolderAction.ACTION_VIEW_PAGER_ON_PAGE_PEEK_START, new Object[]{pager, position, peekPosition});
            }
        });
        addView(mViewPager, new LayoutParams(LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void onEvent(Event event) {

    }

    public ViewPager2 viewPager() {
        return mViewPager;
    }

    public void setLifeCycle(Lifecycle lifeCycle) {
        if (mLifeCycle != lifeCycle) {
            if (mLifeCycle != null) {
                mLifeCycle.removeObserver(this);
            }
            mLifeCycle = lifeCycle;
        }
        if (mLifeCycle != null) {
            mLifeCycle.addObserver(this);
        }
    }

    public void setViewHolderFactory(ViewHolder.Factory factory) {
        this.mViewHolderFactory = factory;
    }

    public void setItems(List<Item> items) {
        L.d(this, "setItems", items == null ? -1: items.size(), ItemHelper.dump(items));

        VideoItem.playScene(VideoItem.findVideoItems(items), PlayScene.SCENE_SHORT);
        mShortVideoAdapter.setItems(items, ItemHelper.comparator()); // TODO
        ShortVideoStrategy.setItems(items);

        play();
    }

    public void prependItems(List<Item> items) {
        if (items == null) return;

        L.d(this, "prependItems", items.size(), ItemHelper.dump(items));

        VideoItem.playScene(VideoItem.findVideoItems(items), PlayScene.SCENE_SHORT);
        mShortVideoAdapter.insertItems(0, items);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
    }

    public void appendItems(List<Item> items) {
        if (items == null) return;

        L.d(this, "appendItems", ItemHelper.dump(items));

        VideoItem.playScene(VideoItem.findVideoItems(items), PlayScene.SCENE_SHORT);
        mShortVideoAdapter.appendItems(items);
        ShortVideoStrategy.appendItems(items);
    }

    public void deleteItem(int position) {
        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;

        final int currentPosition = getCurrentItem();
        final Item item = mShortVideoAdapter.getItem(position);
        L.d(this, "deleteItem", position, ItemHelper.dump(item));

        mShortVideoAdapter.deleteItem(position);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (currentPosition == position) {
            play();
        }
    }

    public void deleteItems(int position, int count) {
        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;

        final int currentPosition = getCurrentItem();
        L.d(this, "deleteItems", position, count);
        mShortVideoAdapter.deleteItems(position, count);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (position <= currentPosition && currentPosition < position + count) {
            play();
        }
    }

    public void replaceItem(int position, Item item) {
        if (item == null) return;
        if (position >= mShortVideoAdapter.getItemCount() || position < 0) return;
        VideoItem.playScene(VideoItem.findVideoItem(item), PlayScene.SCENE_SHORT);
        L.d(this, "replaceItem", position, ItemHelper.dump(item));
        final int currentPosition = getCurrentItem();
        mShortVideoAdapter.replaceItem(position, item);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (currentPosition == position) {
            play();
        }
    }

    public void replaceItems(int position, List<Item> items) {
        if (items == null) return;
        if (mShortVideoAdapter.getItemCount() <= position || position < 0) return;
        VideoItem.playScene(VideoItem.findVideoItems(items), PlayScene.SCENE_SHORT);
        L.d(this, "replaceItems", position, items.size(), ItemHelper.dump(items));
        final int currentPosition = getCurrentItem();
        mShortVideoAdapter.replaceItems(position, items);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (position <= currentPosition && currentPosition < position + items.size()) {
            play();
        }
    }

    public void insertItem(int position, Item item) {
        if (item == null) return;
        if (mShortVideoAdapter.getItemCount() <= position || position < 0) return;
        VideoItem.playScene(VideoItem.findVideoItem(item), PlayScene.SCENE_SHORT);
        L.d(this, "insertItem", position, ItemHelper.dump(item));
        final int currentPosition = getCurrentItem();
        mShortVideoAdapter.insertItem(position, item);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (currentPosition == position) {
            play();
        }
    }

    public void insertItems(int position, List<Item> items) {
        if (items == null) return;
        if (mShortVideoAdapter.getItemCount() <= position || position < 0) return;
        VideoItem.playScene(VideoItem.findVideoItems(items), PlayScene.SCENE_SHORT);
        L.d(this, "insertItems", position, items.size(), ItemHelper.dump(items));
        final int currentPosition = getCurrentItem();
        mShortVideoAdapter.insertItems(position, items);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (position <= currentPosition && currentPosition < position + items.size()) {
            play();
        }
    }

    public List<Item> getItems() {
        return mShortVideoAdapter.getItems();
    }

    public Item getItem(int position) {
        return mShortVideoAdapter.getItem(position);
    }

    public int getItemCount() {
        return mShortVideoAdapter.getItemCount();
    }

    public int getItemViewType(int position) {
        return mShortVideoAdapter.getItemViewType(position);
    }

    public void setCurrentItem(int position, boolean smoothScroll) {
        L.d(this, "setCurrentItem", position, smoothScroll);
        mViewPager.setCurrentItem(position, smoothScroll);
    }

    public int findItemPosition(Item item, Comparator<Item> comparator) {
        return mShortVideoAdapter.findPosition(item, comparator);
    }

    public int getCurrentItem() {
        return mViewPager.getCurrentItem();
    }

    public View getCurrentItemView() {
        final int currentPosition = mViewPager.getCurrentItem();
        return ViewPager2Helper.findItemViewByPosition(mViewPager, currentPosition);
    }

    public ViewHolder getCurrentViewHolder() {
        return findItemViewHolderByPosition(mViewPager, mViewPager.getCurrentItem());
    }

    public Item getCurrentItemModel() {
        final int currentPosition = mViewPager.getCurrentItem();
        return mShortVideoAdapter.getItems().get(currentPosition);
    }

    private void togglePlayback(int position) {
        if (mLifeCycle != null && !mLifeCycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            L.d(this, "togglePlayback", position, "returned",
                    L.string(mLifeCycle.getCurrentState()));
            return;
        }
        L.d(this, "togglePlayback", position, ItemHelper.dump(mShortVideoAdapter.getItem(position)));
        final ViewHolder viewHolder = findItemViewHolderByPosition(mViewPager, position);
        final ViewHolder lastHolder = mCurrentHolder;
        mCurrentHolder = viewHolder;
        // stop last
        if (lastHolder != null && lastHolder != viewHolder) {
            lastHolder.executeAction(ViewHolderAction.ACTION_STOP);
        }
        // start current
        if (viewHolder != null) {
            viewHolder.executeAction(ViewHolderAction.ACTION_PLAY);
        }
    }

    public void play() {
        L.d(this, "play");

        final int currentPosition = mViewPager.getCurrentItem();
        if (currentPosition >= 0) {
            togglePlayback(currentPosition);
        }
    }

    public void pause() {
        L.d(this, "pause");
        ViewHolder viewHolder = getCurrentViewHolder();
        if (viewHolder != null) {
            viewHolder.executeAction(ViewHolderAction.ACTION_PAUSE);
        }
    }

    public void stop() {
        L.d(this, "stop");
        ViewHolder viewHolder = getCurrentViewHolder();
        if (viewHolder != null) {
            viewHolder.executeAction(ViewHolderAction.ACTION_STOP);
        }
    }

    public void setInterceptStartPlaybackOnResume(boolean interceptStartPlay) {
        this.mInterceptStartPlaybackOnResume = interceptStartPlay;
    }

    public boolean isInterceptStartPlaybackOnResume() {
        return mInterceptStartPlaybackOnResume;
    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        switch (event) {
            case ON_RESUME:
                onResume();
                break;
            case ON_PAUSE:
                onPause();
                break;
            case ON_DESTROY:
                onDestroy();
                break;
        }
    }

    private void onResume() {
        L.d(this, "onResume");
        ShortVideoStrategy.setEnabled(true);
        ShortVideoStrategy.setItems(mShortVideoAdapter.getItems());
        if (!mInterceptStartPlaybackOnResume) {
            play();
        }
        mInterceptStartPlaybackOnResume = false;
    }

    private void onPause() {
        L.d(this, "onPause");
        ShortVideoStrategy.setEnabled(false);
        ViewHolder viewHolder = getCurrentViewHolder();
        if (viewHolder != null && viewHolder.isPaused()) {
            mInterceptStartPlaybackOnResume = true;
        } else {
            mInterceptStartPlaybackOnResume = false;
            pause();
        }
    }

    private void onDestroy() {
        L.d(this, "onDestroy");
        if (mLifeCycle != null) {
            mLifeCycle.removeObserver(this);
            mLifeCycle = null;
        }
        stop();
    }

    public boolean onBackPressed() {
        final ViewHolder holder = getCurrentViewHolder();
        if (holder != null && holder.onBackPressed()) {
            return true;
        }
        return false;
    }

    private static ViewHolder findItemViewHolderByPosition(ViewPager2 pager, int position) {
        View itemView = ViewPager2Helper.findItemViewByPosition(pager, position);
        if (itemView != null) {
            return (ViewHolder) itemView.getTag();
        }
        return null;
    }
}
