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

package com.bytedance.playerkit.player.volcengine;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.ss.ttvideoengine.source.Source;

import java.io.Serializable;


public class VolcConfig implements Serializable {
    public static final VolcConfig DEFAULT = new VolcConfig();
    public static final String EXTRA_VOLC_CONFIG = "extra_volc_config";

    @NonNull
    public static VolcConfig get(MediaSource mediaSource) {
        if (mediaSource == null) return VolcConfig.DEFAULT;

        VolcConfig volcConfig = mediaSource.getExtra(VolcConfig.EXTRA_VOLC_CONFIG, VolcConfig.class);
        if (volcConfig == null) {
            return VolcConfig.DEFAULT;
        }
        return volcConfig;
    }

    public static final int CODEC_STRATEGY_DISABLE = 0;
    public static final int CODEC_STRATEGY_COST_SAVING_FIRST = Source.KEY_COST_SAVING_FIRST;
    public static final int CODEC_STRATEGY_HARDWARE_DECODE_FIRST = Source.KEY_HARDWARE_DECODE_FIRST;

    public int codecStrategyType;
    @Player.DecoderType
    public int playerDecoderType;
    @Track.EncoderType
    public int sourceEncodeType;

    public boolean enableAudioTrackVolume = false;
    public boolean enableTextureRender = true;
    public boolean enableHlsSeamlessSwitch = true;
    public boolean enableMP4SeamlessSwitch = true;
    public boolean enableDash = true;
    public boolean enableEngineLooper = true;
    public boolean enableSeekEnd = true;
    public boolean enableSuperResolutionAbility = true;
    public boolean enableSuperResolution = false;

}
