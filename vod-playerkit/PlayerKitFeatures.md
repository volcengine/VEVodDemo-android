# PlayerKit 功能使用

## 创建播放流程控制器
PlaybackController 负责控制一次播放流程的开始和结束，提供 startPlayback 和 stopPlayback 控制开始播放、结束播放。
```java
PlaybackController playbackController = new PlaybackController();
```

## 创建视频控件
VideoView 负责渲染视频内容，管理 VideoLayer。

### 1. 创建 VideoView
```java
// import com.bytedance.playerkit.player.playback.VideoView;
videoView = findViewById(R.id.videoView);
```

### 2. 添加 VideoLayer
播放控件层使用 VideoLayer 来实现视频界面上的各种 UI 元素展示。VideoLayer 是一套非常强大灵活的系统。VideoLayer 特性如下：
1. VideoLayer 本质是个 ViewHolder，提供了 createView/show/dismiss 方法来控制 View 的创建、展示、隐藏。
   * show + createView 方法提供 View 懒加载机制，保证 View 在需要展示的时候再去创建，节省内存。
   * dismiss 不是将 View 简单 gone 掉，而是将 View 从父 View 中移除，保证了视图树的简洁, 提升流畅性。
2. VideoLayer 能接收来自 PlaybackController/Player/VideoView 的事件 + 回调，利用这些丰富的事件来展示 View 状态。
3. VideoLayer 实现的 UI 浮层，可复用，可动态插拔。
4. VideoLayer 能简单安全拿到 PlaybackController/Player/VideoView/Context 实例，也能拿到其他 VideoLayer 实例。
5. VideoLayer 在 VideoLayerHost 中的添加顺序也是浮层 Z 轴顺序以及 Event 事件接收顺序。
```java
VideoLayerHost layerHost = new VideoLayerHost(context);
layerHost.addLayer(new CoverLayer()); // 封面 Layer
layerHost.addLayer(new LoadingLayer()); // loading 圈 Layer
layerHost.addLayer(new PauseLayer()); // 暂停按钮 Layer
if (BuildConfig.DEBUG){
    layerHost.addLayer(new LogLayer()); // log 浮层，能展示信息方便开发
}
layerHost.attachToVideoView(videoView);
```
vod-playerkit 模块没有 VideoLayer 的具体实现。在 vod-scenekit 模块中提供了常见的播放浮层的 Layer 实现，接入方可以直接引用到项目中，按需修改 UI 样式即可。
* 短视频场景参考 ShortVideoAdapter.ViewHolder.createVideoView 方法。
* 中/长视频场景参考 FeedVideoAdapter.ViewHolder.initVideoView 方法。

### 3. 配置 VideoView
```java
// 设置渲染 View 类型。
// 支持 DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW 和 DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW，
// 推荐使用 DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW 兼容性好
videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);

// 设置显示模式。
// DisplayModeHelper.DISPLAY_MODE_DEFAULT; // 可能会变形；画面宽高都充满控件；画面不被裁剪；无黑边
// DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL_X; // 无变形；画面宽充满控件，高按视频比例适配；画面可能被裁剪；可能有黑边。
// DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL_Y; // 无变形；画面高充满控件，宽按视频比例适配；画面可能被裁剪；可能有黑边。
// DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT; // 无变形；画面长边充满控件，短边按比例适配；画面不被裁剪；可能有黑边
// DisplayModeHelper.DISPLAY_MODE_ASPECT_FILL; // 无变形；画面短边充满控件，长边按比例适配；画面可能被裁剪；无黑边
videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT);

// 设置视频场景。
// 支持的场景见 PlayScene 常量类，用于 VideoLayer 中根据场景展示不同状态。
videoView.setPlayScene(PlayScene.SCENE_SHORT);
```

### 4. 绑定 VideoView
```java
playbackController.bind(videoView);
```

## 创建播放源
> 参考 VideoItem.toMediaSource 方法实现

### 1. 创建 VID 播放源
```java
String mediaId = "your video id"; // 必传
String playAuthToken = "your video id's playAuthToken"; // 必传
MediaSource mediaSource = MediaSource.createIdSource(mediaId, playAuthToken);
```

