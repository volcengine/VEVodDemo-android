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
 * Create Date : 2023/6/21
 */

package com.bytedance.playerkit.player.volcengine;

import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.PlayerException;
import com.bytedance.playerkit.player.adapter.PlayerAdapter;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Subtitle;
import com.bytedance.playerkit.player.source.SubtitleText;
import com.bytedance.playerkit.player.source.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class VolcPlayerEventRecorder implements PlayerAdapter.Listener {
    private final List<VolcEvent> mEvents = Collections.synchronizedList(new ArrayList<>());
    private volatile PlayerAdapter.Listener mListener;

    @Override
    public void onPrepared(@NonNull PlayerAdapter mp) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onPrepared,
                null,
                () -> mListener.onPrepared(mp)));
    }

    @Override
    public void onCompletion(@NonNull PlayerAdapter mp) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onCompletion,
                null,
                () -> mListener.onCompletion(mp)));
    }

    @Override
    public void onError(@NonNull PlayerAdapter mp, @NonNull PlayerException e) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onError,
                new Object[]{e},
                () -> mListener.onError(mp, e)));
    }

    @Override
    public void onSeekComplete(@NonNull PlayerAdapter mp) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSeekComplete,
                null,
                () -> mListener.onSeekComplete(mp)));
    }

    @Override
    public void onVideoSizeChanged(@NonNull PlayerAdapter mp, int width, int height) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onVideoSizeChanged,
                new Object[]{width, height},
                () -> mListener.onVideoSizeChanged(mp, width, height)));
    }

    @Override
    public void onSARChanged(@NonNull PlayerAdapter mp, int num, int den) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSARChanged,
                new Object[]{num, den},
                () -> mListener.onSARChanged(mp, num, den)));
    }

    @Override
    public void onBufferingUpdate(@NonNull PlayerAdapter mp, int percent) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onBufferingUpdate,
                new Object[]{percent},
                () -> mListener.onBufferingUpdate(mp, percent)));
    }

    @Override
    public void onProgressUpdate(@NonNull PlayerAdapter mp, long position) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onProgressUpdate,
                new Object[]{position},
                () -> mListener.onProgressUpdate(mp, position)));
    }

    @Override
    public void onInfo(@NonNull PlayerAdapter mp, int what, @Nullable Object extra) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onInfo,
                new Object[]{what, extra},
                () -> mListener.onInfo(mp, what, extra)));
    }

    @Override
    public void onCacheHint(@NonNull PlayerAdapter mp, long cacheSize) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onCacheHint,
                new Object[]{cacheSize},
                () -> mListener.onCacheHint(mp, cacheSize)));
    }

    @Override
    public void onGetPlayInfoResult(@NonNull PlayerAdapter mp, @NonNull MediaSource mediaSource, @Nullable Object playInfo, @Nullable PlayerException e) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onGetPlayInfoResult,
                new Object[]{mediaSource, playInfo, e},
                () -> mListener.onGetPlayInfoResult(mp, mediaSource, playInfo, e)));
    }

    @Override
    public void onTrackInfoReady(@NonNull PlayerAdapter mp, int trackType, @NonNull List<Track> tracks) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onTrackInfoReady,
                new Object[]{trackType, tracks},
                () -> mListener.onTrackInfoReady(mp, trackType, tracks)));
    }

    @Override
    public void onTrackWillChange(@NonNull PlayerAdapter mp, int trackType, @Nullable Track current, @NonNull Track target) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onTrackWillChange,
                new Object[]{trackType, trackType, current, target},
                () -> mListener.onTrackWillChange(mp, trackType, current, target)));
    }

    @Override
    public void onTrackChanged(@NonNull PlayerAdapter mp, int trackType, @NonNull Track pre, @NonNull Track current) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onTrackChanged,
                new Object[]{trackType, pre, current},
                () -> mListener.onTrackChanged(mp, trackType, pre, current)));
    }

    @Override
    public void onSubtitleStateChanged(@NonNull PlayerAdapter mp, boolean enabled) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleStateChanged,
                new Object[]{enabled},
                () -> mListener.onSubtitleStateChanged(mp, enabled)));
    }

    @Override
    public void onSubtitleInfoFetchError(@NonNull PlayerAdapter mp, @NonNull PlayerException e) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleInfoFetchError,
                new Object[]{e},
                () -> mListener.onSubtitleInfoFetchError(mp, e)));
    }

    @Override
    public void onSubtitleInfoReady(@NonNull PlayerAdapter mp, List<Subtitle> subtitles) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleInfoReady,
                new Object[]{subtitles},
                () -> mListener.onSubtitleInfoReady(mp, subtitles)));
    }

    @Override
    public void onSubtitleFileLoadFinish(@NonNull PlayerAdapter mp, int success, String info) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleFileLoadFinish,
                new Object[]{success, info},
                () -> mListener.onSubtitleFileLoadFinish(mp, success, info)));
    }

    @Override
    public void onSubtitleWillChange(@NonNull PlayerAdapter mp, Subtitle current, @NonNull Subtitle target) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleWillChange,
                new Object[]{current, target},
                () -> mListener.onSubtitleWillChange(mp, current, target)));
    }

    @Override
    public void onSubtitleChanged(@NonNull PlayerAdapter mp, Subtitle pre, @NonNull Subtitle current) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleChanged,
                new Object[]{pre, current},
                () -> mListener.onSubtitleChanged(mp, pre, current)));
    }

    @Override
    public void onSubtitleTextUpdate(@NonNull PlayerAdapter mp, @NonNull SubtitleText subtitleText) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleTextUpdate,
                new Object[]{subtitleText},
                () -> mListener.onSubtitleTextUpdate(mp, subtitleText)));
    }

    @Override
    public void onSubtitleCacheHint(@NonNull PlayerAdapter mp, long cacheSize) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onSubtitleCacheHint,
                new Object[]{cacheSize},
                () -> mListener.onSubtitleCacheHint(mp, cacheSize)));
    }

    @Override
    public void onFrameInfoUpdate(@NonNull PlayerAdapter mp, int frameType, long pts, long clockTime) {
        mEvents.add(new VolcEvent(VolcEvent.EVENT_onFrameInfoUpdate,
                new Object[]{frameType, pts, clockTime},
                () -> mListener.onFrameInfoUpdate(mp, frameType, pts, clockTime)));
    }

    void notifyEvents(PlayerAdapter.Listener listener) {
        this.mListener = listener;
        List<VolcEvent> events;
        synchronized (mEvents) {
            events = new ArrayList<>(mEvents);
            mEvents.clear();
        }
        for (VolcEvent event : events) {
            event.execute();
        }
    }

    static class VolcEvent {
        static final int EVENT_onPrepared = 1;
        static final int EVENT_onCompletion = 2;
        static final int EVENT_onError = 3;
        static final int EVENT_onSeekComplete = 4;
        static final int EVENT_onVideoSizeChanged = 5;
        static final int EVENT_onSARChanged = 6;
        static final int EVENT_onBufferingUpdate = 7;
        static final int EVENT_onProgressUpdate = 8;
        static final int EVENT_onInfo = 9;
        static final int EVENT_onCacheHint = 10;

        static final int EVENT_onGetPlayInfoResult = 11;

        static final int EVENT_onTrackInfoReady = 15;
        static final int EVENT_onTrackWillChange = 16;
        static final int EVENT_onTrackChanged = 17;

        static final int EVENT_onSubtitleStateChanged = 18;
        static final int EVENT_onSubtitleInfoReady = 19;
        static final int EVENT_onSubtitleFileLoadFinish = 20;
        static final int EVENT_onSubtitleWillChange = 21;
        static final int EVENT_onSubtitleChanged = 22;
        static final int EVENT_onSubtitleTextUpdate = 23;
        static final int EVENT_onSubtitleCacheHint = 24;
        static final int EVENT_onSubtitleInfoFetchError = 25;

        static final int EVENT_onFrameInfoUpdate = 26;

        final int type;

        final Object[] params;
        final Runnable runnable;
        private long createTime;
        private long executeTime;

        VolcEvent(int type, Object[] params, Runnable runnable) {
            this.type = type;
            this.params = params;
            this.runnable = runnable;
            this.createTime = SystemClock.uptimeMillis();
        }

        void execute() {
            executeTime = SystemClock.uptimeMillis();
            if (this.runnable != null) {
                this.runnable.run();
            }
        }
    }
}
