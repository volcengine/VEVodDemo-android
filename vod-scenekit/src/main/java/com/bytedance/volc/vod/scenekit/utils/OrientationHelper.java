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
 * Create Date : 2022/11/2
 */

package com.bytedance.volc.vod.scenekit.utils;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.OrientationEventListener;

import com.bytedance.playerkit.utils.L;

import java.lang.ref.WeakReference;


public class OrientationHelper extends OrientationEventListener {

    public static final int ORIENTATION_0 = 0;
    public static final int ORIENTATION_90 = 90;
    public static final int ORIENTATION_180 = 180;
    public static final int ORIENTATION_270 = 270;
    public static final int ORIENTATION_360 = 360;

    public static final int DEFAULT_ORIENTATION_DELTA = 10;

    private final int mOrientationDelta = DEFAULT_ORIENTATION_DELTA;
    private final WeakReference<Activity> mActivityRef;
    private int mOrientation = -1;

    private final OrientationChangedListener mListener;
    private boolean mEnabled;

    public OrientationHelper(Activity activity, OrientationChangedListener listener) {
        super(activity);
        this.mActivityRef = new WeakReference<>(activity);
        this.mListener = listener;
    }

    @Override
    public void onOrientationChanged(int orientation) {
        final Activity activity = mActivityRef.get();
        if (activity == null || activity.isFinishing()) return;

        int lastOrientation = mOrientation;
        if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
            return;
        } else if (isPortraitRange(orientation) && mOrientation != ORIENTATION_0) {
            mOrientation = ORIENTATION_0;
        } else if (isReverseLandscapeRange(orientation) && mOrientation != ORIENTATION_90) {
            mOrientation = ORIENTATION_90;
        } else if (isLandScapeRange(orientation) && mOrientation != ORIENTATION_270) {
            mOrientation = ORIENTATION_270;
        }
        if (mListener != null
                && mOrientation != OrientationEventListener.ORIENTATION_UNKNOWN
                && lastOrientation != mOrientation) {
            mListener.orientationChanged(lastOrientation, mOrientation);
        }
    }

    @Override
    public void enable() {
        super.enable();
        if (!mEnabled) {
            mEnabled = true;
            L.v(this, "toggle", mEnabled);
        }
    }

    @Override
    public void disable() {
        super.disable();
        if (mEnabled) {
            mEnabled = false;
            L.v(this, "toggle", mEnabled);
        }
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public int getOrientation() {
        return mOrientation;
    }

    private boolean isPortraitRange(int orientation) {
        return Math.abs(ORIENTATION_0 - orientation) <= mOrientationDelta
                || Math.abs(ORIENTATION_360 - orientation) <= mOrientationDelta;
    }

    private boolean isLandScapeRange(int orientation) {
        return Math.abs(ORIENTATION_270 - orientation) <= mOrientationDelta;
    }


    private boolean isReverseLandscapeRange(int orientation) {
        return Math.abs(ORIENTATION_90 - orientation) <= mOrientationDelta;
    }

    public static boolean isSystemAutoOrientationEnabled(Context context) {
        int status = 0;
        try {
            status = Settings.System.getInt(context.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return status == 1;
    }

    public interface OrientationChangedListener {

        void orientationChanged(int last, int current);

    }
}
