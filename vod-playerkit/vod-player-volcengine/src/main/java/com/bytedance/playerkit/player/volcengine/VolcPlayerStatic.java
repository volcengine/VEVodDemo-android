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

package com.bytedance.playerkit.player.volcengine;

import android.view.Surface;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.pandora.common.env.Env;
import com.ss.ttvideoengine.debugtool2.DebugTool;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.strategy.StrategySettings;

import org.json.JSONObject;

import java.util.List;

public class VolcPlayerStatic {

    private static int CurrentScene = VolcC.SCENE_UNKNOWN;
    private static boolean SceneStrategyEnabled = false;

    public synchronized static void setSceneStrategyEnabled(int volcScene, boolean enabled) {
        L.d(VolcPlayerStatic.class, "setSceneStrategyEnabled", VolcC.mapScene(volcScene), enabled);
        if (CurrentScene != volcScene) {
            if (SceneStrategyEnabled) {
                VolcPlayer.clearSceneStrategy();
                SceneStrategyEnabled = false;
            }
        }
        CurrentScene = volcScene;
        if (SceneStrategyEnabled != enabled) {
            SceneStrategyEnabled = enabled;
            if (enabled) {
                VolcPlayer.setSceneStrategyEnabled(volcScene);
            } else {
                VolcPlayer.clearSceneStrategy();
            }
        }
    }

    public static void setMediaSources(List<MediaSource> mediaSources) {
        VolcPlayer.setMediaSources(mediaSources);
    }

    public static void addMediaSources(List<MediaSource> mediaSources) {
        VolcPlayer.addMediaSources(mediaSources);
    }

    public static void renderFrame(MediaSource mediaSource, Surface surface, int[] frameInfo) {
        VolcPlayer.renderFrame(mediaSource, surface, frameInfo);
    }

    public static String getDeviceId() {
        return VolcPlayer.getDeviceId();
    }

    public static String getSDKVersion() {
        return Env.getVersion();
    }

    @Nullable
    public static JSONObject getPreloadConfig(int scene) {
        switch (scene) {
            case VolcC.SCENE_SHORT_VIDEO: // Short
                return StrategySettings.getInstance().getPreload(StrategyManager.STRATEGY_SCENE_SMALL_VIDEO);
            case VolcC.SCENE_FEED_VIDEO: // Feed
                return StrategySettings.getInstance().getPreload(StrategyManager.STRATEGY_SCENE_SHORT_VIDEO);
        }
        return null;
    }

    // 添加展示 debug 信息的布局，debug 信息页面撑满 containerView
    // 需要在调用 Engine 播放之前设置，
    // 设置布局后，Debug 工具会监听哪个 Engine 实例调用了 play，并将相关信息显示到布局。
    public static void setDebugToolContainerView(ViewGroup containerView) {
        DebugTool.release();
        DebugTool.setContainerView(containerView);
    }

    // 完成 Debug 工具使用时，您可调用release()方法释放资源
    public static void releaseDebugTool() {
        DebugTool.release();
    }
}
