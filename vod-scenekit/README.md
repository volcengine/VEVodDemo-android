# 场景控件层接入

视频场景控件（SceneKit）是火山引擎开源的播放器场景组件。内部使用[播放控件层](../vod-playerkit/README.md)，实现了短视频和中视频的场景控件。

## SceneKit 目录结构

```text
|--gradle-config        // gradle 配置目录
|--vod-playerkit        // 播放控件层根目录
|--vod-scenekit         // 场景控件层根目录
|--vod-settingskit      // 播放设置模块
```

## SceneKit 集成

1. clone git 仓库

```shell
git clone https://github.com/volcengine/VEVodDemo-android
cd VEVodDemo-android
```

2. 拷贝场景控件层模块

拷贝如下几个文件夹到工程根目录下，层级结构与 VEVodDemo-Android 保持一致

```text
gradle-config
vod-playerkit
vod-scenekit
vod-settingskit
```
> 拷贝完成后，建议做一次 git commit，并在 commit message 中记录 VEVodDemo-android 当前最新的 commit id。后续因业务需要可能会更改源码，那这次 commit 就可以起到追溯作用。

3. 确保 project 根目录下的 build.gradle 文件中的 repositories 中配置了`google`、`mavenCentral()` 和 `火山引擎 maven` 服务。

```groovy
allprojects {
    repositories {
        google()
        mavenCentral()
        maven {
            url "https://artifact.bytedance.com/repository/Volcengine/" // 火山引擎 maven 服务
        }
    }
}
```

4. 在 settings.gradle 中引入 SceneKit 模块

```groovy
include ':app'

apply from: file("gradle-config/vod_playerkit_library_settings.gradle")
apply from: file("gradle-config/vod_scenekit_library_settings.gradle")
```

5. 在 App module 的 build.gradle 中引入 SceneKit 依赖

```groovy
// 在 app 的 build.gradle 文件添加 Java 8 支持
android {
    // ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    api project(":vod-scenekit")
}
```

