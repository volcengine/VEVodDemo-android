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
 * Create Date : 2025/5/23
 */

package com.bytedance.vodplayer.example.quickstart

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import com.bytedance.vodplayer.example.App
import com.bytedance.vodplayer.example.BaseActivity
import com.bytedance.vodplayer.example.DataRepository
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.VideoItem
import com.bytedance.vodplayer.example.utils.SurfaceTextureListenerAdapter
import com.bytedance.vodplayer.example.view.RatioFrameLayout
import com.ss.ttvideoengine.TTVideoEngine
import com.ss.ttvideoengine.source.DirectUrlSource
import com.ss.ttvideoengine.source.DirectUrlSource.UrlItem

open class DirectUrlSourceExampleActivity : BaseActivity() {

    companion object {
        fun createDirectUrlSource(videoItem: VideoItem): DirectUrlSource {
            return DirectUrlSource.Builder()
                .setVid(videoItem.vid)
                .addItem(
                    UrlItem.Builder()
                        .setUrl(videoItem.videoUrl)
                        .setCacheKey(generateCacheKey(videoItem.videoUrl))
                        .build()
                )
                .setTag(videoItem)
                .build();
        }

        fun generateCacheKey(url: String): String {
            return TTVideoEngine.computeMD5(java.net.URL(url).path)
        }

        fun createVideoEngine(directUrlSource: DirectUrlSource): TTVideoEngine {
            val videoEngine = TTVideoEngine(App.sContext)
            videoEngine.strategySource = directUrlSource
            return videoEngine
        }
    }

    lateinit var mVideoItem: VideoItem
    lateinit var videoEngine: TTVideoEngine
    lateinit var textureView: TextureView

    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_directurl_source
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
                videoEngine.surface = Surface(surfaceTexture)
            }
        }
    }

    open fun initVideoEngine() {
        val directUrlSource = createDirectUrlSource(mVideoItem)
        videoEngine = createVideoEngine(directUrlSource)
        videoEngine.setDisplayMode(textureView, TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT)
        videoEngine.strategySource = directUrlSource
    }

    override fun onResume() {
        super.onResume()
        videoEngine.play()
    }

    override fun onPause() {
        super.onPause()
        videoEngine.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoEngine.release()
    }
}