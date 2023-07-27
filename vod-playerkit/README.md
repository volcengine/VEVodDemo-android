# 播放控件层接入

播放控件层（PlayerKit）是火山引擎开源的播放器 UI 控件，主要功能如下：

1. 封装了[火山引擎播放器 SDK](https://www.volcengine.com/docs/4/65774)，屏蔽了播放器的使用细节。
2. 提供了 VideoView 控件，使用者只需要关注 View 层的实现即可。
3. 基于 VideoView 提供了播放界面浮层管理，方便基于 VideoLayer 实现灵活、可复用的播放 UI。
4. 集成了默认风格的播放界面，帮助快速搭建播放场景。（可选）

## PlayerKit 目录结构

```text
|--gradle-config              // gradle 配置目录
|--vod-playerkit              // 播放控件层
|--|--vod-player              // 播放器接口层（定义了一套标准播放器接口）
|--|--vod-player-volcengine   // 火山引擎播放器实现层
|--|--vod-player-utils        // 工具类模块
```

| 模块                    | 描述         | 是否必须  | 介绍                                                                                                                                                                                       |
|:----------------------|:-----------|:------|:-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| vod-player            | 播放器接口层     | 必须    | 1. 定义了控件层播放器的标准接口，方便适配各种播放器。<br>2. 封装了VideoView 和 VideoLayer，方便客户基于 VideoLayer 实现灵活/高复用的播放UI。<br>3. 封装了 PlaybackController 把一次播放 Session 开始/结束时 Player/VideoView/MediaSource 的相互调用关系串起来。 |
| vod-player-volcengine | 火山引擎播放器实现层 | 必须    | 1. 用播件层的播放器接口，实现了火山引擎播放器.<br>2. 封装了火山引擎播放器初始化模块，方便业务快速集成。火山引擎播放器 [官方文档](https://www.volcengine.com/docs/4/52)                                                                            |                                                                            |
| vod-player-util       | 工具类模块      | 必须    | 各模块需要的常见工具类如 logcat 输出等                                                                                                                                                                  |

## PlayerKit 集成准备

1. clone git 仓库

```shell
git clone https://github.com/volcengine/VEVodDemo-android
cd VEVodDemo-android
```

2. 拷贝控件层模块

拷贝如下几个文件夹到工程根目录下，层级结构与 VEVodDemo-Android 保持一致
```text
gradle-config
vod-playerkit
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

4. 在 settings.gradle 中引入 PlayerKit 模块

```groovy
include ':app'

apply from: file("gradle-config/vod_playerkit_library_settings.gradle")
```

5. 在 App module 的 build.gradle 中引入 PlayerKit 依赖

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
    api project(":vod-playerkit:vod-player")
    api project(":vod-playerkit:vod-player-utils")
    api project(":vod-playerkit:vod-player-volcengine")
}
```

6. App 权限及混淆规则配置
- 添加点播 SDK 应用权限、混淆规则。参考： [集成准备](https://www.volcengine.com/docs/4/65774)
- 场景控件无新增权限，混淆规则已配置在 `consumer-rules.pro` 使用方无需关心

7. sync 一下 gradle，AndroidStudio 中 vod-playerkit 模块正确引入，并没有报错则完成集成.

<img src="../doc/res/image/project_include_vod_playerkit.png" width="400">

## PlayerKit 快速开始
* [PlayerKit 快速开始](../vod-playerkit/PlayerKitQuickStart.md)

## PlayerKit 功能使用
* [PlayerKit 功能使用](../vod-playerkit/PlayerKitFeatures.md)

播放控件层的用法示例请参考 [场景控件层](../vod-scenekit/README.md) 的实现。