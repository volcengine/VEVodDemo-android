/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/9/8
 */

package com.bytedance.volc.voddemo.ui.ad.shortvideo;

import androidx.annotation.Nullable;
import androidx.viewpager2.widget.ViewPager2;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.DrawADItem;
import com.bytedance.volc.vod.scenekit.data.utils.ItemHelper;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.Item;
import com.bytedance.volc.voddemo.ui.ad.api.AdLoadStrategy;
import com.bytedance.volc.voddemo.ui.ad.api.AdLoader;
import com.bytedance.volc.voddemo.ui.ad.api.AdMapper;

import java.util.ArrayList;
import java.util.List;

public final class ShortVideoAdInsertStrategy {

    private enum Direction {
        UP, DOWN
    }

    private static AdLoader.Factory sAdLoaderFactory;
    private static AdLoadStrategy sAdLoadStrategy;

    public static void init(AdLoader.Factory factory) {
        sAdLoaderFactory = factory;
    }

    private final ShortVideoSceneView mSceneView;
    /**
     * current scene ad index
     */
    private int mAdIndex;

    public ShortVideoAdInsertStrategy(ShortVideoSceneView sceneView) {
        mSceneView = sceneView;
        mSceneView.pageView().viewPager().registerOnPageChangeCallback(new AdInertTrigger(mSceneView.pageView().getCurrentItem()) {
            @Override
            void insertAds(Direction direction) {
                // Post invoke injectAds because calling notifyNotifyDataXXX method
                // in onPageScrollStateChanged directly throw Exception
                mSceneView.pageView().post(() -> injectAds(direction));
            }
        });
    }

    private abstract static class AdInertTrigger extends ViewPager2.OnPageChangeCallback {
        private int mCurrentPosition;

        public AdInertTrigger(int currentPosition) {
            this.mCurrentPosition = currentPosition;
        }

        @Override
        public void onPageSelected(int position) {
            if (mCurrentPosition != position) {
                Direction direction;
                if (position > mCurrentPosition) {
                    direction = Direction.DOWN;
                } else {
                    direction = Direction.UP;
                }
                L.d(ShortVideoAdInsertStrategy.class, "onPageSelected", "IDLE", mCurrentPosition, position, direction);
                insertAds(direction);
                mCurrentPosition = position;
            }
        }

        abstract void insertAds(Direction direction);
    }

    public void startFetchAds() {
        if (sAdLoadStrategy == null) {
            if (sAdLoaderFactory == null) {
                return;
            }
            final int prefetchMaxCount = VideoSettings.intValue(VideoSettings.AD_VIDEO_PREFETCH_MAX_COUNT);
            L.d(this, "startPreloadAds", "prefetchMaxCount", prefetchMaxCount);
            sAdLoadStrategy = new AdLoadStrategy(prefetchMaxCount, sAdLoaderFactory);
        }
        sAdLoadStrategy.start();
    }

    /**
     * 核心的广告注入逻辑。
     * 在滑动停止后被调用，根据最近的滑动方向在未来的视频流中插入广告。
     */
    private void injectAds(Direction direction) {
        // 若预加载的广告缓存为空，则不插入
        if (sAdLoadStrategy == null || sAdLoadStrategy.isEmpty()) {
            return;
        }

        // 1. 找出所有可以插入广告的索引位置
        List<Integer> insertionIndexes = calInsertionIndexes(mSceneView, direction);

        if (insertionIndexes == null || insertionIndexes.isEmpty()) {
            return;
        }

        L.d(this, "injectAds", L.string(insertionIndexes));

        // 2. 执行插入操作
        int insertedCount = 0; // 插入计数，用于计算偏移量
        for (int originalIndex : insertionIndexes) {
            final DrawADItem adItem = popAdFromCache();
            if (adItem == null) {
                // 广告缓存用完
                break;
            }
            int insertIndex = originalIndex + insertedCount;
            mSceneView.pageView().insertItem(insertIndex, adItem);
            insertedCount++;
            L.d(this, "injectAds", direction, insertIndex, ItemHelper.dump(adItem));
        }
    }

    @Nullable
    private static List<Integer> calInsertionIndexes(ShortVideoSceneView sceneView, Direction direction) {
        if (direction == null) {
            return null;
        }
        final List<Item> originalItems = sceneView.pageView().getItems();
        if (originalItems == null || originalItems.isEmpty()) {
            return null;
        }
        final int currentPos = sceneView.pageView().getCurrentItem();

        // 间隔 interval 个视频，插入一个广告
        final int interval = Math.max(VideoSettings.intValue(VideoSettings.AD_VIDEO_SHOW_INTERVAL), 2);
        // 前 N 个视频不展示广告
        final int initialAdInterval = interval;
        List<Integer> insertionIndexes = new ArrayList<>();
        if (direction == Direction.DOWN) {
            for (int i = Math.max(currentPos + 1, initialAdInterval); i < originalItems.size(); i++) {
                if (canInsertAdAtIndex(originalItems, i, interval)) {
                    insertionIndexes.add(i);
                    // 找到一个可插入点后，跳过 interval，寻找下一个
                    i += interval;
                }
            }
        } else if (direction == Direction.UP) {
            for (int i = currentPos - 1; i >= 0; i--) {
                if (canInsertAdAtIndex(originalItems, i + 1, interval)) {
                    insertionIndexes.add(i + 1);
                    i -= interval;
                }
            }
        }
        return insertionIndexes;
    }

    /**
     * 判断在 index 位置是否可以插入广告。index 前后 `interval` 范围内没有其他广告。
     * 1. index 位不是广告
     * 2. index + interval 内没有广告
     * 3. index - interval 内没有广告
     *
     * @param items    视频列表
     * @param index    计划插入广告的索引
     * @param interval 广告之间的最小间隔
     * @return 如果可以插入则返回 true，否则返回 false
     */
    private static boolean canInsertAdAtIndex(List<Item> items, int index, int interval) {
        // 1. 检查插入点本身是否已经是广告
        if (items.get(index) instanceof DrawADItem) {
            return false;
        }

        // 2. 检查插入点之前的 `interval` 范围内是否有广告
        int start = Math.max(0, index - interval);
        for (int i = start; i < index; i++) {
            if (items.get(i) instanceof DrawADItem) {
                return false;
            }
        }
        // 3. 检查插入点之后的 `interval` 范围内是否有广告
        int end = Math.min(items.size(), index + interval + 1);
        for (int i = index + 1; i < end; i++) {
            if (items.get(i) instanceof DrawADItem) {
                return false;
            }
        }
        // 如果前后都安全，则可以插入
        return true;
    }

    @Nullable
    private DrawADItem popAdFromCache() {
        DrawADItem adItem = AdMapper.instance().create(sAdLoadStrategy, mAdIndex);
        if (adItem == null) {
            return null;
        }
        mAdIndex++;
        return adItem;
    }
}
