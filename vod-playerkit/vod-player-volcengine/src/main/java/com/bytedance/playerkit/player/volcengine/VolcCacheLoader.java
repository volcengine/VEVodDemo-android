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

package com.bytedance.playerkit.player.volcengine;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.bytedance.playerkit.player.cache.CacheKeyFactory;
import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.source.TrackSelector;
import com.bytedance.playerkit.player.volcengine.utils.DataLoaderListenerAdapter;
import com.bytedance.playerkit.utils.L;
import com.ss.ttvideoengine.DataLoaderHelper;
import com.ss.ttvideoengine.TTVideoEngine;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


class VolcCacheLoader implements CacheLoader {

    private final List<Task> mTasks = new ArrayList<>();

    private final Context mContext;
    private final Task.Factory mTaskFactory;
    private final CacheKeyFactory mCacheKeyFactory;
    private final TrackSelector mTrackSelector;

    VolcCacheLoader(Context context,
                    Task.Factory taskFactory) {
        this.mContext = context.getApplicationContext();
        this.mTaskFactory = taskFactory;
        this.mTrackSelector = VolcPlayerInit.getTrackSelector();
        this.mCacheKeyFactory = VolcPlayerInit.getCacheKeyFactory();

        TTVideoEngine.setDataLoaderListener(new DataLoaderListenerAdapter() {

            @Override
            public void onLoadProgress(DataLoaderHelper.DataLoaderTaskLoadProgress loadProgress) {
                //TODO
            }
        });
    }

    @Override
    public void preload(@NonNull MediaSource source, Task.Listener listener) {
        L.v(this, "preload", MediaSource.dump(source));
        Task task = mTaskFactory.create();
        task.setDataSource(source);
        task.setTrackSelector(mTrackSelector);
        task.setCacheKeyFactory(mCacheKeyFactory);
        task.setListener(new Task.Listener() {
            @Override
            public void onStart(Task task) {
                if (listener != null) {
                    listener.onStart(task);
                }
            }

            @Override
            public void onFinished(Task task) {
                if (listener != null) {
                    listener.onFinished(task);
                }
                onTaskEnd(task);
            }

            @Override
            public void onStopped(Task task) {
                if (listener != null) {
                    listener.onStopped(task);
                }
                onTaskEnd(task);
            }

            @Override
            public void onError(Task task, IOException e) {
                if (listener != null) {
                    listener.onError(task, e);
                }
                onTaskEnd(task);
            }

            @Override
            public void onDataSourcePrepared(@NonNull Task task, MediaSource source) {
                if (listener != null) {
                    listener.onDataSourcePrepared(task, source);
                }
            }
        });
        task.start();
        synchronized (mTasks) {
            mTasks.add(task);
        }
    }

    @Override
    public void stopTask(@NonNull MediaSource source) {
        L.v(this, "stopTask", MediaSource.dump(source));
        stopTask(source.getMediaId());
    }

    @Override
    public void stopTask(@NonNull String mediaId) {
        L.v(this, "stopTask", mediaId);
        synchronized (mTasks) {
            Iterator<Task> iterator = mTasks.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                if (TextUtils.equals(mediaId, task.getDataSource().getMediaId())) {
                    task.stop();
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void stopAllTask() {
        L.v(this, "stopAllTask");
        synchronized (mTasks) {
            Iterator<Task> iterator = mTasks.iterator();
            while (iterator.hasNext()) {
                Task task = iterator.next();
                task.stop();
                iterator.remove();
            }
        }
    }

    @Override
    public void clearCache() {
        L.v(this, "clearCache");
        TTVideoEngine.clearAllCaches(true);
    }

    @Override
    public File getCacheDir() {
        return VolcPlayerInit.cacheDir(mContext);
    }

    @Override
    public long getCacheSize(@NonNull MediaSource source, @Track.TrackType int type) {
        List<Track> tracks = source.getTracks(type);
        if (tracks != null && !tracks.isEmpty()) {
            final Track track = mTrackSelector.selectTrack(TrackSelector.TYPE_PRELOAD, type, tracks, source);
            if (track != null) {
                String cacheKey = mCacheKeyFactory.generateCacheKey(source, track);
                if (!TextUtils.isEmpty(cacheKey)) {
                    return TTVideoEngine.getCacheFileSize(cacheKey);
                }
            }
        }
        return -1;
    }

    void onTaskEnd(Task task) {
        synchronized (mTasks) {
            mTasks.remove(task);
        }
    }
}
