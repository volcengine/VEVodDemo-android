/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/9/13
 */

package com.bytedance.volc.vod.scenekit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.DisplayView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Quality;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.volcengine.VolcConfig;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.utils.FileUtils;
import com.bytedance.volc.vod.scenekit.strategy.VideoQuality;
import com.bytedance.volc.vod.scenekit.strategy.VideoSubtitle;
import com.bytedance.volc.vod.settingskit.Option;
import com.bytedance.volc.vod.settingskit.Options;
import com.bytedance.volc.vod.settingskit.OptionsDefault;
import com.bytedance.volc.vod.settingskit.SettingItem;
import com.bytedance.volc.vod.settingskit.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoSettings {
    public static final String KEY = "AppSettings";
    public static final String CATEGORY_DEBUG = "调试选项";
    public static final String CATEGORY_INIT = "初始化配置";
    public static final String CATEGORY_SHORT_VIDEO = "短视频";
    public static final String CATEGORY_FEED_VIDEO = "中视频";
    public static final String CATEGORY_LONG_VIDEO = "长视频";
    public static final String CATEGORY_DETAIL_VIDEO = "视频详情页";
    public static final String CATEGORY_MINI_DRAMA_VIDEO = "短剧";
    public static final String CATEGORY_AD = "广告";
    public static final String CATEGORY_QUALITY = "清晰度设置";
    public static final String CATEGORY_SUBTITLE = "字幕设置";
    public static final String CATEGORY_COMMON_VIDEO = "通用配置";

    public static final String SHORT_VIDEO_SCENE_ACCOUNT_ID = "short_video_scene_account_id";
    public static final String SHORT_VIDEO_ENABLE_STRATEGY = "short_video_enable_strategy";
    public static final String SHORT_VIDEO_ENABLE_IMAGE_COVER = "short_video_enable_image_cover";
    public static final String SHORT_VIDEO_PLAYBACK_COMPLETE_ACTION = "short_video_playback_complete_action";
    public static final String SHORT_VIDEO_ENABLE_AD = "short_video_enable_ad";

    public static final String FEED_VIDEO_SCENE_ACCOUNT_ID = "feed_video_scene_account_id";
    public static final String FEED_VIDEO_ENABLE_PRELOAD = "feed_video_enable_preload";

    public static final String LONG_VIDEO_SCENE_ACCOUNT_ID = "long_video_scene_account_id";

    public static final String DETAIL_VIDEO_SCENE_FRAGMENT_OR_ACTIVITY = "detail_video_scene_fragment_or_activity";

    public static final String DRAMA_VIDEO_SCENE_ACCOUNT_ID = "drama_video_scene_account_id";
    public static final String DRAMA_VIDEO_PREVENT_SCREEN_SHOT = "drama_video_prevent_screen_shot";
    public static final String DRAMA_DETAIL_ENABLE_AD = "drama_detail_enable_ad";
    public static final String DRAMA_RECOMMEND_ENABLE_AD = "drama_recommend_enable_ad";


    public static final String AD_VIDEO_ACCOUNT_ID = "ad_video_account_id";
    public static final String AD_VIDEO_SHOW_INTERVAL = "ad_video_show_interval";
    public static final String AD_VIDEO_PREFETCH_MAX_COUNT = "ad_video_prefetch_max_count";

    public static final String DEBUG_ENABLE_LOG_LAYER = "debug_enable_log_layer";
    public static final String DEBUG_ENABLE_DEBUG_TOOL = "debug_enable_debug_tool";

    public static final String INIT_ENABLE_LOGCAT = "init_enable_logcat";
    public static final String INIT_ENABLE_ASSERTS = "init_enable_asserts";
    public static final String INIT_ENABLE_VOD_SDK_ASYNC_INIT = "init_vod_sdk_async_init";

    public static final String QUALITY_ENABLE_STARTUP_ABR = "quality_enable_startup_abr";
    public static final String QUALITY_VIDEO_QUALITY_USER_SELECTED = "quality_video_quality_user_selected";

    public static final String SUBTITLE_ENABLE = "subtitle_enable";
    public static final String SUBTITLE_SOURCE_TYPE = "subtitle_source_type";
    public static final String SUBTITLE_ENABLE_PRELOAD_STRATEGY = "subtitle_enable_preload_strategy";
    public static final String SUBTITLE_LANGUAGE_ID_USER_SELECTED = "subtitle_language_id_user_selected";

    public static final String COMMON_CODEC_STRATEGY = "common_codec_strategy";
    public static final String COMMON_HARDWARE_DECODE = "common_hardware_decode";
    public static final String COMMON_SOURCE_TYPE = "common_source_type";
    public static final String COMMON_SOURCE_ENCODE_TYPE_H265 = "common_source_encode_type_h265";
    public static final String COMMON_SOURCE_VIDEO_FORMAT_TYPE = "common_source_video_format_type";
    public static final String COMMON_SOURCE_VIDEO_ENABLE_PRIVATE_DRM = "common_source_video_enable_private_drm";
    public static final String COMMON_ENABLE_SUPER_RESOLUTION = "common_enable_super_resolution";
    public static final String COMMON_ENABLE_ECDN = "common_enable_ecdn";
    public static final String COMMON_ENABLE_SOURCE_403_REFRESH = "common_enable_source_403_refresh";
    public static final String COMMON_ENABLE_PIP = "common_enable_pip";
    public static final String COMMON_RENDER_VIEW_TYPE = "common_render_view_type";

    private static Options sOptions;

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

    private static SettingItem.OnEventListener sEventListener;

    public static class ClickableItemId {
        public static final String INPUT_SOURCE = "input_source";
        public static final String CLEAR_CACHE = "clear_cache";
    }

    public static class DecoderType {
        public static final int AUTO = Player.DECODER_TYPE_UNKNOWN;
        public static final int HARDWARE = Player.DECODER_TYPE_HARDWARE;
        public static final int SOFTWARE = Player.DECODER_TYPE_SOFTWARE;
    }

    public static class CodecStrategy {
        public static final int CODEC_STRATEGY_DISABLE = VolcConfig.CODEC_STRATEGY_DISABLE;
        public static final int CODEC_STRATEGY_COST_SAVING_FIRST = VolcConfig.CODEC_STRATEGY_COST_SAVING_FIRST;
        public static final int CODEC_STRATEGY_HARDWARE_DECODE_FIRST = VolcConfig.CODEC_STRATEGY_HARDWARE_DECODE_FIRST;
    }

    public static class FormatType {
        public static final int FORMAT_TYPE_MP4 = Track.FORMAT_MP4;
        public static final int FORMAT_TYPE_DASH = Track.FORMAT_DASH;
        public static final int FORMAT_TYPE_HLS = Track.FORMAT_HLS;
    }

    public static class SourceType {
        public static final int SOURCE_TYPE_URL = MediaSource.SOURCE_TYPE_URL;
        public static final int SOURCE_TYPE_VID = MediaSource.SOURCE_TYPE_ID;
        public static final int SOURCE_TYPE_MODEL = MediaSource.SOURCE_TYPE_MODEL;
    }

    public static void init(Context context, @Nullable SettingItem.OnEventListener eventListener) {
        sContext = context;
        sEventListener = eventListener;
        List<SettingItem> settings = createSettings();
        sOptions = new OptionsDefault(context, createOptions(settings), option -> null);
        Settings.put(KEY, settings);
    }

    private static List<Option> createOptions(List<SettingItem> settings) {
        List<Option> options = new ArrayList<>();
        for (SettingItem item : settings) {
            if (item.type == SettingItem.TYPE_OPTION) {
                options.add(item.option);
            }
        }
        return options;
    }

    public static Option option(String key) {
        return sOptions.option(key);
    }

    public static int intValue(String key) {
        return option(key).intValue();
    }

    public static boolean booleanValue(String key) {
        return option(key).booleanValue();
    }

    public static long longValue(String key) {
        return option(key).longValue();
    }

    public static float floatValue(String key) {
        return option(key).floatValue();
    }

    @Nullable
    public static String stringValue(String key) {
        return option(key).stringValue();
    }

    private static List<SettingItem> createSettings() {
        List<SettingItem> settings = new ArrayList<>();
        createDebugSettings(settings);
        createInitSettings(settings);
        createShortVideoSettings(settings);
        createFeedVideoSettings(settings);
        createLongVideoSettings(settings);
        createDetailVideoSettings(settings);
        createDramaSettings(settings);
        createAdSettings(settings);
        createQualitySettings(settings);
        createSubtitleSettings(settings);
        createCommonSettings(settings);
        return settings;
    }

    private static void createDebugSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_DEBUG));
        settings.add(SettingItem.createOptionItem(CATEGORY_DEBUG,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_DEBUG,
                        DEBUG_ENABLE_LOG_LAYER,
                        "开启 LogLayer",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));
        settings.add(SettingItem.createOptionItem(CATEGORY_DEBUG,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_DEBUG,
                        DEBUG_ENABLE_DEBUG_TOOL,
                        "开启 Debug 工具",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));
        settings.add(SettingItem.createClickableItem(CATEGORY_COMMON_VIDEO,
                ClickableItemId.INPUT_SOURCE,
                "输入播放源",
                null,
                sEventListener));
    }

    private static void createInitSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_INIT));
        settings.add(SettingItem.createOptionItem(CATEGORY_INIT,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_INIT,
                        INIT_ENABLE_LOGCAT,
                        "开启 Logcat 输出",
                        Option.STRATEGY_RESTART_APP,
                        Boolean.class,
                        Boolean.TRUE,
                        null)));
        settings.add(SettingItem.createOptionItem(CATEGORY_INIT,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_INIT,
                        INIT_ENABLE_ASSERTS,
                        "开启 Debug 断言",
                        Option.STRATEGY_RESTART_APP,
                        Boolean.class,
                        Boolean.TRUE,
                        null)));
        settings.add(SettingItem.createOptionItem(CATEGORY_INIT,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_INIT,
                        INIT_ENABLE_VOD_SDK_ASYNC_INIT,
                        "开启异步初始化点播 SDK",
                        Option.STRATEGY_RESTART_APP,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));
    }

    private static void createShortVideoSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_SHORT_VIDEO));

        settings.add(SettingItem.createOptionItem(CATEGORY_SHORT_VIDEO,
                new Option(
                        Option.TYPE_EDITABLE_TEXT,
                        CATEGORY_SHORT_VIDEO,
                        SHORT_VIDEO_SCENE_ACCOUNT_ID,
                        "短视频账号",
                        Option.STRATEGY_IMMEDIATELY,
                        String.class,
                        "short-video",
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_SHORT_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_SHORT_VIDEO,
                        SHORT_VIDEO_ENABLE_STRATEGY,
                        "短视频开启策略",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.TRUE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_SHORT_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_SHORT_VIDEO,
                        SHORT_VIDEO_ENABLE_IMAGE_COVER,
                        "短视频开启图片封面",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.TRUE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_SHORT_VIDEO,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_SHORT_VIDEO,
                        SHORT_VIDEO_PLAYBACK_COMPLETE_ACTION,
                        "短视频播放完成行为",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        0,
                        Arrays.asList(0, 1)), new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        final int action = (int) value;
                        switch (action) {
                            case 0:
                                return "循环播放";
                            case 1:
                                return "播放下一个";
                        }
                        return null;
                    }
                }));

        settings.add(SettingItem.createOptionItem(CATEGORY_SHORT_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_SHORT_VIDEO,
                        SHORT_VIDEO_ENABLE_AD,
                        "短视频开启流内广告",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));
    }

    private static void createFeedVideoSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_FEED_VIDEO));
        settings.add(SettingItem.createOptionItem(CATEGORY_SHORT_VIDEO,
                new Option(
                        Option.TYPE_EDITABLE_TEXT,
                        CATEGORY_FEED_VIDEO,
                        FEED_VIDEO_SCENE_ACCOUNT_ID,
                        "中视频账号",
                        Option.STRATEGY_IMMEDIATELY,
                        String.class,
                        "feedvideo",
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_FEED_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_FEED_VIDEO,
                        FEED_VIDEO_ENABLE_PRELOAD,
                        "中视频开启预加载",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.TRUE,
                        null)));
    }

    private static void createLongVideoSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_LONG_VIDEO));
        settings.add(SettingItem.createOptionItem(CATEGORY_LONG_VIDEO,
                new Option(
                        Option.TYPE_EDITABLE_TEXT,
                        CATEGORY_LONG_VIDEO,
                        LONG_VIDEO_SCENE_ACCOUNT_ID,
                        "长视频账号",
                        Option.STRATEGY_IMMEDIATELY,
                        String.class,
                        "long-video",
                        null)));
    }

    private static void createDetailVideoSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_DETAIL_VIDEO));
        settings.add(SettingItem.createOptionItem(CATEGORY_DETAIL_VIDEO,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_DETAIL_VIDEO,
                        DETAIL_VIDEO_SCENE_FRAGMENT_OR_ACTIVITY,
                        "视频详情页使用 Fragment/Activity",
                        Option.STRATEGY_IMMEDIATELY,
                        String.class,
                        "Fragment",
                        Arrays.asList("Fragment", "Activity"))));
    }

    private static void createDramaSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_MINI_DRAMA_VIDEO));
        settings.add(SettingItem.createOptionItem(CATEGORY_MINI_DRAMA_VIDEO,
                new Option(
                        Option.TYPE_EDITABLE_TEXT,
                        CATEGORY_MINI_DRAMA_VIDEO,
                        DRAMA_VIDEO_SCENE_ACCOUNT_ID,
                        "短剧账号",
                        Option.STRATEGY_IMMEDIATELY,
                        String.class,
                        "mini-drama-video",
                        null)));
        settings.add(SettingItem.createOptionItem(CATEGORY_MINI_DRAMA_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_MINI_DRAMA_VIDEO,
                        DRAMA_VIDEO_PREVENT_SCREEN_SHOT,
                        "禁止截屏",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.TRUE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_MINI_DRAMA_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_MINI_DRAMA_VIDEO,
                        DRAMA_DETAIL_ENABLE_AD,
                        "短剧详情开启流内广告",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_MINI_DRAMA_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_MINI_DRAMA_VIDEO,
                        DRAMA_RECOMMEND_ENABLE_AD,
                        "短剧推荐开启流内广告",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));
    }

    private static void createAdSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_AD));
        settings.add(SettingItem.createOptionItem(CATEGORY_AD,
                new Option(
                        Option.TYPE_EDITABLE_TEXT,
                        CATEGORY_AD,
                        AD_VIDEO_ACCOUNT_ID,
                        "Mock 广告账号",
                        Option.STRATEGY_IMMEDIATELY,
                        String.class,
                        /*"mock-ad-video",*/ "short-video",
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_AD,
                new Option(
                        Option.TYPE_EDITABLE_TEXT,
                        CATEGORY_AD,
                        AD_VIDEO_SHOW_INTERVAL,
                        "广告展示间隔",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        5,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_AD,
                new Option(
                        Option.TYPE_EDITABLE_TEXT,
                        CATEGORY_AD,
                        AD_VIDEO_PREFETCH_MAX_COUNT,
                        "广告预获取条数",
                        Option.STRATEGY_RESTART_APP,
                        Integer.class,
                        10,
                        null)));
    }

    private static void createQualitySettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_QUALITY));
        settings.add(SettingItem.createOptionItem(CATEGORY_QUALITY,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_QUALITY,
                        QUALITY_ENABLE_STARTUP_ABR,
                        "开启 ABR 起播选档",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        0,
                        Arrays.asList(0, 1, 2)), new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        final int type = (int) value;
                        switch (type) {
                            case 1:
                                return "ABR 起播选档";
                            case 2:
                                return "ABR 起播选档 + 超分降档";
                            case 0:
                            default:
                                return "关闭";
                        }
                    }
                }));

        settings.add(SettingItem.createOptionItem(CATEGORY_QUALITY,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_QUALITY,
                        QUALITY_VIDEO_QUALITY_USER_SELECTED,
                        "用户选择的清晰度",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        Quality.QUALITY_RES_DEFAULT,
                        new ArrayList<>(VideoQuality.QUALITY_RES_ARRAY_USER_SELECTED)), new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        final int qualityRes = (int) value;
                        return VideoQuality.qualityDesc(qualityRes);
                    }
                }));
    }

    private static void createSubtitleSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_SUBTITLE));
        settings.add(SettingItem.createOptionItem(CATEGORY_SUBTITLE,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_SUBTITLE,
                        SUBTITLE_ENABLE,
                        "开启字幕",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_SUBTITLE,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_SUBTITLE,
                        SUBTITLE_SOURCE_TYPE,
                        "字幕源类型",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        SourceType.SOURCE_TYPE_VID,
                        Arrays.asList(SourceType.SOURCE_TYPE_VID, SourceType.SOURCE_TYPE_URL)),
                new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        switch ((Integer) value) {
                            case SourceType.SOURCE_TYPE_VID:
                                return "Vid + SubtitleAuthToken\n（仅支持 Vid/VideoModel 源类型）";
                            case SourceType.SOURCE_TYPE_URL:
                                return "DirectURL";
                        }
                        return null;
                    }
                }));
        settings.add(SettingItem.createOptionItem(CATEGORY_SUBTITLE,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_SUBTITLE,
                        SUBTITLE_ENABLE_PRELOAD_STRATEGY,
                        "开启字幕预加载策略",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));
        settings.add(SettingItem.createOptionItem(CATEGORY_SUBTITLE,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_SUBTITLE,
                        SUBTITLE_LANGUAGE_ID_USER_SELECTED,
                        "用户选择的字幕语言",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        VideoSubtitle.LANGUAGE_ID_CN,
                        new ArrayList<>(VideoSubtitle.createLanguageIds())),
                new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        Integer i = (Integer) value;
                        switch (i) {
                            case VideoSubtitle.LANGUAGE_ID_CN:
                                return "中文";
                            case VideoSubtitle.LANGUAGE_ID_US:
                                return "英文";
                            default:
                                throw new IllegalArgumentException();
                        }
                    }
                }));
    }

    private static void createCommonSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_COMMON_VIDEO));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_SOURCE_TYPE,
                        "源类型",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        SourceType.SOURCE_TYPE_VID,
                        Arrays.asList(SourceType.SOURCE_TYPE_VID, SourceType.SOURCE_TYPE_URL, SourceType.SOURCE_TYPE_MODEL)),
                new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        switch ((Integer) value) {
                            case SourceType.SOURCE_TYPE_VID:
                                return "VideoID";
                            case SourceType.SOURCE_TYPE_URL:
                                return "DirectURL";
                            case SourceType.SOURCE_TYPE_MODEL:
                                return "VideoModel";
                        }
                        return null;
                    }
                }));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_SOURCE_VIDEO_FORMAT_TYPE,
                        "视频格式",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        FormatType.FORMAT_TYPE_MP4,
                        Arrays.asList(FormatType.FORMAT_TYPE_MP4, FormatType.FORMAT_TYPE_DASH, FormatType.FORMAT_TYPE_HLS)),
                new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        switch ((Integer) value) {
                            case FormatType.FORMAT_TYPE_MP4:
                                return "MP4";
                            case FormatType.FORMAT_TYPE_DASH:
                                return "DASH";
                            case FormatType.FORMAT_TYPE_HLS:
                                return "HLS";
                        }
                        return null;
                    }
                }));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_CODEC_STRATEGY,
                        "Codec 策略",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        CodecStrategy.CODEC_STRATEGY_DISABLE,
                        Arrays.asList(CodecStrategy.CODEC_STRATEGY_DISABLE,
                                CodecStrategy.CODEC_STRATEGY_COST_SAVING_FIRST,
                                CodecStrategy.CODEC_STRATEGY_HARDWARE_DECODE_FIRST)),
                new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        switch ((Integer) value) {
                            case CodecStrategy.CODEC_STRATEGY_DISABLE:
                                return "关闭";
                            case CodecStrategy.CODEC_STRATEGY_COST_SAVING_FIRST:
                                return "成本优先";
                            case CodecStrategy.CODEC_STRATEGY_HARDWARE_DECODE_FIRST:
                                return "硬解优先";
                        }
                        return null;
                    }
                }));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_HARDWARE_DECODE,
                        "开启硬解码",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        DecoderType.AUTO,
                        Arrays.asList(DecoderType.AUTO, DecoderType.HARDWARE, DecoderType.SOFTWARE)),
                new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        switch ((Integer) value) {
                            case DecoderType.AUTO:
                                return "自动";
                            case DecoderType.HARDWARE:
                                return "硬解";
                            case DecoderType.SOFTWARE:
                                return "软解";
                        }
                        return null;
                    }
                }));


        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_SOURCE_ENCODE_TYPE_H265,
                        "开启 H265",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.TRUE,
                        null)));


        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_SOURCE_VIDEO_ENABLE_PRIVATE_DRM,
                        "开启视频自研 DRM",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_ENABLE_SUPER_RESOLUTION,
                        "开启超分",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_ENABLE_ECDN,
                        "开启 ECDN",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));


        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_ENABLE_SOURCE_403_REFRESH,
                        "开启播放源过期刷新",
                        Option.STRATEGY_RESTART_APP,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_ENABLE_PIP,
                        "开启小窗",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null),
                null,
                sEventListener));

        settings.add(SettingItem.createOptionItem(CATEGORY_COMMON_VIDEO,
                new Option(
                        Option.TYPE_SELECTABLE_ITEMS,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_RENDER_VIEW_TYPE,
                        "渲染 View 类型",
                        Option.STRATEGY_IMMEDIATELY,
                        Integer.class,
                        DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW,
                        Arrays.asList(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW, DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW)),
                new SettingItem.ValueMapper() {
                    @Override
                    public String toString(Object value) {
                        switch ((Integer) value) {
                            case DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW:
                                return "TextureView";
                            case DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW:
                                return "SurfaceView(不推荐)";
                        }
                        return null;
                    }
                }));


        settings.add(SettingItem.createCopyableTextItem(CATEGORY_COMMON_VIDEO,
                "Device ID",
                new SettingItem.Getter(VolcPlayerInit::getDeviceId)
        ));

        settings.add(SettingItem.createCopyableTextItem(CATEGORY_COMMON_VIDEO,
                "TTSDK Version",
                new SettingItem.Getter(VolcPlayerInit::getSDKVersion)
        ));

        final CleanCacheHolder holder = new CleanCacheHolder();
        settings.add(SettingItem.createClickableItem(CATEGORY_COMMON_VIDEO,
                ClickableItemId.CLEAR_CACHE,
                "清理缓存",
                new SettingItem.Getter(holder),
                holder));
    }

    private static class CleanCacheHolder implements SettingItem.Getter.AsyncGetter, SettingItem.OnEventListener {

        private boolean mIsGetting = false;
        private boolean mIsCleaning = false;

        @Override
        public void get(SettingItem.Setter setter) {
            if (mIsGetting) return;
            mIsGetting = true;
            new Thread(() -> {
                long videoFileSize = FileUtils.getFileSize(VolcPlayerInit.config().playerCacheDir);
                long imageFileSize = FileUtils.getFileSize(Glide.getPhotoCacheDir(sContext));
                long size = imageFileSize + videoFileSize;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        setter.set(FileUtils.formatSize(size));
                        mIsGetting = false;
                    }
                });
            }).start();
        }

        @Override
        public void onEvent(int eventType, Context context, SettingItem settingItem, RecyclerView.ViewHolder holder) {
            if (eventType == SettingItem.OnEventListener.EVENT_TYPE_CLICK) {
                if (mIsCleaning) return;
                mIsCleaning = true;

                Toast.makeText(context, "Cleaning cache...", Toast.LENGTH_SHORT).show();
                new Thread(() -> {
                    VolcPlayerInit.clearDiskCache();
                    Glide.get(context).clearDiskCache();
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mIsGetting = false;
                            mIsCleaning = false;
                            Toast.makeText(context, "Clean done!", Toast.LENGTH_SHORT).show();
                            RecyclerView.Adapter<?> adapter = holder.getBindingAdapter();
                            if (adapter != null) {
                                adapter.notifyItemChanged(holder.getAbsoluteAdapterPosition());
                            }
                        }
                    }, 1000);
                }).start();
            }
        }
    }

}

