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

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;

import java.util.List;

public class VodSDK {

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    public static Context context() {
        return sContext;
    }

    public static void init(Context context,
                            String appId,
                            String appName,
                            String appChannel,
                            String appVersion,
                            String appRegion,
                            String licenseUri) {
        sContext = context;

        L.ENABLE_LOG = true;

        VideoSettings.init(context);

        VolcPlayerInit.AppInfo appInfo = new VolcPlayerInit.AppInfo.Builder()
                .setAppId(appId)
                .setAppName(appName)
                .setAppRegion(appRegion)
                .setAppChannel(appChannel)
                .setAppVersion(appVersion)
                .setLicenseUri(licenseUri)
                .build();

        final int qualityRes = Quality.QUALITY_RES_720;

        final TrackSelector trackSelector = new TrackSelector() {
            @NonNull
            @Override
            public Track selectTrack(int type, int trackType, @NonNull List<Track> tracks, @NonNull MediaSource source) {
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

        VolcPlayerInit.init(context, appInfo, CacheKeyFactory.DEFAULT, trackSelector);
    }
}
