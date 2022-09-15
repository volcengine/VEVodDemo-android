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

package com.bytedance.playerkit.player.playback;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultPlayerPool implements PlayerPool {

    private final Map<String, Player> mAcquiredPlayers = Collections.synchronizedMap(new LinkedHashMap<>());

    @NonNull
    @Override
    public Player acquire(@NonNull MediaSource source, Player.Factory factory) {
        Player player = get(source);
        if (player != null) {
            if (player.isError() || player.isReleased()) {
                recycle(player);
                player = null;
            }
        }
        if (player == null) {
            player = create(source, factory);
        }
        L.d(this, "acquire", source, player);
        return player;
    }

    @NonNull
    private Player create(@NonNull MediaSource source, @NonNull Player.Factory factory) {
        Player player = factory.create(source);
        player.addPlayerListener(new Dispatcher.EventListener() {
            @Override
            public void onEvent(Event event) {
                if (event.code() == PlayerEvent.Action.RELEASE) {
                    Player p = event.owner(Player.class);
                    p.removePlayerListener(this);
                    recycle(source);
                }
            }
        });
        mAcquiredPlayers.put(key(source), player);
        return player;
    }

    @Override
    public Player get(@NonNull MediaSource source) {
        return mAcquiredPlayers.get(key(source));
    }

    @Override
    public void recycle(@NonNull Player player) {
        L.d(this, "recycle", player.getDataSource(), player);
        synchronized (mAcquiredPlayers) {
            Map<String, Player> copy = new LinkedHashMap<>(mAcquiredPlayers);
            for (Map.Entry<String, Player> entry : copy.entrySet()) {
                if (entry.getValue() == player) {
                    mAcquiredPlayers.remove(entry.getKey());
                }
            }
        }
        player.release();
    }

    private void recycle(@NonNull MediaSource source) {
        Player o = mAcquiredPlayers.remove(key(source));
        if (o != null) {
            L.d(this, "recycle by player", source, o);
        }
    }

    private String key(@NonNull MediaSource mediaSource) {
        return mediaSource.getUniqueId();
    }
}