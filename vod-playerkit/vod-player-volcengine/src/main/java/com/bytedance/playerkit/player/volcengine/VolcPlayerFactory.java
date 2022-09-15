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

package com.bytedance.playerkit.player.volcengine;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.AVPlayer;
import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.adapter.PlayerAdapter;
import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.TrackSelector;

class VolcPlayerFactory implements Player.Factory {

    private final Context mContext;
    private final CacheKeyFactory mCacheKeyFactory;
    private final TrackSelector mTrackSelector;

    VolcPlayerFactory(Context context) {
        this.mContext = context.getApplicationContext();
        this.mCacheKeyFactory = VolcPlayerInit.getCacheKeyFactory();
        this.mTrackSelector = VolcPlayerInit.getTrackSelector();
    }

    @Override
    public Player create(@NonNull MediaSource source) {
        PlayerAdapter.Factory factory = new VolcPlayer.Factory(mContext, source);
        return new AVPlayer(mContext, factory, Looper.getMainLooper());
    }
}
