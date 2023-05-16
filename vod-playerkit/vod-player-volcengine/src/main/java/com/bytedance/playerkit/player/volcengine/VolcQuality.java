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

import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.source.Quality;
import com.ss.ttvideoengine.Resolution;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class VolcQuality {
    public static final Quality QUALITY_240P =
            new Quality(Quality.QUALITY_RES_240,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "240P",
                    Resolution.L_Standard);

    public static final Quality QUALITY_360P =
            new Quality(Quality.QUALITY_RES_360,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "360P",
                    Resolution.Standard);

    public static final Quality QUALITY_480P =
            new Quality(Quality.QUALITY_RES_480,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "480P",
                    Resolution.High);

    public static final Quality QUALITY_540P =
            new Quality(Quality.QUALITY_RES_540,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "540P",
                    Resolution.H_High);

    public static final Quality QUALITY_720P =
            new Quality(Quality.QUALITY_RES_720,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "720P",
                    Resolution.SuperHigh);

    public static final Quality QUALITY_1080P =
            new Quality(Quality.QUALITY_RES_1080,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "1080P",
                    Resolution.ExtremelyHigh);

    public static final Quality QUALITY_2K =
            new Quality(Quality.QUALITY_RES_2K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "2K",
                    Resolution.H_High);
    public static final Quality QUALITY_4K =
            new Quality(Quality.QUALITY_RES_4K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "4K",
                    Resolution.FourK);

    public static final Quality QUALITY_8K =
            new Quality(Quality.QUALITY_RES_8K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "8K",
                    Resolution.EightK);

    public static final Quality QUALITY_240P_HDR =
            new Quality(Quality.QUALITY_RES_240,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "240P HDR",
                    Resolution.L_Standard_HDR);
    public static final Quality QUALITY_360P_HDR =
            new Quality(Quality.QUALITY_RES_360,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "360P HDR",
                    Resolution.Standard_HDR);
    public static final Quality QUALITY_480P_HDR =
            new Quality(Quality.QUALITY_RES_480,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "480P HDR",
                    Resolution.High_HDR);
    public static final Quality QUALITY_540P_HDR =
            new Quality(Quality.QUALITY_RES_540,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "540P HDR",
                    Resolution.H_High_HDR);
    public static final Quality QUALITY_720P_HDR =
            new Quality(Quality.QUALITY_RES_720,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "720P HDR",
                    Resolution.SuperHigh_HDR);
    public static final Quality QUALITY_1080P_HDR =
            new Quality(Quality.QUALITY_RES_1080,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "1080P HDR",
                    Resolution.ExtremelyHigh_HDR);
    public static final Quality QUALITY_2K_HDR =
            new Quality(Quality.QUALITY_RES_2K,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "2K HDR",
                    Resolution.TwoK_HDR);
    public static final Quality QUALITY_4K_HDR =
            new Quality(Quality.QUALITY_RES_4K,
                    Quality.QUALITY_DYNAMIC_RANGE_HDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "4K HDR",
                    Resolution.FourK_HDR);

    //Resolution.HDR,
    //Resolution.Auto,
    private static final Quality QUALITY_1080P_50FPS =
            new Quality(Quality.QUALITY_RES_1080,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_50,
                    "1080P 50FPS",
                    Resolution.ExtremelyHigh_50F);
    private static final Quality QUALITY_2K_50FPS =
            new Quality(Quality.QUALITY_RES_2K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_50,
                    "2K 50FPS",
                    Resolution.TwoK_50F);
    private static final Quality QUALITY_4K_50FPS =
            new Quality(Quality.QUALITY_RES_4K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_50,
                    "4K 50FPS",
                    Resolution.FourK_50F);
    private static final Quality QUALITY_1080P_60FPS =
            new Quality(Quality.QUALITY_RES_1080,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_60,
                    "1080P 60FPS",
                    Resolution.ExtremelyHigh_60F);
    private static final Quality QUALITY_2K_60FPS =
            new Quality(Quality.QUALITY_RES_2K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_60,
                    "2K 60FPS",
                    Resolution.TwoK_60F);
    private static final Quality QUALITY_4K_60FPS =
            new Quality(Quality.QUALITY_RES_4K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_60,
                    "4K 60FPS",
                    Resolution.FourK_60F);
    private static final Quality QUALITY_1080P_120FPS =
            new Quality(Quality.QUALITY_RES_1080,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_120,
                    "1080P 120FPS",
                    Resolution.ExtremelyHigh_120F);
    private static final Quality QUALITY_2K_120FPS =
            new Quality(Quality.QUALITY_RES_2K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_120,
                    "2K 120FPS",
                    Resolution.TwoK_120F);
    private static final Quality QUALITY_4K_120FPS =
            new Quality(Quality.QUALITY_RES_4K,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "4K 120FPS",
                    Resolution.FourK_120F);
    private static final Quality QUALITY_1080P_PLUS =
            new Quality(Quality.QUALITY_RES_1080,
                    Quality.QUALITY_DYNAMIC_RANGE_SDR,
                    Quality.QUALITY_FPS_DEFAULT,
                    "1080P+",
                    Resolution.ExtremelyHighPlus);

    private static final Map<Resolution, Quality> MAP = new LinkedHashMap<>();
    private static final Map<Resolution, Quality> MAP_COMMON = new LinkedHashMap<>();

    static {
        MAP_COMMON.put(Resolution.Standard, VolcQuality.QUALITY_360P);
        MAP_COMMON.put(Resolution.High, VolcQuality.QUALITY_480P);
        MAP_COMMON.put(Resolution.SuperHigh, VolcQuality.QUALITY_720P);
        MAP_COMMON.put(Resolution.ExtremelyHigh, VolcQuality.QUALITY_1080P);
        MAP_COMMON.put(Resolution.FourK, VolcQuality.QUALITY_4K);
        //Resolution.HDR,
        //Resolution.Auto,
        MAP_COMMON.put(Resolution.L_Standard, VolcQuality.QUALITY_240P);
        MAP_COMMON.put(Resolution.H_High, VolcQuality.QUALITY_540P);
        MAP_COMMON.put(Resolution.TwoK, VolcQuality.QUALITY_2K);
        MAP.put(Resolution.ExtremelyHigh_50F, VolcQuality.QUALITY_1080P_50FPS);
        MAP.put(Resolution.TwoK_50F, VolcQuality.QUALITY_2K_50FPS);
        MAP.put(Resolution.FourK_50F, VolcQuality.QUALITY_4K_50FPS);
        MAP.put(Resolution.ExtremelyHigh_60F, VolcQuality.QUALITY_1080P_60FPS);
        MAP.put(Resolution.TwoK_60F, VolcQuality.QUALITY_2K_60FPS);
        MAP.put(Resolution.FourK_60F, VolcQuality.QUALITY_4K_60FPS);
        MAP.put(Resolution.ExtremelyHigh_120F, VolcQuality.QUALITY_1080P_120FPS);
        MAP.put(Resolution.TwoK_120F, VolcQuality.QUALITY_2K_120FPS);
        MAP.put(Resolution.FourK_120F, VolcQuality.QUALITY_4K_120FPS);
        MAP.put(Resolution.L_Standard_HDR, VolcQuality.QUALITY_240P_HDR);
        MAP.put(Resolution.Standard_HDR, VolcQuality.QUALITY_360P_HDR);
        MAP.put(Resolution.High_HDR, VolcQuality.QUALITY_480P_HDR);
        MAP.put(Resolution.H_High_HDR, VolcQuality.QUALITY_540P_HDR);
        MAP.put(Resolution.SuperHigh_HDR, VolcQuality.QUALITY_720P_HDR);
        MAP.put(Resolution.ExtremelyHigh_HDR, VolcQuality.QUALITY_1080P_HDR);
        MAP.put(Resolution.TwoK_HDR, VolcQuality.QUALITY_2K_HDR);
        MAP.put(Resolution.FourK_HDR, VolcQuality.QUALITY_4K_HDR);
        MAP.put(Resolution.EightK, VolcQuality.QUALITY_8K);
        MAP.put(Resolution.ExtremelyHighPlus, VolcQuality.QUALITY_1080P_PLUS);
        MAP.putAll(MAP_COMMON);
    }

    @Nullable
    static Resolution quality2Resolution(Quality quality) {
        if (quality != null) {
            Resolution resolution = (Resolution) quality.getQualityTag();
            if (resolution != null) return resolution;

            for (Map.Entry<Resolution, Quality> entry : MAP.entrySet()) {
                Quality value = entry.getValue();
                if (Objects.equals(value, quality)) {
                    return entry.getKey();
                }
            }
        }
        return null;
    }

    static Quality resolution2Quality(Resolution resolution) {
        return MAP.get(resolution);
    }

    @Nullable
    public static Quality quality(@Quality.QualityRes int qualityRes) {
        return quality(qualityRes, Quality.QUALITY_DYNAMIC_RANGE_SDR, Quality.QUALITY_FPS_DEFAULT, MAP_COMMON);
    }

    @Nullable
    private static Quality quality(@Quality.QualityRes int qualityRes,
                                   @Quality.QualityDynamicRange int qualityDynamicRange,
                                   @Quality.QualityFps int qualityFps, Map<Resolution, Quality> map) {
        for (Map.Entry<Resolution, Quality> entry : map.entrySet()) {
            Quality value = entry.getValue();
            if (value.getQualityRes() == qualityRes &&
                    value.getQualityDynamicRange() == qualityDynamicRange &&
                    value.getQualityFps() == qualityFps) {
                return entry.getValue();
            }
        }
        return null;
    }


}
