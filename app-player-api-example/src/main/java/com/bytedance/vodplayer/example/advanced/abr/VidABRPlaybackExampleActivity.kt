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
 * Create Date : 2025/6/18
 */

package com.bytedance.vodplayer.example.advanced.abr

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT
import androidx.core.view.children
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.VideoItem
import com.bytedance.vodplayer.example.quickstart.VidSourceExampleActivity
import com.bytedance.vodplayer.example.utils.ViewFactory.button
import com.ss.ttvideoengine.Resolution
import com.ss.ttvideoengine.VideoEngineCallback
import com.ss.ttvideoengine.abr.TTVideoABRConfig
import com.ss.ttvideoengine.abr.TTVideoABRStrategy
import com.ss.ttvideoengine.model.IVideoModel
import com.ss.ttvideoengine.model.VideoModel
import com.ss.ttvideoengine.model.VideoRef
import com.ss.ttvideoengine.selector.BestResolution
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource
import com.ss.ttvideoengine.strategy.StrategyManager
import okhttp3.internal.immutableListOf


class VidABRPlaybackExampleActivity : VidSourceExampleActivity() {

    companion object {
        const val TAG = "[VidABRPlayback]";
        const val VIDEO_SCENE_SHORT = StrategyManager.STRATEGY_SCENE_SMALL_VIDEO
        const val VIDEO_SCENE_FEED = StrategyManager.STRATEGY_SCENE_SHORT_VIDEO
        const val VIDEO_SCENE_FULLSCREEN = 2
        const val VIDEO_SCENE_PIP = 3

        private val sVideoScenes = immutableListOf(VIDEO_SCENE_SHORT, VIDEO_SCENE_FEED, VIDEO_SCENE_FULLSCREEN, VIDEO_SCENE_PIP)

        private val DEFAULT_RESOLUTION: Resolution = Resolution.SuperHigh // 默认 720P

        private var sUserSelectedResolution: Resolution? = null

        fun createABRConfig(context: Context, videoScene: Int): TTVideoABRConfig {
            val dm = context.resources.displayMetrics
            val screenWidth = dm.widthPixels
            val screenHeight = dm.heightPixels;

            val abrConfig = TTVideoABRConfig()
            abrConfig.screenWidth = screenWidth // 当前设备屏幕宽
            abrConfig.screenHeight = screenHeight // 当前设备屏幕高

            abrConfig.defaultResolution = Resolution.High // 无测速信息时，默认清晰度（一般为第一个起播视频），480P

            when (videoScene) {
                VIDEO_SCENE_SHORT -> {
                    // 类抖音竖版短视频场景
                    abrConfig.displayWidth = Math.min(screenWidth, screenHeight)
                    abrConfig.displayHeight = (abrConfig.displayWidth / 9f * 16).toInt()
                    abrConfig.wifiMaxResolution = Resolution.ExtremelyHigh // Wifi 网络，清晰度上限，1080P
                    abrConfig.mobileMaxResolution = Resolution.SuperHigh // 移动网络，清晰度上限，720P
                }
                VIDEO_SCENE_FEED -> {
                    // 类西瓜横版中视频场景
                    abrConfig.displayWidth = Math.min(screenWidth, screenHeight)
                    abrConfig.displayHeight = (abrConfig.displayWidth / 16f * 9).toInt()
                    abrConfig.wifiMaxResolution = Resolution.H_High // Wifi 网络，清晰度上限，540P
                    abrConfig.mobileMaxResolution = Resolution.H_High // 移动网络，清晰度上限，540P
                }
                VIDEO_SCENE_FULLSCREEN -> {
                    // 横版视频全屏场景
                    abrConfig.displayHeight = Math.min(screenWidth, screenHeight)
                    abrConfig.displayWidth = (abrConfig.displayHeight / 9f * 16).toInt()
                    abrConfig.wifiMaxResolution = Resolution.ExtremelyHigh // Wifi 网络，清晰度上限，1080P
                    abrConfig.mobileMaxResolution = Resolution.ExtremelyHigh // 移动网络，清晰度上限，1080P
                }
                VIDEO_SCENE_PIP -> {
                    // 小窗场景
                    abrConfig.displayHeight = screenWidth / 3
                    abrConfig.displayWidth = screenHeight / 3
                    abrConfig.wifiMaxResolution = Resolution.High // Wifi 网络，清晰度上限，480P
                    abrConfig.mobileMaxResolution = Resolution.High // 移动网络，清晰度上限，480P
                }
            }
            return abrConfig
        }

        fun getStartPlayResolution(videoModel: IVideoModel?): Resolution {
            val userSelectedResolution: Resolution? = getUserSelectedResolution()
            if (userSelectedResolution == null) {
                if (videoModel != null && (videoModel.isSupportBash || videoModel.isSupportHLSSeamlessSwitch)) {
                    // 播放源支持平滑切换
                    return Resolution.Auto
                } else {
                    // 播放源不支持 ABR
                    return BestResolution.findDefaultResolution(videoModel, DEFAULT_RESOLUTION)
                } // 使用 SharedPref 记录用户选择的清晰度
            }
            return userSelectedResolution
        }

        fun createVidSource(videoItem: VideoItem): VidPlayAuthTokenSource {
            return VidPlayAuthTokenSource.Builder()
                .setVid(videoItem.vid)
                .setPlayAuthToken(videoItem.playAuthToken)
                // 设置起播清晰度
                .setResolution(Resolution.Auto)
                .setTag(videoItem)
                .build();
        }

        fun setUserSelectedResolution(resolution: Resolution) {
            sUserSelectedResolution = resolution
        }

        fun getUserSelectedResolution(): Resolution? {
            return sUserSelectedResolution
        }
    }

