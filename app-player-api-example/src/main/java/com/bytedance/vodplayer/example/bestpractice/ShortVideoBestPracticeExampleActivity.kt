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

package com.bytedance.vodplayer.example.bestpractice

import android.annotation.SuppressLint
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import androidx.viewpager2.widget.ViewPager2
import coil.load
import com.bytedance.vodplayer.example.App
import com.bytedance.vodplayer.example.BaseActivity
import com.bytedance.vodplayer.example.DataRepository
import com.bytedance.vodplayer.example.R
import com.bytedance.vodplayer.example.VideoItem
import com.bytedance.vodplayer.example.features.VideoViewHolder
import com.bytedance.vodplayer.example.quickstart.DirectUrlSourceExampleActivity
import com.bytedance.vodplayer.example.view.RatioFrameLayout
import com.ss.ttvideoengine.TTVideoEngine
import com.ss.ttvideoengine.VideoEngineCallback
import com.ss.ttvideoengine.VideoEngineInfoListener
import com.ss.ttvideoengine.VideoEngineInfos
import com.ss.ttvideoengine.strategy.EngineStrategyListener
import com.ss.ttvideoengine.strategy.StrategyManager
import com.ss.ttvideoengine.strategy.preload.StrategyPreloadConfig.Builder
import com.ss.ttvideoengine.strategy.preload.StrategyPreloadConfig.DEFAULT_COUNT
import com.ss.ttvideoengine.strategy.preload.StrategyPreloadConfig.DEFAULT_SIZE
import com.ss.ttvideoengine.strategy.preload.StrategyPreloadConfig.DEFAULT_START_BUFFER_LIMIT
import com.ss.ttvideoengine.strategy.preload.StrategyPreloadConfig.DEFAULT_STOP_BUFFER_LIMIT
import com.ss.ttvideoengine.strategy.prerender.PreRenderSurfaceHolder
import com.ss.ttvideoengine.strategy.source.StrategySource
import com.ss.ttvideoengine.utils.Error

class ShortVideoBestPracticeExampleActivity : BaseActivity() {

    companion object {
        const val TAG: String = "[ShortVideo]"

        fun initVodSDKBestStrategy() {
            StrategyManager.setVersion(StrategyManager.VERSION_2)
            StrategyManager.instance().enableReleasePreRenderEngineInstanceByLRU(true)
            StrategyManager.instance().enablePreRenderSurfaceHolder(true)
            StrategyManager.instance().setCustomPreloadConfig(
                Builder()
                    .setStartBufferLimit(DEFAULT_START_BUFFER_LIMIT)
                    .setStopBufferLimit(DEFAULT_STOP_BUFFER_LIMIT)
                    .setSize(DEFAULT_SIZE)
                    .setCount(DEFAULT_COUNT)
                    .build()
            )
            TTVideoEngine.setEngineStrategyListener(object : EngineStrategyListener {

                override fun createPreRenderEngine(source: StrategySource): TTVideoEngine {
                    return createVideoEngine(source)
                }
            })
        }

        private fun setBestStrategyEnabled(enable: Boolean) {
            if (enable) {
                TTVideoEngine.enableEngineStrategy(
                    StrategyManager.STRATEGY_TYPE_PRELOAD,
                    StrategyManager.STRATEGY_SCENE_SMALL_VIDEO
                )
                TTVideoEngine.enableEngineStrategy(
                    StrategyManager.STRATEGY_TYPE_PRE_RENDER,
                    StrategyManager.STRATEGY_SCENE_SMALL_VIDEO
                )
            } else {
                TTVideoEngine.clearAllStrategy()
            }
        }

        private fun setVideoItems(videoItems: List<VideoItem>) {
            TTVideoEngine.setStrategySources(videoItems2StrategySources(videoItems))
        }

        private fun addVideoItems(videoItems: List<VideoItem>) {
            TTVideoEngine.addStrategySources(videoItems2StrategySources(videoItems))
        }

        private fun videoItems2StrategySources(videoItems: List<VideoItem>): List<StrategySource> {
            val strategySources = mutableListOf<StrategySource>()
            for (videoItem in videoItems) {
                strategySources.add(DirectUrlSourceExampleActivity.createDirectUrlSource(videoItem))
            }
            return strategySources;
        }

        private fun createVideoEngine(strategySource: StrategySource): TTVideoEngine {
            val engine = TTVideoEngine(App.sContext)
            engine.strategySource = strategySource
            engine.setIntOption(TTVideoEngine.PLAYER_OPTION_USE_VIDEOMODEL_CACHE, 1)
            engine.setIntOption(
                TTVideoEngine.PLAYER_OPTION_IMAGE_LAYOUT,
                TTVideoEngine.IMAGE_LAYOUT_TO_FILL
            )
            engine.setIntOption(TTVideoEngine.PLAYER_OPTION_POSITION_UPDATE_INTERVAL, 200)
            engine.setIntOption(TTVideoEngine.PLAYER_OPTION_ENABLE_START_TIME_SKIP_AVSKIPSERIAL, 1);
            engine.setIntOption(TTVideoEngine.PLAYER_OPTION_OPTIMIZE_START_TIME_PRERENDER, 1);
            return engine
        }
    }

