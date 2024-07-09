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
 * Create Date : 2024/7/4
 */

package com.bytedance.volc.voddemo.ui.minidrama.data.mock;

import androidx.annotation.Nullable;

import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodePayInfo;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class MockAppServer {

    private static final Set<String> APP_SERVER_UNLOCKED_SET = Collections.synchronizedSet(new HashSet<>());

    @Deprecated
    public static void notifyPaySuccess(EpisodeVideo episode) {
        if (episode == null) return;
        if (episode.episodePayInfo == null) return;
        APP_SERVER_UNLOCKED_SET.add(key(episode));
    }

    @Deprecated
    public static void mockDramaDetailLockState(List<EpisodeVideo> episodes) {
        if (episodes == null || episodes.isEmpty()) return;
        for (int i = 0; i < episodes.size(); i++) {
            final EpisodeVideo episode = episodes.get(i);
            if (episode.episodePayInfo == null) {
                if (episode.episodeInfo != null && episode.episodeInfo.episodeNumber <= 5) {
                    episode.episodePayInfo = new EpisodePayInfo(EpisodePayInfo.EPISODE_PAY_TYPE_FREE);
                } else {
                    if (APP_SERVER_UNLOCKED_SET.contains(key(episode))) {
                        episode.episodePayInfo = new EpisodePayInfo(EpisodePayInfo.EPISODE_PAY_TYPE_UNLOCKED);
                    } else {
                        episode.episodePayInfo = new EpisodePayInfo(EpisodePayInfo.EPISODE_PAY_TYPE_LOCKED);
                        mockClearEpisodeVideoSourceInfo(episode);
                    }
                }
            }
        }
    }

    @Deprecated
    private static void mockClearEpisodeVideoSourceInfo(EpisodeVideo episode) {
        episode.videoModel = null;
        episode.playAuthToken = null;
    }

    public static String key(@Nullable EpisodeVideo episode) {
        if (episode == null) return "";
        if (episode.episodeInfo == null) return "";
        String dramaId = null;
        if (episode.episodeInfo.dramaInfo != null) {
            dramaId = episode.episodeInfo.dramaInfo.dramaId;
        }
        return "DramaId=" + dramaId + "&EpisodeNumber=" + episode.episodeInfo.episodeNumber + "&VideoId=" + episode.vid;
    }
}
