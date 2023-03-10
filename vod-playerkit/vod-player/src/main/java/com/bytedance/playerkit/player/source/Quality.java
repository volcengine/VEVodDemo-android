/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.player.source;

import androidx.annotation.IntDef;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class Quality implements Serializable {
    /**
     * Quality dynamic range type. One of
     * {@link #QUALITY_DYNAMIC_RANGE_SDR},
     * {@link #QUALITY_DYNAMIC_RANGE_SDR_PLUS},
     * {@link #QUALITY_DYNAMIC_RANGE_HDR}
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({QUALITY_DYNAMIC_RANGE_SDR, QUALITY_DYNAMIC_RANGE_SDR_PLUS, QUALITY_DYNAMIC_RANGE_HDR})
    public @interface QualityDynamicRange {
    }

    public static final int QUALITY_DYNAMIC_RANGE_SDR = 0;
    public static final int QUALITY_DYNAMIC_RANGE_SDR_PLUS = 1;
    public static final int QUALITY_DYNAMIC_RANGE_HDR = 2;

    public static String mapQualityDynamicRange(@QualityDynamicRange int colorRange) {
        switch (colorRange) {
            case QUALITY_DYNAMIC_RANGE_SDR:
                return "SDR";
            case QUALITY_DYNAMIC_RANGE_SDR_PLUS:
                return "SDR+";
            case QUALITY_DYNAMIC_RANGE_HDR:
                return "HDR";
        }
        throw new IllegalArgumentException("Unsupported colorRange " + colorRange);
    }

    /**
     * Quality fps. One of
     * {@link #QUALITY_FPS_DEFAULT},
     * {@link #QUALITY_FPS_50},
     * {@link #QUALITY_FPS_60},
     * {@link #QUALITY_FPS_120}
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({QUALITY_FPS_DEFAULT,
            QUALITY_FPS_50,
            QUALITY_FPS_60,
            QUALITY_FPS_120})
    public @interface QualityFps {
    }

    public static final int QUALITY_FPS_DEFAULT = 0;
    public static final int QUALITY_FPS_50 = 50;
    public static final int QUALITY_FPS_60 = 60;
    public static final int QUALITY_FPS_120 = 120;

    /**
     * Quality resolution.  One of
     * {@link #QUALITY_RES_240},
     * {@link #QUALITY_RES_360},
     * {@link #QUALITY_RES_480},
     * {@link #QUALITY_RES_540},
     * {@link #QUALITY_RES_720},
     * {@link #QUALITY_RES_1080},
     * {@link #QUALITY_RES_2K},
     * {@link #QUALITY_RES_4K}
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({QUALITY_RES_240,
            QUALITY_RES_360,
            QUALITY_RES_480,
            QUALITY_RES_540,
            QUALITY_RES_720,
            QUALITY_RES_1080,
            QUALITY_RES_2K,
            QUALITY_RES_4K
    })
    public @interface QualityRes {
    }

    public static final int QUALITY_RES_240 = 240;
    public static final int QUALITY_RES_360 = 360;
    public static final int QUALITY_RES_480 = 480;
    public static final int QUALITY_RES_540 = 540;
    public static final int QUALITY_RES_720 = 720;
    public static final int QUALITY_RES_1080 = 1080;
    public static final int QUALITY_RES_2K = 2000;
    public static final int QUALITY_RES_4K = 4000;

    @QualityRes
    private int qualityRes;
    @QualityDynamicRange
    private int qualityDynamicRange;
    @QualityFps
    private int qualityFps;

    private String qualityDesc;
    private Serializable qualityTag;

    public Quality() {
    }

    public Quality(@QualityRes int qualityRes, String qualityDesc) {
        this(qualityRes, QUALITY_DYNAMIC_RANGE_SDR, QUALITY_FPS_DEFAULT, qualityDesc, null);
    }

    public Quality(@QualityRes int qualityRes,
                   @QualityDynamicRange int qualityDynamicRange,
                   @QualityFps int qualityFps,
                   String qualityDesc,
                   Serializable qualityTag) {
        this.qualityRes = qualityRes;
        this.qualityDynamicRange = qualityDynamicRange;
        this.qualityFps = qualityFps;
        this.qualityDesc = qualityDesc;
        this.qualityTag = qualityTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Quality quality = (Quality) o;
        return qualityRes == quality.qualityRes && qualityDynamicRange == quality.qualityDynamicRange && qualityFps == quality.qualityFps;
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualityRes, qualityDynamicRange, qualityFps);
    }

    @QualityRes
    public int getQualityRes() {
        return qualityRes;
    }

    public void setQualityRes(@QualityRes int qualityRes) {
        this.qualityRes = qualityRes;
    }

    @QualityDynamicRange
    public int getQualityDynamicRange() {
        return qualityDynamicRange;
    }

    public void setQualityDynamicRange(@QualityDynamicRange int qualityDynamicRange) {
        this.qualityDynamicRange = qualityDynamicRange;
    }

    @QualityFps
    public int getQualityFps() {
        return qualityFps;
    }

    public void setQualityFps(@QualityFps int qualityFps) {
        this.qualityFps = qualityFps;
    }

    @Nullable
    public String getQualityDesc() {
        return qualityDesc;
    }

    public void setQualityDesc(String qualityDesc) {
        this.qualityDesc = qualityDesc;
    }

    public Serializable getQualityTag() {
        return qualityTag;
    }

    public void setQualityTag(Serializable qualityTag) {
        this.qualityTag = qualityTag;
    }
}
