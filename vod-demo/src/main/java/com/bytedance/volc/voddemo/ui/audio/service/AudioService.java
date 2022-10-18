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
 * Create Date : 2022/10/18
 */

package com.bytedance.volc.voddemo.ui.audio.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.PlayerEvent;
import com.bytedance.playerkit.player.event.InfoProgressUpdate;
import com.bytedance.playerkit.player.event.StateError;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.impl.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AudioService extends Service {

    public static final String EXTRA_VIDEO_ITEMS = "video_items";

    public static final String EXTRA_TITLE = "extra_title";

    public static final String ACTION_START_PLAY_LIST = "StartPlayList";
    public static final String ACTION_STOP_PLAY_LIST = "StopPlayList";

    private static final String CHANNEL_ID_AUDIO = UUID.randomUUID().toString();
    private static final int AUDIO_NOTIFICATION_ID = 100;

    private Notifications mNotifications;

    public static void startPlayList(Context context, ArrayList<VideoItem> videoItems) {
        Intent intent = new Intent(context, AudioService.class);
        intent.putParcelableArrayListExtra(EXTRA_VIDEO_ITEMS, videoItems);
        intent.setAction(ACTION_START_PLAY_LIST);
        context.startService(intent);
    }

    public static void stopPlayList(Context context) {
        Intent intent = new Intent(context, AudioService.class);
        intent.setAction(ACTION_STOP_PLAY_LIST);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        switch (action) {
            case ACTION_START_PLAY_LIST:
                if (!mStarted) {
                    mStarted = true;
                    onStartPlayList(createPlayList(intent));
                }
                break;
            case ACTION_STOP_PLAY_LIST:
                if (mStarted) {
                    mStarted = false;
                    onStopPlayList();
                }
                break;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private List<MediaSource> createPlayList(Intent intent) {
        List<VideoItem> videoItems = intent.getParcelableArrayListExtra(EXTRA_VIDEO_ITEMS);
        List<MediaSource> mediaSources = new ArrayList<>();
        for (VideoItem videoItem : videoItems) {
            mediaSources.add(VideoItem.toMediaSource(videoItem, false));
        }
        return mediaSources;
    }

    private List<MediaSource> mMedias;
    private Player mPlayer;
    private boolean mStarted;
    private int mCurrentIndex;

    private void onStartPlayList(List<MediaSource> playList) {
        mMedias = playList;

        Notification notification = createNotification();
        startForeground(AUDIO_NOTIFICATION_ID, notification);

        setStayAwake(true);

        play(0);
    }

    private WifiManager.WifiLock mWifiLock;
    private PowerManager.WakeLock mWakeLock;

    private void setStayAwake(boolean awake) {
        if (awake) {
            if (mWakeLock == null) {
                PowerManager powerManager = ((PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE));
                if (powerManager != null) {
                    mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "vodemo:audio_play");
                    mWakeLock.setReferenceCounted(false);
                }
            }
            if (mWakeLock != null) {
                mWakeLock.acquire();
            }
            if (mWifiLock == null) {
                WifiManager wifiManager = ((WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE));
                if (wifiManager != null) {
                    mWifiLock = wifiManager
                            .createWifiLock(WifiManager.WIFI_MODE_FULL, "vodemo:audio_play");
                    mWifiLock.setReferenceCounted(false);
                }
            }
            if (mWifiLock != null) {
                mWifiLock.acquire();
            }
        } else {
            if (mWifiLock != null) {
                mWifiLock.release();
            }
            if (mWakeLock != null) {
                mWakeLock.release();
            }
        }
    }

    private Notification createNotification() {
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID_AUDIO)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setAutoCancel(false)
                .setOngoing(true);
        mNotifications.put(AUDIO_NOTIFICATION_ID, builder);
        return builder.build();
    }

    private void play(int index) {
        mCurrentIndex = index;
        L.i(AudioService.this, "play", index, "prepare");
        MediaSource source = mMedias.get(index);

        NotificationCompat.Builder builder = mNotifications.get(AUDIO_NOTIFICATION_ID);
        builder.setProgress(100, 0, false);
        builder.setContentTitle(source.getExtra(EXTRA_TITLE, String.class));
        builder.setContentText("start");
        mNotifications.notify(AUDIO_NOTIFICATION_ID);

        mPlayer = Player.Factory.Default.get().create(source);
        mPlayer.setStartWhenPrepared(true);
        mPlayer.prepare(source);
        mPlayer.addPlayerListener(new Dispatcher.EventListener() {
            @Override
            public void onEvent(Event event) {
                switch (event.code()) {
                    case PlayerEvent.Info.AUDIO_RENDERING_START: {
                        L.i(AudioService.this, "onEvent", index, "playing");

                        NotificationCompat.Builder builder = mNotifications.get(AUDIO_NOTIFICATION_ID);
                        builder.setContentText("playing");
                        mNotifications.notify(AUDIO_NOTIFICATION_ID);
                        break;
                    }
                    case PlayerEvent.Info.PROGRESS_UPDATE: {
                        InfoProgressUpdate e = event.cast(InfoProgressUpdate.class);

                        NotificationCompat.Builder builder = mNotifications.get(AUDIO_NOTIFICATION_ID);
                        builder.setProgress(100, (int) (e.currentPosition / (float) e.duration * 100), false);
                        mNotifications.notify(AUDIO_NOTIFICATION_ID);
                        break;
                    }
                    case PlayerEvent.State.COMPLETED: {
                        L.i(AudioService.this, "onEvent", index, "complete");

                        NotificationCompat.Builder builder = mNotifications.get(AUDIO_NOTIFICATION_ID);
                        builder.setProgress(100, 100, false)
                                .setContentText("complete");
                        mNotifications.notify(AUDIO_NOTIFICATION_ID);

                        mPlayer.release();

                        int next = index + 1;
                        if (next < mMedias.size()) {
                            play(next);
                        } else {
                            L.i(AudioService.this, "onEvent", "All Complete");
                        }
                        break;
                    }
                    case PlayerEvent.State.ERROR: {
                        NotificationCompat.Builder builder = mNotifications.get(AUDIO_NOTIFICATION_ID);
                        builder.setContentText("error " + event.cast(StateError.class).e.getCode());
                        mNotifications.notify(AUDIO_NOTIFICATION_ID);
                        break;
                    }
                }
            }
        });
    }

    private void stop() {
        if (mPlayer != null) {
            L.i(AudioService.this, "stop", mCurrentIndex, "release");
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void onStopPlayList() {
        stop();
        stopForeground(true);
        setStayAwake(false);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notifications.newChannel(this,
                    CHANNEL_ID_AUDIO,
                    "Audio Playback",
                    "Audio Playback notification.",
                    NotificationManager.IMPORTANCE_LOW);
        }
        mNotifications = new Notifications(this);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mStarted = false;
        onStopPlayList();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
