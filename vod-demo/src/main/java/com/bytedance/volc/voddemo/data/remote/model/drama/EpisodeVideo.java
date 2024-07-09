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

import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.voddemo.data.remote.model.base.BaseVideo;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class EpisodeVideo extends BaseVideo {
    @SerializedName("episodeDetail")
    public EpisodeInfo episodeInfo;
    public EpisodePayInfo episodePayInfo;

    public static boolean isLastEpisode(EpisodeVideo episode) {
        return getEpisodeNumber(episode) >= getTotalEpisodeNumber(episode);
    }

    public static int getEpisodeNumber(EpisodeVideo episode) {
        if (episode == null) return -1;
        if (episode.episodeInfo == null) return -1;
        return episode.episodeInfo.episodeNumber;
    }

    public static int getTotalEpisodeNumber(EpisodeVideo episode) {
        if (episode == null) return -1;
        if (episode.episodeInfo == null || episode.episodeInfo.dramaInfo == null) return -1;
        return episode.episodeInfo.dramaInfo.totalEpisodeNumber;
    }

    public static DramaInfo getDramaInfo(EpisodeVideo episode) {
        if (episode == null) return null;
        if (episode.episodeInfo == null) return null;
        return episode.episodeInfo.dramaInfo;
    }

    @Nullable
    public static String getDramaTitle(EpisodeVideo episode) {
        if (episode == null) return null;
        if (episode.episodeInfo == null || episode.episodeInfo.dramaInfo == null) return null;
        return episode.episodeInfo.dramaInfo.dramaTitle;
    }

    public static String getDramaId(EpisodeVideo episode) {
        if (episode == null) return null;
        if (episode.episodeInfo == null || episode.episodeInfo.dramaInfo == null) return null;
        return episode.episodeInfo.dramaInfo.dramaId;
    }

    public static String getEpisodeDesc(EpisodeVideo episode) {
        if (episode == null) return null;
        if (episode.episodeInfo == null) return null;
        return episode.episodeInfo.episodeDesc;
    }

    public static int episodeNumber2VideoItemIndex(List<VideoItem> videoItems, int episodeNumber) {
        for (int i = 0; i < videoItems.size(); i++) {
            VideoItem videoItem = videoItems.get(i);
            EpisodeVideo episode = EpisodeVideo.get(videoItem);
            if (EpisodeVideo.getEpisodeNumber(episode) == episodeNumber) {
                return i;
            }
        }
        return -1;
    }

    public static List<EpisodeVideo> videoItems2EpisodeVideos(List<VideoItem> videoItems) {
        List<EpisodeVideo> episodes = new ArrayList<>();
        for (VideoItem videoItem : videoItems) {
            EpisodeVideo episode = EpisodeVideo.get(videoItem);
            if (episode != null) {
                episodes.add(episode);
            }
        }
        return episodes;
    }

    public static String dump(EpisodeVideo episode) {
        if (episode == null) return null;
        return L.obj2String(episode) + " " + episode.vid + " " + EpisodeVideo.getDramaId(episode) + " " + EpisodeVideo.getEpisodeNumber(episode) + "/" + EpisodeVideo.getTotalEpisodeNumber(episode);
    }
}


