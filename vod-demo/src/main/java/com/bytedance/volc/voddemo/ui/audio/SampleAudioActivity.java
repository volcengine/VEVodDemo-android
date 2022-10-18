/*
 * Copyright (C) 2022 bytedance
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
 * Create Date : 2022/10/18
 */

package com.bytedance.volc.voddemo.ui.audio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.playerkit.player.ui.utils.UIUtils;
import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.VideoSettings;
import com.bytedance.volc.vod.scenekit.data.model.VideoItem;
import com.bytedance.volc.vod.scenekit.data.page.Book;
import com.bytedance.volc.vod.scenekit.data.page.Page;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.api2.RemoteApi2;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.audio.service.AudioService;

import java.util.ArrayList;
import java.util.List;

public class SampleAudioActivity extends AppCompatActivity {

    public static void intentInto(Activity activity) {
        Intent intent = new Intent(activity, SampleAudioActivity.class);
        activity.startActivity(intent);
    }

    private final Book<VideoItem> mBook = new Book<>(100);

    private RemoteApi mRemoteApi;
    private String mAccount;

    private boolean mStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_music);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        UIUtils.setSystemBarTheme(
                this,
                Color.BLACK,
                false,
                false,
                Color.BLACK,
                false,
                false
        );

        mRemoteApi = new RemoteApi2();
        mAccount = VideoSettings.stringValue(VideoSettings.SHORT_VIDEO_SCENE_ACCOUNT_ID);
    }

    private void refresh() {
        L.d(this, "refresh", "start", 0, mBook.pageSize());
        mRemoteApi.getFeedStreamWithPlayAuthToken(mAccount, 0, mBook.pageSize(), new RemoteApi.Callback<Page<VideoItem>>() {
            @Override
            public void onSuccess(Page<VideoItem> page) {
                if (isDestroyed() || isFinishing()) return;

                L.d(this, "refresh", "success");
                List<VideoItem> videoItems = mBook.firstPage(page);
                start(videoItems);
            }

            @Override
            public void onError(Exception e) {
                if (isDestroyed() || isFinishing()) return;
                L.d(this, "refresh", e, "error");
                Toast.makeText(SampleAudioActivity.this, e.getMessage() + "", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void onStartClick(View view) {
        refresh();
    }

    public void onStopClick(View view) {
        stop();
    }

    private void start(List<VideoItem> videoItems) {
        if (mStarted) return;
        mStarted = true;
        AudioService.startPlayList(SampleAudioActivity.this, new ArrayList<>(videoItems));
    }

    private void stop() {
        if (!mStarted) return;
        mStarted = false;
        AudioService.stopPlayList(this);
    }
}