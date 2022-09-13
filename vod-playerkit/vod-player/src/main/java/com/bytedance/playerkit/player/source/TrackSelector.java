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
import androidx.annotation.NonNull;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Preload or playback track selector
 */
public interface TrackSelector {

    int TYPE_PLAY = 0;
    int TYPE_PRELOAD = 1;

    /**
     * Select situation type. One of
     * {@link #TYPE_PLAY},
     * {@link #TYPE_PRELOAD}
     */
    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_PLAY, TYPE_PRELOAD})
    @interface Type {
    }

    /**
     * Default implements of {@link TrackSelector}. Return first track in {@code tracks}.
     */
    TrackSelector DEFAULT = (type, trackType, tracks, source) -> tracks.get(0);

    /**
     * Select track for preload or playback
     *
     * @param type      Select situation type. One of {@link Type}
     * @param trackType Select track type. One of {@link Track.TrackType}
     * @param tracks    List of tracks to be selected
     * @return Selected track in tracks.
     */
    @NonNull
    Track selectTrack(@Type int type, @Track.TrackType int trackType, @NonNull List<Track> tracks, @NonNull MediaSource source);
}
