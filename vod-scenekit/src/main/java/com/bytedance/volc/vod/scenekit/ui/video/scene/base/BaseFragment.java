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
 * Create Date : 2021/12/28
 */

package com.bytedance.volc.vod.scenekit.ui.video.scene.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.CallSuper;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bytedance.playerkit.utils.L;


public class BaseFragment extends Fragment {

    private boolean mUserExiting = false;

    @Override
    @CallSuper
    public void onAttach(@NonNull Context context) {
        L.d(this, "onAttach");
        super.onAttach(context);
    }

    @Override
    @CallSuper
    public void onCreate(@Nullable Bundle savedInstanceState) {
        L.d(this, "onCreate");
        super.onCreate(savedInstanceState);
        mUserExiting = false;
        initBackPressedHandler();
    }

    @Nullable
    @Override
    @CallSuper
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        L.d(this, "onCreateView");
        final int layoutResId = getLayoutResId();
        return layoutResId > 0 ? inflater.inflate(layoutResId, container, false) : null;
    }

    @LayoutRes
    protected int getLayoutResId() {
        return -1;
    }

    @Override
    @CallSuper
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        L.d(this, "onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    @CallSuper
    public void onStart() {
        L.d(this, "onStart");
        super.onStart();
    }

    @Override
    @CallSuper
    public void onResume() {
        L.d(this, "onResume");
        super.onResume();
    }

    @Override
    @CallSuper
    public void onPause() {
        L.d(this, "onPause");
        super.onPause();
    }

    @Override
    @CallSuper
    public void onStop() {
        L.d(this, "onStop");
        super.onStop();
    }

    @Override
    @CallSuper
    public void onHiddenChanged(boolean hidden) {
        L.d(this, "onHiddenChanged", hidden);
        super.onHiddenChanged(hidden);
    }

    @Override
    @CallSuper
    public void onDestroyView() {
        L.d(this, "onDestroyView");
        super.onDestroyView();
    }

    @Override
    @CallSuper
    public void onDestroy() {
        L.d(this, "onDestroy");
        super.onDestroy();
    }


    @Override
    @CallSuper
    public void onDetach() {
        L.d(this, "onDetach");
        super.onDetach();
    }

    protected void initBackPressedHandler() {
        requireActivity().getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        L.d(BaseFragment.this, "back try");
                        if (!onBackPressed()) {
                            L.d(BaseFragment.this, "back");
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        } else {
                            setEnabled(true);
                        }
                    }
                });
    }

    public boolean onBackPressed() {
        return false;
    }

    protected void setUserExiting(boolean userExiting) {
        this.mUserExiting = userExiting;
    }

    protected boolean isUserExiting() {
        return mUserExiting;
    }
}
