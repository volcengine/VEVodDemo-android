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

package com.bytedance.playerkit.player.playback;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.bytedance.playerkit.utils.L;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class DisplayView {

    public static final int DISPLAY_VIEW_TYPE_NONE = -1;
    public static final int DISPLAY_VIEW_TYPE_TEXTURE_VIEW = 0;
    public static final int DISPLAY_VIEW_TYPE_SURFACE_VIEW = 1;

    @Documented
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({DISPLAY_VIEW_TYPE_NONE, DISPLAY_VIEW_TYPE_TEXTURE_VIEW, DISPLAY_VIEW_TYPE_SURFACE_VIEW})
    public @interface DisplayViewType {
    }

    static DisplayView create(Context context, @DisplayViewType int displayType) {
        if (displayType == DISPLAY_VIEW_TYPE_SURFACE_VIEW) {
            return new DisplayView.SurfaceDisplayView(context);
        } else {
            return new DisplayView.TextureDisplayView(context);
        }
    }

    abstract int getViewType();

    abstract View getDisplayView();

    abstract Surface getSurface();

    abstract void setReuseSurface(boolean reuseSurface);

    abstract boolean isReuseSurface();

    abstract void setSurfaceListener(SurfaceListener surfaceListener);

    interface SurfaceListener {
        void onSurfaceAvailable(Surface surface, int width, int height);

        void onSurfaceSizeChanged(Surface surface, int width, int height);

        void onSurfaceUpdated(Surface surface);

        void onSurfaceDestroy(Surface surface);
    }

    static class SurfaceDisplayView extends DisplayView {

        android.view.SurfaceView surfaceView;

        SurfaceDisplayView(Context context) {
            surfaceView = new android.view.SurfaceView(context);

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    if (surfaceListener == null) return;
                    surfaceListener.onSurfaceAvailable(holder.getSurface(), surfaceView.getWidth(), surfaceView.getHeight());
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
                    if (surfaceListener == null) return;
                    surfaceListener.onSurfaceSizeChanged(holder.getSurface(), width, height);
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    if (surfaceListener == null) return;
                    surfaceListener.onSurfaceDestroy(holder.getSurface());
                }
            });
        }


        @Override
        int getViewType() {
            return DISPLAY_VIEW_TYPE_SURFACE_VIEW;
        }

        @Override
        public View getDisplayView() {
            return surfaceView;
        }

        @Override
        public Surface getSurface() {
            return surfaceView.getHolder().getSurface();
        }

        @Override
        void setReuseSurface(boolean reuseSurface) {
        }

        @Override
        boolean isReuseSurface() {
            return false;
        }

        private SurfaceListener surfaceListener;

        @Override
        public void setSurfaceListener(SurfaceListener surfaceListener) {
            this.surfaceListener = surfaceListener;
        }
    }

    static class TextureDisplayView extends DisplayView {

        private final android.view.TextureView textureView;
        private TextureSurface ttSurface;
        private SurfaceListener surfaceListener;
        private boolean reuseSurface = false;

        TextureDisplayView(Context context) {
            textureView = new android.view.TextureView(context);
            textureView.setSurfaceTextureListener(new android.view.TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                    if (reuseSurface && ttSurface != null && ttSurface.isValid()) {
                        textureView.setSurfaceTexture(ttSurface.getSurfaceTexture());
                        L.d(TextureDisplayView.this, "onSurfaceTextureAvailable", "reuse", ttSurface, surfaceTexture);
                    } else {
                        if (ttSurface != null) {
                            ttSurface.releaseDeep();
                        }
                        ttSurface = new TextureSurface(surfaceTexture);
                        L.d(TextureDisplayView.this, "onSurfaceTextureAvailable", "create", ttSurface, surfaceTexture);
                    }
                    if (surfaceListener != null) {
                        surfaceListener.onSurfaceAvailable(ttSurface, width, height);
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                    if (surfaceListener != null) {
                        surfaceListener.onSurfaceSizeChanged(ttSurface, width, height);
                    }
                }

                @Override
                public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                    L.d(TextureDisplayView.this, "onSurfaceTextureDestroyed", reuseSurface ? "keep" : "destroy", ttSurface, surfaceTexture);
                    if (!reuseSurface) {
                        if (surfaceListener != null) {
                            surfaceListener.onSurfaceDestroy(ttSurface);
                        }
                        ttSurface.releaseDeep();
                        ttSurface = null;
                    }
                    return !reuseSurface;
                }

                @Override
                public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {
                    if (surfaceListener != null) {
                        surfaceListener.onSurfaceUpdated(ttSurface);
                    }
                }
            });
        }

        @Override
        public Surface getSurface() {
            return ttSurface;
        }

        @Override
        void setReuseSurface(boolean reuseSurface) {
            this.reuseSurface = reuseSurface;
            if (!reuseSurface && !textureView.isAttachedToWindow() && ttSurface != null) {
                L.d(TextureDisplayView.this, "setReuseSurface", false, "destroy", ttSurface, ttSurface.getSurfaceTexture());
                if (surfaceListener != null) {
                    surfaceListener.onSurfaceDestroy(ttSurface);
                }
                ttSurface.releaseDeep();
                ttSurface = null;
            } else {
                L.d(TextureDisplayView.this, "setReuseSurface", reuseSurface);
            }
        }

        @Override
        boolean isReuseSurface() {
            return reuseSurface;
        }

        @Override
        int getViewType() {
            return DISPLAY_VIEW_TYPE_TEXTURE_VIEW;
        }

        @Override
        public View getDisplayView() {
            return textureView;
        }

        @Override
        public void setSurfaceListener(SurfaceListener surfaceListener) {
            this.surfaceListener = surfaceListener;
        }

        static class TextureSurface extends Surface {

            private SurfaceTexture mSurfaceTexture;

            @SuppressLint("Recycle")
            TextureSurface(SurfaceTexture surfaceTexture) {
                super(surfaceTexture);
                mSurfaceTexture = surfaceTexture;
            }

            SurfaceTexture getSurfaceTexture() {
                return mSurfaceTexture;
            }

            @Override
            public void release() {
                if (mSurfaceTexture != null) {
                    L.d(TextureSurface.this, "release", mSurfaceTexture);
                    super.release();
                    mSurfaceTexture = null;
                }
            }

            public void releaseDeep() {
                if (mSurfaceTexture != null) {
                    L.d(TextureSurface.this, "releaseDeep", mSurfaceTexture);
                    super.release();
                    mSurfaceTexture.release();
                    mSurfaceTexture = null;
                }
            }

            @Override
            public boolean isValid() {
                return super.isValid() && mSurfaceTexture != null;
            }
        }
    }
}
