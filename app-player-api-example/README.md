# 火山引擎播放器 SDK - API Example

提供火山引擎播放器 SDK API 示例代码，旨在示例播放器 SDK 的使用方式，帮助您快速熟悉 API 加速完成集成工作。

## 示例功能
### 快速开始
  - [初始化 SDK 示例](src/main/java/com/bytedance/vodplayer/example/quickstart/InitSDKExample.kt)：默认展示同步初始化，并复用同一套初始化任务
  - [回调式异步初始化](src/main/java/com/bytedance/vodplayer/example/quickstart/VodSdkInitializer.java)：在专用后台线程执行初始化，并通过回调提供播放器就绪门禁
  - [Kotlin 协程异步初始化](src/main/java/com/bytedance/vodplayer/example/quickstart/VodSdkCoroutineInitializer.kt)：把同一个回调式初始化结果转换为可取消的挂起函数
  - [Vid 源示例](src/main/java/com/bytedance/vodplayer/example/quickstart/VidSourceExampleActivity.kt)：展示 Vid 播放源播放流程，清晰度获取和选择
  - [Direct URL 示例](src/main/java/com/bytedance/vodplayer/example/quickstart/DirectUrlSourceExampleActivity.kt)：展示 Direct Url 播放源播放流程
### 基本功能
  - [基础播放示例](src/main/java/com/bytedance/vodplayer/example/features/BasicPlaybackExampleActivity.kt)：展示播放控制、音量调节、网速获取等 API 使用方式。
  - [显示模式示例](src/main/java/com/bytedance/vodplayer/example/features/DisplayModeExampleActivity.kt)：展示 View 比例与视频比例不一致时，如何使用显示模式 API。
  - [清除视频缓存示例](src/main/java/com/bytedance/vodplayer/example/MainActivity.kt)
### 最佳实践
  - [抖音同款短视频最佳实践示例](src/main/java/com/bytedance/vodplayer/example/bestpractice/ShortVideoBestPracticeExampleActivity.kt)：展示预加载+预渲染策略、播放提前、播放器异步调用、设置封面图等最佳实践策略。
### 进阶功能
  - [Debug 工具示例](src/main/java/com/bytedance/vodplayer/example/advanced/DebugToolExampleActivity.kt)：展示 Debug 工具的集成。
  - [Direct URL 字幕示例](src/main/java/com/bytedance/vodplayer/example/advanced/subtitle/DirectUrlSubtitleExampleActivity.kt)：展示 DirectUrl 字幕，字幕语言切换
  - [Vid 字幕示例](src/main/java/com/bytedance/vodplayer/example/advanced/subtitle/VidSubtitleExampleActivity.kt)：展示 Vid 字幕，字幕语言切换

## 编译运行
1. Demo 需要设置 AppId 和 License 才能成功运行，否则会抛出异常。 请联系火山引擎商务获取体验 License 文件和 AppId。获取到 License 文件后请将 License 放置在 app 的 assets 文件夹中。
设置方式：
修改 [App.kt](src/main/java/com/bytedance/vodplayer/example/App.kt) 
```kotlin
class App : Application() {
    
    companion object {
        const val APP_ID: String = "your app id" // 替换为您申请的 AppId
        const val LICENSE_URI: String = "your license assets uri" // 替换为您购买的 License 地址
    }
}
```
2. Android Studio 打开 VEVodDemo-android 文件夹，点击运行 app-player-api-example.

## SDK 初始化

API Example 默认使用同步初始化：

```kotlin
// Application.onCreate
InitSDKExample.initSync()
```

同步调用返回后可直接进入播放器页面，不需要初始化回调或页面门禁。

SDK 初始化可能包含 License、动态库、播放内核和缓存模块初始化。如果启动耗时不满足业务预算，可以按需改用下面任一种异步方式。异步初始化与同步初始化不能在同一进程同时调用，修改后需要冷启动 App。

### 回调式异步初始化

在 `Application.onCreate` 中启动初始化：

```kotlin
InitSDKExample.initAsync { success, error ->
    if (!success) {
        Log.e("VodSdkInitializer", "Vod SDK initialization failed", error)
    }
}
```

异步初始化必须配合就绪门禁：

1. `Application` 尽早发起初始化，但不等待结果。
2. 初始化成功前，不创建 `TTVideoEngine`，不调用预加载、预渲染或下载。
3. 页面通过 `VodSdkInitializer.runWhenReady()` 提交一次业务动作，回调统一在主线程执行。
4. 初始化失败时上报错误并 `return`，阻断本次播放器操作；不要在同一进程重复初始化。

```kotlin
private var readyRequest: VodSdkInitializer.ReadyRequest? = null

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readyRequest = VodSdkInitializer.runWhenReady(
        this,
        Runnable { enterVodFeature() }
    ) { error ->
        reportInitFailure(error)
    }
}

override fun onDestroy() {
    readyRequest?.cancel()
    readyRequest = null
    super.onDestroy()
}
```

`enterVodFeature()` 是业务占位方法，可以表示进入播放页、创建播放器、预加载、预渲染或下载。`ReadyRequest.cancel()` 只取消这一次尚未执行的业务动作，不停止全局 `Env.start()`，也不影响其他等待方。传入 `LifecycleOwner` 时，请求会在 `ON_DESTROY` 自动取消；传 `null` 时仍在主线程执行，但调用方应避免捕获页面对象，或自行保存并取消请求。

如果调用方已经位于主线程，且初始化状态已经是 `SUCCESS` 或 `FAILED`，结果会在当前调用栈原地执行，不再额外 `Handler.post()`；从后台线程调用或等待中的初始化完成时，结果仍会切换到主线程。调用方不应依赖回调一定晚于 `runWhenReady()` 返回。

统一入口已经完成门禁后，同一进程内已有播放器的 `setSurface`、`prepare`、`play`、`pause`、`seekTo` 等操作无需重复门禁。只有可能在初始化完成前独立触发的播放器创建、预加载、预渲染、下载等根入口，才复用 `runWhenReady()`。不要为了保留同步返回值而实现一个缓存所有 Engine 命令的延迟播放器代理，这会显著增加状态、返回值和 Surface 生命周期的一致性风险。

如果业务还需要初始化播放策略，应保持 SDK 要求的调用顺序，并把整段初始化工作放在同一个后台任务中。API Example 的顺序是：

```text
StrategyManager 预配置 -> Env.start(config) -> ABR 初始化 -> 成功回调
```

不要在主线程使用 `Future.get()`、`CountDownLatch.await()`、`Thread.join()` 或循环查询等待初始化完成。

### Kotlin 协程异步初始化

协程版本复用 `VodSdkInitializer`，不创建第二套线程或初始化状态。`Application` 只负责启动：

```kotlin
VodSdkCoroutineInitializer.start()
```

页面通过 `lifecycleScope` 等待：

```kotlin
lifecycleScope.launch {
    try {
        VodSdkCoroutineInitializer.awaitReady()
        enterVodFeature()
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        reportInitFailure(error)
    }
}
```

页面销毁时，`lifecycleScope` 会自动取消等待。取消只移除当前页面的订阅，已经开始的 `Env.start()` 会继续执行，其他等待方不受影响。

不要再套 `withContext(Dispatchers.IO)`。后台串行线程由 `VodSdkInitializer` 统一管理，协程这里只负责用结构化并发管理页面等待和取消。

## 官网文档
- [集成 SDK](https://www.volcengine.com/docs/4/65774)
- [快速开始](https://www.volcengine.com/docs/4/112130)
- [基础功能](https://www.volcengine.com/docs/4/65784)
- [发布历史](https://www.volcengine.com/docs/4/66437)
