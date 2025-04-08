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

import android.os.Looper;
import android.view.Surface;

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.utils.ProgressRecorder;
import com.bytedance.playerkit.utils.CollectionUtils;
import com.bytedance.playerkit.utils.Getter;
import com.bytedance.playerkit.utils.L;
import com.ss.ttvideoengine.PreloaderURLItem;
import com.ss.ttvideoengine.PreloaderVidItem;
import com.ss.ttvideoengine.PreloaderVideoModelItem;
import com.ss.ttvideoengine.Resolution;
import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.selector.strategy.GearStrategy;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource;
import com.ss.ttvideoengine.source.VideoModelSource;
import com.ss.ttvideoengine.strategy.EngineStrategyListener;
import com.ss.ttvideoengine.strategy.StrategyManager;
import com.ss.ttvideoengine.strategy.StrategySettings;
import com.ss.ttvideoengine.strategy.preload.PreloadTaskFactory;
import com.ss.ttvideoengine.strategy.source.StrategySource;

import org.json.JSONObject;

import java.util.List;

public class VolcEngineStrategy {
    private static int sCurrentScene = VolcScene.SCENE_UNKNOWN;
    private static boolean sSceneStrategyEnabled = false;

    static void init() {
        if (!VolcConfigGlobal.ENABLE_SCENE_STRATEGY_INIT) return;
        // preRender
        StrategyManager.instance().enableReleasePreRenderEngineInstanceByLRU(true);
        TTVideoEngine.setEngineStrategyListener(new EngineStrategyListener() {
            @Override
            public TTVideoEngine createPreRenderEngine(StrategySource strategySource) {
                final MediaSource mediaSource = (MediaSource) strategySource.tag();
                final long recordPosition = ProgressRecorder.getProgress(mediaSource.getSyncProgressId());
                final VolcPlayer player = (VolcPlayer) new VolcPlayer.Factory(mediaSource).preCreate(Looper.getMainLooper());
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
                VolcPlayerInit.config().configUpdater.updateVolcConfig(mediaSource);
                item.setFetchEndListener((videoModel, error) -> {
                    Mapper.updateMediaSource(mediaSource, videoModel);
                    final VolcConfig volcConfig = VolcConfig.get(mediaSource);
                    Track playTrack = null;
                    if (VolcQualityStrategy.isEnableStartupABR(volcConfig)) {
                        VolcQualityStrategy.StartupTrackResult result = VolcQualityStrategy.select(
                                GearStrategy.GEAR_STRATEGY_SELECT_TYPE_PRELOAD,
                                mediaSource,
                                videoModel);
                        playTrack = result.track;
                    }
                    if (playTrack == null) {
                        playTrack = selectPlayTrack(TrackSelector.TYPE_PRELOAD, mediaSource);
                    }
                    final Resolution resolution = playTrack != null ? Mapper.track2Resolution(playTrack) : null;
                    if (resolution != null) {
                        item.mResolution = resolution;
                    }
                });
                return item;
            }

            @Override
            public PreloaderVideoModelItem createVideoModelItem(VideoModelSource source, long preloadSize) {
                final PreloaderVideoModelItem item = PreloadTaskFactory.super.createVideoModelItem(source, preloadSize);
                final MediaSource mediaSource = (MediaSource) source.tag();
                if (mediaSource == null) return item; // error
                VolcPlayerInit.config().configUpdater.updateVolcConfig(mediaSource);
                final VolcConfig volcConfig = VolcConfig.get(mediaSource);
                Track playTrack = null;
                if (VolcQualityStrategy.isEnableStartupABR(volcConfig)) {
                    VolcQualityStrategy.StartupTrackResult result = VolcQualityStrategy.select(
                            GearStrategy.GEAR_STRATEGY_SELECT_TYPE_PRELOAD,
                            mediaSource,
                            source.videoModel());
                    playTrack = result.track;
                }
                if (playTrack == null) {
                    playTrack = selectPlayTrack(TrackSelector.TYPE_PRELOAD, mediaSource);
                }
                final Resolution resolution = playTrack != null ? Mapper.track2Resolution(playTrack) : null;
                if (resolution != null) {
                    item.mResolution = resolution;
                }
                return item;
            }

            @Override
            public PreloaderURLItem createUrlItem(DirectUrlSource source, long preloadSize) {
                PreloaderURLItem item = PreloadTaskFactory.super.createUrlItem(source, preloadSize);
                final MediaSource mediaSource = (MediaSource) source.tag();
                if (mediaSource == null) return item; // error
                VolcPlayerInit.config().configUpdater.updateVolcConfig(mediaSource);
                return item;
            }

            @Nullable
            @Override
            public PreloaderURLItem createSubtitleUrlItem(DirectUrlSource source, long preloadSize) {
                final MediaSource mediaSource = (MediaSource) source.tag();
                if (mediaSource == null) return null; // error
                // VolcPlayerInit.config().configUpdater.updateVolcConfig(mediaSource);
                final Subtitle subtitle = selectPlaySubtitle(mediaSource, mediaSource.getSubtitles());
                if (subtitle != null) {
                    String cacheKey = VolcPlayerInit.config().cacheKeyFactory.generateCacheKey(mediaSource, subtitle);
                    return new PreloaderURLItem(cacheKey, source.vid(), preloadSize, new String[]{subtitle.getUrl()});
                }
                return PreloadTaskFactory.super.createSubtitleUrlItem(source, preloadSize);
            }
        });
    }

