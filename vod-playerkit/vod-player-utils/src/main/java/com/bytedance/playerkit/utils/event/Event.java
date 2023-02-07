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

package com.bytedance.playerkit.utils.event;

import android.os.SystemClock;

import androidx.annotation.CallSuper;
import androidx.annotation.Keep;

@Keep
public class Event {
    private final int code;
    private Object owner;
    private Dispatcher dispatcher;

    private long dispatchTime;

    protected Event(int code) {
        this.code = code;
    }

    public final int code() {
        return code;
    }

    public final <T> T owner(Class<T> clazz) {
        return clazz.cast(owner);
    }

    public final Event owner(Object owner) {
        this.owner = owner;
        return this;
    }

    public final Event dispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
        return this;
    }

    public final Dispatcher dispatcher() {
        return this.dispatcher;
    }

    public final void dispatch() {
        this.dispatchTime = SystemClock.uptimeMillis();
        this.dispatcher.dispatchEvent(this);
    }

    public final long dispatchTime() {
        return this.dispatchTime;
    }

    @CallSuper
    public void recycle() {
        owner = null;
        dispatcher = null;
        dispatchTime = 0;
    }

    public boolean isRecycled() {
        return dispatcher == null;
    }

    public <E> E cast(Class<E> clazz) {
        return clazz.cast(this);
    }
}
