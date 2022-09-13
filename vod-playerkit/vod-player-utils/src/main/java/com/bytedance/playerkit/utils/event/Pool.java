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

package com.bytedance.playerkit.utils.event;

import android.util.ArrayMap;

import androidx.core.util.Pools;

import java.util.Map;

class Pool {
    private static final Map<Class<? extends Event>, Pools.SimplePool<Event>> sPools = new ArrayMap<>();

    static <T extends Event> T acquire(Class<T> clazz) {
        Pools.SimplePool<Event> pools = sPools.get(clazz);
        if (pools == null) {
            pools = new Pools.SimplePool<>(5);
            sPools.put(clazz, pools);
        }
        final Event event = pools.acquire();
        if (event != null) {
            return clazz.cast(event);
        }
        return Factory.create(clazz);
    }

    static void release(Event event) {
        event.recycle();
        final Pools.SimplePool<Event> eventPool = sPools.get(event.getClass());
        if (eventPool != null) {
            eventPool.release(event);
        }
    }
}
