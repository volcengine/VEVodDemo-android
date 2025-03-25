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

package com.bytedance.volc.voddemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.cache.DefaultCacheKeyFactory;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.SubtitleSelector;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.volcengine.VolcConfig;
import com.bytedance.playerkit.player.volcengine.VolcConfigGlobal;
import com.bytedance.playerkit.player.volcengine.VolcConfigUpdater;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInitConfig;
import com.bytedance.playerkit.player.volcengine.VolcQuality;
import com.bytedance.playerkit.player.volcengine.VolcSubtitleSelector;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.strategy.VideoQuality;
import com.bytedance.volc.vod.scenekit.strategy.VideoSubtitle;
import com.bytedance.volc.vod.settingskit.SettingItem;
import com.bytedance.volc.voddemo.impl.BuildConfig;
import com.bytedance.volc.voddemo.ui.sample.SampleSourceActivity;
import com.bytedance.volc.voddemo.ui.video.scene.pipvideo.PipVideoController;
import com.bytedance.volc.voddemo.utils.CacheKeyUtils;
import com.bytedance.volc.voddemo.video.AppUrlRefreshFetcher;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class VodSDK {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;
    private static final AtomicBoolean sInited = new AtomicBoolean();

    public static Context context() {
        return sContext;
    }

    public static void init(Context context,
                            String appId,
                            String appName,
                            String appChannel,
                            String appVersion,
                            String licenseUri) {

        if (sInited.getAndSet(true)) return;

        sContext = context;

        VideoSettings.init(context, new SettingItem.OnEventListener() {
            @Override
            public void onEvent(int eventType, Context context1, SettingItem settingItem, RecyclerView.ViewHolder holder) {
                if (settingItem.type == SettingItem.TYPE_CLICKABLE_ITEM) {
                    if (TextUtils.equals(settingItem.id, VideoSettings.ClickableItemId.INPUT_SOURCE)) {
                        SampleSourceActivity.intentInto(context);
                    }
                } else if (settingItem.type == SettingItem.TYPE_OPTION) {
                    if (TextUtils.equals(settingItem.option.key, VideoSettings.COMMON_ENABLE_PIP)) {
                        if (eventType == EVENT_TYPE_RATIO_ITEM_ON_CHECKED_CHANGED && settingItem.option.booleanValue()) {
                            PipVideoController.instance().requestPermission(context1, settingItem, holder);
                        }
                    }
                }
            }
        });

        // FIXME logcat 开关，上线请关闭
        L.ENABLE_LOG = VideoSettings.booleanValue(VideoSettings.INIT_ENABLE_LOGCAT) && BuildConfig.DEBUG;
        // FIXME asserts 开关，上线请关闭
        Asserts.DEBUG = VideoSettings.booleanValue(VideoSettings.INIT_ENABLE_ASSERTS) && BuildConfig.DEBUG;

        final CacheKeyFactory cacheKeyFactory = new DefaultCacheKeyFactory() {

            @Override
            public String generateCacheKey(@NonNull String url) {
                // 1. 兼容 短/中/长中的源使用 Type-C CDN 签名，过期时间在 url path 中，若不是 Type-C 返回 null
                String cacheKey = CacheKeyUtils.generateVolcCDNUrlTypeCCacheKey(url);
                if (!TextUtils.isEmpty(cacheKey)) {
                    return cacheKey;
                }
                // 2. 短剧使用 Type-A CDN 签名，使用 PlayerKit 中默认规则即可
                return super.generateCacheKey(url);
            }
        };

        final TrackSelector trackSelector = new TrackSelector() {
            @NonNull
            @Override
            public Track selectTrack(int type, int trackType, @NonNull List<Track> tracks, @NonNull MediaSource source) {
                int qualityRes = VideoQuality.getUserSelectedQualityRes(source);
                if (qualityRes <= 0) {
                    qualityRes = VideoQuality.VIDEO_QUALITY_DEFAULT;
                }
                for (Track track : tracks) {
                    Quality quality = track.getQuality();
                    if (quality != null) {
                        if (quality.getQualityRes() == qualityRes) {
                            return track;
                        }
                    }
                }
                return tracks.get(0);
            }
        };

        final SubtitleSelector subtitleSelector = new VolcSubtitleSelector() {
            @NonNull
            @Override
            public Subtitle selectSubtitle(@NonNull MediaSource mediaSource, @NonNull List<Subtitle> subtitles) {
                // 起播 + 字幕选择全局回调

                // 1. 优先用户上次选择的字幕语言
                final int languageId = VideoSubtitle.getUserSelectedLanguageId(mediaSource);
                if (languageId > 0) {
                    for (Subtitle subtitle : subtitles) {
                        if (subtitle.getLanguageId() == languageId) {
                            return subtitle;
                        }
                    }
                }

                // 2. 按照偏好语言优先级返回语言
                // 3. 若未命中，兜底返回第 0 个
                return super.selectSubtitle(mediaSource, subtitles);
            }
        };

        final VolcConfigUpdater configUpdater = new VolcConfigUpdater() {
            @Override
            public void updateVolcConfig(MediaSource mediaSource) {
                VolcConfig config = VolcConfig.get(mediaSource);
                if (config.qualityConfig == null) return;
                if (!config.qualityConfig.enableStartupABR) return;

                final int qualityRes = VideoQuality.getUserSelectedQualityRes(mediaSource);
                if (qualityRes <= 0) {
                    config.qualityConfig.userSelectedQuality = null;
                } else {
                    config.qualityConfig.userSelectedQuality = VolcQuality.quality(qualityRes);
                }
            }
        };

        // 不使用 ECDN 无需关心
        if (VolcConfigGlobal.ENABLE_ECDN) {
            VolcConfig.ECDN_FILE_KEY_REGULAR_EXPRESSION = "[a-zA-z]+://[^/]*/[^/]*/[^/]*/(.*?)\\?.*";
        }

        // 播放源刷新策略
        AppUrlRefreshFetcher.Factory urlRefresherFactory = null;
        if (VideoSettings.booleanValue(VideoSettings.COMMON_ENABLE_SOURCE_403_REFRESH)) {
            urlRefresherFactory = new AppUrlRefreshFetcher.Factory();
        }

        // PlayerKit 初始化 Step1：在 Application#onCreate 中设置 VolcPlayerInitConfig
        // 仅设置 config 对象，不耗时，无敏感信息采集。
        VolcPlayerInit.config(new VolcPlayerInitConfig.Builder()
                .setContext(context)
                .setAppInfo(new VolcPlayerInitConfig.AppInfo.Builder()
                        .setAppId(appId)
                        .setAppName(appName)
                        .setAppChannel(appChannel)
                        .setAppVersion(appVersion)
                        .setLicenseUri(licenseUri)
                        .build())
                .setCacheKeyFactory(cacheKeyFactory)
                .setTrackSelector(trackSelector)
                .setSubtitleSelector(subtitleSelector)
                .setConfigUpdater(configUpdater)
                .setUrlRefreshFetcherFactory(urlRefresherFactory)
                .build()
        );

        // PlayerKit 初始化 Step2：初始化点播 SDK
        if (VideoSettings.booleanValue(VideoSettings.INIT_ENABLE_VOD_SDK_ASYNC_INIT)) {
            // 异步初始化
            // TODO：对初始化耗时不敏感的话，推荐使用同步初始化，不容易出错。
            //  若使用异步初始化，上线前请充分测试，先灰度上线看一下效果，再全量上线。
            VolcPlayerInit.initAsync((result) -> {
                L.d("VodSDK", String.format("init result: %s", result > 0 ? "success" : "fail"));
            });
        } else {
            // 同步初始化（推荐）
            VolcPlayerInit.initSync();
            L.d("VodSDK", String.format("init result: %s", "success"));
        }
    }
}
