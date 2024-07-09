/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/3/26
 */

package com.bytedance.volc.voddemo.data.remote.model.drama;

import java.io.Serializable;

public class DramaInfo implements Serializable {
    public String dramaId; // 剧 id
    public String dramaTitle; // 剧名
    public String description; // 剧描述
    public String coverUrl; // 剧封面
    public int totalEpisodeNumber; // 集数
    public int latestEpisodeNumber; // 更新集数
    public String authorId;

    public static String dump(DramaInfo dramaInfo) {
        if (dramaInfo == null) return null;
        return dramaInfo.dramaId + " " + dramaInfo.dramaTitle + " " + dramaInfo.totalEpisodeNumber;
    }
}