6. App 权限及混淆规则配置
    - 添加点播 SDK 应用权限、混淆规则。参考： [集成准备](https://www.volcengine.com/docs/4/65774)
    - 场景控件无新增权限，混淆规则已配置在 `consumer-rules.pro` 使用方无需关心

7. sync 一下 gradle，AndroidStudio 中模块正确引入，并没有报错则完成集成.

<img src="../doc/res/image/project_include_vod_scenekit.png" width="400">

## SceneKit 快速开始

1. 初始化 SDK

> App.java

```java
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 1. 初始化设置模块
        VideoSettings.init(this); 
        
        // 2. 设置 Logcat & Asserts 开关
        // 开启日志方便排查问题，release 版本一定要关闭
        L.ENABLE_LOG = BuildConfig.DEBUG;
        // 开启断言在内部状态出错时会抛 crash，便于及时发现问题。release 版本一定要关闭。
        Asserts.DEBUG = BuildConfig.DEBUG;
        
        // 3. 初始化配置
        VolcPlayerInit.config(new VolcPlayerInitConfig.Builder()
                .setContext(context)
                .setAppInfo(new AppInfo.Builder()
                        .setAppId("your app id")
                        .setAppName("your app English name")
                        .setAppRegion("china")
                        .setAppChannel("your app channel")
                        .setAppVersion(BuildConfig.VERSION_NAME)
                        .setLicenseUri("your license assets path")
                        .build())
                .build()
        );

        // 4. 调用初始化方法
        VolcPlayerInit.initSync();
    }
}
```

* VolcPlayerInit 中实现了 [快速开始](https://www.volcengine.com/docs/4/65783) 中初始化部分，只需要传入相关初始化参数即可。
* 初始化需要的 appId/appName/licenseUri 等信息请登陆火山引擎点播控制台获取,
  参考官方文档: [管理应用](https://www.volcengine.com/docs/4/65772)

2. 快速实现短视频场景

> ShortVideoActivity.java

```java
public class ShortVideoActivity extends AppCompatActivity {

    private ShortVideoSceneView mSceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSceneView = new ShortVideoSceneView(this);
        mSceneView.setRefreshEnabled(false);
        mSceneView.setLoadMoreEnabled(false);

        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.pageView().setItems(createItems());

        setContentView(mSceneView);
    }

    private static List<VideoItem> createItems() {
        List<VideoItem> videoItems = new ArrayList<>();
        // direct url 播放，传入 videoUrl + coverUrl(可选)
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_0.mp4", "http://www.example.com/cover_0.jpg"));
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_1.mp4", "http://www.example.com/cover_1.jpg"));
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_2.mp4", "http://www.example.com/cover_2.jpg"));
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_3.mp4", "http://www.example.com/cover_3.jpg"));

        // vid 播放，传入 vid + playAuthToken + coverUrl(可选)
        videoItems.add(VideoItem.createVidItem("vid_example_0", "playAuthToken_example_0", "http://www.example.com/cover_0.jpg"));
        videoItems.add(VideoItem.createVidItem("vid_example_1", "playAuthToken_example_1", "http://www.example.com/cover_1.jpg"));
        videoItems.add(VideoItem.createVidItem("vid_example_2", "playAuthToken_example_2", "http://www.example.com/cover_2.jpg"));
        videoItems.add(VideoItem.createVidItem("vid_example_3", "playAuthToken_example_3", "http://www.example.com/cover_3.jpg"));
        return videoItems;
    }
}
```

3. 快速实现中视频场景

中视频场景涉及横竖屏切换，对应 activity 需要在 AndroidManifest 中配置 android:configChanges 属性。`screenSize|orientation` 必须配置。

> AndroidManifest.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".App">
        <activity
            android:name=".FeedVideoActivity"
            android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation|keyboard"
            android:screenOrientation="portrait"
            android:exported="false" />
    </application>
</manifest>
```

> FeedVideoActivity.java

```java
public class FeedVideoActivity extends AppCompatActivity {

    private FeedVideoSceneView mSceneView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSceneView = new FeedVideoSceneView(this);
        mSceneView.setRefreshEnabled(false);
        mSceneView.setLoadMoreEnabled(false);

        mSceneView.pageView().setLifeCycle(getLifecycle());
        mSceneView.pageView().setItems(createItems());

        setContentView(mSceneView);
    }

    @Override
    public void onBackPressed() {
        if (mSceneView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    private static List<VideoItem> createItems() {
        List<VideoItem> videoItems = new ArrayList<>();
        // direct url 播放，传入 videoUrl + coverUrl(可选)
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_0.mp4", "http://www.example.com/cover_0.jpg"));
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_1.mp4", "http://www.example.com/cover_1.jpg"));
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_2.mp4", "http://www.example.com/cover_2.jpg"));
        videoItems.add(VideoItem.createUrlItem("http://www.example.com/video_3.mp4", "http://www.example.com/cover_3.jpg"));

        // vid 播放，传入 vid + playAuthToken + coverUrl(可选)
        videoItems.add(VideoItem.createVidItem("vid_example_0", "playAuthToken_example_0", "http://www.example.com/cover_0.jpg"));
        videoItems.add(VideoItem.createVidItem("vid_example_1", "playAuthToken_example_1", "http://www.example.com/cover_1.jpg"));
        videoItems.add(VideoItem.createVidItem("vid_example_2", "playAuthToken_example_2", "http://www.example.com/cover_2.jpg"));
        videoItems.add(VideoItem.createVidItem("vid_example_3", "playAuthToken_example_3", "http://www.example.com/cover_3.jpg"));
        return videoItems;
    }
}
```

4. 短/中视频场景实现下拉刷新 + 上拉加载(参考 vod-demo 模块中的实现)
    - 短视频场景，参考 [ShortVideoFragment](../vod-demo/src/main/java/com/bytedance/volc/voddemo/ui/video/scene/shortvideo/ShortVideoFragment.java)
    - 中视频场景，参考 [FeedVideoFragment](../vod-demo/src/main/java/com/bytedance/volc/voddemo/ui/video/scene/feedvideo/FeedVideoFragment.java)