    private lateinit var mViewPager: ViewPager2
    private lateinit var mAdapter: Adapter
    private var mPlayingHolder: ItemViewHolder? = null

    override fun getLayoutId(): Int {
        return R.layout.vevod_api_example_short_video_best_practice
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        refresh()
    }

    fun initView() {
        mViewPager = findViewById(R.id.viewPager2)
        mViewPager.offscreenPageLimit = 1
        mViewPager.orientation = ViewPager2.ORIENTATION_VERTICAL
        mAdapter = Adapter()
        mViewPager.adapter = mAdapter
        mViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                val holder = mAdapter.holderMap[position]
                if (mPlayingHolder == null) {
                    mPlayingHolder = holder
                } else if (mPlayingHolder != holder) {
                    mPlayingHolder?.stopPlayback()
                    mPlayingHolder = holder
                }
                mPlayingHolder?.startPlayback()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        mPlayingHolder?.startPlayback()
        setBestStrategyEnabled(true)
    }

    override fun onPause() {
        super.onPause()
        mPlayingHolder?.pausePlayback();
        setBestStrategyEnabled(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayingHolder?.stopPlayback()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun refresh() {
        mAdapter.videoItems.addAll(DataRepository.videoItems)
        mAdapter.notifyDataSetChanged()
        setVideoItems(mAdapter.videoItems)
    }

    class Adapter : RecyclerView.Adapter<ItemViewHolder>() {

        val videoItems: MutableList<VideoItem> = ArrayList()
        val holderMap: MutableMap<Int, ItemViewHolder> = mutableMapOf()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
            return ItemViewHolder.create(parent)
        }

        override fun getItemCount(): Int {
            return videoItems.size
        }

        override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
            val videoItem = videoItems[position]
            holder.bind(videoItem)
        }

        override fun onViewDetachedFromWindow(holder: ItemViewHolder) {
            holder.stopPlayback()
        }

        override fun onViewAttachedToWindow(holder: ItemViewHolder) {
            holderMap[holder.adapterPosition] = holder
        }

        override fun onViewRecycled(holder: ItemViewHolder) {
            holder.stopPlayback()
            holderMap.remove(holder.adapterPosition)
        }
    }

    class ItemViewHolder(itemView: View) : ViewHolder(itemView) {

        companion object {
            fun create(parent: ViewGroup): ItemViewHolder {
                return ItemViewHolder(
                    LayoutInflater.from(parent.context)
                        .inflate(R.layout.vevod_api_example_layout_videoview, parent, false)
                )
            }
        }

        private val videoViewHolder: VideoViewHolder
        private var surface: Surface? = null
        private var videoItem: VideoItem? = null
        private var videoEngine: TTVideoEngine? = null

        private val preRenderSurfaceHolder: PreRenderSurfaceHolder
        private var preRenderSurfaceListener: PreRenderSurfaceHolder.SurfaceListener? = null

        init {
            videoViewHolder = VideoViewHolder(itemView as RatioFrameLayout).apply {

                videoView.layoutParams =
                    LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)

                listener = object : VideoViewHolder.VideoViewListener {

                    override fun onStopTrackingTouch(seekBar: SeekBar) {
                        videoEngine?.seekTo(seekBar.progress, null)
                        videoEngine?.setVideoEngineInfoListener(object : VideoEngineInfoListener {
                            override fun onVideoEngineInfos(infos: VideoEngineInfos) {
                                if (TextUtils.equals(
                                        infos.key,
                                        VideoEngineInfos.USING_RENDER_SEEK_COMPLETE
                                    )
                                ) {
                                    Log.d(
                                        TAG,
                                        "[$adapterPosition] ${videoItem?.vid} seek render complete $position"
                                    )
                                }
                            }
                        })
                    }

                    override fun onSurfaceTextureAvailable(
                        surfaceTexture: SurfaceTexture,
                        width: Int,
                        height: Int
                    ) {
                        surface = Surface(surfaceTexture)

                        Log.d(TAG, "[$adapterPosition] ${videoItem?.vid} surfaceAvailable $surface")

                        StrategyManager.instance().handler()?.post {
                            preRenderSurfaceListener?.onSurfaceAvailable(surface, width, height)
                        }

                        if (videoEngine != null) {
                            videoEngine!!.surface = surface
                        }
                    }

                    override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture) {
                        Log.d(TAG, "[$adapterPosition] ${videoItem?.vid} surfaceDestroyed $surface")
                        videoEngine?.surface = null
                    }
                }

