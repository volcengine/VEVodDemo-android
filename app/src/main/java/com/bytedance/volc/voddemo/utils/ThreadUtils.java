/*
 * Copyright 2021 bytedance
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
 * Create Date : 5/5/22
 */
package com.bytedance.volc.voddemo.utils;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import com.ss.ttvideoengine.TTVideoEngineLooperThread;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadUtils {
    private static Handler sEngineWorkHandler;

    public static void runOnUiThread(Runnable task) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run();
        } else {
            new Handler(Looper.getMainLooper()).post(task);
        }
    }

    public static synchronized void runOnWorkThread(Runnable task) {
        if (sEngineWorkHandler == null) {
            HandlerThread sEngineWorkThread = new HandlerThread("engine_work");
            sEngineWorkThread.start();
            sEngineWorkHandler = new Handler(sEngineWorkThread.getLooper());
        }

        sEngineWorkHandler.post(task);
    }
}