### 2. 创建 URL 播放源

单个清晰度
```java
String mediaId = "your video id"; // 选传.
String url = "http://example.com/video_480p.mp4?expired=160898989089"; // 必传
String cacheKey = MD5.md5(new URL(url).getPath()); // 选传，一般使用 url 去掉时间戳的 path 部分作为缓存 key
// media Id 和 cacheKey 若传 null，内部会自动生成
MediaSource mediaSource = MediaSource.createUrlSource(mediaId, url, cacheKey);
```

多个清晰度
```java
String mediaId = "your video id";
MediaSource mediaSource = new MediaSource(mediaId, MediaSource.SOURCE_TYPE_URL);

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
```

### 3. 创建 VideoModel 播放源
```java
String mediaId = "your video id"; // 必传
String videoModelJson = "your video model json"; // 必传
MediaSource mediaSource = MediaSource.createModelSource(mediaId, videoModelJson);
```

## 全局续播
Player 内部会自动记录播放进度，参考 AVPlayer 的 recordProgress 方法。起播前 Player 会用 MediaSource.getSyncProgressId() 作为 key 在 ProgressRecorder 中寻找续播记录，如果找到则将通过 setStartTime 设置给播放器，以记录的进度起播起播。所以对于需要全局续播的 case，只需要给 MediaSource 实例设置稳定的 syncProgressId 即可。 
```java
mediaSource.setSyncProgressId(mediaSource.getMediaId()); // 开启全局续播
mediaSource.setSyncProgressId(null); // 关闭全局续播
```

## 设置播放源
> 参考 ShortVideoAdapter 和 FeedVideoAdapter 实现
```java
videoView.bindDataSource(mediaSource);
```

## 监听播放事件
PlaybackController 支持设置 EventListener 监听 PlaybackController/Player 的状态。具体事件常量定义，请参考常量类 PlayerEvent 和 PlaybackEvent。监听播放状态如果是为了实现播放器上某个 View 浮层, 建议使用 VideoLayer，在 VideoLayer 的 onBindPlaybackController/onUnbindPlaybackController 方法中可进行监听器的注册与反注册。
* 播放器事件 PlayerEvent
    * 播放器行为事件 PlayerEvent.Action
    * 播放器状态事件 PlayerEvent.State
    * 播放器信息事件 PlayerEvent.Info
* 播放事件 PlaybackEvent
    * 播放行为事件 PlaybackEvent.Action
    * 播放状态事件 PlaybackEvent.State

```java
final EventListener eventListener = new EventListener() {
    
    @Override
    public void onEvent(Event event) {
        final int code = event.code; // Event 类型码
        // 下面列举常见 Event 码，更多请参考 PlayerEvent 类和 PlaybackEvent 类
        switch(code) {
            case PlaybackEvent.Action.START_PLAYBACK:
                // 调用 PlaybackController.startPlayback() 后触发
                break;
            case PlaybackEvent.ACTION.STOP_PLAYBACK:
                // 调用 PlaybackController.stopPlayback() 后触发
                break;
            case PlayerEvent.Action.PREPARE:
                // 调用 AVPlayer.prepare 后触发
                break;
            case PlayerEvent.State.PREPARED:
                // 播放器准备完成回调
                break;
            case PlayerEvent.Info.VIDEO_RENDERING_START:
                // 播放器首帧渲染回调
                break;
        }
    }
};
// 设置监听
playbackController.addPlaybackListener(eventListener);
// 移除监听
playbackController.removePlaybackListener(eventListener);
```

## TTVideoEngine Option 配置
在 VolcConfig/VolcGlobalConfig 中看是否能找到配置，若找不到就需要修改 vod-playerkit 模块源码。

### TTVideoEngine 实例 Option
在 TTVideoEngineFactory 添加。若需要灵活控制，可在 VolcConfig 中添加配置。

### TTVideoEngine 全局 Option
在 VolcPlayerInit 中，添加在 Env.init 前即可。若需灵活控制，可在 VolcConfigGlobal 中添加静态变量控制。