                videoView.setOnClickListener {
                    videoEngine?.let { engine ->
                        if (engine.playbackState == TTVideoEngine.PLAYBACK_STATE_PLAYING) {
                            engine.pause()
                        } else {
                            engine.play()
                        }
                    }
                }
            }

            preRenderSurfaceHolder = object : PreRenderSurfaceHolder {
                override fun getSurface(): Surface? {
                    return this@ItemViewHolder.surface
                }

                override fun setSurfaceListener(surfaceListener: PreRenderSurfaceHolder.SurfaceListener) {
                    StrategyManager.instance().handler()!!.post {
                        preRenderSurfaceListener = surfaceListener
                    }
                }

                override fun bindVideoEngine(source: StrategySource, engine: TTVideoEngine) {
                    // 监听预渲染 Engine 实例状态
                    engine.addVideoEngineCallback(object : VideoEngineCallback {

                        override fun onReadyForDisplay(engine: TTVideoEngine) {
                            engine.removeVideoEngineCallback(this)
                            engine.setDisplayMode(
                                videoViewHolder.textureView,
                                TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT
                            )
                            videoViewHolder.imageCover.visibility = View.GONE
                            videoViewHolder.playPause.visibility = View.GONE
                            Log.d(
                                TAG,
                                "[$adapterPosition] ${engine.videoID} preRenderFirstFrame success $engine"
                            )
                        }

                        override fun onError(error: Error?) {
                            engine.removeVideoEngineCallback(this)
                            Log.d(
                                TAG,
                                "[$adapterPosition] ${engine.videoID} preRenderFirstFrame error $engine"
                            )
                        }

                        override fun onPlaybackStateChanged(
                            engine: TTVideoEngine,
                            playbackState: Int
                        ) {
                            if (engine.isReleased) {
                                engine.removeVideoEngineCallback(this)
                                Log.d(
                                    TAG,
                                    "[$adapterPosition] ${engine.videoID} preRenderFirstFrame canceled $engine"
                                )
                            }
                        }
                    })
                }
            }
        }

        fun bind(item: VideoItem) {
            if (this.videoItem != null && !TextUtils.equals(videoItem?.vid, item.vid)) {
                stopPlayback()
            }
            videoItem = item
            videoViewHolder.imageCover.load(item.coverUrl)
            StrategyManager.instance().setPreRenderSurfaceHolder(item.vid, preRenderSurfaceHolder)
        }

        fun stopPlayback() {
            videoEngine?.let { videoEngine ->
                Log.d(TAG, "[$adapterPosition] ${videoEngine.videoID} stopPlayback $videoEngine")
                videoEngine.surface = null
                videoEngine.releaseAsync()
                this@ItemViewHolder.videoEngine = null

                with(videoViewHolder) {
                    playPause.visibility = VISIBLE
                    videoView.keepScreenOn = false
                }
            }
        }

        fun startPlayback() {
            if (videoItem == null) return

            if (videoEngine == null) {
                // init videoEngine
                val preRenderEngine: TTVideoEngine? =
                    TTVideoEngine.removePreRenderEngine(videoItem!!.vid)
                if (preRenderEngine != null) {
                    Log.d(
                        TAG,
                        "[$adapterPosition] ${videoItem!!.vid} startPlayback preRender $preRenderEngine"
                    )
                    videoEngine = preRenderEngine
                    // 1. 不为 null 说明预渲染完成，直接调用播放即可。业务持有这个播放器，负责实例 release
                    // 2. 预渲染播放器已经处于 prepared 状态，prepared 之前播放器抛的回调，如果业务需要，都需要在这里按顺序手动抛出
                    videoViewHolder.onPrepared(videoEngine)
                } else {
                    val strategySource =
                        DirectUrlSourceExampleActivity.createDirectUrlSource(videoItem!!)
                    // 为 null 说明无预渲染，您需构造 engine 后播放。
                    videoEngine = createVideoEngine(strategySource)
                    Log.d(
                        TAG,
                        "[$adapterPosition] ${videoItem?.vid} startPlayback create $videoEngine"
                    )
                }

                // set surface & callback & displayMode
                videoEngine?.let { videoEngine ->
                    if (surface != null && surface!!.isValid) {
                        videoEngine.surface = surface
                    }
                    videoEngine.addVideoEngineCallback(videoViewHolder)
                    videoEngine.setDisplayMode(
                        videoViewHolder.textureView,
                        TTVideoEngine.IMAGE_LAYOUT_ASPECT_FIT
                    )
                }
            } else {
                Log.d(TAG, "[$adapterPosition] ${videoItem?.vid} startPlayback resume $videoEngine")
            }
            videoEngine!!.play()

            with(videoViewHolder) {
                playPause.visibility = GONE
                videoView.keepScreenOn = true
            }
        }

        fun pausePlayback() {
            Log.d(TAG, "[$adapterPosition] ${videoEngine?.videoID} pausePlayback $videoEngine")
            videoEngine?.pause()
        }
    }
}