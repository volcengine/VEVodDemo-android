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

package com.bytedance.vodplayer.example.features

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.core.view.children
import coil.load
import com.bytedance.vodplayer.example.App
import com.bytedance.vodplayer.example.BaseActivity
import com.bytedance.vodplayer.example.DataRepository
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.VideoItem
import com.bytedance.vodplayer.example.quickstart.DirectUrlSourceExampleActivity
import com.bytedance.vodplayer.example.utils.OnSeekBarChangeListenerAdapter
import com.bytedance.vodplayer.example.utils.SpeedUtils
import com.bytedance.vodplayer.example.utils.ViewFactory.button
import com.bytedance.vodplayer.example.utils.VolumeReceiver
import com.ss.ttm.player.PlaybackParams
import com.ss.ttvideoengine.DataLoaderHelper
import com.ss.ttvideoengine.DataLoaderListener2
import com.ss.ttvideoengine.TTVideoEngine
import com.ss.ttvideoengine.strategy.source.StrategySource


open class BasicPlaybackExampleActivity : BaseActivity() {

    companion object {
        const val TAG = "[BasicPlayback]"
    }

    lateinit var mVideoItem: VideoItem
    lateinit var mVideoEngine: TTVideoEngine
    lateinit var mVideoViewHolder: VideoViewHolder


