/*
 * Copyright (C) 2021 bytedance
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
 * Create Date : 2021/12/3
 */

package com.bytedance.playerkit.player.ui.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.DisplayCutout;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;


public class UIUtils {

    public static void setSystemBarTheme(
            Activity activity,
            int statusBarColor,
            boolean lightStatusBar,
            boolean immersiveStatusBar,
            int navigationBarColor,
            boolean lightNavigationBar,
            boolean immersiveNavigationBar) {

        Window window = activity.getWindow();

        int flags = window.getDecorView().getSystemUiVisibility();

        if (immersiveStatusBar) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }

        if (immersiveNavigationBar) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }

        if (lightNavigationBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags = (flags & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR) | (0 & View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
        }

        if (lightStatusBar) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                flags = (flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) | (0 & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
        window.getDecorView().setSystemUiVisibility(flags);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(statusBarColor);
        window.setNavigationBarColor(navigationBarColor);
    }

    public static void setWindowFlags(WindowManager.LayoutParams attrs, int mask) {
        setWindowFlags(attrs, mask, mask);
    }

    public static void clearWindowFlags(WindowManager.LayoutParams attrs, int mask) {
        setWindowFlags(attrs, 0, mask);
    }

    private static void setWindowFlags(WindowManager.LayoutParams attrs, int flags, int mask) {
        if (attrs == null) return;
        attrs.flags = (attrs.flags & ~mask) | (flags & mask);
    }

    public static WindowManager.LayoutParams getWindowLayoutParams(View view) {
        if (view.getContext() instanceof Activity) {
            Activity activity = (Activity) view.getContext();
            if (activity != null) {
                Window window = activity.getWindow();
                if (window != null) {
                    return window.getAttributes();
                }
            }
        }
        return null;
    }

    public static boolean hasDisplayCutout(Window window) {
        DisplayCutout displayCutout;
        View rootView = window.getDecorView();
        WindowInsets insets = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            insets = rootView.getRootWindowInsets();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && insets != null) {
            displayCutout = insets.getDisplayCutout();
            if (displayCutout != null) {
                if (displayCutout.getBoundingRects() != null &&
                        displayCutout.getBoundingRects().size() > 0 &&
                        displayCutout.getSafeInsetTop() > 0) {
                    return true;
                }
            }
        }
        return true;
    }

    public static float getSystemBrightness(Context context) {
        int systemBrightness = 0;
        try {
            systemBrightness = Settings.System.getInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return systemBrightness / UIUtils.getSystemSettingBrightnessMax();
    }

    public static float getSystemSettingBrightnessMax() {
        try {
            final Resources res = Resources.getSystem();
            int resId = res.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");
            if (resId != 0) {
                return res.getInteger(resId);
            }
        } catch (Exception e) { /* ignore */ }
        return 255f;
    }

    public static int getScreenWidth(Context context) {
        if (context == null) {
            return 0;
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (dm == null) ? 0 : dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        if (context == null) {
            return 0;
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (dm == null) ? 0 : dm.heightPixels;
    }

    public static float sp2px(Context context, float sp) {
        if (context != null) {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                    context.getResources().getDisplayMetrics());
        }
        return 0;
    }

    public static float dip2Px(Context context, float dipValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return dipValue * scale + 0.5f;
        }
        return 0;
    }

    public static int px2dip(Context context, float pxValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (pxValue / scale + 0.5f);
        }
        return 0;
    }

    public static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean isNavigationBarShow(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(activity).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            return !menu && !back;
        }
    }

    public static int getNavigationBarHeight(Context context) {
        if (isNavigationBarShow((Activity) context)) {
            return getSizeByReflection(context, "navigation_bar_height");
        }
        return 0;
    }

    public static int getSizeByReflection(Context context, String field) {
        int size = -1;
        try {
            Class<?> clazz = Class.forName("com.android.internal.R$dimen");
            Object object = clazz.newInstance();
            int height = Integer.parseInt(clazz.getField(field).get(object).toString());
            size = context.getResources().getDimensionPixelSize(height);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }


    private static final int[] sLocation = new int[2];

    public static int[] getLocationInWindow(View view) {
        view.getLocationInWindow(sLocation);
        return sLocation;
    }


    public static boolean isInViewArea(MotionEvent e, View view) {
        if (view == null) return false;

        float x = e.getRawX();
        float y = e.getRawY();

        int[] location = getLocationInWindow(view);
        int width = view.getWidth();
        int height = view.getHeight();

        int left = location[0];
        int top = location[1];
        int right = left + width;
        int bottom = top + height;

        return left < x && x < right && top < y && y < bottom;
    }
}
