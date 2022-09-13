# 播放控件层接入

playerkit 是开源的，可以 copy 或 link 到项目中使用，根据需要进行源码修改。

## PlayerKit 集成

1. clone git 仓库

```shell
git clone https://github.com/volcengine/VEVodDemo-android
cd VEVodDemo-android
git checkout feature/playerkit/dev
```

2. 确保 project 根目录下的 build.gradle 文件中的 repositories 中配置了 mavenCentral() 和 火山引擎maven 服务。

```groovy
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
        maven {
            url "https://artifact.bytedance.com/repository/Volcengine/" // volc public maven repo
        }
    }
}
```

3. 在 settings.gradle 中引入 playerkit 模块

```groovy
include ':app'

gradle.ext.playerKitModulePrefix = 'vod-playerkit:'
// 这里示例 playerkit app 同级目录的 case。 您需要根据 VEVodDemo-android 的 clone 路径来调整这里的路径
// 或者将 playerkit 目录拷贝到项目中，采用下面的路径集成
apply from: new File(getRootDir(), 'vod-playerkit/config/library_settings.gradle')
```

4. 在 App module 的 build.gradle 中引入 playerkit 依赖

```groovy
dependencies {
    implementation project(":${gradle.ext.playerKitModulePrefix}flavor:flavor-volc-ui")
}
```

5. sync 一下 gradle，完成集成.
   <img src="doc/res/image/img.png" width="200">

## PlayerKit 模块说明

```text
|--|--vod-playerkit             // 火山引擎点播 SDK 播放控件层 SDK
|--|--|--config                 // 控件层 gradle 配置目录
|--|--|--core
|--|--|--|--player-volcengine   // 火山引擎播放器实现模块
|--|--|--flavor
|--|--|--|--flavor-basic        // 包含：播放器适配层
|--|--|--|--flavor-volc         // 包含：播放器适配层 + 火山引擎播放器内核实现（player-volcengine）
|--|--|--|--flavor-volc-ui      // 包含：播放器适配层 + 火山引擎播放器内核实现（player-volcengine） + 控件层内置的默认风格的业务 ui 模块（player-ui）
|--|--|--library
|--|--|--|--player              // 播放器适配层（定义了一套标准播放器接口）
|--|--|--|--player-playback     // 播放流程控制模块
|--|--|--|--player-settings     // 播放器 options 配置模块
|--|--|--|--player-utils        // 工具类模块
|--|--|--ui
|--|--|--|--player-ui           // 控件层内置的默认风格的业务 ui 模块
```

| 模块 | 描述 |  |
| ---- | ---- | ---- |
| :ui:player-ui | 默认风格的业务 ui 模块 | 我们基于播放控件层 Layer 系统，参考西瓜/抖音播放界面风格实现了一些默认的 UI 来帮助业务快速集成。若默认风格不满足需求，可以随意修改源码。|
| :core:player-volcengine | 火山引擎播放器实现模块  | 1. 用播件层的播放器接口，实现了火山引擎播放器.2. 封装了火山引擎播放器初始化模块，方便业务快速集成。火山引擎播放器 [官方文档](https://www.volcengine.com/docs/4/52) |
| :library:player-settings | 播放器 options 配置模块 | 方便业务在 App 中调整播放器配置                              |
| :library:player-playback | 播放流程控制模块 | 1. 封装了VideoView 和 VideoLayer，方便客户基于 VideoLayer 实现灵活/高复用的播放UI。2. 封装了 PlaybackController 把一次播放 Session 开始/结束时 Player/VideoView/MediaSource 的相互调用关系串起来。 |
| :library:player | 播放器适配层模块 | 定义了控件层播放器的标准接口，方便适配各种播放器。 |
| :library:player-util | 工具类模块 | 各模块需要的常见工具类如 logcat 输出等 |

## PlayerKit 快速开始

下面用一个简单的例子快速展示 playerkit 的使用方式。完成后即可完成类似西瓜视频单个视频的播放界面，包含各种播放相关功能。 在完成 playerkit 的集成后，以下代码可以直接 copy
到工程中使用。

<video width="720" height="1280" controls>
    <source src="movie.mp4" type="doc/res/video/quick_start_demo.mp4">
</video>

1. 初始化 SDK

> App.java

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        L.ENABLE_LOG = true; // 控件层 logcat 开关

        VolcPlayerInit.AppInfo appInfo = new VolcPlayerInit.AppInfo.Builder()
                .setAppId("229234")
                .setAppName("VOLCVodDemo")
                .setAppRegion("china")
                .setAppChannel("github_channel")
                .setAppVersion(BuildConfig.VERSION_NAME)
                .setLicenseUri("assets:///license2/volc_vod_demo_license2.lic")
                .build();
        VolcPlayerInit.init(this, appInfo);
    }
}
```

* 初始化需要的 APP_ID / LicenseUri 信息请登陆火山引擎点播控制台获取,
  参考官方文档: [管理应用](https://www.volcengine.com/docs/4/65772)
* VolcPlayerInit 中实现了 [快速开始](https://www.volcengine.com/docs/4/65783) 中初始化部分，只需要传入相关初始化参数即可。

2. 使用 VideoView 进行播放

> simple_video_activity.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#000000">

    <com.bytedance.playerkit.player.playback.VideoView
        android:id="@+id/videoView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />

</FrameLayout>
```

> SimpleVideoActivity.java

