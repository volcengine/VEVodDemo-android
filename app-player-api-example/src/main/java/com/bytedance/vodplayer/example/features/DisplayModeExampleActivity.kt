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
 * Create Date : 2025/5/26
 */

package com.bytedance.vodplayer.example.features

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar.LayoutParams
import androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT
import androidx.core.view.children
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.utils.ViewFactory.button
import com.bytedance.vodplayer.example.view.RatioFrameLayout
import com.ss.ttvideoengine.TTVideoEngine

class DisplayModeExampleActivity : BasicPlaybackExampleActivity() {

    companion object {
        private const val RATIO_1_2: Float = 1 / 2f
        private const val RATIO_9_16: Float = 9 / 16f
        private const val RATIO_3_4: Float = 3 / 4f
        private const val RATIO_1_1: Float = 1f
        private const val RATIO_4_3: Float = 4 / 3f
        private const val RATIO_16_9: Float = 16 / 9f
        private const val RATIO_2_1: Float = 2 / 1f

        private val DISPLAY_MODES = listOf(
            Pair(TTVideoEngine.IMAGE_LAYOUT_TO_FILL, "FILL"),
            Pair(TTVideoEngine.IMAGE_LAYOUT_ASPECT_FILL_X, "A_FILL_X"),
            Pair(TTVideoEngine.IMAGE_LAYOUT_ASPECT_FILL_Y, "A_FILL_Y"),
            Pair(TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT, "A_FIT"),
            Pair(TTVideoEngine.IMAGE_LAYOUT_ASPECT_FILL, "A_FILL")
        )

        private val VIDEO_VIEW_RATIOS = listOf(
            Pair(RATIO_1_2, "1:2"),
            Pair(RATIO_9_16, "9:16"),
            Pair(RATIO_3_4, "3:4"),
            Pair(RATIO_1_1, "1:1"),
            Pair(RATIO_4_3, "4:3"),
            Pair(RATIO_16_9, "16:9"),
            Pair(RATIO_2_1, "2:1")
        )
    }

    private lateinit var videoViewRatioActionLayout: LinearLayout
    private lateinit var displayModesActionLayout: LinearLayout

    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_displaymode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        findViewById<RatioFrameLayout>(R.id.videoViewContainer).ratio = RATIO_1_1

        setVideoViewRatio(RATIO_1_1)

        setDisplayMode(TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT)
    }

    private fun setDisplayMode(displayMode: Int) {
        mVideoEngine.setDisplayMode(mVideoViewHolder.textureView, displayMode)

        displayModesActionLayout.children.forEachIndexed { i, button ->
            button as Button
            button.isSelected = DISPLAY_MODES[i].first == displayMode
        }
    }

    private fun setVideoViewRatio(ratio: Float) {
        if (ratio >= 1) {
            mVideoViewHolder.videoView.setRatioBy(RatioFrameLayout.RATIO_BY_WIDTH)
        } else {
            mVideoViewHolder.videoView.setRatioBy(RatioFrameLayout.RATIO_BY_HEIGHT)
        }
        mVideoViewHolder.videoView.ratio = ratio

        videoViewRatioActionLayout.children.forEachIndexed { i, button ->
            button as Button
            button.isSelected = VIDEO_VIEW_RATIOS[i].first == ratio
        }
    }

    override fun initActions() {
        val container = findViewById<LinearLayout>(R.id.actionContainer)

        // videoView ratios
        val videoViewRatioTitle = TextView(this)
        videoViewRatioTitle.text = getString(R.string.vevod_api_example_displaymode_title_select_videoview_ratios)
        container.addView(videoViewRatioTitle, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        videoViewRatioActionLayout = LinearLayout(this)
        container.addView(videoViewRatioActionLayout, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        VIDEO_VIEW_RATIOS.forEach { item ->
            button(this, item.second, false).apply {
                setOnClickListener {
                    setVideoViewRatio(item.first)
                }
                videoViewRatioActionLayout.addView(this)
            }
        }

        // displayModes
        val displayModeTitle = TextView(this)
        displayModeTitle.text = getString(R.string.vevod_api_example_displaymode_title_select_displaymodes)
        container.addView(displayModeTitle, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        displayModesActionLayout = LinearLayout(this)
        container.addView(displayModesActionLayout, LayoutParams(MATCH_PARENT, WRAP_CONTENT))
        DISPLAY_MODES.forEach{item ->
            button(this, item.second, false).apply {
                setOnClickListener {
                    setDisplayMode(item.first)
                }
                displayModesActionLayout.addView(this)
            }
        }
    }
}