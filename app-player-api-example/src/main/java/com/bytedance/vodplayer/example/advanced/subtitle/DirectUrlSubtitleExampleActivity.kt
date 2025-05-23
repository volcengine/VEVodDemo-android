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
 * Create Date : 2025/5/27
 */

package com.bytedance.vodplayer.example.advanced.subtitle

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT
import androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT
import androidx.core.view.children
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.VideoItem
import com.bytedance.vodplayer.example.advanced.subtitle.SubtitleLanguage.CN
import com.bytedance.vodplayer.example.advanced.subtitle.SubtitleLanguage.US
import com.bytedance.vodplayer.example.quickstart.DirectUrlSourceExampleActivity
import com.bytedance.vodplayer.example.utils.UIUtils.dip2Px
import com.bytedance.vodplayer.example.utils.ViewFactory.button
import com.ss.ttvideoengine.SubDesInfoModel
import com.ss.ttvideoengine.SubInfoSimpleCallBack
import com.ss.ttvideoengine.SubModel
import com.ss.ttvideoengine.TTVideoEngine
import org.json.JSONObject

class DirectUrlSubtitleExampleActivity : DirectUrlSourceExampleActivity() {

    companion object {
        const val TAG: String = "[DirectURLSubtitle]"

        val PREFERRED_LANGUAGES = listOf(CN, US)

        fun createSubtitleSource(videoItem: VideoItem): SubDesInfoModel {
            return SubDesInfoModel(JSONObject(videoItem.subtitleModel))
        }

        fun findSubtitleItem(subtitleItems: MutableList<SubtitleItem>): SubtitleItem {
            subtitleItems.forEach { subtitleItem ->
                PREFERRED_LANGUAGES.forEach { subtitleLanguage ->
                    if (subtitleItem.subModel != null &&
                        subtitleItem.subModel.languageId == subtitleLanguage.languageId
                    ) {
                        return subtitleItem
                    }
                }
            }
            return subtitleItems[0]
        }

        fun parseSubtitleText(info: String): String? {
            try {
                return JSONObject(info).run {
                    optString("info")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return null
        }
    }

    lateinit var subtitleView: TextView
    lateinit var mSubtitleItems: MutableList<SubtitleItem>

    var mSubtitleSource: SubDesInfoModel? = null
    var mSubtitleItem: SubtitleItem? = null

    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_directurl_subtitle
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initActions()
    }

    override fun initView() {
        super.initView()
        val videoView = findViewById<FrameLayout>(R.id.videoView)

        subtitleView = TextView(this).apply {
            val margin = dip2Px(context, 56f).toInt()
            layoutParams = FrameLayout.LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            ).apply {
                leftMargin = margin
                rightMargin = margin
                bottomMargin = margin
            }
            gravity = Gravity.CENTER
            setTextColor(Color.RED)
        }
        videoView.addView(subtitleView)
    }

    override fun initVideoEngine() {
        super.initVideoEngine()
        // 1. 开启字幕功能
        videoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_OPEN_SUB_THREAD, 1)
        videoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_SUB_ENABLE_MDL, 1)

        // 2. 设置 DirectUrl 字幕源
        mSubtitleSource = createSubtitleSource(mVideoItem)
        videoEngine.setSubDesInfoModel(mSubtitleSource)

        // 3. 设置字幕监听
        videoEngine.setSubInfoCallBack(object : SubInfoSimpleCallBack() {
            override fun onSubSwitchCompleted(success: Int, subId: Int) {
                Log.d(TAG, "onSubSwitchCompleted subtitleId:$subId");
            }

            override fun onSubLoadFinished2(success: Int, info: String?) {
                Log.d(TAG, "onSubLoadFinished2 success:${success == 1} info:$info")
            }

            override fun onSubInfoCallback(code: Int, info: String) {
                Log.d(TAG, "onSubInfoCallback code:$code info:$info")
                subtitleView.text = parseSubtitleText(info)
            }
        })

        // 4. 构造字幕列表
        mSubtitleItems = mutableListOf<SubtitleItem>().apply {
            add(SubtitleItem(null))
        }
        mSubtitleSource?.subModelList?.forEach { subModel ->
            mSubtitleItems.add(SubtitleItem(subModel as SubModel))
        }

        // 5. 设置起播使用的字幕
        val subtitleItem = findSubtitleItem(mSubtitleItems)
        selectSubtitleItem(subtitleItem)
    }

    data class SubtitleItem(val subModel: SubModel?)

    private fun selectSubtitleItem(subtitleItem: SubtitleItem) {
        if (mSubtitleItem == subtitleItem) return

        mSubtitleItem = subtitleItem
        if (subtitleItem.subModel == null) {
            videoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_OPEN_SUB, 0)
            subtitleView.visibility = View.GONE
        } else {
            videoEngine.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_OPEN_SUB, 1)
            videoEngine.setIntOption(
                TTVideoEngine.PLAYER_OPTION_SWITCH_SUB_ID,
                subtitleItem.subModel.subId
            )
            subtitleView.visibility = View.VISIBLE
        }
    }

    private fun initActions() {
        val actionContainer = findViewById<LinearLayout>(R.id.actionContainer)

        // set select subtitle language
        TextView(this).apply {
            text = getString(R.string.vevod_api_example_subtitle_select_language_title)
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        val subtitleSelectLayout = LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
            actionContainer.addView(this)
        }
        mSubtitleItems.forEachIndexed { index1, subtitleItem ->
            val text = if (subtitleItem.subModel == null) {
                getText(R.string.vevod_api_example_subtitle_close)
            } else {
                SubtitleLanguage.languageDes(this, subtitleItem.subModel)
            }
            button(this, text, subtitleItem == mSubtitleItem).apply {
                setOnClickListener {
                    subtitleSelectLayout.children.forEachIndexed { index2, view ->
                        view.isSelected = index2 == index1
                        // 6. 播放中切换字幕
                        selectSubtitleItem(subtitleItem)
                    }
                }
                subtitleSelectLayout.addView(this)
            }
        }
    }
}