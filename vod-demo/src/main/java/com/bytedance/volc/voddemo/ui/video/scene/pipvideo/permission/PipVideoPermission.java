/*
 * Copyright (C) 2025 bytedance
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
 * Create Date : 2025/3/19
 */

package com.bytedance.volc.voddemo.ui.video.scene.pipvideo.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PipVideoPermission {

    public interface Callback {
        void onResult(boolean isGranted);

        void onRationale(Context context, UserAction action);
    }

    public interface UserAction {
        void granted();

        void denied();
    }

    private static final Map<String, WeakReference<PipVideoPermission>> sPermissionMap = new HashMap<>();

    private final String mKey;

    public final Context mContext;

    private Callback mCallback;

    public PipVideoPermission(Context context) {
        this.mKey = UUID.randomUUID().toString();
        this.mContext = context;
        sPermissionMap.put(mKey, new WeakReference<>(this));
    }

    public boolean isPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(mContext);
        } else {
            return false;
        }
    }

    public void requestPermission(Callback callback) {
        if (isPermissionGranted()) {
            callback.onResult(true);
        } else {
            mCallback = callback;
            PipVideoPermissionActivity.intentInto(mContext, mKey);
        }
    }

    static PipVideoPermission get(String key) {
        final WeakReference<PipVideoPermission> ref = sPermissionMap.get(key);
        return ref != null ? ref.get() : null;
    }

    void rationale(Activity activity) {
        if (mCallback == null) {
            activity.finish();
            return;
        }
        mCallback.onRationale(activity, new UserAction() {
            @Override
            public void granted() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + mContext.getPackageName()));
                    activity.startActivityForResult(intent, 100);
                } else {
                    mCallback.onResult(false);
                }
            }

            @Override
            public void denied() {
                activity.finish();
                mCallback.onResult(false);
            }
        });
    }

    void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            mCallback.onResult(isPermissionGranted());
        }
    }
}
