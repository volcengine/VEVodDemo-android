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
 * Create Date : 2023/5/29
 */

package com.bytedance.playerkit.player.volcengine;

import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_SCENE_SHORT_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_SCENE_SMALL_VIDEO;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRELOAD;
import static com.ss.ttvideoengine.strategy.StrategyManager.STRATEGY_TYPE_PRE_RENDER;

import android.content.Context;
import android.os.Looper;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.utils.ProgressRecorder;
import com.bytedance.playerkit.utils.L;
import com.ss.ttvideoengine.PreloaderVidItem;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.strategy.EngineStrategyListener;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.strategy.StrategySettings;
import com.ss.ttvideoengine.strategy.preload.PreloadTaskFactory;
import com.ss.ttvideoengine.strategy.source.StrategySource;

import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolcEngineStrategy {
    private static final Map<String, TTVideoEngine> PRERENDER_ENGINE_MAP = Collections.synchronizedMap(new HashMap<>());
    private static int sCurrentScene = VolcScene.SCENE_UNKNOWN;
    private static boolean sSceneStrategyEnabled = false;

    static void init() {
        if (!VolcConfigGlobal.ENABLE_SCENE_STRATEGY_INIT) return;
        // preRender
        StrategyManager.instance().controlEngineRelease(false);
        TTVideoEngine.setEngineStrategyListener(new EngineStrategyListener() {
            @Override
            public TTVideoEngine createPreRenderEngine(StrategySource strategySource) {
                final Context context = VolcPlayerInit.getContext();
                final MediaSource mediaSource = (MediaSource) strategySource.tag();
                final long recordPosition = ProgressRecorder.getProgress(mediaSource.getSyncProgressId());
                final VolcPlayer player = new VolcPlayer.Factory(context, mediaSource).preCreate(Looper.getMainLooper());
                player.setListener(new VolcPlayerEventRecorder());
                player.setDataSource(mediaSource);
                player.setStartTime(recordPosition > 0 ? recordPosition : 0);
                player.prepareAsync();
                return player.getTTVideoEngine();
            }
        });

        // preload
        StrategyManager.instance().setPreloadTaskFactory(new PreloadTaskFactory() {
            @Override
            public PreloaderVidItem createVidItem(VidPlayAuthTokenSource source, long preloadSize) {
                final PreloaderVidItem item = PreloadTaskFactory.super.createVidItem(source, preloadSize);
                final MediaSource mediaSource = (MediaSource) source.tag();
                if (mediaSource == null) return item; // error
                item.setFetchEndListener((videoModel, error) -> {
                    Mapper.updateMediaSource(mediaSource, videoModel);
                    Track playTrack = selectPlayTrack(TrackSelector.TYPE_PRELOAD, mediaSource);
                    final Resolution resolution = playTrack != null ? Mapper.track2Resolution(playTrack) : null;
                    if (resolution != null) {
                        item.mResolution = resolution;
                    }
                });
                return item;
            }
        });
    }

    /**
     * For vid only
     */
    private static Track selectPlayTrack(int type, MediaSource mediaSource) {
        @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);
        List<Track> tracks = mediaSource.getTracks(trackType);
        if (tracks != null) {
            return VolcPlayerInit.getTrackSelector().selectTrack(type, trackType, tracks, mediaSource);
        }
        return null;
    }

    public synchronized static void setEnabled(int volcScene, boolean enabled) {
        L.d(VolcEngineStrategy.class, "setEnabled", VolcScene.mapScene(volcScene), enabled);
        if (sCurrentScene != volcScene) {
            if (sSceneStrategyEnabled) {
                clearSceneStrategy();
                sSceneStrategyEnabled = false;
            }
        }
        sCurrentScene = volcScene;
        if (sSceneStrategyEnabled != enabled) {
            sSceneStrategyEnabled = enabled;
            if (enabled) {
                setEnabled(volcScene);
            } else {
                clearSceneStrategy();
            }
        }
    }

    public static void clearSceneStrategy() {
        releasePreReRenderEngines();
        TTVideoEngine.clearAllStrategy();
    }

    public static void setMediaSources(List<MediaSource> mediaSources) {
        if (mediaSources == null) return;
        List<StrategySource> strategySources = Mapper.mediaSources2StrategySources(
                mediaSources,
                VolcPlayerInit.getCacheKeyFactory(),
                VolcPlayerInit.getTrackSelector(),
                TrackSelector.TYPE_PRELOAD);
        if (strategySources == null) return;
        TTVideoEngine.setStrategySources(strategySources);
    }

    public static void addMediaSources(List<MediaSource> mediaSources) {
        if (mediaSources == null) return;
        List<StrategySource> strategySources = Mapper.mediaSources2StrategySources(
                mediaSources,
                VolcPlayerInit.getCacheKeyFactory(),
                VolcPlayerInit.getTrackSelector(),
                TrackSelector.TYPE_PRELOAD);
        if (strategySources == null) return;
        TTVideoEngine.addStrategySources(strategySources);
    }

    @Nullable
    public static JSONObject getPreloadConfig(int scene) {
        switch (scene) {
            case VolcScene.SCENE_SHORT_VIDEO: // Short
                return StrategySettings.getInstance().getPreload(StrategyManager.STRATEGY_SCENE_SMALL_VIDEO);
            case VolcScene.SCENE_FEED_VIDEO: // Feed
                return StrategySettings.getInstance().getPreload(StrategyManager.STRATEGY_SCENE_SHORT_VIDEO);
        }
        return null;
    }

    public static void renderFrame(MediaSource mediaSource, Surface surface, int[] frameInfo) {
        if (mediaSource == null) return;
        if (surface == null || !surface.isValid()) return;

        final TTVideoEngine player = getPreRenderEngine(mediaSource);
        if (player != null && player != StrategyManager.instance().getPlayEngine()) {
            player.setSurface(surface);
            player.forceDraw();
            frameInfo[0] = player.getVideoWidth();
            frameInfo[1] = player.getVideoHeight();
        }
    }

    private static void setEnabled(int volcScene) {
        final int engineScene = Mapper.mapVolcScene2EngineScene(volcScene);
        switch (engineScene) {
            case STRATEGY_SCENE_SMALL_VIDEO:
                TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRELOAD, STRATEGY_SCENE_SMALL_VIDEO);
                TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRE_RENDER, STRATEGY_SCENE_SMALL_VIDEO);
                break;
            case STRATEGY_SCENE_SHORT_VIDEO:
                TTVideoEngine.enableEngineStrategy(STRATEGY_TYPE_PRELOAD, STRATEGY_SCENE_SHORT_VIDEO);
                break;
        }
    }

    @Nullable
    static TTVideoEngine getPreRenderEngine(MediaSource mediaSource) {
        if (mediaSource == null) return null;

        final String key = key(mediaSource);

        TTVideoEngine player = PRERENDER_ENGINE_MAP.get(key);
        if (player != null && player.isPrepared()) {
            final TTVideoEngine duplicated = TTVideoEngine.getPreRenderEngine(key);
            if (duplicated != null && duplicated != player) {
                L.e(VolcEngineStrategy.class, "getPreRenderEngine", "duplicated", key, player, duplicated);
                duplicated.clearTextureRef();
                duplicated.releaseAsync();
            }
            L.d(VolcEngineStrategy.class, "getPreRenderEngine", "cached", key, player);
            return player;
        }
        PRERENDER_ENGINE_MAP.remove(key);

        player = TTVideoEngine.getPreRenderEngine(key);
        if (player != null && player.isPrepared()) {
            PRERENDER_ENGINE_MAP.put(key, player);
            L.d(VolcEngineStrategy.class, "getPreRenderEngine", "get", key, player);
            return player;
        }
        return null;
    }

    static TTVideoEngine removePreRenderEngine(MediaSource mediaSource) {
        if (mediaSource == null) return null;

        final String key = key(mediaSource);

        TTVideoEngine player = PRERENDER_ENGINE_MAP.remove(key);
        if (player != null && player.isPrepared()) {
            final TTVideoEngine duplicated = TTVideoEngine.getPreRenderEngine(key);
            if (duplicated != null && duplicated != player) {
                L.e(VolcEngineStrategy.class, "removePreRenderEngine", "duplicated", key, player, duplicated);
                duplicated.clearTextureRef();
                duplicated.releaseAsync();
            }
            L.d(VolcEngineStrategy.class, "removePreRenderEngine", "cached", key, player);
            return player;
        }

        player = TTVideoEngine.getPreRenderEngine(key);
        if (player != null && player.isPrepared()) {
            L.d(VolcEngineStrategy.class, "removePreRenderEngine", "get", key, player);
            return player;
        }
        return null;
    }

    static void releasePreReRenderEngines() {
        Map<String, TTVideoEngine> copy = new HashMap<>();
        synchronized (PRERENDER_ENGINE_MAP) {
            copy.putAll(PRERENDER_ENGINE_MAP);
            PRERENDER_ENGINE_MAP.clear();
        }
        for (Map.Entry<String, TTVideoEngine> entry : copy.entrySet()) {
            TTVideoEngine engine = entry.getValue();
            if (engine != null) {
                engine.releaseAsync();
            }
        }
        copy.clear();
    }

    private static String key(MediaSource mediaSource) {
        return mediaSource.getMediaId();
    }
}
