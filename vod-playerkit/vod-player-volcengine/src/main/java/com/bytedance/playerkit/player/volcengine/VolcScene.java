/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/2/16
 */

package com.bytedance.playerkit.player.volcengine;

public class VolcScene {
    public static final int SCENE_UNKNOWN = 0;
    public static final int SCENE_SHORT_VIDEO = 1;
    public static final int SCENE_FEED_VIDEO = 2;
    public static final int SCENE_LONG_VIDEO = 3;
    public static final int SCENE_DETAIL_VIDEO = 4;
    public static final int SCENE_FULLSCREEN = 5;
    public static final int SCENE_PIP = 6;

    public static String mapScene(int volcScene) {
        switch (volcScene) {
            case SCENE_UNKNOWN:
                return "unknown";
            case SCENE_SHORT_VIDEO:
                return "short";
            case SCENE_FEED_VIDEO:
                return "feed";
            case SCENE_LONG_VIDEO:
                return "long";
            case SCENE_DETAIL_VIDEO:
                return "detail";
            case SCENE_FULLSCREEN:
                return "fullscreen";
            case SCENE_PIP:
                return "pip";
            default:
                throw new IllegalArgumentException("illegal scene " + volcScene);
        }
    }
}