    private var mStartTime:Int? = null

    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_basic_playback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mVideoItem = DataRepository.videoItems[0]
        initView()
        initVideoEngine(createStrategySource())
        initActions()
    }

    open fun initView() {
        mVideoViewHolder = VideoViewHolder(findViewById(R.id.videoView)).apply {
            videoView.ratio = 16 / 9f
            listener = object : VideoViewHolder.VideoViewListener {

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    mVideoEngine.seekTo(seekBar.progress, null)
                }

                override fun onSurfaceTextureAvailable(
                    surfaceTexture: SurfaceTexture,
                    width: Int,
                    height: Int
                ) {
                    mVideoEngine.surface = Surface(surfaceTexture)
                }
            }
            videoView.setOnClickListener {
                if (mVideoEngine.playbackState == TTVideoEngine.PLAYBACK_STATE_PLAYING) {
                    mVideoEngine.pause()
                } else {
                    mVideoEngine.play()
                }
            }
        }
        mVideoViewHolder.imageCover.load(mVideoItem.coverUrl)
    }

    override fun onResume() {
        super.onResume()
        registerNetSpeed()
        mVideoEngine.play()
    }

    override fun onPause() {
        super.onPause()
        unRegisterNetSpeed()
        mVideoEngine.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mStartTime = mVideoEngine.currentPlaybackTime
        mVideoEngine.release()
    }

    open fun createStrategySource(): StrategySource {
        return DirectUrlSourceExampleActivity.createDirectUrlSource(mVideoItem)
    }

    open fun initVideoEngine(strategySource: StrategySource) {
        mVideoEngine = TTVideoEngine(App.sContext)
        mVideoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_POSITION_UPDATE_INTERVAL, 200)
        mVideoEngine.strategySource = strategySource
        mVideoEngine.setDisplayMode(
            mVideoViewHolder.textureView,
            TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT
        )
        mVideoEngine.addVideoEngineCallback(mVideoViewHolder)

        // 设置起播位置
        mStartTime?.let {
            mVideoEngine.setStartTime(it)
        }
    }

    private val mPlaybackParams = PlaybackParams().setSpeed(1f)

    private fun setSpeed(speed: Float) {
        mPlaybackParams.setSpeed(speed)
        mVideoEngine.setPlaybackParams(mPlaybackParams)
    }

    private fun getSpeed(): Float {
        return mPlaybackParams.speed
    }

    private fun setLooping(loop: Boolean) {
        mVideoEngine.isLooping = loop
    }

    private fun isLooping(): Boolean {
        return mVideoEngine.isLooping
    }

    private fun isMute(): Boolean {
        return mVideoEngine.isMute()
    }

    private fun setMute(item: Boolean) {
        mVideoEngine.setIsMute(item)
    }

    private fun volume2Progress(): Int {
        return (mVideoEngine.getVolume() / mVideoEngine.getMaxVolume() * 100).toInt()
    }

    private fun progress2Volume(progress: Int): Float {
        return mVideoEngine.getMaxVolume() * progress / 100f
    }

    private val mNetSpeedListener = object : DataLoaderListener2 {
        /**
         * what == DATALOADER_KEY_NOTIFY_SPEEDINFO 时为网速回调，此时：
         * code 为 size（单位 Byte）
         * parameter 为 time（单位 ms）
         */
        override fun onNotify(what: Int, code: Long, parameter: Long, info: String?) {
            if (what == DataLoaderHelper.DATALOADER_KEY_NOTIFY_SPEEDINFO) {
                val sizeInByte = code
                val timeInMS = parameter
                val speedInBytePerSecond = if (timeInMS <= 0) {
                    0
                } else {
                    sizeInByte * 1000 / timeInMS
                }
                Log.d(TAG, "onNetSpeedChange: ${SpeedUtils.format(speedInBytePerSecond)}")
            }
        }
    }

    private fun registerNetSpeed() {
        DataLoaderHelper.getDataLoader().addListener(mNetSpeedListener)
    }

    private fun unRegisterNetSpeed() {
        DataLoaderHelper.getDataLoader().removeListener(mNetSpeedListener)
    }

    open fun initActions() {
        val actionContainer = findViewById<LinearLayout>(R.id.actionContainer)

        initSpeedApiExample(actionContainer)

        initLoopingApiExample(actionContainer)

        initVolumeApiExample(actionContainer)
    }

    private fun initSpeedApiExample(actionContainer: LinearLayout) {
        // set speed
        TextView(this).apply {
            text = getString(R.string.vevod_api_example_basic_playback_set_speed_title)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        val speedActionLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        val speeds = listOf(0.5f, 1f, 1.5f, 2f)
        speeds.forEachIndexed { index1, item ->
            button(this, item.toString(), item == getSpeed()).apply {
                setOnClickListener {
                    setSpeed(item)
                    speedActionLayout.children.forEachIndexed { index2, view ->
                        view.isSelected = index2 == index1
                    }
                }
                speedActionLayout.addView(this)
            }
        }
    }

    private fun initLoopingApiExample(actionContainer: LinearLayout) {
        // set loop
        TextView(this).apply {
            text = getString(R.string.vevod_api_example_basic_playback_set_loop_title)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        val loopActionLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        val loopItems = listOf(false, true)
        loopItems.forEachIndexed { index1, item ->
            button(this, item.toString(), item == isLooping()).apply {
                setOnClickListener {
                    setLooping(item)
                    loopActionLayout.children.forEachIndexed { index2, view ->
                        view.isSelected = index2 == index1
                    }
                }
                loopActionLayout.addView(this)
            }
        }
    }

    private fun initVolumeApiExample(actionContainer: LinearLayout) {
        // set volume
        TextView(this).apply {
            text = getString(R.string.vevod_api_example_set_volume_title)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        SeekBar(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            progress = volume2Progress()
            setOnSeekBarChangeListener(object : OnSeekBarChangeListenerAdapter {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val volume = progress2Volume(progress)
                        mVideoEngine.setVolume(volume, volume)
                    }
                }
            })
            actionContainer.addView(this)
            viewTreeObserver.addOnWindowAttachListener(object :
                ViewTreeObserver.OnWindowAttachListener {
                val volumeReceiver = object : VolumeReceiver() {
                    override fun onSystemVolumeChanged() {
                        progress = volume2Progress()
                    }
                }

                override fun onWindowAttached() {
                    volumeReceiver.register()
                }

                override fun onWindowDetached() {
                    volumeReceiver.unregister()
                }
            })
        }

        // set mute
        val muteTitle = TextView(this).apply {
            text = getString(R.string.vevod_api_example_basic_playback_set_mute_title)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
        }
        actionContainer.addView(muteTitle)
        val muteActionLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        val muteItems = listOf(false, true)
        muteItems.forEachIndexed { index1, item ->
            button(this, item.toString(), item == isMute()).apply {
                setOnClickListener {
                    setMute(item)
                    muteActionLayout.children.forEachIndexed { index2, view ->
                        view.isSelected = index2 == index1
                    }
                }
                muteActionLayout.addView(this)
            }
        }
    }
}