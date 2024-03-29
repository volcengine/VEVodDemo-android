/*
 * Copyright (C) 2023 bytedance
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
 * Create Date : 2023/5/26
 */

package com.bytedance.playerkit.player.volcengine;

import android.view.ViewGroup;

import com.ss.ttvideoengine.debugtool2.DebugTool;

public class VolcDebugTools {

    // 添加展示 debug 信息的布局，debug 信息页面撑满 containerView
    // 需要在调用 Engine 播放之前设置，
    // 设置布局后，Debug 工具会监听哪个 Engine 实例调用了 play，并将相关信息显示到布局。
    public static void setContainerView(ViewGroup containerView) {
        DebugTool.release();
        DebugTool.setContainerView(containerView);
    }

    // 完成 Debug 工具使用时，您可调用release()方法释放资源
    public static void release() {
        DebugTool.release();
    }
}
