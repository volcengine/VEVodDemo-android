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

package com.bytedance.playerkit.player.volcengine;

import android.content.Context;

import com.bytedance.playerkit.player.source.MediaSource;
import com.ss.ttvideoengine.TTVideoEngine;

public interface TTVideoEngineFactory {

    class Default {

        private static TTVideoEngineFactory sInstance = new TTVideoEngineFactoryDefault();

        public static TTVideoEngineFactory get() {
            return sInstance;
        }

        public static void set(TTVideoEngineFactory instance) {
            sInstance = instance;
        }
    }

    TTVideoEngine create(Context context, MediaSource mediaSource);
}
