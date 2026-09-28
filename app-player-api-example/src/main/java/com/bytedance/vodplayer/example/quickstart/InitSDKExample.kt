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

    /**
     * 默认同步接入。初始化返回后，业务可以直接创建播放器。
     */
    @JvmStatic
    fun initSync() {
        openLogCat()
        runInitTask()
        onInitSuccess()
    }

    /**
     * 把 TTSDK 及其前后置全局组件作为一个完整任务提交到同一后台串行线程。
     * callback 只用于接收最终结果，不应在调用方同步等待。
     */
    @JvmStatic
    fun initAsync(callback: VodSdkInitializer.Callback? = null) {
        openLogCat()
        VodSdkInitializer.start(
            {
                runInitTask()
            }
        ) { success, error ->
            if (success) {
                onInitSuccess()
            }
            callback?.onResult(success, error)
        }
    }

    /**
     * 同步和异步入口复用同一段初始化任务，避免调用顺序出现差异。
     */
    @JvmStatic
    fun runInitTask() {
        // StrategyManager 的版本必须在 Env.start() 首次访问策略单例前设置。
        initVodSDKBestStrategy()
        Env.start(createVodSDKConfig())
        // ABR 依赖 Env.start() 建立的全局环境，因此放在其后执行。
        TTVideoABRStrategy.init()
    }

    private fun onInitSuccess() {
        // 以下操作依赖初始化后的 AppLog/Engine 全局状态，只在成功后执行。
        initUserUniqueId()
        initDeviceId()
    }

    private fun openLogCat() {
        // open logcat on debug mode only
        VodSDK.openAllVodLog();
    }

    private fun createVodSDKConfig(): Config {
        val context = App.sContext;
        val videoCacheDir = File(context.cacheDir, "video_cache")
        if (!videoCacheDir.exists()) videoCacheDir.mkdirs()
        val vodBuilder = VodConfig.Builder(context)
            .setCacheDirPath(videoCacheDir.absolutePath)
            .setMaxCacheSize(300 * 1024 * 1024)

        return Config.Builder()
                .setApplicationContext(context)
                .setAppID(App.APP_ID)
                .setAppName(App.APP_NAME) // 合法版本号应大于、等于 2 个分隔符，如："1.3.2"
                .setAppVersion(getAppVersionName(context))
                .setAppChannel(App.APP_CHANNEL) // 将 license 文件拷贝到 app 的 assets 文件夹中，并设置 LicenseUri
                // 下面 LicenseUri 对应工程中 assets 路径为：assets/l-1503814402-ch-vod-a-770022.lic
                .setLicenseUri(App.LICENSE_URI) // 可不设置，默认值见下表
                .setVodConfig(vodBuilder.build())
                .setBizType(Config.BIZ_TYPE_VOD)
                .build()
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
