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
 * Create Date : 2025/5/22
 */

package com.bytedance.vodplayer.example

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.widget.Toast
import com.bytedance.vodplayer.example.quickstart.InitSDKExample

class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var sContext: Context private set

        const val APP_ID: String = "" // 替换为您申请的 appid
        const val LICENSE_URI: String = "" // 替换为您申请的 license
        const val APP_NAME: String = "player_api_example"
        const val APP_CHANNEL: String = "github"
    }

    override fun onCreate() {
        super.onCreate()
        sContext = applicationContext

        if (TextUtils.isEmpty(APP_ID) || TextUtils.isEmpty(LICENSE_URI)) {
            Toast.makeText(
                this,
                "请前往“火山引擎-视频点播-控制台”免费申请 License，5秒钟后自动退出程序",
                Toast.LENGTH_LONG
            ).show()
            Handler().postDelayed({
                throw IllegalArgumentException("点击免费申请播放器 SDK License：https://www.volcengine.com/docs/4/65772#%E7%94%B3%E8%AF%B7%E5%85%8D%E8%B4%B9%E6%B5%8B%E8%AF%95-license");
            }, 5000)
        } else {
            InitSDKExample.init()
        }
    }
}