## 定制 VideoLayer
VideoLayer 机制非常灵活，当业务遇到需求时可以先看下 vod-scenekit 中是否提供了示例实现。业务可以修改 Layer 中的 UI，复用消息和控制逻辑。

### 使用方式
目前 vod-scenekit 中示例了三种使用方式：
1. View 浮层。VideoLayer 带 Layout，接收 PlaybackController 中的消息展示不同的状态，Layout 本身也生成各种 touch 事件，控制自身和其他 Layer 的状态或者播放器的状态。比如：PlayPauseLayer。
2. 播放属性控制。VideoLayer 的 onBindPlaybackController/onUnbindPlaybackController 方法非常适合 PlaybackController 的 EventListener 注册与反注册。 PlayerEvent.Action 中提供了对 Player 播控方法的 Action 调用事件，可以动态拦截相关事件修改 Player 的属性。比如：PlayerConfigLayer 中实现循环播放。
3. 播放日志记录。VideoLayer 能获取到 PlaybackController/Player/VideoView/MediaSource 和其他 VideoLayer 的实例和事件，非常适合用于日志记录。比如：LogLayer

### 动画浮层
视频浮层的显示隐藏都需要带有动画，动画对于 VideoLayer 是一个基础且通用的使用场景。所以在 vod-scenekit 中抽象出了 AnimateLayer 作为动画浮层基类。
1. 默认为 alpha 显示/alpha 隐藏动画，可通过重写 createAnimator/resetViewAnimateProperty/initAnimateShowProperty/initAnimateDismissProperty 来定制动画。示例：PauseLayer
2. 可以通过调用 animateShow/animateDismiss 触发动画，通过 isAnimateShowing/isAnimateDismissing 来判断动画是否正在执行。

### Dialog 浮层
根据 UI 浮层的显示特性，抽象出了 DialogLayer。DialogLayer 相比普通 VideoLayer，拓展了：
1. DialogLayer 显示，会自动隐藏普通的 VideoLayer。
2. DialogLayer 具有回退栈，可以拦截 onBackPressed 事件。需要依赖在 Activity/Fragment 的 onBackPressed 方法中调用 mVideoView.layerHost().onBackPressed()。

```java
public class MainActivity extends AppCompatActivity {
    private VideoView mVideoView;

    @Override
    public void onBackPressed() {
        // VideoLayer 层拦截 Activity 的返回事件，用于处理 VideoLayer 内部的返回逻辑
        if (mVideoView.layerHost().onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}

```

### 手势管理浮层
播放界面区域有丰富的手势控制，GestureLayer 作为手势管理器，实现了如下功能：
* 播放区域左右滑调节进度
* 左右区域上下滑调节亮度和音量
* 播放区域双击暂停
* 播放区域单点呼起/隐藏播控浮层

如果你需要定制更多功能，可以参考或修改 GestureLayer 来完成你的需求。需要注意的是 GestureLayer 手势的优先级是最低的，添加 Layer 的时候第一个添加，确保 GestureLayer View 的 Z index 处于最底层。

### 浮层交互
* 与 Player 交互
  * VideoLayer -> Player: VideoLayer 通过 player() 方法可以直接获取 Player 实例。
  * Player -> VideoLayer: VideoLayer 通过 PlaybackController.addPlaybackListener 来获取 Player Action/State/Info 类型的状态。

* 与 PlaybackController 交互
  * VideoLayer -> PlaybackController: VideoLayer 通过 controller() 方法可以获取到 PlaybackController 的实例。
  * PlaybackController -> VideoLayer: VideoLayer 通过 PlaybackController.addPlaybackListener 来获取 PlaybackController Action/State 类型的状态。

* 与 VideoView 交互
  * VideoLayer -> VideoView: VideoLayer 通过 videoView() 方法可以获取到 VideoView 的实例。
  * VideoView -> VideoLayer: VideoLayer 实现了 VideoViewListener，当 VideoLayerHost 添加到 VideoView 中后，VideoLayerHost 中的所有 VideoLayer 都会监听 VideoView 的 VideoViewListener。

