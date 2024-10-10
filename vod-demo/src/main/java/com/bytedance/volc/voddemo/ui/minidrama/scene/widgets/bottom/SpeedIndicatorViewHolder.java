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
 * Create Date : 2024/7/8
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.bottom;

import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.VideoView;
import com.bytedance.volc.vod.scenekit.ui.video.scene.shortvideo.ShortVideoSceneView;
import com.bytedance.volc.vod.scenekit.ui.widgets.adatper.ViewHolder;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.layer.DramaGestureLayer;
import com.bytedance.volc.voddemo.ui.minidrama.scene.widgets.viewholder.DramaEpisodeVideoViewHolder;

import java.io.IOException;
import java.util.Locale;

public class SpeedIndicatorViewHolder implements DramaGestureLayer.DramaGestureContract {
    private final ShortVideoSceneView mSceneView;
    private final View mSpeedIndicatorView;
    private final TextView mSpeedDescView;
    private final ImageView mSpeedArrow;

    public SpeedIndicatorViewHolder(View view) {
        mSceneView = view.findViewById(R.id.shortVideoSceneView);
        mSpeedIndicatorView = view.findViewById(R.id.bottomBarCardSpeedIndicator);
        mSpeedDescView = view.findViewById(R.id.speedDesc);
        mSpeedArrow = view.findViewById(R.id.speedArrow);
        showSpeedIndicator(false);
    }

    @Override
    public boolean isSpeedIndicatorShowing() {
        return mSpeedIndicatorView.getVisibility() == View.VISIBLE;
    }

    @Override
    public void showSpeedIndicator(boolean show) {
        if (show) {
            final ViewHolder viewHolder = mSceneView.pageView().getCurrentViewHolder();
            if (!(viewHolder instanceof DramaEpisodeVideoViewHolder)) {
                return;
            }
            VideoView videoView = ((DramaEpisodeVideoViewHolder) viewHolder).videoView;
            if (videoView == null) return;
            final Player player = videoView.player();
            if (player == null || !player.isPlaying()) return;

            mSpeedIndicatorView.setVisibility(View.VISIBLE);
            mSpeedDescView.setText(String.format(Locale.getDefault(), mSpeedDescView.getResources().getString(R.string.vevod_video_bottom_bar_card_speed_desc), player.getSpeed()));

            Drawable drawable = mSpeedArrow.getDrawable();
            if (drawable == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        drawable = (AnimatedImageDrawable) ImageDecoder.decodeDrawable(ImageDecoder.createSource(mSpeedDescView.getResources(), R.drawable.vevod_mini_drama_video_bottom_card_speed_ic));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (drawable == null) {
                    drawable = ResourcesCompat.getDrawable(mSpeedDescView.getResources(), R.drawable.vevod_mini_drama_video_bottom_card_speed_ic, null);
                }
                mSpeedArrow.setImageDrawable(drawable);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (drawable instanceof AnimatedImageDrawable) {
                    AnimatedImageDrawable gif = (AnimatedImageDrawable) drawable;
                    gif.start();
                }
            }
        } else {
            mSpeedIndicatorView.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Drawable drawable = mSpeedArrow.getDrawable();
                if (drawable instanceof AnimatedImageDrawable) {
                    AnimatedImageDrawable gif = (AnimatedImageDrawable) drawable;
                    gif.stop();
                }
            }
        }
    }
}