```java
public class SimpleVideoActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_video_activity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        
        // 1. create VideoView instance
        videoView = findViewById(R.id.videoView);

        // 2. create VideoLayerHost instance. Add Layers to VideoLayerHost.
        VideoLayerHost layerHost = new VideoLayerHost(this);
        layerHost.addLayer(new GestureLayer());
        layerHost.addLayer(new FullScreenLayer());
        layerHost.addLayer(new CoverLayer());
        layerHost.addLayer(new TimeProgressBarLayer());
        layerHost.addLayer(new TitleBarLayer());
        layerHost.addLayer(new QualitySelectDialogLayer());
        layerHost.addLayer(new SpeedSelectDialogLayer());
        layerHost.addLayer(new MoreDialogLayer());
        layerHost.addLayer(new TipsLayer());
        layerHost.addLayer(new SyncStartTimeLayer());
        layerHost.addLayer(new VolumeBrightnessIconLayer());
        layerHost.addLayer(new VolumeBrightnessDialogLayer());
        layerHost.addLayer(new TimeProgressDialogLayer());
        layerHost.addLayer(new PlayPauseLayer());
        layerHost.addLayer(new LockLayer());
        layerHost.addLayer(new LoadingLayer());
        layerHost.addLayer(new PlayErrorLayer());
        layerHost.addLayer(new PlayCompleteLayer());
        layerHost.addLayer(new LogLayer());

        // 3. attach VideoLayerHost to VideoView
        layerHost.attachToVideoView(videoView);

        // 4. config VideoView
        videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW);
        videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT);

        // 5. create PlaybackController and bind VideoView
        PlaybackController controller = new PlaybackController();
        controller.bind(videoView);

        // 6. create MediaSource and bind into VideoView
        MediaSource mediaSource = createDirectUrlMultiQualityMediaSource();
        //MediaSource mediaSource = createDirectUrlSimpleMediaSource();
        //MediaSource mediaSource = createVidMediaSource();
        videoView.bindDataSource(mediaSource);
    }

    /**
     * 创建多分辨率播放源，配合 QualitySelectDialogLayer 可以默认实现清晰度切换功能。
     * 下面使用的 cdn url 可能过期，可以替换成自己的播放源
     */
    private MediaSource createDirectUrlMultiQualityMediaSource() {
        MediaSource mediaSource = new MediaSource(UUID.randomUUID().toString(), MediaSource.SOURCE_TYPE_URL);

        Track track1 = new Track();
        track1.setTrackType(Track.TRACK_TYPE_VIDEO);
        track1.setUrl("http://vod-demo-play.volccdn.com/5c16acfc7441b063c881c42b01bf0621/6320435e/video/tos/cn/tos-cn-v-c91ba9/89742152c58249b4a1195b422be26c39/");
        track1.setQuality(new Quality(Quality.QUALITY_RES_360, "360P"));

        Track track2 = new Track();
        track2.setTrackType(Track.TRACK_TYPE_VIDEO);
        track2.setUrl("http://vod-demo-play.volccdn.com/aa9a0ee27981ce723b57bf6ae2509a3c/6320435e/video/tos/cn/tos-cn-v-c91ba9/435833d5158e4f40962fb8385da0806d/");
        track2.setQuality(new Quality(Quality.QUALITY_RES_480, "480P"));

        Track track3 = new Track();
        track3.setTrackType(Track.TRACK_TYPE_VIDEO);
        track3.setUrl("http://vod-demo-play.volccdn.com/fa181c9eff91ac39c5c067525932db1d/6320435e/video/tos/cn/tos-cn-v-c91ba9/9e8966fbd41547c69615562678506821/");
        track3.setQuality(new Quality(Quality.QUALITY_RES_720, "720P"));

        Track track4 = new Track();
        track4.setTrackType(Track.TRACK_TYPE_VIDEO);
        track4.setUrl("http://vod-demo-play.volccdn.com/df56fb9527bd5099fb8406100c0fe57e/6320435f/video/tos/cn/tos-cn-v-c91ba9/9ef67ccb2b5e43bfbb555b54adf2745d/");
        track4.setQuality(new Quality(Quality.QUALITY_RES_1080, "1080P"));

        mediaSource.setTracks(Arrays.asList(track1, track2, track3, track4));
        return mediaSource;
    }

    /**
     * 快速创建单清晰度播放源
     */
    private MediaSource createDirectUrlSimpleMediaSource() {
        String url = "http://vod-demo-play.volccdn.com/aa9a0ee27981ce723b57bf6ae2509a3c/6320435e/video/tos/cn/tos-cn-v-c91ba9/435833d5158e4f40962fb8385da0806d/";
        // media Id 和 cacheKey 若不指定，内部会自动生成
        return MediaSource.createUrlSource(/*mediaId*/null, url, /*cacheKey*/null);
    }

    /**
     * 快速创建 vid 播放源
     */
    private MediaSource createVidMediaSource() {
        String mediaId = "your video id";
        String playAuthToken = "your video id's playAuthToken";
        return MediaSource.createIdSource(mediaId, playAuthToken);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 7. start playback in onResume
        videoView.startPlayback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 8. pause playback in onResume
        videoView.pausePlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 9. stop playback in onDestroy
        videoView.stopPlayback();
    }

    @Override
    public void onBackPressed() {
        // 10. handle back pressed event
        if (videoView != null && videoView.layerHost().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
```

播放控件层的更多用法请参考 app demo 中的实现。