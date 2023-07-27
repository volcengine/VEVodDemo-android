# PlayerKit 快速开始
下面用一个简单的例子快速展示 PlayerKit 的使用方式。完成后即可完成类似西瓜视频单个视频的播放界面，包含各种播放相关功能。 在完成 PlayerKit 的集成后，以下代码可以直接 copy
到工程中使用。

## 1. 初始化 SDK

> App.java

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        L.ENABLE_LOG = true; // 控件层 logcat 开关

        VolcPlayerInit.AppInfo appInfo = new VolcPlayerInit.AppInfo.Builder()
                .setAppId("your app id")
                .setAppName("your app English name")
                .setAppRegion("china")
                .setAppChannel("your app channel")
                .setAppVersion(BuildConfig.VERSION_NAME)
                .setLicenseUri("your license assets path")
                .build();
        VolcPlayerInit.init(this, appInfo);
    }
}
```

* VolcPlayerInit 中实现了 [快速开始](https://www.volcengine.com/docs/4/65783) 中初始化部分，只需要传入相关初始化参数即可。
* 初始化需要的 appId/appName/licenseUri 等信息请登陆火山引擎点播控制台获取,
  参考官方文档: [管理应用](https://www.volcengine.com/docs/4/65772)

## 2. 使用 VideoView 进行播放

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

> SampleVideoActivity.java

```java
public class SampleVideoActivity extends AppCompatActivity {

    private VideoView videoView;

    public static void intentInto(Activity activity) {
        Intent intent = new Intent(activity, SampleVideoActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_video_activity);

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
        videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
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
     * 下面使用的 url 需要替换成真实的播放地址
     */
    private MediaSource createDirectUrlMultiQualityMediaSource() {
        MediaSource mediaSource = new MediaSource(UUID.randomUUID().toString(), MediaSource.SOURCE_TYPE_URL);

        Track track1 = new Track();
        track1.setTrackType(Track.TRACK_TYPE_VIDEO);
        track1.setUrl("http://example.com/video_360p.mp4");
        track1.setQuality(new Quality(Quality.QUALITY_RES_360, "360P"));

        Track track2 = new Track();
        track2.setTrackType(Track.TRACK_TYPE_VIDEO);
        track2.setUrl("http://example.com/video_480p.mp4");
        track2.setQuality(new Quality(Quality.QUALITY_RES_480, "480P"));

        Track track3 = new Track();
        track3.setTrackType(Track.TRACK_TYPE_VIDEO);
        track3.setUrl("http://example.com/video_720p.mp4");
        track3.setQuality(new Quality(Quality.QUALITY_RES_720, "720P"));

        Track track4 = new Track();
        track4.setTrackType(Track.TRACK_TYPE_VIDEO);
        track4.setUrl("http://example.com/video_1080p.mp4");
        track4.setQuality(new Quality(Quality.QUALITY_RES_1080, "1080P"));

        mediaSource.setTracks(Arrays.asList(track1, track2, track3, track4));
        return mediaSource;
    }

    /**
     * 快速创建单清晰度播放源
     */
    private MediaSource createDirectUrlSimpleMediaSource() {
        String url = "http://example.com/video_480p.mp4"; // 必传
        // media Id 和 cacheKey 若不指定，内部会自动生成
        return MediaSource.createUrlSource(/*mediaId*/null, url, /*cacheKey*/null);
    }

    /**
     * 快速创建 vid 播放源
     */
    private MediaSource createVidMediaSource() {
        String mediaId = "your video id"; // 必传
        String playAuthToken = "your video id's playAuthToken"; // 必传
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