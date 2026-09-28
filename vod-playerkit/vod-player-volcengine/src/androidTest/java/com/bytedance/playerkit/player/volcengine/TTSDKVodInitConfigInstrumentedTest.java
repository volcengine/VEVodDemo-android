/*
 * Copyright (C) 2026 bytedance
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
 */

package com.bytedance.playerkit.player.volcengine;

import static org.junit.Assert.assertEquals;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.pandora.common.env.config.Config;
import com.pandora.common.env.config.VodConfig;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TTSDKVodInitConfigInstrumentedTest {

    @Test
    public void testBuildVodSdkConfig_usesVodOnlyBizType() {
        Context context =
                InstrumentationRegistry.getInstrumentation()
                        .getTargetContext()
                        .getApplicationContext();
        VolcPlayerInitConfig initConfig =
                new VolcPlayerInitConfig.Builder()
                        .setContext(context)
                        .setWorkerHandler(new Handler(Looper.getMainLooper()))
                        .setAppInfo(
                                new VolcPlayerInitConfig.AppInfo.Builder()
                                        .setAppId("test-app-id")
                                        .setAppName("test-app")
                                        .setAppChannel("androidTest")
                                        .setAppVersion("1.0.0")
                                        .setLicenseUri("")
                                        .build())
                        .build();

        Config config =
                TTSDKVodInit.buildVodSdkConfig(
                        initConfig,
                        new VodConfig.Builder(context).build());

        assertEquals(Config.BIZ_TYPE_VOD, config.getBizType());
        assertEquals(0, config.getBizType() & Config.BIZ_TYPE_LIVE);
        assertEquals(0, config.getBizType() & Config.BIZ_TYPE_UPLOAD);
    }
}
