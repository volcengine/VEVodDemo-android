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

package com.bytedance.playerkit.utils;

import android.util.Log;


public class L {
    private static final String TAG = "Player_Kit";
    public static boolean ENABLE_LOG = false;

    private L() {
    }

    public static void v(Object o, String method, Object... messages) {
        if (ENABLE_LOG) {
            Log.v(TAG, createLog(o, method, messages));
        }
    }

    public static void v(Object o, String method, Throwable throwable, Object... messages) {
        if (ENABLE_LOG) {
            Log.v(TAG, createLog(o, method, messages), throwable);
        }
    }

    public static void d(Object o, String method, Object... messages) {
        if (ENABLE_LOG) {
            Log.d(TAG, createLog(o, method, messages));
        }
    }

    public static void d(Object o, String method, Throwable throwable, Object... messages) {
        if (ENABLE_LOG) {
            Log.d(TAG, createLog(o, method, messages), throwable);
        }
    }

    public static void i(Object o, String method, Object... messages) {
        if (ENABLE_LOG) {
            Log.i(TAG, createLog(o, method, messages));
        }
    }

    public static void i(Object o, String method, Throwable throwable, Object... messages) {
        if (ENABLE_LOG) {
            Log.i(TAG, createLog(o, method, messages), throwable);
        }
    }

    public static void e(Object o, String method, Object... messages) {
        if (ENABLE_LOG) {
            Log.e(TAG, createLog(o, method, messages));
        }
    }

    public static void e(Object o, String method, Throwable throwable, Object... messages) {
        if (ENABLE_LOG) {
            Log.e(TAG, createLog(o, method, messages), throwable);
        }
    }

    public static void w(Object o, String method, Object... messages) {
        if (ENABLE_LOG) {
            Log.w(TAG, createLog(o, method, messages));
        }
    }

    public static void w(Object o, String method, Throwable throwable, Object... messages) {
        if (ENABLE_LOG) {
            Log.w(TAG, createLog(o, method, messages), throwable);
        }
    }

    private static String createLog(Object o, String method, Object... messages) {
        StringBuilder msg = new StringBuilder("[" + obj2String(o) + "]").append(" -> ").append(method);
        if (messages != null) {
            for (Object message : messages) {
                msg.append(" -> ").append(obj2String(message));
            }
        }
        return msg.toString();
    }

    public static String string(Object o) {
        if (o == null) return "null";
        if (ENABLE_LOG) {
            return o.toString();
        } else {
            return "";
        }
    }

    public static String obj2String(Object o) {
        if (o == null) {
            return "null";
        } else if (o instanceof String) {
            return (String) o;
        } else if (o instanceof Boolean) {
            return String.valueOf(o);
        } else if (o instanceof Number) {
            return String.valueOf(o);
        } else if (o.getClass().isAnonymousClass()) {
            String s = o.toString();
            return s.substring(s.lastIndexOf('.'));
        } else if(o instanceof Class<?>) {
            return ((Class<?>)o).getSimpleName();
        } else {
            return o.getClass().getSimpleName() + '@' + Integer.toHexString(o.hashCode());
        }
    }
}
