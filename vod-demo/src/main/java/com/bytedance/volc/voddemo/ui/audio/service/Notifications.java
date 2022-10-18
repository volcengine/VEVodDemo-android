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
import android.app.NotificationChannel;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.LinkedHashMap;
import java.util.Map;


public class Notifications {

    public static void newChannel(Context context, String id, String name, String desc, int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(desc);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            android.app.NotificationManager notificationManager = context.getSystemService(android.app.NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Map<Integer, NotificationCompat.Builder> notifications = new LinkedHashMap<>();

    private NotificationManagerCompat manager;

    private Handler handler;

    public Notifications(Context context) {
        manager = NotificationManagerCompat.from(context);
        HandlerThread handlerThread = new HandlerThread("notification");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    public void put(int id, NotificationCompat.Builder builder) {
        notifications.put(id, builder);
    }

    public NotificationCompat.Builder get(int id) {
        return notifications.get(id);
    }

    public void notify(final int id) {
        NotificationCompat.Builder builder = notifications.get(id);
        if (builder != null) {
            final Notification notification = builder.build();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    manager.notify(id, notification);
                }
            });
        }
    }

    public void cancel(final int id) {
        notifications.remove(id);
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.cancel(id);
            }
        });
    }

    public void cancelAll() {
        notifications.clear();
        handler.post(new Runnable() {
            @Override
            public void run() {
                manager.cancelAll();
            }
        });
    }

    public void release() {
        cancelAll();
        handler.post(new Runnable() {
            @Override
            public void run() {
                handler.getLooper().quitSafely();
            }
        });
    }
}
