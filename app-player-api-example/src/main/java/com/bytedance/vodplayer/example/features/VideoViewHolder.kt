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
import android.view.TextureView
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.utils.OnSeekBarChangeListenerAdapter
import com.bytedance.vodplayer.example.utils.SurfaceTextureListenerAdapter
import com.bytedance.vodplayer.example.utils.TimeUtils
import com.bytedance.vodplayer.example.view.RatioFrameLayout
import com.ss.ttvideoengine.TTVideoEngine
import com.ss.ttvideoengine.VideoEngineCallback
import com.ss.ttvideoengine.utils.Error

class VideoViewHolder(val videoView: RatioFrameLayout) : VideoEngineCallback {
    val textureView: TextureView = videoView.findViewById(R.id.textureView)
    val imageCover: ImageView = videoView.findViewById(R.id.imageCover)
    val progressBar: RelativeLayout = videoView.findViewById(R.id.progressBar)
    val seekBar: SeekBar = videoView.findViewById(R.id.seekBar)
    val time1: TextView = videoView.findViewById(R.id.text1)
    val time2: TextView = videoView.findViewById(R.id.text2)
    val playPause: ImageView = videoView.findViewById(R.id.playPause)
    val loading: ProgressBar = videoView.findViewById(R.id.loading)
    val replay: TextView = videoView.findViewById(R.id.replay)

    var listener: VideoViewListener? = null

    init {
        textureView.surfaceTextureListener = object : SurfaceTextureListenerAdapter {
            override fun onSurfaceTextureAvailable(
                surfaceTexture: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                listener?.onSurfaceTextureAvailable(surfaceTexture, width, height)
            }

            override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                listener?.onSurfaceTextureDestroyed(surfaceTexture)
                return true;
            }
        }
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListenerAdapter {

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                listener?.onStopTrackingTouch(seekBar)
            }
        })

        imageCover.visibility = VISIBLE
        playPause.visibility = VISIBLE
        replay.visibility = GONE
        loading.visibility = GONE

        progressBar.visibility = GONE
        time1.text = ""
        time2.text = ""
        seekBar.progress = 0
        seekBar.max = 0
    }

    interface VideoViewListener {
        fun onStopTrackingTouch(seekBar: SeekBar) {}
        fun onSurfaceTextureAvailable(surfaceTexture: SurfaceTexture, width: Int, height: Int) {}
        fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture) {}
    }

    /* VideoEngineCallback */
    override fun onLoadStateChanged(videoEngine: TTVideoEngine, loadState: Int) {
        if (loadState == TTVideoEngine.LOAD_STATE_STALLED) {
            loading.visibility = VISIBLE
        } else {
            loading.visibility = GONE
        }
    }

    /* VideoEngineCallback */
    override fun onPlaybackStateChanged(videoEngine: TTVideoEngine, playbackState: Int) {
        if (playbackState == TTVideoEngine.PLAYBACK_STATE_PLAYING) {
            playPause.visibility = GONE
        } else if (playbackState == TTVideoEngine.PLAYBACK_STATE_PAUSED) {
            playPause.visibility = VISIBLE
        } else if (playbackState == TTVideoEngine.PLAYBACK_STATE_STOPPED) {
            if (videoEngine.isReleased) {
                imageCover.visibility = VISIBLE
                playPause.visibility = VISIBLE
                replay.visibility = GONE
                loading.visibility = GONE

                progressBar.visibility = GONE
                time1.text = ""
                time2.text = ""
                seekBar.progress = 0
                seekBar.max = 0
            }
        }
    }

    /* VideoEngineCallback */
    override fun onRenderStart(videoEngine: TTVideoEngine) {
        imageCover.visibility = GONE
        playPause.visibility = GONE
        loading.visibility = GONE
        replay.visibility = GONE

        progressBar.visibility = VISIBLE
        seekBar.max = videoEngine.duration
        time2.text = TimeUtils.time2String(videoEngine.duration.toLong())
    }

    /* VideoEngineCallback */
    override fun onError(error: Error?) {
        replay.visibility = VISIBLE
    }

    /* VideoEngineCallback */
    override fun onCurrentPlaybackTimeUpdate(videoEngine: TTVideoEngine, currentPlaybackTime: Int) {
        time1.text = TimeUtils.time2String(currentPlaybackTime.toLong())
        seekBar.progress = currentPlaybackTime
    }

    /* VideoEngineCallback */
    override fun onBufferingUpdate(engine: TTVideoEngine, percent: Int) {
        seekBar.secondaryProgress = (percent / 100f * seekBar.max).toInt()
    }

    /* VideoEngineCallback */
    override fun onCompletion(engine: TTVideoEngine) {
        imageCover.visibility = GONE
        playPause.visibility = GONE
        loading.visibility = GONE
        replay.visibility = if (engine.isLooping) {
            GONE
        } else {
            VISIBLE
        }
    }
}