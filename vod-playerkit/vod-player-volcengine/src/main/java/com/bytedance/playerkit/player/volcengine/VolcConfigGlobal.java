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
 * Create Date : 2022/12/15
 */

package com.bytedance.playerkit.player.volcengine;


public class VolcConfigGlobal {
    public static final boolean ENABLE_HLS_CACHE_MODULE = true;
    public static final boolean ENABLE_USE_ORIGINAL_URL = true;
    public static final boolean ENABLE_BUFFER_START_MSG_OPT = true;
    public static final boolean ENABLE_SCENE_STRATEGY_INIT = true;
    public static final boolean ENABLE_SPEED_TEST_STRATEGY_INIT = true;
    public static final boolean ENABLE_ECDN = false; // Not ready for now.
    public static final boolean ENABLE_STARTUP_ABR_INIT = true;
    public static final boolean ENABLE_USE_BACKUP_URL = true;

    public static class CacheDir {
        public static final String ROOT_DIR = "bytedance/playerkit/volcplayer";
        public static final String PLAYER_CACHE_DIR = ROOT_DIR + "/video_cache";
    }

    public static class FilesDir {
        public static final String ROOT_DIR = "bytedance/playerkit/volcplayer";
        public static final String PLAYER_BMF_SR_BIN_DIR = ROOT_DIR + "/bmf";
    }
}
