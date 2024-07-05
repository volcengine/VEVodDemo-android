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
 * Create Date : 2024/6/28
 */

package com.bytedance.volc.voddemo.ui.minidrama.utils;

import androidx.annotation.Nullable;

import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodePayInfo;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.ui.minidrama.data.mock.MockUser;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DramaPayUtils {
    private static final Set<String> APP_CLIENT_UNLOCKED_SET = Collections.synchronizedSet(new HashSet<>());

    public static boolean isLocked(VideoItem videoItem) {
        return isLocked(EpisodeVideo.get(videoItem));
    }

    public static boolean isLocked(EpisodeVideo episode) {
        if (episode == null) return false;
        if (episode.episodePayInfo == null) return false;
        if (episode.episodePayInfo.payType == EpisodePayInfo.EPISODE_PAY_TYPE_LOCKED) {
            if (APP_CLIENT_UNLOCKED_SET.contains(key(episode))) {
                episode.episodePayInfo.payType = EpisodePayInfo.EPISODE_PAY_TYPE_UNLOCKED;
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public static void unlock(VideoItem videoItem) {
        unlock(EpisodeVideo.get(videoItem));
    }

    public static void unlock(EpisodeVideo episode) {
        if (episode == null) return;
        if (episode.episodePayInfo == null) return;

        episode.episodePayInfo.payType = EpisodePayInfo.EPISODE_PAY_TYPE_UNLOCKED;
        APP_CLIENT_UNLOCKED_SET.add(key(episode));
    }

    public static String key(@Nullable EpisodeVideo episode) {
        if (episode == null) return "";
        if (episode.episodeInfo == null) return "";
        String userId = MockUser.mockUserId(); // TODO use real userid instead
        String dramaId = null;
        if (episode.episodeInfo.dramaInfo != null) {
            dramaId = episode.episodeInfo.dramaInfo.dramaId;
        }
        return "UserId=" + userId + "&DramaId=" + dramaId + "&EpisodeNumber=" + episode.episodeInfo.episodeNumber + "&VideoId=" + episode.vid;
    }
}