* 与 MediaSource 交互
  * VideoLayer -> MediaSource: VideoLayer 通过 dataSource() 方法可以获取到 MediaSource 实例。
  * MediaSource -> VideoLayer: 当 VideoView.bindDataSource 调用后，VideoLayer 会收到 onVideoViewBindDataSource 回调。

* 浮层间交互
  * VideoLayer 通过 layerHost().findLayer(VideoLayer.class)/findLayer(tag) 方法可以找到其他 VideoLayer 实例。
  * VideoLayer 通过 notifyEvent(code, object)/handleEvent(code, object) 方法可在 VideoLayer 间发送和接收消息。

### 播放属性控制
* Q：使用播放控件层，我们推荐使用 PlaybackController 的 startPlayback 和 stopPlayback 来开始播放和结束播放流程。PlaybackController 的 startPlayback 封装了 Player 的起播流程，但 PlaybackController 是 final 的不支持拓展。如果业务想在播放器 create/prepare/start 前后改变播放器的属性，比如倍速、循环播放等，该如何实现呢？
* A：PlaybackController / Player 都实现了 Action 事件，可以在 Action 事件中处理处理。参考：PlayerConfigLayer 在 PlayerEvent.Action.Prepare 中给播放器设置循环播放。

## 播放控制
AVPlayer 是强状态机实现，在不合理的时机调用播控方法会抛异常，PlaybackController 提供的播控方法 

> 参考 ShortVideoPageView 和 PlayPauseLayer 实现
```java
playbackController.startPlayback(); // 开始播放
playbackController.pausePlayback(); // 暂停播放
playbackController.stopPlayback();  // 停止播放

// VideoView 代理了 playbackController 的播控方法，方便调用
videoView.startPlayback();
videoView.pausePlayback();
videoView.stopPlayback();

// VideoLayer 代理了 playbackController 的播控方法，方便调用
videoLayer.startPlayback();
videoLayer.pausePlayback();
videoLayer.stopPlayback();
```

## 播放器实例获取
```java
// 从 PlaybackController 对象获取
Player player = playbackController.player();

// 从 VideoView 对象获取
Player player = videoView.player();

// 从 VideoLayer 对象获取
Player player = videoLayer.player();

// 从 PlayerPool 中获取
Player player = PlayerPool.get(mediaSource);
```
Tips：
* Player 实例是在 PlaybackController 的 startPlayback 方法中创建的。

## Seek
> 参考 SimpleProgressBarLayer 的实现
```java
player.seekTo(1000L); // seek 到 1秒
```

## 获取播放信息

### 获取视频宽高
```java
int videoWidth = player.getVideoWidth();
int videoHeight = player.getVideoHeight();
```

### 获取视频时长
```java
long durationInMS = player.getDuration(); // 单位：MS
```

### 获取播放进度
> 参考 SimpleProgressBarLayer 实现

主动获取播放进度
```java
long currentPositionInMS = player.getCurrentPosition(); // 单位：MS
```

接收播放进度回调
```java
playbackController.addPlaybackListener(new EventListener() {
    
    @Override
    public void onEvent(Event event) {
        switch(event.code()) {
            case PlayerEvent.Info.PROGRESS_UPDATE:{
                final InfoProgressUpdate e = event.cast(InfoProgressUpdate.class);
                long currentPositionInMS = e.currentPosition;
                long durationInMS = e.duration;
                break;
            }
        }
    }
});
```

### 获取缓存进度
> 参考 SimpleProgressBarLayer 实现

主动获取缓存进度
```java
long bufferedDurationInMs = player.getBufferedDuration(); // 获取缓存时长
int bufferedPercent = player.getBufferedPercentage(); // 获取缓存百分比 0-100
```

接收缓存进度回调
```java
playbackController.addPlaybackListener(new EventListener() {
    @Override
    public void onEvent(Event event) {
        switch (event.code()) {
            case PlayerEvent.Info.BUFFERING_UPDATE: {
                InfoBufferingUpdate e = event.cast(InfoBufferingUpdate.class);
                int bufferedPercent = e.percent; // 缓存百分比 0-100
                break;
            }
        }

    }
});
```

