/*
 * Copyright 2021 bytedance
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
 * Create Date : 2021/2/28
 */
package com.bytedance.volc.voddemo.data.local;

import android.content.Context;
import androidx.room.Room;

public class VodDataBaseManager {
    private static final String DATA_BASE_NAME = "volc_vod_item.db";

    private volatile static VodDataBaseManager sVodDataBaseManager;
    private final VodDataBase mVodDataBase;

    private VodDataBaseManager(final VodDataBase vodDataBase) {
        mVodDataBase = vodDataBase;
    }

    public VodDataBase getVodDataBase() {
        return mVodDataBase;
    }

    public static VodDataBaseManager getInstance(Context context) {
        if (context == null) {
            return null;
        }

        if (sVodDataBaseManager == null) {
            synchronized (VodDataBase.class) {
                if (sVodDataBaseManager == null) {
                    VodDataBase vodDataBase = Room
                            .databaseBuilder(context.getApplicationContext(), VodDataBase.class,
                                    DATA_BASE_NAME)
                            .build();
                    sVodDataBaseManager = new VodDataBaseManager(vodDataBase);
                }
            }
        }

        return sVodDataBaseManager;
    }
}
