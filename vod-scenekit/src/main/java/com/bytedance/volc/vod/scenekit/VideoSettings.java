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
import com.bytedance.playerkit.player.cache.CacheLoader;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.player.volcengine.VolcConfig;
import com.bytedance.playerkit.player.volcengine.VolcPlayerInit;
import com.bytedance.playerkit.utils.FileUtils;
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

    public static final String CATEGORY_SHORT_VIDEO = "短视频";
    public static final String CATEGORY_FEED_VIDEO = "中视频";
    public static final String CATEGORY_LONG_VIDEO = "长视频";

    public static final String CATEGORY_DETAIL_VIDEO = "视频详情页";

    public static final String CATEGORY_COMMON_VIDEO = "通用配置";
    public static final String CATEGORY_DEBUG = "调试选项";

    public static final String SHORT_VIDEO_SCENE_ACCOUNT_ID = "short_video_scene_account_id";
    public static final String SHORT_VIDEO_ENABLE_STRATEGY = "short_video_enable_strategy";
    public static final String SHORT_VIDEO_ENABLE_IMAGE_COVER = "short_video_enable_image_cover";
    public static final String SHORT_VIDEO_PLAYBACK_COMPLETE_ACTION = "short_video_playback_complete_action";

    public static final String FEED_VIDEO_ENABLE_PRELOAD = "feed_video_enable_preload";
    public static final String FEED_VIDEO_SCENE_ACCOUNT_ID = "feed_video_scene_account_id";

    public static final String LONG_VIDEO_SCENE_ACCOUNT_ID = "long_video_scene_account_id";

    public static final String DETAIL_VIDEO_SCENE_FRAGMENT_OR_ACTIVITY = "detail_video_scene_fragment_or_activity";

    public static final String DEBUG_ENABLE_LOG_LAYER = "debug_enable_log_layer";
    public static final String DEBUG_ENABLE_DEBUG_TOOL = "debug_enable_debug_tool";

    public static final String COMMON_CODEC_STRATEGY = "common_codec_strategy";
    public static final String COMMON_HARDWARE_DECODE = "common_hardware_decode";
    public static final String COMMON_SUPER_RESOLUTION = "common_super_resolution";
    public static final String COMMON_SOURCE_TYPE = "common_source_type";
    public static final String COMMON_SOURCE_ENCODE_TYPE_H265 = "common_source_encode_type_h265";
    public static final String COMMON_SOURCE_VIDEO_FORMAT_TYPE = "common_source_video_format_type";
    public static final String COMMON_SOURCE_VIDEO_ENABLE_PRIVATE_DRM = "common_source_video_enable_private_drm";

    private static Options sOptions;

    @SuppressLint("StaticFieldLeak")
    private static Context sContext;

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

    public static void init(Context context) {
        sContext = context;
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
        createShortVideoSettings(settings);
        createFeedVideoSettings(settings);
        createLongVideoSettings(settings);
        createDetailVideoSettings(settings);
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

    private static void createCommonSettings(List<SettingItem> settings) {
        settings.add(SettingItem.createCategoryItem(CATEGORY_COMMON_VIDEO));

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
                        COMMON_SUPER_RESOLUTION,
                        "开启超分",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));

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
                        Option.TYPE_RATIO_BUTTON,
                        CATEGORY_COMMON_VIDEO,
                        COMMON_SOURCE_VIDEO_ENABLE_PRIVATE_DRM,
                        "开启视频自研 DRM",
                        Option.STRATEGY_IMMEDIATELY,
                        Boolean.class,
                        Boolean.FALSE,
                        null)));

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
                "清理缓存", new SettingItem.Getter(holder), holder));
    }

    private static class CleanCacheHolder implements SettingItem.Getter.AsyncGetter, SettingItem.OnEventListener {

        private boolean mIsGetting = false;
        private boolean mIsCleaning = false;

        @Override
        public void get(SettingItem.Setter setter) {
            if (mIsGetting) return;
            mIsGetting = true;
            new Thread(() -> {
                long videoFileSize = FileUtils.getFileSize(CacheLoader.Default.get().getCacheDir());
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
                    CacheLoader.Default.get().clearCache();
                    Glide.get(context).clearDiskCache();
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
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
                    });
                }).start();
            }
        }
    }

}

