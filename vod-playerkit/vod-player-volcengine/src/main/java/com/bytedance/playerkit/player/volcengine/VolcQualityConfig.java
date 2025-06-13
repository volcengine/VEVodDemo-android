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
 * Create Date : 2023/5/24
 */

package com.bytedance.playerkit.player.volcengine;

import androidx.annotation.IntDef;

import com.bytedance.playerkit.player.config.ABRQualityConfig;
import com.bytedance.playerkit.player.source.Quality;

import java.io.Serializable;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class VolcQualityConfig implements Serializable {
    /**
     * Quality mode. One of
     * {@link #QUALITY_MODE_DEFAULT},
     * {@link #QUALITY_MODE_STARTUP_ABR},
     * {@link #QUALITY_MODE_ABR}
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({QUALITY_MODE_DEFAULT, QUALITY_MODE_STARTUP_ABR, QUALITY_MODE_ABR})
    public @interface QualityMode {
    }
    public static final int QUALITY_MODE_DEFAULT = 0;
    public static final int QUALITY_MODE_STARTUP_ABR = 1;
    public static final int QUALITY_MODE_ABR = 2;

    @QualityMode
    public int qualityMode;
    public boolean enableSupperResolutionDowngrade;
    public Quality userSelectedQuality;
    public ABRQualityConfig abrQualityConfig;
}
