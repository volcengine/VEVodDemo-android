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

package com.bytedance.vodplayer.example.quickstart

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VodSdkCoroutineInitializerTest {

    @Test
    fun awaitReady_successResumesCoroutine() = runTest {
        lateinit var callback: VodSdkInitializer.Callback

        val result = async {
            VodSdkCoroutineInitializer.awaitReady {
                callback = it
                TestReadyRequest()
            }
        }
        runCurrent()

        callback.onResult(true, null)
        result.await()
    }

    @Test
    fun awaitReady_failureThrowsOriginalError() = runTest {
        lateinit var callback: VodSdkInitializer.Callback
        val expected = IllegalStateException("init failed")

        supervisorScope {
            val result = async {
                VodSdkCoroutineInitializer.awaitReady {
                    callback = it
                    TestReadyRequest()
                }
            }
            runCurrent()

            callback.onResult(false, expected)

            try {
                result.await()
                fail("awaitReady should throw the initialization error")
            } catch (actual: IllegalStateException) {
                assertEquals(expected.message, actual.message)
            }
        }
    }

    @Test
    fun awaitReady_cancelRemovesCurrentSubscription() = runTest {
        val request = TestReadyRequest()
        val result = async {
            VodSdkCoroutineInitializer.awaitReady {
                request
            }
        }
        runCurrent()

        result.cancelAndJoin()

        assertTrue(request.cancelled)
    }

    @Test
    fun awaitReady_terminalCallbackCanResumeBeforeSubscribeReturns() = runTest {
        VodSdkCoroutineInitializer.awaitReady { callback ->
            callback.onResult(true, null)
            TestReadyRequest()
        }
    }

    private class TestReadyRequest : VodSdkInitializer.ReadyRequest {
        var cancelled = false

        override fun cancel() {
            cancelled = true
        }
    }
}
