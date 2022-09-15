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

package com.bytedance.playerkit.player.ui.layer.base;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.utils.L;


public abstract class AnimateLayer extends BaseLayer {
    public static final long DEFAULT_ANIMATE_DURATION = 300;
    public static final long DEFAULT_ANIMATE_DISMISS_DELAY = 4000;

    public static final int IDLE = 0;
    public static final int SHOWING = 1;
    public static final int DISMISSING = -1;

    private Animator mAnimator;
    private int mState = IDLE;

    protected final Handler mH = new Handler(Looper.getMainLooper());
    protected final Runnable animateDismissRunnable = this::animateDismiss;

    public Animator.AnimatorListener mAnimateShowListener;
    public Animator.AnimatorListener mAnimateDismissListener;

    public void setAnimateShowListener(Animator.AnimatorListener listener) {
        this.mAnimateShowListener = listener;
    }

    public void setAnimateDismissListener(Animator.AnimatorListener listener) {
        this.mAnimateDismissListener = listener;
    }

    protected Animator createAnimator() {
        ObjectAnimator animator = new ObjectAnimator();
        animator.setPropertyName("alpha");
        return animator;
    }

    protected void resetViewAnimateProperty() {
        View view = getView();
        if (view != null) {
            view.setAlpha(1);
        }
    }

    protected void initAnimateShowProperty(Animator animator) {
        if (animator instanceof ObjectAnimator) {
            ((ObjectAnimator) animator).setFloatValues(0f, 1f);
        }
    }

    protected void initAnimateDismissProperty(Animator animator) {
        if (animator instanceof ObjectAnimator) {
            ((ObjectAnimator) animator).setFloatValues(1f, 0f);
        }
    }

    public final int getAnimateState() {
        return mState;
    }

    public final void animateToggle(boolean autoDismiss) {
        switch (mState) {
            case IDLE:
                if (isShowing()) {
                    animateDismiss();
                } else {
                    animateShow(autoDismiss);
                }
                break;
            case SHOWING:
                animateDismiss();
                break;
            case DISMISSING:
                animateShow(autoDismiss);
                break;
        }
    }

    public boolean isAnimateShowing() {
        return mState == SHOWING;
    }

    public boolean isAnimateDismissing() {
        return mState == DISMISSING;
    }

    public void animateShow(boolean autoDismiss) {
        animateShow(autoDismiss, null);
    }

    public void animateShow(
            boolean autoDismiss,
            @Nullable Animator.AnimatorListener showListener) {

        animateShow(0, DEFAULT_ANIMATE_DURATION, autoDismiss, showListener);
    }

    public final void animateShow(
            long startDelay,
            long duration,
            boolean autoDismiss,
            @Nullable Animator.AnimatorListener showListener) {

        removeDismissRunnable();
        if (mState == SHOWING) {
            L.v(this, "animateShow", mapState(mState), mapState(SHOWING), "ignore");
            if (autoDismiss) {
                postDismissRunnable();
            }
            return;
        } else if (mState == DISMISSING) {
            L.v(this, "animateShow", mapState(mState), mapState(SHOWING), "cancel");
            if (mAnimator != null && mAnimator.isStarted()) {
                mAnimator.cancel();
            }
        } else {
            if (isShowing()) {
                L.v(this, "animateShow", mapState(mState), mapState(SHOWING), "ignore");
                if (autoDismiss) {
                    postDismissRunnable();
                }
                return;
            }
        }

        L.v(this, "animateShow", "start");
        show();

        if (!isShowing()) return; // support lock

        if (mAnimator == null) {
            mAnimator = createAnimator();
            mAnimator.setTarget(getView());
        }
        mAnimator.removeAllListeners();
        mAnimator.setStartDelay(startDelay);
        mAnimator.setDuration(duration);
        initAnimateShowProperty(mAnimator);
        mAnimator.start();
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                resetViewAnimateProperty();
                L.v(AnimateLayer.this, "animateShow", "cancel");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                resetViewAnimateProperty();
                setState(IDLE);
                L.v(AnimateLayer.this, "animateShow", "end");
            }
        });
        if (showListener != null) {
            mAnimator.addListener(showListener);
        }
        if (mAnimateShowListener != null) {
            mAnimator.addListener(mAnimateShowListener);
        }
        setState(SHOWING);

        if (autoDismiss) {
            postDismissRunnable();
        }
    }

    public void requestAnimateDismiss(@NonNull String reason) {
        animateDismiss();
    }

    public void animateDismiss() {
        animateDismiss(null);
    }

    public final void animateDismiss(Animator.AnimatorListener listener) {
        animateDismiss(0, DEFAULT_ANIMATE_DURATION, listener);
    }

    public final void animateDismiss(long startDelay, long duration, Animator.AnimatorListener listener) {
        removeDismissRunnable();
        if (mState == DISMISSING) {
            L.v(this, "animateDismiss", mapState(mState), mapState(DISMISSING), "ignore");
            return;
        } else if (mState == SHOWING) {
            L.v(this, "animateDismiss", mapState(mState), mapState(DISMISSING), "cancel");
            if (mAnimator != null && mAnimator.isStarted()) {
                mAnimator.cancel();
            }
        } else {
            if (!isShowing()) {
                L.v(this, "animateDismiss", mapState(mState), mapState(DISMISSING), "ignore");
                return;
            }
        }
        L.v(this, "animateDismiss", "start");
        if (mAnimator == null) {
            mAnimator = createAnimator();
        }
        mAnimator.removeAllListeners();
        mAnimator.setStartDelay(startDelay);
        mAnimator.setDuration(duration);
        initAnimateDismissProperty(mAnimator);
        mAnimator.start();
        mAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                resetViewAnimateProperty();
                L.v(AnimateLayer.this, "animateDismiss", "cancel");
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                dismiss();
                L.v(AnimateLayer.this, "animateDismiss", "end");
            }
        });
        if (listener != null) {
            mAnimator.addListener(listener);
        }
        if (mAnimateDismissListener != null) {
            mAnimator.addListener(mAnimateDismissListener);
        }
        setState(DISMISSING);
    }

    @Override
    public void show() {
        removeDismissRunnable();
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
            mAnimator.removeAllListeners();
        }
        super.show();
        resetViewAnimateProperty();
        setState(IDLE);
    }

    @Override
    public void dismiss() {
        removeDismissRunnable();
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
            mAnimator.removeAllListeners();
        }
        super.dismiss();
        resetViewAnimateProperty();
        setState(IDLE);
    }

    @Override
    public void hide() {
        removeDismissRunnable();
        if (mAnimator != null && mAnimator.isStarted()) {
            mAnimator.cancel();
            mAnimator.removeAllListeners();
        }
        super.hide();
        resetViewAnimateProperty();
        setState(IDLE);
    }

    private void removeDismissRunnable() {
        mH.removeCallbacks(animateDismissRunnable);
    }

    private void postDismissRunnable() {
        mH.postDelayed(animateDismissRunnable, DEFAULT_ANIMATE_DISMISS_DELAY);
    }

    private void setState(int state) {
        if (this.mState != state) {
            L.v(this, "setState", mapState(mState), mapState(state));
            this.mState = state;
        }
    }

    private static String mapState(int state) {
        switch (state) {
            case IDLE:
                return "idle";
            case DISMISSING:
                return "dismissing";
            case SHOWING:
                return "showing";
            default:
                throw new IllegalArgumentException();
        }
    }
}
