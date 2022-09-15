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

import android.os.Looper;

public class Asserts {

    public static void checkMainThread() {
        checkThread(Looper.getMainLooper());
    }

    public static void checkThread(Looper looper) {
        Asserts.checkNotNull(looper);
        if (Thread.currentThread() != looper.getThread()) {
            throw new IllegalThreadStateException(String.format("You must call this method in %s thread!", looper.getThread()));
        }
    }

    public static void checkState(Object currentState, Object... validStates) {
        for (Object s : validStates) {
            if (currentState == s) {
                return;
            }
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object s : validStates) {
            sb.append(s).append(",");
        }
        sb.replace(sb.length() - 1, sb.length() - 1, "]");
        throw new IllegalStateException(String.format("Thread:%s. Current state is %s, You can only call this method in %s",
                Thread.currentThread().getName(), currentState, sb));
    }

    public static void checkState(boolean legalState) {
        if (!legalState) {
            throw new IllegalStateException();
        }
    }

    public static void checkState(boolean legalState, String illegalMsg) {
        if (!legalState) {
            throw new IllegalStateException(illegalMsg);
        }
    }

    public static void checkArgument(boolean legalArgument) {
        if (!legalArgument) {
            throw new IllegalArgumentException();
        }
    }

    public static <T> T checkNotNull(T t) {
        if (t == null) {
            throw new NullPointerException();
        }
        return t;
    }

    public static <T> T checkNotNull(T t, String msg) {
        if (t == null) {
            throw new NullPointerException(msg);
        }
        return t;
    }

    public static <T> T checkOneOf(T o, T... ts) {
        if (ts == null) {
            throw new NullPointerException();
        }
        for (T t : ts) {
            if (o == t) {
                return o;
            }
        }
        StringBuilder sb = new StringBuilder('[');
        for (T t : ts) {
            sb.append(t).append(',');
        }
        sb.replace(sb.length() - 1, sb.length() - 1, "]");
        throw new IllegalArgumentException(o + " must be one of " + sb);
    }
}
