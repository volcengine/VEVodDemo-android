package com.bytedance.vodplayer.example

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.bytedance.vodplayer.example.MainActivity.Item.Companion.ITEM_TYPE_BUTTON_ITEM
import com.bytedance.vodplayer.example.MainActivity.Item.Companion.ITEM_TYPE_GROUP_TITLE
import com.bytedance.vodplayer.example.advanced.DebugToolExampleActivity
import com.bytedance.vodplayer.example.advanced.subtitle.DirectUrlSubtitleExampleActivity
import com.bytedance.vodplayer.example.advanced.subtitle.VidSubtitleExampleActivity
import com.bytedance.vodplayer.example.bestpractice.ShortVideoBestPracticeExampleActivity
import com.bytedance.vodplayer.example.features.BasicPlaybackExampleActivity
import com.bytedance.vodplayer.example.features.DisplayModeExampleActivity
import com.bytedance.vodplayer.example.quickstart.DirectUrlSourceExampleActivity
import com.bytedance.vodplayer.example.quickstart.VidSourceExampleActivity
import com.ss.ttvideoengine.TTVideoEngine


class MainActivity : BaseActivity() {
    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_main_activity
    }

    override fun initActionBar() {
        super.initActionBar()
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("VideoPlay", "DataSource:${DataRepository.videoItems}")
        val items = createExampleItems()
        initExampleViews(items)
    }

    private fun createExampleItems(): MutableList<Item> {
        val items: MutableList<Item> = ArrayList()

        items += Item(
            ITEM_TYPE_GROUP_TITLE,
            getString(R.string.vevod_api_example_quick_start),
            null
        )

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_vid_source),
        ) {
            val intent = Intent(this@MainActivity, VidSourceExampleActivity::class.java)
            startActivity(intent)
        }

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_directurl_source),
        ) {
            val intent = Intent(this@MainActivity, DirectUrlSourceExampleActivity::class.java)
            startActivity(intent)
        }

        items += Item(
            ITEM_TYPE_GROUP_TITLE,
            getString(R.string.vevod_api_example_basic_features),
            null
        )

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_basic_playback)
        ) {
            val intent = Intent(this@MainActivity, BasicPlaybackExampleActivity::class.java)
            startActivity(intent)
        }

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_displaymode)
        ) {
            val intent = Intent(this@MainActivity, DisplayModeExampleActivity::class.java)
            startActivity(intent)
        }

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_clear_video_cache)
        ) {
            TTVideoEngine.clearAllCaches(true)
        }

        items += Item(
            ITEM_TYPE_GROUP_TITLE,
            getString(R.string.vevod_api_example_best_practice),
            null
        )

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_short_video_best_practice)
        ) {
            val intent =
                Intent(this@MainActivity, ShortVideoBestPracticeExampleActivity::class.java)
            startActivity(intent)
        }

        items += Item(
            ITEM_TYPE_GROUP_TITLE,
            getString(R.string.vevod_api_example_advanced_features),
            null
        )

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_debugtool)
        ) {
            val intent = Intent(this@MainActivity, DebugToolExampleActivity::class.java)
            startActivity(intent)
        }

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_directurl_subtitle)
        ) {
            val intent = Intent(this@MainActivity, DirectUrlSubtitleExampleActivity::class.java)
            startActivity(intent)
        }

        items += Item(
            ITEM_TYPE_BUTTON_ITEM,
            getString(R.string.vevod_api_example_vid_subtitle)
        ) {
            val intent = Intent(this@MainActivity, VidSubtitleExampleActivity::class.java)
            startActivity(intent)
        }

        return items
    }

    data class Item(
        val itemType: Int,
        val title: String,
        val clickListener: View.OnClickListener?
    ) {
        companion object {
            const val ITEM_TYPE_GROUP_TITLE: Int = 0
            const val ITEM_TYPE_BUTTON_ITEM: Int = 1
        }
    }

    private fun initExampleViews(items: MutableList<Item>) {
        val linearLayout = findViewById<LinearLayout>(R.id.contentView)
        val size = resources.getDimensionPixelSize(R.dimen.vevod_api_example_group_title_margin)
        for (item in items) {
            if (item.itemType == ITEM_TYPE_GROUP_TITLE) {
                val textView = TextView(this)
                textView.text = item.title
                textView.setTextColor(Color.BLUE)
                linearLayout.addView(textView)
                val lp = (textView.layoutParams as ViewGroup.MarginLayoutParams)
                lp.setMargins(size, size, size, size)
            } else if (item.itemType == ITEM_TYPE_BUTTON_ITEM) {
                val button = Button(this)
                button.text = item.title
                button.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
                linearLayout.addView(button)
                button.setOnClickListener {
                    if (!TextUtils.isEmpty(App.APP_ID) && !TextUtils.isEmpty(App.LICENSE_URI)) {
                        item.clickListener?.onClick(it)
                    }
                }
                val lp = (button.layoutParams as ViewGroup.MarginLayoutParams)
                lp.setMargins(size, 0, size, size / 2)
                lp.height =
                    resources.getDimensionPixelSize(R.dimen.vevod_api_example_item_button_height)
            }
        }
    }
}