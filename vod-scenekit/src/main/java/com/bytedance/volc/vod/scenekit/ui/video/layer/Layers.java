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

package com.bytedance.volc.vod.scenekit.ui.video.layer;


public class Layers {

    public static final class BackPriority {
        public static final int DIALOG_LAYER_BACK_PRIORITY = 10000;
        private static int sIndex = 0;
        public static final int QUALITY_SELECT_DIALOG_LAYER_BACK_PRIORITY = DIALOG_LAYER_BACK_PRIORITY + (sIndex++);
        public static final int SPEED_SELECT_DIALOG_LAYER_BACK_PRIORITY = DIALOG_LAYER_BACK_PRIORITY + (sIndex++);
        public static final int VOLUME_BRIGHTNESS_DIALOG_BACK_PRIORITY = DIALOG_LAYER_BACK_PRIORITY + (sIndex++);
        public static final int TIME_PROGRESS_DIALOG_LAYER_PRIORITY = DIALOG_LAYER_BACK_PRIORITY + (sIndex++);
        public static final int MORE_DIALOG_LAYER_BACK_PRIORITY = DIALOG_LAYER_BACK_PRIORITY + (sIndex++);
    }

    public static final class VisibilityRequestReason {
        public static final String REQUEST_DISMISS_REASON_DIALOG_SHOW = "request_dismiss_reason_dialog_show";
    }
}
