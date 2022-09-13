# VEVodDemo-android

VEVodDemo-android 是火山引擎视频云点播 SDK Android 端的开源 Demo. 我们提供了
`场景控件层（vod-scenekit）`，来帮助业务快速搭建常见的点播场景。

Demo 实现了常见的三种播放场景：

- 短视频场景（Short Video）- 类似抖音首页竖版视频场景
- 中视频场景（Feed Video）- 类似西瓜视频 Feed 视频流场景
- 长视频场景（Long Video） - 类似爱奇艺/腾讯视频/优酷视频的电视剧/电影场景

针对短视频、中视频场景，我们提供了 `短视频场景控件`、`中视频场景控件` 进一步简化接入。 业务可将 'vod-scenekit'
模块引入工程，添加数据源即可快速搭建播放场景。无需关心播放器如何使用。

# 目录结构

```text
|--VEVodDemo-android
|--|--app               // 主 app （壳工程）
|--|--vod-demo-api      // vod-demo 模块与壳工程交互接口（组件化）
|--|--vod-demo          // 业务层 demo 核心实现
|--|--vod-scenekit      // 场景控件层
|--|--vod-playerkit     // 播放控件层
```

# 编译运行

1. 命令行编译

```shell
cd VEVodDemo-android
git checkout feature/playerkit/dev
./gradlew :app:installdebug
```

2. Android Studio 打开 `VEVodDemo-android` 文件夹，点击运行 `app`.

# 业务接入

我们提供了三种接入方式：

1. [使用场景控件层接入](vod-scenekit/README.md)
2. [使用播放控件层接入](vod-playerkit/README.md)
3. [使用播放器 SDK 接入](https://www.volcengine.com/docs/4/65774)

# Issue

有任何问题可以提交 github issue，我们会定期 check 解决。

# PullRequests

暂不接受 PullRequests。

# License

```text
Copyright 2021 bytedance

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```