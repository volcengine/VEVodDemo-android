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
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.volcengine.VolcConfig;
import com.bytedance.playerkit.player.volcengine.VolcConfigGlobal;
import com.bytedance.playerkit.player.volcengine.VolcConfigUpdater;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.player.volcengine.VolcQuality;
import com.bytedance.playerkit.player.volcengine.VolcSubtitleSelector;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.strategy.VideoQuality;
import com.bytedance.volc.vod.settingskit.SettingItem;
import com.bytedance.volc.voddemo.ui.sample.SampleSourceActivity;

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

        L.ENABLE_LOG = true;

        VideoSettings.init(context, new SettingItem.OnEventListener() {
            @Override
            public void onEvent(int eventType, Context context1, SettingItem settingItem, RecyclerView.ViewHolder holder) {
                if (settingItem.type == SettingItem.TYPE_CLICKABLE_ITEM) {
                    if (TextUtils.equals(settingItem.id, VideoSettings.ClickableItemId.INPUT_SOURCE)) {
                        SampleSourceActivity.intentInto(context);
                    }
                }
            }
        });

        VolcPlayerInit.AppInfo appInfo = new VolcPlayerInit.AppInfo.Builder()
                .setAppId(appId)
                .setAppName(appName)
                .setAppChannel(appChannel)
                .setAppVersion(appVersion)
                .setLicenseUri(licenseUri)
                .build();
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
        VolcPlayerInit.init(context,
                appInfo,
                CacheKeyFactory.DEFAULT,
                trackSelector,
                new VolcSubtitleSelector(),
                configUpdater);
    }
}
