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

package com.bytedance.playerkit.player.volcengine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.SystemClock;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bytedance.playerkit.player.PlayerKit;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class VolcPlayerInitInstrumentedTest {

    private static final long TIMEOUT_SECONDS = 20;

    @Test
    public void testAsyncInit_stateMachineAndThreadGuards() throws Exception {
        testMissingConfigFailure();
        resetBeforeSdkInitialization();

        testMainLooperWorkerFailure();
        resetBeforeSdkInitialization();

        testStoppedWorkerFailure();
        resetBeforeSdkInitialization();

        testBackgroundWaitFailsFast();
        resetBeforeSdkInitialization();

        testConcurrentCallbacksMainThreadGuardAndRealInitialization();
    }

    private void testMissingConfigFailure() throws Exception {
        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger();
        AtomicReference<Thread> callbackThread = new AtomicReference<>();

        VolcPlayerAsyncInitializer.start((value, error) -> {
            result.set(value);
            callbackThread.set(Thread.currentThread());
            callbackLatch.countDown();
        });

        assertTrue(callbackLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(VolcPlayerAsyncInitializer.InitCallback.RESULT_ERROR, result.get());
        assertEquals(Looper.getMainLooper().getThread(), callbackThread.get());
        assertEquals(VolcPlayerInit.INIT_STATE_ERROR, VolcPlayerInit.getInitState());
        assertNotNull(VolcPlayerInit.getInitError());
        assertFalse(VolcPlayerInit.isInitialized());
    }

    private void testMainLooperWorkerFailure() throws Exception {
        configure(new Handler(Looper.getMainLooper()));
        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger();

        VolcPlayerAsyncInitializer.start((value, error) -> {
            result.set(value);
            callbackLatch.countDown();
        });

        assertTrue(callbackLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(VolcPlayerAsyncInitializer.InitCallback.RESULT_ERROR, result.get());
        assertTrue(VolcPlayerInit.getInitError() instanceof IllegalArgumentException);
        assertEquals(VolcPlayerInit.INIT_STATE_ERROR, VolcPlayerInit.getInitState());
    }

    private void testStoppedWorkerFailure() throws Exception {
        HandlerThread stoppedThread = new HandlerThread("stopped-vod-init");
        stoppedThread.start();
        Handler stoppedHandler = new Handler(stoppedThread.getLooper());
        stoppedThread.quitSafely();
        stoppedThread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        configure(stoppedHandler);

        CountDownLatch callbackLatch = new CountDownLatch(1);
        AtomicInteger result = new AtomicInteger();
        VolcPlayerAsyncInitializer.start((value, error) -> {
            result.set(value);
            callbackLatch.countDown();
        });

        assertTrue(callbackLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(VolcPlayerAsyncInitializer.InitCallback.RESULT_ERROR, result.get());
        assertTrue(VolcPlayerInit.getInitError() instanceof IllegalStateException);
        assertEquals(VolcPlayerInit.INIT_STATE_ERROR, VolcPlayerInit.getInitState());
    }

    private void testBackgroundWaitFailsFast() throws Exception {
        setStaticField(
                VolcPlayerInit.class,
                "sInitState",
                VolcPlayerInit.INIT_STATE_INITING);

        CountDownLatch waiterReturned = new CountDownLatch(1);
        AtomicReference<Throwable> waitError = new AtomicReference<>();
        Thread waiter = new Thread(() -> {
            try {
                VolcPlayerInit.waitInitAsyncResult();
            } catch (Throwable throwable) {
                waitError.set(throwable);
            } finally {
                waiterReturned.countDown();
            }
        }, "vod-api-caller");

        try {
            waiter.start();
            assertTrue(
                    "SDK readiness checks must not block background callers",
                    waiterReturned.await(1, TimeUnit.SECONDS));
            assertTrue(waitError.get() instanceof IllegalStateException);
        } finally {
            waiter.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        }
    }

    private void testConcurrentCallbacksMainThreadGuardAndRealInitialization()
            throws Exception {
        HandlerThread workerThread = new HandlerThread("test-vod-init");
        workerThread.start();
        Handler workerHandler = new Handler(workerThread.getLooper());
        configure(workerHandler);

        CountDownLatch releaseWorker = new CountDownLatch(1);
        CountDownLatch workerBlocked = new CountDownLatch(1);
        workerHandler.post(() -> {
            workerBlocked.countDown();
            await(releaseWorker);
        });
        assertTrue(workerBlocked.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

        int subscriberCount = 20;
        CountDownLatch initCallsFinished = new CountDownLatch(subscriberCount);
        CountDownLatch callbacksFinished = new CountDownLatch(subscriberCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger mainThreadCallbackCount = new AtomicInteger();
        ExecutorService callers = Executors.newFixedThreadPool(8);
        try {
            for (int i = 0; i < subscriberCount; i++) {
                callers.execute(() -> {
                    VolcPlayerAsyncInitializer.start((value, error) -> {
                        if (value == VolcPlayerAsyncInitializer.InitCallback.RESULT_SUCCESS) {
                            successCount.incrementAndGet();
                        }
                        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
                            mainThreadCallbackCount.incrementAndGet();
                        }
                        callbacksFinished.countDown();
                    });
                    initCallsFinished.countDown();
                });
            }
            assertTrue(initCallsFinished.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(VolcPlayerInit.INIT_STATE_INITING, VolcPlayerInit.getInitState());

            AtomicBoolean pendingCancelledCallbackCalled = new AtomicBoolean();
            VolcPlayerAsyncInitializer.InitRequest pendingRequest =
                    VolcPlayerAsyncInitializer.whenReady(
                            (value, error) -> pendingCancelledCallbackCalled.set(true));
            pendingRequest.cancel();
            pendingRequest.cancel();

            AtomicReference<Throwable> mainThreadFailure = new AtomicReference<>();
            long waitStartMs = SystemClock.elapsedRealtime();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                try {
                    VolcPlayerInit.waitInitAsyncResult();
                } catch (Throwable throwable) {
                    mainThreadFailure.set(throwable);
                }
            });
            long mainThreadWaitMs = SystemClock.elapsedRealtime() - waitStartMs;
            assertTrue(mainThreadFailure.get() instanceof IllegalStateException);
            assertTrue("main thread guard should fail fast", mainThreadWaitMs < 1000);

            releaseWorker.countDown();
            assertTrue(callbacksFinished.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(subscriberCount, successCount.get());
            assertEquals(subscriberCount, mainThreadCallbackCount.get());
            assertFalse(pendingCancelledCallbackCalled.get());
            assertEquals(VolcPlayerInit.INIT_STATE_SUCCESS, VolcPlayerInit.getInitState());
            assertTrue(VolcPlayerInit.isInitialized());
            assertNull(VolcPlayerInit.getInitError());
            VolcPlayerInit.requireInitialized();

            CountDownLatch lateCallbackLatch = new CountDownLatch(1);
            AtomicInteger lateCallbackResult = new AtomicInteger();
            VolcPlayerAsyncInitializer.whenReady((value, error) -> {
                lateCallbackResult.set(value);
                lateCallbackLatch.countDown();
            });
            assertTrue(lateCallbackLatch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(
                    VolcPlayerAsyncInitializer.InitCallback.RESULT_SUCCESS,
                    lateCallbackResult.get());

            AtomicBoolean lateRequestReturned = new AtomicBoolean();
            AtomicBoolean lateCallbackCalledInline = new AtomicBoolean();
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                VolcPlayerAsyncInitializer.whenReady((value, error) ->
                        lateCallbackCalledInline.set(!lateRequestReturned.get()));
                lateRequestReturned.set(true);
            });
            assertTrue(
                    "terminal callback should run before addInitCallback returns",
                    lateCallbackCalledInline.get());
        } finally {
            releaseWorker.countDown();
            callers.shutdownNow();
            workerThread.quitSafely();
            workerThread.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        }
    }

    private static void configure(Handler workerHandler) {
        Context context =
                InstrumentationRegistry.getInstrumentation()
                        .getTargetContext()
                        .getApplicationContext();
        VolcPlayerInit.config(
                new VolcPlayerInitConfig.Builder()
                        .setContext(context)
                        .setWorkerHandler(workerHandler)
                        .setAppInfo(
                                new VolcPlayerInitConfig.AppInfo.Builder()
                                        .setAppId("123456")
                                        .setAppName("VEVodInitTest")
                                        .setAppChannel("androidTest")
                                        .setAppVersion("1.0.0")
                                        .setLicenseUri("")
                                        .setEnableInitAppLog(false)
                                        .setAutoStartAppLog(false)
                                        .build())
                        .build());
    }

    @SuppressWarnings("unchecked")
    private static void resetBeforeSdkInitialization() throws Exception {
        setStaticField(VolcPlayerInit.class, "sInitState", VolcPlayerInit.INIT_STATE_IDLE);
        setStaticField(VolcPlayerInit.class, "sInitError", null);
        setStaticField(VolcPlayerInit.class, "sConfig", null);
        List<Object> callbacks =
                (List<Object>)
                        getStaticField(VolcPlayerAsyncInitializer.class, "INIT_CALLBACKS");
        callbacks.clear();
        setStaticField(PlayerKit.class, "sConfig", null);
    }

    private static Object getStaticField(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static void setStaticField(Class<?> owner, String name, Object value)
            throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
