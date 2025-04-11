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
 * Create Date : 2022/11/2
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene;


import com.bytedance.playerkit.player.volcengine.VolcScene;

public class PlayScene {
    public static final int SCENE_UNKNOWN = VolcScene.SCENE_UNKNOWN;
    public static final int SCENE_SHORT = VolcScene.SCENE_SHORT_VIDEO;
    public static final int SCENE_FEED = VolcScene.SCENE_FEED_VIDEO;
    public static final int SCENE_LONG = VolcScene.SCENE_LONG_VIDEO;
    public static final int SCENE_DETAIL = VolcScene.SCENE_DETAIL_VIDEO;
    public static final int SCENE_FULLSCREEN = VolcScene.SCENE_FULLSCREEN;
    public static final int SCENE_PIP = VolcScene.SCENE_PIP;

    public static String map(int scene) {
        switch (scene) {
            case SCENE_SHORT:
                return "short";
            case SCENE_FEED:
                return "feed";
            case SCENE_LONG:
                return "long";
            case SCENE_DETAIL:
                return "detail";
            case SCENE_FULLSCREEN:
                return "fullscreen";
            case SCENE_PIP:
                return "pip";
            case SCENE_UNKNOWN:
            default:
                return "unknown";
        }
    }
}
