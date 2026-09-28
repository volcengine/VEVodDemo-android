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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class VodSdkInitializerTest {

    private static final long TIMEOUT_SECONDS = 5;

    @Test
    public void testStart_successDispatchesCallbackAndStoresTerminalState() {
        QueuedExecutor worker = new QueuedExecutor();
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, callbacks);
        AtomicInteger initCount = new AtomicInteger();
        AtomicReference<Boolean> success = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        coordinator.start(
                initCount::incrementAndGet,
                (result, throwable) -> {
                    success.set(result);
                    error.set(throwable);
                });

        assertEquals(VodSdkInitializer.State.INITIALIZING, coordinator.getState());
        assertEquals(1, worker.size());
        assertEquals(0, callbacks.size());

        worker.runNext();

        assertEquals(1, initCount.get());
        assertEquals(VodSdkInitializer.State.SUCCESS, coordinator.getState());
        assertNull(coordinator.getError());
        assertNull(success.get());

        callbacks.runAll();

        assertEquals(Boolean.TRUE, success.get());
        assertNull(error.get());
    }

    @Test
    public void testStart_failureDispatchesOriginalErrorAndDoesNotRetry() {
        QueuedExecutor worker = new QueuedExecutor();
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, callbacks);
        IllegalStateException expectedError = new IllegalStateException("init failed");
        AtomicInteger initCount = new AtomicInteger();
        AtomicReference<Boolean> success = new AtomicReference<>();
        AtomicReference<Throwable> actualError = new AtomicReference<>();

        coordinator.start(
                () -> {
                    initCount.incrementAndGet();
                    throw expectedError;
                },
                (result, throwable) -> {
                    success.set(result);
                    actualError.set(throwable);
                });
        worker.runNext();
        callbacks.runAll();

        coordinator.start(initCount::incrementAndGet, null);

        assertEquals(1, initCount.get());
        assertEquals(0, worker.size());
        assertEquals(VodSdkInitializer.State.FAILED, coordinator.getState());
        assertSame(expectedError, coordinator.getError());
        assertEquals(Boolean.FALSE, success.get());
        assertSame(expectedError, actualError.get());
    }

    @Test
    public void testStart_rejectedTaskReportsFailure() {
        IllegalStateException expectedError =
                new IllegalStateException("init worker is unavailable");
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(runnable -> false, callbacks);
        AtomicReference<Boolean> success = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();

        coordinator.start(
                () -> {
                    throw new AssertionError("rejected task must not run");
                },
                (result, throwable) -> {
                    success.set(result);
                    error.set(throwable);
                });
        callbacks.runAll();

        assertEquals(VodSdkInitializer.State.FAILED, coordinator.getState());
        assertEquals(expectedError.getMessage(), coordinator.getError().getMessage());
        assertEquals(Boolean.FALSE, success.get());
        assertSame(coordinator.getError(), error.get());
    }

    @Test
    public void testStart_executorThrowsReportsFailure() {
        IllegalStateException expectedError = new IllegalStateException("executor stopped");
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(
                        runnable -> {
                            throw expectedError;
                        },
                        callbacks);
        AtomicReference<Throwable> actualError = new AtomicReference<>();

        coordinator.start(() -> {}, (success, error) -> actualError.set(error));
        callbacks.runAll();

        assertEquals(VodSdkInitializer.State.FAILED, coordinator.getState());
        assertSame(expectedError, coordinator.getError());
        assertSame(expectedError, actualError.get());
    }

    @Test
    public void testWhenReady_beforeStartReceivesFutureResult() {
        QueuedExecutor worker = new QueuedExecutor();
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, callbacks);
        AtomicInteger callbackCount = new AtomicInteger();

        coordinator.whenReady((success, error) -> {
            assertTrue(success);
            assertNull(error);
            callbackCount.incrementAndGet();
        });
        coordinator.start(() -> {}, null);
        worker.runNext();

        assertEquals(0, callbackCount.get());
        callbacks.runAll();
        assertEquals(1, callbackCount.get());
    }

    @Test
    public void testWhenReady_afterSuccessDispatchesStoredResult() {
        QueuedExecutor worker = new QueuedExecutor();
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, callbacks);
        AtomicInteger callbackCount = new AtomicInteger();

        coordinator.start(() -> {}, null);
        worker.runNext();
        coordinator.whenReady((success, error) -> {
            assertTrue(success);
            assertNull(error);
            callbackCount.incrementAndGet();
        });

        assertEquals(0, callbackCount.get());
        callbacks.runAll();
        assertEquals(1, callbackCount.get());
    }

    @Test
    public void testWhenReady_afterSuccessImmediateDispatcherRunsBeforeReturn() {
        QueuedExecutor worker = new QueuedExecutor();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, new ImmediateDispatcher());
        AtomicBoolean methodReturned = new AtomicBoolean();
        AtomicBoolean callbackCalledInline = new AtomicBoolean();

        coordinator.start(() -> {}, null);
        worker.runNext();
        coordinator.whenReady((success, error) -> {
            assertTrue(success);
            assertNull(error);
            callbackCalledInline.set(!methodReturned.get());
        });
        methodReturned.set(true);

        assertTrue(callbackCalledInline.get());
    }

    @Test
    public void testWhenReady_cancelBeforeCompletionRemovesPendingCallback() {
        QueuedExecutor worker = new QueuedExecutor();
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, callbacks);
        AtomicInteger callbackCount = new AtomicInteger();

        VodSdkInitializer.ReadyRequest request =
                coordinator.whenReady((success, error) -> callbackCount.incrementAndGet());
        request.cancel();
        request.cancel();
        coordinator.start(() -> {}, null);
        worker.runNext();
        callbacks.runAll();

        assertEquals(0, callbackCount.get());
        assertEquals(VodSdkInitializer.State.SUCCESS, coordinator.getState());
    }

    @Test
    public void testWhenReady_cancelAfterDispatchSuppressesQueuedCallback() {
        QueuedExecutor worker = new QueuedExecutor();
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, callbacks);
        AtomicInteger callbackCount = new AtomicInteger();

        coordinator.start(() -> {}, null);
        worker.runNext();
        VodSdkInitializer.ReadyRequest request =
                coordinator.whenReady((success, error) -> callbackCount.incrementAndGet());

        assertEquals(1, callbacks.size());
        request.cancel();
        callbacks.runAll();

        assertEquals(0, callbackCount.get());
        assertEquals(0, callbacks.size());
    }

    @Test
    public void testStart_inlineExecutorCannotReverseSuccessToFailure() {
        QueuedDispatcher callbacks = new QueuedDispatcher();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(
                        runnable -> {
                            runnable.run();
                            return false;
                        },
                        callbacks);
        AtomicReference<Boolean> success = new AtomicReference<>();

        coordinator.start(() -> {}, (result, error) -> success.set(result));
        callbacks.runAll();

        assertEquals(VodSdkInitializer.State.SUCCESS, coordinator.getState());
        assertNull(coordinator.getError());
        assertEquals(Boolean.TRUE, success.get());
    }

    @Test
    public void testWhenReady_nullCallbackThrows() {
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(runnable -> true, new ImmediateDispatcher());

        NullPointerException error =
                assertThrows(NullPointerException.class, () -> coordinator.whenReady(null));

        assertEquals("callback", error.getMessage());
    }

    @Test
    public void testStart_concurrentCallersRunOneInitAndNotifyAll() throws Exception {
        int callerCount = 20;
        BlockingExecutor worker = new BlockingExecutor();
        VodSdkInitializer.Coordinator coordinator =
                new VodSdkInitializer.Coordinator(worker, new ImmediateDispatcher());
        AtomicInteger initCount = new AtomicInteger();
        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch callersFinished = new CountDownLatch(callerCount);
        ExecutorService callers = Executors.newFixedThreadPool(8);

        try {
            for (int i = 0; i < callerCount; i++) {
                callers.execute(() -> {
                    coordinator.start(
                            initCount::incrementAndGet,
                            (success, error) -> {
                                if (success && error == null) {
                                    successCount.incrementAndGet();
                                }
                            });
                    callersFinished.countDown();
                });
            }

            assertTrue(callersFinished.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
            assertEquals(1, worker.postCount.get());
            assertEquals(VodSdkInitializer.State.INITIALIZING, coordinator.getState());

            worker.runTask();

            assertEquals(1, initCount.get());
            assertEquals(callerCount, successCount.get());
            assertEquals(VodSdkInitializer.State.SUCCESS, coordinator.getState());
        } finally {
            callers.shutdownNow();
        }
    }

    private static final class QueuedExecutor implements VodSdkInitializer.TaskExecutor {

        private final List<Runnable> tasks = new ArrayList<>();

        @Override
        public boolean post(Runnable runnable) {
            tasks.add(runnable);
            return true;
        }

        int size() {
            return tasks.size();
        }

        void runNext() {
            tasks.remove(0).run();
        }
    }

    private static final class QueuedDispatcher
            implements VodSdkInitializer.CallbackDispatcher {

        private final List<Runnable> callbacks = new ArrayList<>();

        @Override
        public boolean dispatch(Runnable runnable) {
            callbacks.add(runnable);
            return true;
        }

        @Override
        public void remove(Runnable runnable) {
            callbacks.remove(runnable);
        }

        int size() {
            return callbacks.size();
        }

        void runAll() {
            List<Runnable> snapshot = new ArrayList<>(callbacks);
            callbacks.clear();
            for (Runnable callback : snapshot) {
                callback.run();
            }
        }
    }

    private static final class ImmediateDispatcher
            implements VodSdkInitializer.CallbackDispatcher {

        @Override
        public boolean dispatch(Runnable runnable) {
            runnable.run();
            return true;
        }

        @Override
        public void remove(Runnable runnable) {
        }
    }

    private static final class BlockingExecutor implements VodSdkInitializer.TaskExecutor {

        private final AtomicInteger postCount = new AtomicInteger();
        private final List<Runnable> tasks =
                Collections.synchronizedList(new ArrayList<>());

        @Override
        public boolean post(Runnable runnable) {
            postCount.incrementAndGet();
            tasks.add(runnable);
            return true;
        }

        void runTask() {
            Runnable task;
            synchronized (tasks) {
                task = tasks.remove(0);
            }
            task.run();
        }
    }
}
