/*
 * Copyright (C) 2026 bytedance
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
 */

package com.bytedance.vodplayer.example.quickstart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.ss.ttvideoengine.TTVideoEngine;
import com.ss.ttvideoengine.VideoEngineCallback;
import com.ss.ttvideoengine.source.DirectUrlSource;
import com.ss.ttvideoengine.source.DirectUrlSource.UrlItem;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.json.JSONArray;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class VodSdkInitializerInstrumentedTest {

    private static final long TIMEOUT_SECONDS = 20;

    @Test
    public void testProductionInitializer_callbackGateAllowsEngineCreation() throws Exception {
        CountDownLatch firstCallback = new CountDownLatch(1);
        AtomicBoolean firstSuccess = new AtomicBoolean();
        AtomicReference<Throwable> firstError = new AtomicReference<>();
        AtomicReference<Thread> callbackThread = new AtomicReference<>();

        // 默认 App 走同步初始化；这里用无副作用任务单独验证异步协调器和页面门禁。
        VodSdkInitializer.start(() -> {
        }, null);
        VodSdkInitializer.whenReady((success, error) -> {
            firstSuccess.set(success);
            firstError.set(error);
            callbackThread.set(Thread.currentThread());
            firstCallback.countDown();
        });

        assertTrue(firstCallback.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertTrue(firstSuccess.get());
        assertNull(firstError.get());
        assertEquals(Looper.getMainLooper().getThread(), callbackThread.get());
        assertEquals(VodSdkInitializer.State.SUCCESS, VodSdkInitializer.getState());
        assertNull(VodSdkInitializer.getInitError());

        CountDownLatch lateCallback = new CountDownLatch(1);
        AtomicReference<Thread> lateCallbackThread = new AtomicReference<>();
        Thread backgroundCaller = new Thread(() ->
                VodSdkInitializer.whenReady((success, error) -> {
                    if (success && error == null) {
                        lateCallbackThread.set(Thread.currentThread());
                        lateCallback.countDown();
                    }
                }), "late-vod-ready-caller");
        backgroundCaller.start();
        backgroundCaller.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        assertTrue(lateCallback.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(Looper.getMainLooper().getThread(), lateCallbackThread.get());

        AtomicBoolean requestReturned = new AtomicBoolean();
        AtomicBoolean actionCalledInline = new AtomicBoolean();
        AtomicReference<VodSdkInitializer.ReadyRequest> immediateRequest =
                new AtomicReference<>();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            immediateRequest.set(
                    VodSdkInitializer.runWhenReady(
                            null,
                            () -> actionCalledInline.set(!requestReturned.get()),
                            error -> {
                            }));
            requestReturned.set(true);
        });
        assertTrue("terminal action should run before runWhenReady returns",
                actionCalledInline.get());
        assertNull(
                "completed request must not retain its init subscription",
                getField(immediateRequest.get(), "initRequest"));

        AtomicBoolean destroyedOwnerActionCalled = new AtomicBoolean();
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            TestLifecycleOwner owner = new TestLifecycleOwner();
            owner.handleEvent(Lifecycle.Event.ON_CREATE);
            owner.handleEvent(Lifecycle.Event.ON_DESTROY);
            VodSdkInitializer.runWhenReady(
                    owner,
                    () -> destroyedOwnerActionCalled.set(true),
                    error -> destroyedOwnerActionCalled.set(true));
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        assertFalse(destroyedOwnerActionCalled.get());

        CountDownLatch playbackPrepared = new CountDownLatch(1);
        AtomicReference<com.ss.ttvideoengine.utils.Error> playbackError =
                new AtomicReference<>();
        AtomicReference<TTVideoEngine> engineRef = new AtomicReference<>();
        String videoUrl = loadFirstVideoUrl();
        String cacheKey = TTVideoEngine.computeMD5(new URL(videoUrl).getPath());
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            TTVideoEngine engine = new TTVideoEngine(
                    InstrumentationRegistry.getInstrumentation()
                            .getTargetContext()
                            .getApplicationContext());
            engineRef.set(engine);
            DirectUrlSource source = new DirectUrlSource.Builder()
                    .setVid("async-init-playback-test")
                    .addItem(
                            new UrlItem.Builder()
                                    .setUrl(videoUrl)
                                    .setCacheKey(cacheKey)
                                    .build())
                    .build();
            engine.setStrategySource(source);
            engine.addVideoEngineCallback(new VideoEngineCallback() {
                @Override
                public void onPrepared(TTVideoEngine videoEngine) {
                    playbackPrepared.countDown();
                }

                @Override
                public void onError(com.ss.ttvideoengine.utils.Error error) {
                    playbackError.set(error);
                    playbackPrepared.countDown();
                }
            });
            engine.play();
        });

        try {
            assertTrue(playbackPrepared.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertNull(playbackError.get());
        } finally {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                TTVideoEngine engine = engineRef.get();
                if (engine != null) {
                    engine.release();
                }
            });
        }
    }

    private static String loadFirstVideoUrl() throws Exception {
        try (InputStream inputStream =
                     InstrumentationRegistry.getInstrumentation()
                             .getTargetContext()
                             .getAssets()
                             .open("video_items.json");
             Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A")) {
            return new JSONArray(scanner.next()).getJSONObject(0).getString("videoUrl");
        }
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static final class TestLifecycleOwner implements LifecycleOwner {

        private final LifecycleRegistry lifecycle = new LifecycleRegistry(this);

        @NonNull
        @Override
        public Lifecycle getLifecycle() {
            return lifecycle;
        }

        private void handleEvent(Lifecycle.Event event) {
            lifecycle.handleLifecycleEvent(event);
        }
    }
}
