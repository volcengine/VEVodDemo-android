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

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

public class Dispatcher {

    public interface EventListener {
        void onEvent(Event event);
    }

    private final H mHandler;
    private final CopyOnWriteArrayList<EventListener> mListeners = new CopyOnWriteArrayList<>();

    public Dispatcher(Looper looper) {
        this.mHandler = new H(looper, this);
    }

    public <T extends Event> T obtain(Class<T> clazz, Object owner) {
        final T event = Config.EVENT_POOL_ENABLE ? Pool.acquire(clazz) : Factory.create(clazz);
        return clazz.cast(event.owner(owner).dispatcher(this));
    }

    public final void addEventListener(EventListener listener) {
        this.mListeners.addIfAbsent(listener);
    }

    public final void removeEventListener(EventListener listener) {
        if (listener != null) {
            this.mListeners.remove(listener);
        }
    }

    public final void removeAllEventListener() {
        this.mListeners.clear();
    }

    public void dispatchEvent(Event event) {
        if (Thread.currentThread() != this.mHandler.getLooper().getThread()) {
            this.mHandler.obtainMessage(0, event).sendToTarget();
        } else {
            dispatch(event);
        }
    }

    public void release() {
        mHandler.post(() -> {
            mHandler.removeCallbacksAndMessages(null);
            mListeners.clear();
        });
    }

    private void dispatch(Event event) {
        for (EventListener listener : mListeners) {
            listener.onEvent(event);
        }
        if (event.dispatcher() == this) {
            if (Config.EVENT_POOL_ENABLE) {
                Pool.release(event);
            }
        }
    }

    private final static class H extends Handler {
        private final WeakReference<Dispatcher> mRef;

        H(@NonNull Looper looper, Dispatcher dispatcher) {
            super(looper);
            this.mRef = new WeakReference<>(dispatcher);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final Dispatcher dispatcher = this.mRef.get();
            if (dispatcher == null) return;

            if (msg.what == 0) {
                dispatcher.dispatch((Event) msg.obj);
                return;
            }
            throw new IllegalArgumentException();
        }
    }
}
