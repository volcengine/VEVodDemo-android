# 火山引擎播放器 SDK - API Example

提供火山引擎播放器 SDK API 示例代码，旨在示例播放器 SDK 的使用方式，帮助您快速熟悉 API 加速完成集成工作。

## 示例功能
### 快速开始
  - [初始化 SDK 示例](src/main/java/com/bytedance/vodplayer/example/quickstart/InitSDKExample.kt)：展示播放器 SDK 初始化流程
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

## 官网文档
- [集成 SDK](https://www.volcengine.com/docs/4/65774)
- [快速开始](https://www.volcengine.com/docs/4/112130)
- [基础功能](https://www.volcengine.com/docs/4/65784)
- [发布历史](https://www.volcengine.com/docs/4/66437)