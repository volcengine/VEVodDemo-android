/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/5/29
 */

package com.bytedance.vodplayer.example.utils

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.text.TextUtils
import com.bytedance.vodplayer.example.App

/**
 * 系统音量变化监听
 */
abstract class VolumeReceiver : BroadcastReceiver() {

    companion object {
        const val VOLUME_CHANGED_ACTION: String = "android.media.VOLUME_CHANGED_ACTION"
        const val EXTRA_VOLUME_STREAM_TYPE: String = "android.media.EXTRA_VOLUME_STREAM_TYPE"
    }

    private var mRegistered = false

    @SuppressLint("[ByDesign2.5]AddPermissionForDynamicReceiver")
    fun register() {
        if (mRegistered) return

        mRegistered = true
        val filter = IntentFilter()
        filter.addAction(VOLUME_CHANGED_ACTION)
        App.sContext.registerReceiver(this, filter)
    }

    fun unregister() {
        if (!mRegistered) return

        mRegistered = false
        App.sContext.unregisterReceiver(this)
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val volumeStreamType = intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1)
        if (TextUtils.equals(action, VOLUME_CHANGED_ACTION)) {
            if (volumeStreamType == AudioManager.STREAM_MUSIC) {
                onSystemVolumeChanged()
            }
        }
    }

    abstract fun onSystemVolumeChanged();
}