    var mCurrentResolution: Resolution? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVideoSceneActions()
    }

    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_abr_playback
    }

    override fun initVideoEngine() {
        super.initVideoEngine()
        TTVideoABRStrategy.initEngine(mVideoEngine, createABRConfig(this, mVideoScene))
        mVideoEngine.addVideoEngineCallback(object : VideoEngineCallback {
            override fun onVideoStreamBitrateChanged(resolution: Resolution?, bitrate: Int) {
                mCurrentResolution = resolution
                updateAutoResolution()
                if (mResolution == Resolution.Auto) {
                    // resolution 为 ABR 自动切换的清晰度
                    Log.d(TAG, "onVideoStreamBitrateChanged auto $resolution $bitrate")
                } else {
                    // resolution 为 用户手动切换的清晰度
                    Log.d(TAG, "onVideoStreamBitrateChanged user $resolution $bitrate")
                }
            }
        })
    }

    override fun onFetchedVideoInfo(vidSource: VidPlayAuthTokenSource, videoModel: VideoModel) {
        // 起播清晰度设置
        mResolution = getStartPlayResolution(videoModel);
        mVideoEngine.configResolution(mResolution)

        // 清晰度列表展示
        mResolutions.clear()
        mVideoEngine.supportedResolutionTypes()?.let {
            if (videoModel.isSupportBash() || videoModel.isSupportHLSSeamlessSwitch()) {
                mResolutions.add(Resolution.Auto);
            }
            mResolutions.addAll(it)
        }
        initResolutionActions()
    }

    private fun updateAutoResolution() {
        val button: Button? = mResolutionActionLayout?.getChildAt(0) as Button?
        button?.apply {
            text = resolveResolutionText(Resolution.Auto)
        }
    }

    override fun setResolution(resolution: Resolution) {
        if (mResolution == resolution) return
        super.setResolution(resolution)
        setUserSelectedResolution(resolution)
        updateAutoResolution()
    }

    override fun resolveResolutionText(resolution: Resolution?): String {
        if (resolution == null || resolution == Resolution.Auto) {
            if (mCurrentResolution != null && (mResolution == null || mResolution == Resolution.Auto)) {
                return "AUTO[${mCurrentResolution!!.toString(VideoRef.TYPE_VIDEO)}]"
            } else {
                return "AUTO"
            }
        } else {
            return resolution.toString(VideoRef.TYPE_VIDEO)
        }
    }

    var mVideoScene: Int = VIDEO_SCENE_SHORT
    var mVideoSceneActionLayout : LinearLayout? = null

    fun initVideoSceneActions() {
        val actionContainer = findViewById<LinearLayout>(R.id.actionContainer)

        // set resolution
        TextView(this).apply {
            text = getString(R.string.vevod_api_example_vid_abr_playback_abr_config)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        mVideoSceneActionLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        sVideoScenes.forEachIndexed { index1, videoScene ->
            button(
                this,
                resolveVideoSceneText(videoScene),
                mVideoScene == videoScene
            ).apply {
                setOnClickListener {
                    mVideoScene = videoScene
                    TTVideoABRStrategy.setABRConfig(mVideoEngine, createABRConfig(this@VidABRPlaybackExampleActivity, videoScene))
                    mVideoSceneActionLayout!!.children.forEachIndexed { index2, view ->
                        view.isSelected = index2 == index1
                    }
                }
                mVideoSceneActionLayout!!.addView(this)
            }
        }
    }

    private fun resolveVideoSceneText(videoScene: Int):String {
        when(videoScene) {
            VIDEO_SCENE_SHORT -> return "Short"
            VIDEO_SCENE_FEED -> return "Feed"
            VIDEO_SCENE_FULLSCREEN -> return "FullScreen"
            VIDEO_SCENE_PIP -> return "Pip"
        }
        return ""
    }
}