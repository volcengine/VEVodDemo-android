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

package com.bytedance.volc.voddemo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.bytedance.playerkit.player.volcengine.VolcPlayerAsyncInitializer;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class VodDemoApiInstrumentedTest {

    private static final long TIMEOUT_SECONDS = 5;

    @After
    public void tearDown() throws Exception {
        setInitState(VolcPlayerInit.INIT_STATE_IDLE, null);
    }

    @Test
    public void testRunWhenVodSdkReady_successRunsOnMainThread() throws Exception {
        setInitState(VolcPlayerInit.INIT_STATE_SUCCESS, null);
        CountDownLatch actionCalled = new CountDownLatch(1);
        AtomicReference<Thread> actionThread = new AtomicReference<>();

        VodDemoApi.runWhenVodSdkReady(
                null,
                () -> {
                    actionThread.set(Thread.currentThread());
                    actionCalled.countDown();
                },
                error -> {
                });

        assertTrue(actionCalled.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertEquals(Looper.getMainLooper().getThread(), actionThread.get());
    }

    @Test
    public void testRunWhenVodSdkReady_terminalSuccessOnMainRunsInline()
            throws Exception {
        setInitState(VolcPlayerInit.INIT_STATE_SUCCESS, null);
        AtomicBoolean requestReturned = new AtomicBoolean();
        AtomicBoolean actionCalledInline = new AtomicBoolean();
        AtomicReference<VodDemoApi.ReadyRequest> requestRef = new AtomicReference<>();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            requestRef.set(VodDemoApi.runWhenVodSdkReady(
                    null,
                    () -> actionCalledInline.set(!requestReturned.get()),
                    error -> {
                    }));
            requestReturned.set(true);
        });

        assertTrue(actionCalledInline.get());
        assertNull(getField(requestRef.get(), "mInitRequest"));
    }

    @Test
    public void testRunWhenVodSdkReady_cancelSuppressesQueuedAction() throws Exception {
        setInitState(VolcPlayerInit.INIT_STATE_INITING, null);
        AtomicBoolean actionCalled = new AtomicBoolean();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            VodDemoApi.ReadyRequest request =
                    VodDemoApi.runWhenVodSdkReady(
                            null,
                            () -> actionCalled.set(true),
                            error -> actionCalled.set(true));
            request.cancel();
            request.cancel();
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertFalse(actionCalled.get());
    }

    @Test
    public void testRunWhenVodSdkReady_destroyedOwnerCancelsAction() throws Exception {
        setInitState(VolcPlayerInit.INIT_STATE_INITING, null);
        AtomicBoolean actionCalled = new AtomicBoolean();

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            TestLifecycleOwner owner = new TestLifecycleOwner();
            owner.handleEvent(Lifecycle.Event.ON_CREATE);
            VodDemoApi.runWhenVodSdkReady(
                    owner,
                    () -> actionCalled.set(true),
                    error -> actionCalled.set(true));
            owner.handleEvent(Lifecycle.Event.ON_DESTROY);
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertFalse(actionCalled.get());
    }

    @Test
    public void testRunWhenVodSdkReady_failureUsesStoredInitError() throws Exception {
        IllegalStateException expected = new IllegalStateException("init failed");
        setInitState(VolcPlayerInit.INIT_STATE_ERROR, expected);
        CountDownLatch failureCalled = new CountDownLatch(1);
        AtomicReference<Throwable> actual = new AtomicReference<>();

        VodDemoApi.runWhenVodSdkReady(
                null,
                () -> {
                },
                error -> {
                    actual.set(error);
                    failureCalled.countDown();
                });

        assertTrue(failureCalled.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
        assertSame(expected, actual.get());
    }

    @SuppressWarnings("unchecked")
    private static void setInitState(int state, Throwable error) throws Exception {
        setStaticField(VolcPlayerInit.class, "sInitState", state);
        setStaticField(VolcPlayerInit.class, "sInitError", error);
        List<Object> callbacks =
                (List<Object>)
                        getStaticField(
                                VolcPlayerAsyncInitializer.class,
                                "INIT_CALLBACKS");
        callbacks.clear();
    }

    private static Object getStaticField(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(null);
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private static void setStaticField(Class<?> owner, String name, Object value)
            throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        field.set(null, value);
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
