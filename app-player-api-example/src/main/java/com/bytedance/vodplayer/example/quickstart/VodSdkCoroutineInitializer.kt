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

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Kotlin 协程适配层。
 *
 * 初始化线程和进程级状态仍由 [VodSdkInitializer] 管理，本类只把单次结果订阅转换为挂起函数。
 */
object VodSdkCoroutineInitializer {

    fun start() {
        InitSDKExample.initAsync()
    }

    suspend fun awaitReady() {
        awaitReady(VodSdkInitializer::whenReady)
    }

    internal suspend fun awaitReady(
        subscribe: (VodSdkInitializer.Callback) -> VodSdkInitializer.ReadyRequest
    ) {
        suspendCancellableCoroutine { continuation ->
            val request = subscribe(
                VodSdkInitializer.Callback { success, error ->
                    if (success) {
                        continuation.resume(Unit)
                    } else {
                        continuation.resumeWithException(
                            error ?: IllegalStateException("Vod SDK initialization failed")
                        )
                    }
                }
            )
            // 页面协程取消时只解除当前订阅，不停止进程级 Env.start()。
            continuation.invokeOnCancellation {
                request.cancel()
            }
        }
    }
}