    private static Subtitle selectPlaySubtitle(MediaSource mediaSource, List<Subtitle> subtitles) {
        if (!CollectionUtils.isEmpty(subtitles)) {
            return VolcPlayerInit.config().subtitleSelector.selectSubtitle(mediaSource, subtitles);
        }
        return null;
    }

    /**
     * For vid only
     */
    private static Track selectPlayTrack(@TrackSelector.Type int type, MediaSource mediaSource) {
        @Track.TrackType final int trackType = MediaSource.mediaType2TrackType(mediaSource);
        List<Track> tracks = mediaSource.getTracks(trackType);
        if (tracks != null) {
            return VolcPlayerInit.config().trackSelector.selectTrack(type, trackType, tracks, mediaSource);
        }
        return null;
    }

    public static void setEnabled(int volcScene, boolean enabled) {
        if (VolcPlayerInit.config() == null) return;

        VolcPlayerInit.config().workerHandler.post(() -> {
            L.d(VolcEngineStrategy.class, "setEnabled", VolcScene.mapScene(volcScene), enabled);
            VolcPlayerInit.waitInitAsyncResult();

            if (sCurrentScene != volcScene) {
                if (sSceneStrategyEnabled) {
                    TTVideoEngine.clearAllStrategy();
                    sSceneStrategyEnabled = false;
                }
            }
            sCurrentScene = volcScene;
            if (sSceneStrategyEnabled != enabled) {
                sSceneStrategyEnabled = enabled;
                if (enabled) {
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
                } else {
                    TTVideoEngine.clearAllStrategy();
                }
            }
        });
    }

    public static void setMediaSourcesAsync(Getter<List<MediaSource>> asyncGetter) {
        if (VolcPlayerInit.config() == null) return;

        VolcPlayerInit.config().workerHandler.post(() -> {
            VolcPlayerInit.waitInitAsyncResult();

            final List<MediaSource> mediaSources = asyncGetter.get();
            if (mediaSources == null) return;
            final List<StrategySource> strategySources = Mapper.mediaSources2StrategySources(
                    mediaSources,
                    VolcPlayerInit.config().cacheKeyFactory,
                    VolcPlayerInit.config().trackSelector,
                    TrackSelector.TYPE_PRELOAD);
            if (strategySources == null) return;

            TTVideoEngine.setStrategySources(strategySources);
        });
    }

    public static void addMediaSourcesAsync(Getter<List<MediaSource>> asyncGetter) {
        if (VolcPlayerInit.config() == null) return;

        VolcPlayerInit.config().workerHandler.post(() -> {
            VolcPlayerInit.waitInitAsyncResult();

            final List<MediaSource> mediaSources = asyncGetter.get();
            if (mediaSources == null) return;
            final List<StrategySource> strategySources = Mapper.mediaSources2StrategySources(
                    mediaSources,
                    VolcPlayerInit.config().cacheKeyFactory,
                    VolcPlayerInit.config().trackSelector,
                    TrackSelector.TYPE_PRELOAD);
            if (strategySources == null) return;
            TTVideoEngine.addStrategySources(strategySources);
        });
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

        VolcPlayerInit.waitInitAsyncResult();

        final TTVideoEngine player = getPreRenderEngine(mediaSource);
        if (player != null && player != StrategyManager.instance().getPlayEngine()) {
            player.setSurface(surface);
            player.forceDraw();
            frameInfo[0] = player.getVideoWidth();
            frameInfo[1] = player.getVideoHeight();
        }
    }

    static TTVideoEngine getPreRenderEngine(MediaSource mediaSource) {
        if (mediaSource == null) return null;

        final String key = key(mediaSource);

        TTVideoEngine player = StrategyManager.instance().getPreRenderEngine(key);
        if (player != null && player.isPrepared()) {
            L.d(VolcEngineStrategy.class, "getPreRenderEngine", key, player);
            return player;
        }
        return null;
    }

    static TTVideoEngine removePreRenderEngine(MediaSource mediaSource) {
        if (mediaSource == null) return null;

        final String key = key(mediaSource);

        TTVideoEngine player = StrategyManager.instance().removePreRenderEngine(key);
        if (player != null && player.isPrepared()) {
            L.e(VolcEngineStrategy.class, "removePreRenderEngine", key, player);
            return player;
        }
        return null;
    }

    private static String key(MediaSource mediaSource) {
        return mediaSource.getMediaId();
    }
}