## 循环播放
> 参考 PlayerConfigLayer 和 MoreDialogLayer 实现
```java
player.setLooping(true); // 开启循环播放
boolean isLooping = player.isLooping(); // 获取循环播放状态
```

## 倍速播放
> 参考 SpeedSelectDialogLayer 实现
```java
player.setSpeed(1f); // 默认1倍速，取值范围(0, 3]
float speed = pllayer.getSpeed();
```

## 设置音量
### 静音
```java
player.setMuted(true);
boolean isMuted = player.isMuted();
```

### 调节音量
> 参考 VolumeBrightnessDialogLayer 实现
```java
// 音量调节默认调节系统音量，若需要调节播放器音量，在调用 startPlayback 之前设置
VolcConfig volcConfig = new VolcConfig();
volcConfig.enableAudioTrackVolume = true;
VolcConfig.set(meddiaSource, volcConfig);

player.setVolume(1f, 1f); // 音量范围 [0-1f]
float[] volume = player.getVolume(); // 音量获取
float leftVolume = volume[0];
float rightVolume = volume[1];
```

## 设置清晰度
### 起播清晰度设置
> 参考 VodSDK.init 方法实现
```java

final int qualityRes = Quality.QUALITY_RES_720; // 设置起播清晰度为 720P

final TrackSelector trackSelector = new TrackSelector() {
    @NonNull
    @Override
    public Track selectTrack(int type, int trackType, @NonNull List<Track> tracks, @NonNull MediaSource source) {
        for (Track track : tracks) {
            Quality quality = track.getQuality();
            if (quality != null) {
                if (quality.getQualityRes() == qualityRes) {
                    return track;
                }
            }
        }
        return tracks.get(0);
    }
};

VolcPlayerInit.init(context, appInfo, CacheKeyFactory.DEFAULT, trackSelector, new VolcSubtitleSelector());
```

### 播放中切换清晰度
> 参考 QualitySelectDialogLayer 实现
```java
player.selectTrack(track);
```

### 清晰度获取
```java
@TrackType
final int trackType = Track.TRACK_TYPE_VIDEO;
Track currentTrack = player.getCurrentTrack(trackType); // 获取当前播放清晰度
Track selectedTrack = player.getSelectedTrack(trackType); // 获取选择的清晰度
List<Track> tracks = player.getTracks(trackType); // 获取清晰度列表
```

### 清晰度事件回调
> 参考 QualitySelectDialogLayer 实现
```java
playbackController.addPlaybackListener(new Dispatcher.EventListener() {
    @Override
    public void onEvent(Event event) {
        switch (event.code()) {
            case PlayerEvent.Info.TRACK_INFO_READY: {
                InfoTrackInfoReady e = event.cast(InfoTrackInfoReady.class);
                @Track.TrackType
                int trackType =  e.trackType; // track 类型
                List<Track> tracks = e.tracks; // track 列表
                break;
            }
            case PlayerEvent.Info.TRACK_WILL_CHANGE: {
                InfoTrackWillChange e = event.cast(InfoTrackWillChange.class);
                @Track.TrackType
                int trackType =  e.trackType; // track 类型
                Track current = e.current; // 当前播放清晰度
                Track target = e.target; // 目标切换清晰度
                break;
            }
            case PlayerEvent.Info.TRACK_CHANGED: {
                InfoTrackChanged e = event.cast(InfoTrackChanged.class);
                int trackType =  e.trackType; // track 类型
                Track current = e.current; // 当前播放清晰度
                Track pre = e.pre; // 切换前的清晰度
                break;
            }
        }
    }
});
```

## 设置业务类型/自定义标签
在创建 MediaSource 后，startPlayback 调用前设置
```java
VolcConfig volcConfig = new VolcConfig();
volcConfig.tag = "your tag";
volcConfig.subtag = "your subtag";
VolcConfig.set(meddiaSource, volcConfig);
```

## 屏幕长亮
调用 View#setKeepScreenOn(boolean) 设置屏幕保持常亮，View#getKeepScreenOn 获取是否保持常亮。VideoView 中默认已经实现。


