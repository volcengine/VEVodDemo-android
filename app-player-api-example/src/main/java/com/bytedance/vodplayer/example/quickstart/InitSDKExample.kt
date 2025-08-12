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

package com.bytedance.vodplayer.example.quickstart

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.text.TextUtils
import android.util.Log
import com.bytedance.vodplayer.example.App
import com.bytedance.vodplayer.example.bestpractice.ShortVideoBestPracticeExampleActivity.Companion.initVodSDKBestStrategy
import com.bytedance.vodplayer.example.utils.IDataObserverAdapter
import com.pandora.common.applog.AppLogWrapper
import com.pandora.common.env.Env
import com.pandora.common.env.config.Config
import com.pandora.common.env.config.VodConfig
import com.pandora.vod.VodSDK
import com.ss.ttvideoengine.TTVideoEngine
import com.ss.ttvideoengine.abr.TTVideoABRStrategy
import java.io.File


object InitSDKExample {

    fun init() {
        openLogCat()
        initVodSDK()
        initUserUniqueId()
        initDeviceId()
    }

    private fun openLogCat() {
        // open logcat on debug mode only
        VodSDK.openAllVodLog();
    }

    private fun initVodSDK() {
        initVodSDKBestStrategy()

        val context = App.sContext;
        val videoCacheDir = File(context.cacheDir, "video_cache")
        if (!videoCacheDir.exists()) videoCacheDir.mkdirs()
        val vodBuilder = VodConfig.Builder(context)
            .setCacheDirPath(videoCacheDir.absolutePath)
            .setMaxCacheSize(300 * 1024 * 1024)

        Env.init(
            Config.Builder()
                .setApplicationContext(context)
                .setAppID(App.APP_ID)
                .setAppName(App.APP_NAME) // 合法版本号应大于、等于 2 个分隔符，如："1.3.2"
                .setAppVersion(getAppVersionName(context))
                .setAppChannel(App.APP_CHANNEL) // 将 license 文件拷贝到 app 的 assets 文件夹中，并设置 LicenseUri
                // 下面 LicenseUri 对应工程中 assets 路径为：assets/l-1503814402-ch-vod-a-770022.lic
                .setLicenseUri(App.LICENSE_URI) // 可不设置，默认值见下表
                .setVodConfig(vodBuilder.build())
                .build()
        )

        TTVideoABRStrategy.init()
    }

    private fun initUserUniqueId() {
        val applog = AppLogWrapper.getAppLogInstance()
        applog?.setUserUniqueID("your user id")
    }

    private fun initDeviceId() {
        val deviceId = TTVideoEngine.getDeviceID()
        if (TextUtils.isEmpty(deviceId)) {
            val applog = AppLogWrapper.getAppLogInstance()
            applog?.addDataObserver(object : IDataObserverAdapter {
                override fun onIdLoaded(did: String, iid: String, ssid: String) {
                    Log.d("VideoPlay", "deviceId = $deviceId")
                }
            })
        } else {
            Log.d("VideoPlay", "deviceId = $deviceId")
        }
    }

    private fun getAppVersionName(context: Context): String? {
        val packageInfo: PackageInfo
        try {
            packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d("App", "get versionName failed", e)
            return ""
        }
    }
}