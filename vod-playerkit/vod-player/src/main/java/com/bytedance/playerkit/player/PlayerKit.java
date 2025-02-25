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
 * Create Date : 2025/2/25
 */

package com.bytedance.playerkit.player;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.playback.PlayerPool;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.ReflectUtils;

public class PlayerKit {
    @SuppressLint("StaticFieldLeak")
    private static PlayerKitConfig sConfig;

    public synchronized static void config(PlayerKitConfig config) {
        if (sConfig != null) {
            L.w(PlayerKit.class, "config", "already config, return");
            return;
        }
        sConfig = config;
    }

    public synchronized static PlayerKitConfig config() {
        if (sConfig == null) {
            L.w(PlayerKit.class, "config", "not config, return default");
            return PlayerKitConfig.DEFAULT;
        }
        return sConfig;
    }

    private static Player.Factory createDefaultPlayerFactory() {
        return ReflectUtils.newInstance("com.bytedance.playerkit.player.volcengine.VolcPlayerFactory");
    }

    public static class PlayerKitConfig {
        private static final PlayerKitConfig DEFAULT = new Builder().build();

        @NonNull
        public final Player.Factory playerFactory;
        @NonNull
        public final PlayerPool playerPool;

        private PlayerKitConfig(Builder builder) {
            this.playerFactory = builder.playerFactory == null ? createDefaultPlayerFactory() : builder.playerFactory;
            this.playerPool = builder.playerPool == null ? PlayerPool.DEFAULT : builder.playerPool;
        }

        public static class Builder {
            @Nullable
            private Player.Factory playerFactory;
            @Nullable
            private PlayerPool playerPool;

            public Builder setPlayerFactory(@Nullable Player.Factory playerFactory) {
                this.playerFactory = playerFactory;
                return this;
            }

            public Builder setPlayerPool(@Nullable PlayerPool playerPool) {
                this.playerPool = playerPool;
                return this;
            }

            public PlayerKitConfig build() {
                return new PlayerKitConfig(this);
            }
        }
    }
}
