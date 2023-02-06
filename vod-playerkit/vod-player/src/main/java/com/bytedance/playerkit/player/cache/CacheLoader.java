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

package com.bytedance.playerkit.player.cache;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;

import java.io.File;
import java.io.IOException;
import java.util.List;


public interface CacheLoader {

    class Default {

        private static CacheLoader sInstance;

        public synchronized static void set(CacheLoader loader) {
            sInstance = loader;
        }

        public synchronized static CacheLoader get() {
            return sInstance;
        }
    }

    void preload(@NonNull MediaSource source, Task.Listener listener);

    void stopTask(@NonNull MediaSource source);

    void stopTask(@NonNull String mediaId);

    void stopAllTask();

    void clearCache();

    File getCacheDir();

    @NonNull
    CacheInfo getCacheInfo(String cacheKey);

    long getCachedSize(String cacheKey);

    class CacheInfo {
        public final String cacheKey;
        // The size of media.
        public final long sizeInBytes;
        // Non-stop cache size from scratch.
        public final long cachedSizeInBytes;
        // Local file path.
        public final String cachePath;

        public CacheInfo(String cacheKey, long sizeInBytes, long cachedSizeInBytes, String cachePath) {
            this.cacheKey = cacheKey;
            this.sizeInBytes = sizeInBytes;
            this.cachedSizeInBytes = cachedSizeInBytes;
            this.cachePath = cachePath;
        }
    }

    interface Task {

        String EXTRA_PRELOAD_SIZE_IN_BYTES = "extra_preload_size_in_bytes";

        interface Factory {
            Task create();
        }

        long DEFAULT_PRELOAD_SIZE = 800 * 1024;

        default String mapState(int state) {
            switch (state) {
                case STATE_IDLE:
                    return "idle";
                case STATE_STARTED:
                    return "started";
                case STATE_END_FINISHED:
                    return "end_finished";
                case STATE_END_STOPPED:
                    return "end_stopped";
                case STATE_END_ERROR:
                    return "end_error";
            }
            throw new IllegalArgumentException("unsupported state " + state);
        }

        int STATE_IDLE = 0;
        int STATE_STARTED = 1;
        int STATE_END_FINISHED = 2;
        int STATE_END_STOPPED = 3;
        int STATE_END_ERROR = 4;

        interface Listener extends SourceListener {
            class Adapter implements Listener {

                @Override
                public void onStart(Task task) {
                }

                @Override
                public void onFinished(Task task) {
                }

                @Override
                public void onStopped(Task task) {
                }

                @Override
                public void onError(Task task, IOException e) {
                }

                @Override
                public void onDataSourcePrepared(@NonNull Task task, MediaSource source) {
                }
            }

            void onStart(Task task);

            //void onProgressChanged(Task task, long totalBytes, long downloadedBytes);

            void onFinished(Task task);

            void onStopped(Task task);

            void onError(Task task, IOException e);
        }

        interface SourceListener {
            void onDataSourcePrepared(@NonNull Task task, MediaSource source);
        }

        void setDataSource(@NonNull MediaSource mediaSource);

        MediaSource getDataSource();

        void setCacheKeyFactory(CacheKeyFactory keyFactory);

        void setTrackSelector(TrackSelector trackSelector);

        void setListener(Listener listener);

        void addDepListener(Listener listener);

        Track getSelectedTrack(@Track.TrackType int type);

        List<Track> getTracks(@Track.TrackType int type);

        void start();

        void stop();

        boolean isStarted();

        boolean isStopped();

        boolean isEnd();

        File getCacheDir();
    }
}
