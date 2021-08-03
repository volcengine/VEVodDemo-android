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
 * Create Date : 2021/6/8
 */
package com.bytedance.volc.voddemo.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.WindowManager;
import android.widget.TextView;

public class UIUtils {

    public static final char ELLIPSIS_CHAR = '\u2026';
    public static final int LAYOUT_PARAMS_KEEP_OLD = -3;

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

    public static void expandClickRegion(final View view, final int left, final int top,
            final int right,
            final int bottom) {
        view.post(new Runnable() {
            @Override
            public void run() {
                Rect delegateArea = new Rect();
                view.getHitRect(delegateArea);
                delegateArea.top += top;
                delegateArea.bottom += bottom;
                delegateArea.left += left;
                delegateArea.right += right;
                TouchDelegate expandedArea = new TouchDelegate(delegateArea, view);
                // give the delegate to an ancestor of the view we're delegating
                // the area to
                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(expandedArea);
                }
            }
        });
    }

    public static void setViewBackgroundWithPadding(View view, int resid) {
        if (view == null) {
            return;
        }
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int top = view.getPaddingTop();
        int bottom = view.getPaddingBottom();
        view.setBackgroundResource(resid);
        view.setPadding(left, top, right, bottom);
    }

    public static void setViewBackgroundWithPadding(View view, Resources res, int colorid) {
        if (view == null) {
            return;
        }
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int top = view.getPaddingTop();
        int bottom = view.getPaddingBottom();
        view.setBackgroundColor(res.getColor(colorid));
        view.setPadding(left, top, right, bottom);
    }

    @SuppressWarnings("deprecation")
    public static void setViewBackgroundWithPadding(View view, Drawable drawable) {
        if (view == null) {
            return;
        }
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int top = view.getPaddingTop();
        int bottom = view.getPaddingBottom();
        view.setBackgroundDrawable(drawable);
        view.setPadding(left, top, right, bottom);
    }

    public final static String getDisplayCount(int count) {
        if (count > 10000) {
            String result = String.format("%.1f", 1.0 * count / 10000);
            if ('0' == result.charAt(result.length() - 1)) {
                return result.substring(0, result.length() - 2) + "万";
            } else {
                return result + "万";
            }
        }
        return String.valueOf(count);
    }

    public final static int getScreenWidth(Context context) {
        if (context == null) {
            return 0;
        }

        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return (dm == null) ? 0 : dm.widthPixels;
    }

    public final static int getRatioOfScreen(Context context, float ratio) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        if (dm == null) {
            return 0;
        }
        return (int) (dm.widthPixels * ratio);
    }

    public static boolean isInUIThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    public final static int getScreenHeight(Context context) {
        if (context == null) {
            return 0;
        }

        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        return (dm == null) ? 0 : dm.heightPixels;
    }

    private static int DPI = -1;

    public static int getDpi(Context context) {
        if (DPI == -1) {
            if (context != null) {
                DPI = context.getApplicationContext().getResources().getDisplayMetrics().densityDpi;
            }
        }
        return DPI;
    }

    public static int getDiggBuryWidth(Context context) {
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        screenWidth = screenWidth * 1375 / 10000 + (int) (UIUtils.dip2Px(context, 20));
        return screenWidth;
    }

    public final static int getStatusBarHeight(Context context) {
        if (context == null) {
            return 0;
        }
        int result = 0;
        int resourceId = context.getResources()
                .getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private static boolean visibilityValid(int visiable) {
        return visiable == View.VISIBLE || visiable == View.GONE || visiable == View.INVISIBLE;
    }

    public final static void setViewVisibility(View v, int visiable) {
        if (v == null || v.getVisibility() == visiable || !visibilityValid(visiable)) {
            return;
        }
        v.setVisibility(visiable);
    }

    public final static boolean isViewVisible(View view) {
        if (view == null) {
            return false;
        }

        return view.getVisibility() == View.VISIBLE;
    }

    /**
     * get location of view relative to given upView. get center location if getCenter is true.
     */
    public static void getLocationInUpView(View upView, View view, int[] loc, boolean getCenter) {
        if (upView == null || view == null || loc == null || loc.length < 2) {
            return;
        }
        upView.getLocationInWindow(loc);
        int x1 = loc[0];
        int y1 = loc[1];
        view.getLocationInWindow(loc);
        int x2 = loc[0] - x1;
        int y2 = loc[1] - y1;
        if (getCenter) {
            int w = view.getWidth();
            int h = view.getHeight();
            x2 = x2 + w / 2;
            y2 = y2 + h / 2;
        }
        loc[0] = x2;
        loc[1] = y2;
    }

    public static void updateLayout(View view, int w, int h) {
        if (view == null) {
            return;
        }
        LayoutParams params = view.getLayoutParams();
        if (params == null || (params.width == w && params.height == h)) {
            return;
        }
        if (w != LAYOUT_PARAMS_KEEP_OLD) {
            params.width = w;
        }
        if (h != LAYOUT_PARAMS_KEEP_OLD) {
            params.height = h;
        }
        view.setLayoutParams(params);
    }

    public static void updateLayoutMargin(View view, int l, int t, int r, int b) {
        if (view == null) {
            return;
        }
        LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }
        if (params instanceof ViewGroup.MarginLayoutParams) {
            updateMargin(view, (ViewGroup.MarginLayoutParams) params, l, t, r, b);
        }
    }

    private static void updateMargin(View view, ViewGroup.MarginLayoutParams params, int l, int t,
            int r, int b) {
        if (view == null
            || params == null
            || (params.leftMargin == l
                && params.topMargin == t
                && params.rightMargin == r
                && params.bottomMargin == b)) {
            return;
        }
        if (l != LAYOUT_PARAMS_KEEP_OLD) {
            params.leftMargin = l;
        }
        if (t != LAYOUT_PARAMS_KEEP_OLD) {
            params.topMargin = t;
        }
        if (r != LAYOUT_PARAMS_KEEP_OLD) {
            params.rightMargin = r;
        }
        if (b != LAYOUT_PARAMS_KEEP_OLD) {
            params.bottomMargin = b;
        }
        view.setLayoutParams(params);
    }

    /**
     * @param view
     * @param topMarginInDp dp
     */
    public static void setTopMargin(View view, float topMarginInDp) {
        if (view == null) {
            return;
        }
        DisplayMetrics dm = view.getContext().getResources().getDisplayMetrics();
        int topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMarginInDp,
                dm);
        updateLayoutMargin(view, LAYOUT_PARAMS_KEEP_OLD, topMargin, LAYOUT_PARAMS_KEEP_OLD,
                LAYOUT_PARAMS_KEEP_OLD);
    }

    public static void setLayoutParams(View view, int width, int height) {
        LayoutParams params = view.getLayoutParams();
        if (params == null) {
            return;
        }
        if (width != Integer.MIN_VALUE) {
            params.width = width;
        }
        if (height != Integer.MIN_VALUE) {
            params.height = height;
        }
    }

    public static void setTxtAndAdjustVisible(TextView tv, CharSequence txt) {
        if (tv == null) {
            return;
        }
        if (TextUtils.isEmpty(txt)) {
            setViewVisibility(tv, View.GONE);
        } else {
            setViewVisibility(tv, View.VISIBLE);
            tv.setText(txt);
        }
    }

    public static void setText(TextView textView, CharSequence text) {
        if (textView == null || TextUtils.isEmpty(text)) {
            return;
        }

        textView.setText(text);
    }

    public static void detachFromParent(View view) {
        if (view == null || view.getParent() == null) {
            return;
        }
        ViewParent parent = view.getParent();
        if (!(parent instanceof ViewGroup)) {
            return;
        }
        try {
            ((ViewGroup) parent).removeView(view);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    public static void setViewMinHeight(View view, int minHeight) {
        if (view == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 16 && view.getMinimumHeight() == minHeight) {
            return;
        }
        view.setMinimumHeight(minHeight);
    }

    @SuppressLint("NewApi")
    public static void setTextViewMaxLines(TextView textView, int maxLines) {
        if (textView == null || maxLines <= 0) {
            return;
        }
        if (Build.VERSION.SDK_INT >= 16 && textView.getMaxLines() == maxLines) {
            return;
        }
        textView.setSingleLine(maxLines == 1);
        textView.setMaxLines(maxLines);
    }

    public static int setColorAlpha(int color, int alpha) {
        if (alpha > 0xff) {
            alpha = 0xff;
        } else if (alpha < 0) {
            alpha = 0;
        }
        return (color & 0xffffff) | (alpha * 0x1000000);
    }

    public static int floatToIntBig(float value) {
        return (int) (value + 0.999f);
    }

    public static class EllipsisMeasureResult {
        public String ellipsisStr;
        public int length;
    }

    public static EllipsisMeasureResult sTempEllipsisResult = new EllipsisMeasureResult();

    public static void requestOrienation(Activity activity, boolean landscape) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.setRequestedOrientation(landscape ?
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (landscape) {
            activity.getWindow()
                    .setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    public static int getIndexInParent(View view) {
        if (view == null || view.getParent() == null) {
            return -1;
        }
        ViewParent parent = view.getParent();
        if (parent instanceof ViewGroup) {
            return ((ViewGroup) parent).indexOfChild(view);
        }
        return -1;
    }

    public static boolean clearAnimation(View view) {
        if (view == null || view.getAnimation() == null) {
            return false;
        }
        view.clearAnimation();
        return true;
    }

    public static void setClickListener(boolean clickable, View view,
            View.OnClickListener clickListener) {
        if (view == null) {
            return;
        }
        if (clickable) {
            view.setOnClickListener(clickListener);
            view.setClickable(true);
        } else {
            view.setOnClickListener(null);
            view.setClickable(false);
        }
    }
}
