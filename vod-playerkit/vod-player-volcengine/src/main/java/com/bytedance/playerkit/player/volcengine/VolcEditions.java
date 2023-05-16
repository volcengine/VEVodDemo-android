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
 * Create Date : 2023/5/25
 */

package com.bytedance.playerkit.player.volcengine;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.ss.ttvideoengine.TTVideoEngine;

public class VolcEditions {

    public static final String PLAYER_EDITION_LITE = "lite";
    public static final String PLAYER_EDITION_PREMIUM = "premium";
    public static final String PLAYER_EDITION_STANDARD = "standard";

    public static int engineCoreType() {
        final String playerEdition = BuildConfig.TTSDK_PLAYER_EDITION;
        switch (playerEdition) {
            case PLAYER_EDITION_PREMIUM:
            case PLAYER_EDITION_STANDARD:
                return TTVideoEngine.PLAYER_TYPE_OWN;
            case PLAYER_EDITION_LITE:
                return TTVideoEngine.PLAYER_TYPE_EXO;
            default:
                throw new IllegalArgumentException("unsupported playerEdition " + playerEdition);
        }
    }

    @NonNull
    public static String dumpEngineCoreType(TTVideoEngine engine) {
        String enginePlayerType = "";
        if (engine != null) {
            if (engine.isPlayerType(TTVideoEngine.PLAYER_TYPE_EXO)) {
                enginePlayerType = "exo";
            } else if (engine.isPlayerType(TTVideoEngine.PLAYER_TYPE_OWN)) {
                enginePlayerType = "own " + BuildConfig.TTSDK_PLAYER_EDITION;
            } else if (engine.isPlayerType(TTVideoEngine.PLAYER_TYPE_OS)) {
                enginePlayerType = "os";
            }
        }
        return enginePlayerType;
    }

    public static boolean isSupportTextureRender() {
        return engineCoreType() == TTVideoEngine.PLAYER_TYPE_OWN;
    }

    public static boolean isSupportSuperResolution() {
        return TextUtils.equals(BuildConfig.TTSDK_PLAYER_EDITION, PLAYER_EDITION_PREMIUM);
    }

    public static boolean isSupportEngineLooper() {
        return engineCoreType() == TTVideoEngine.PLAYER_TYPE_OWN;
    }
}
