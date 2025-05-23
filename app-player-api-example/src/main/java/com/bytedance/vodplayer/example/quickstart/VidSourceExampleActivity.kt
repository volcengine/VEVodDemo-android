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

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT
import androidx.core.view.children
import com.bytedance.vodplayer.example.App
import com.bytedance.vodplayer.example.BaseActivity
import com.bytedance.vodplayer.example.DataRepository
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.VideoItem
import com.bytedance.vodplayer.example.utils.SurfaceTextureListenerAdapter
import com.bytedance.vodplayer.example.utils.ViewFactory.button
import com.bytedance.vodplayer.example.view.RatioFrameLayout
import com.ss.ttvideoengine.Resolution
import com.ss.ttvideoengine.TTVideoEngine
import com.ss.ttvideoengine.VideoEngineCallback
import com.ss.ttvideoengine.VideoInfoListener
import com.ss.ttvideoengine.model.VideoModel
import com.ss.ttvideoengine.model.VideoRef
import com.ss.ttvideoengine.source.VidPlayAuthTokenSource
import com.ss.ttvideoengine.utils.Error

open class VidSourceExampleActivity : BaseActivity() {

    companion object {
        const val TAG = "[VidSource]";

        val START_PLAY_RESOLUTION = Resolution.SuperHigh // 720P

        fun createVidSource(videoItem: VideoItem): VidPlayAuthTokenSource {
            return VidPlayAuthTokenSource.Builder()
                .setVid(videoItem.vid)
                .setPlayAuthToken(videoItem.playAuthToken)
                // 设置起播清晰度
                .setResolution(START_PLAY_RESOLUTION)
                .setTag(videoItem)
                .build();
        }

        fun createVideoEngine(vidSource: VidPlayAuthTokenSource): TTVideoEngine {
            val videoEngine = TTVideoEngine(App.sContext)
            videoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_USE_VIDEOMODEL_CACHE, 1)
            videoEngine.strategySource = vidSource
            return videoEngine
        }
    }

    lateinit var mVideoItem: VideoItem
    lateinit var mVideoEngine: TTVideoEngine

    private lateinit var textureView: TextureView
    private var mResolutions: MutableList<Resolution> = mutableListOf()
    private var mResolution: Resolution? = null

    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_vid_source
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mVideoItem = DataRepository.videoItems[0]

        initView()

        initVideoEngine()
    }

    open fun initView() {
        findViewById<RatioFrameLayout>(R.id.videoView).ratio = 16 / 9f
        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object : SurfaceTextureListenerAdapter {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                mVideoEngine.surface = Surface(surfaceTexture)
            }
        }
    }

    open fun initVideoEngine() {
        val vidSource = createVidSource(DataRepository.videoItems[0])

        mVideoEngine = createVideoEngine(vidSource)
        mVideoEngine.setDisplayMode(textureView, TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT)
        mVideoEngine.setVideoInfoListener(object : VideoInfoListener {

            override fun onFetchedVideoInfo(videoModel: VideoModel?): Boolean {
                // 获取清晰度列表，可用来展示供用户切换清晰度
                mVideoEngine.supportedResolutionTypes()?.let {
                    mResolutions.clear()
                    mResolutions.addAll(it)
                }
                // 默认清晰度 720P
                val defaultResolution = START_PLAY_RESOLUTION
                // 播放源中可能不包含 defaultResolution，调用 findDefaultResolution 找出与 defaultResolution 最接近的清晰度。
                mResolution = TTVideoEngine.findDefaultResolution(videoModel, defaultResolution)
                // 设置最终的起播清晰度
                mVideoEngine.configResolution(mResolution)
                Log.d(
                    TAG,
                    "vid=${vidSource.vid()} " + "startResolution=${vidSource.resolution()} " + "resolutions=${mResolutions}"
                )
                initResolutionActions()
                return false
            }
        })
        mVideoEngine.addVideoEngineCallback(object : VideoEngineCallback {

            override fun onError(error: Error?) {
                // 播放失败，清晰度切换失败回调
                Log.d(TAG, "vid=${vidSource.vid()} " + error)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mVideoEngine.play()
    }

    override fun onPause() {
        super.onPause()
        mVideoEngine.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mVideoEngine.release()
    }

    private fun setResolution(resolution: Resolution) {
        if (mResolution == resolution) return

        mResolution = resolution
        mVideoEngine.configResolution(resolution)
    }

    fun initResolutionActions() {
        val actionContainer = findViewById<LinearLayout>(R.id.actionContainer)

        // set resolution
        TextView(this).apply {
            text = getString(R.string.vevod_api_example_basic_playback_set_resolution)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        val speedActionLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        mResolutions.forEachIndexed { index1, resolution ->
            button(
                this,
                resolution.toString(VideoRef.TYPE_VIDEO),
                mResolution == resolution
            ).apply {
                setOnClickListener {
                    setResolution(resolution)
                    speedActionLayout.children.forEachIndexed { index2, view ->
                        view.isSelected = index2 == index1
                    }
                }
                speedActionLayout.addView(this)
            }
        }
    }
}