/*
 * Copyright (C) 2024 bytedance
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
 * Create Date : 2024/9/5
 */

package com.bytedance.volc.voddemo.ui.minidrama.scene.detail;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.bytedance.playerkit.utils.L;
import com.bytedance.volc.vod.scenekit.ui.base.BaseDialogFragment;
import com.bytedance.volc.vod.scenekit.utils.UIUtils;
import com.bytedance.volc.voddemo.data.remote.RemoteApi;
import com.bytedance.volc.voddemo.data.remote.model.drama.EpisodeVideo;
import com.bytedance.volc.voddemo.impl.R;
import com.bytedance.volc.voddemo.ui.minidrama.data.mock.MockGetEpisodes;
import com.bytedance.volc.voddemo.ui.minidrama.data.mock.MockThirdPartPayService;
import com.bytedance.volc.voddemo.ui.minidrama.data.remote.api.GetEpisodesApi;

import java.util.Arrays;
import java.util.List;

public class DramaEpisodePayDialogFragment extends BaseDialogFragment {

    public static final String ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED = "action_drama_episode_pay_dialog_episode_unlocked";
    public static final String EXTRA_EPISODE_VIDEO = "extra_episode_video";

    public static DramaEpisodePayDialogFragment newInstance(EpisodeVideo episodeVideo) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_EPISODE_VIDEO, episodeVideo);
        DramaEpisodePayDialogFragment fragment = new DramaEpisodePayDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    private EpisodeVideo mEpisode;
    private GetEpisodesApi mGetEpisodesApi;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mEpisode = (EpisodeVideo) bundle.getSerializable(EXTRA_EPISODE_VIDEO);
        }
        mGetEpisodesApi = new MockGetEpisodes();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog == null) return;
        Window window = dialog.getWindow();
        if (window == null) return;

        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout((int) UIUtils.dip2Px(requireActivity(), 280),
                (int) UIUtils.dip2Px(requireActivity(), 229));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.vevod_mini_drama_episode_pay_dialog_fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.pay).setOnClickListener(v -> pay());
        view.findViewById(R.id.cancel).setOnClickListener(v -> dismiss());
    }

    private void pay() {
        if (mEpisode == null) return;
        L.d(this, "pay", mEpisode);
        Toast.makeText(requireActivity(), R.string.vevod_mini_drama_paying, Toast.LENGTH_SHORT).show();

        // TODO remove mock code
        MockThirdPartPayService.requestPay(mEpisode, () -> {
            if (getActivity() == null) return;
            // mock 第三方支付返回成功
            fetchUnlockedEpisode(mEpisode);
        });
    }

    private void fetchUnlockedEpisode(EpisodeVideo lockedEpisode) {
        mGetEpisodesApi.getEpisodeVideosByIds(EpisodeVideo.getDramaId(lockedEpisode), Arrays.asList(EpisodeVideo.getEpisodeNumber(lockedEpisode)), new RemoteApi.Callback<List<EpisodeVideo>>() {
            @Override
            public void onSuccess(List<EpisodeVideo> episodeVideos) {
                if (getActivity() == null) return;

                EpisodeVideo unlockedEpisode = episodeVideos != null && !episodeVideos.isEmpty() ? episodeVideos.get(0) : null;
                if (unlockedEpisode == null) {
                    onPayError(mEpisode, new Exception("MockAppServer Error! Get unlocked episode video return null!"));
                    return;
                }
                if (!TextUtils.equals(lockedEpisode.vid, unlockedEpisode.vid)) {
                    onPayError(mEpisode, new Exception("MockAppServer Error! " + "Expected:" + lockedEpisode.vid + " Returned:" + unlockedEpisode.vid));
                    return;
                }
                if (EpisodeVideo.isLocked(unlockedEpisode)) {
                    onPayError(mEpisode, new Exception("MockAppServer Error! [" + EpisodeVideo.dump(lockedEpisode) + "] is locked! Expected an unlocked one."));
                    return;
                }
                onPaySuccess(lockedEpisode, unlockedEpisode);
            }

            @Override
            public void onError(Exception e) {
                if (getActivity() == null) return;

                onPayError(lockedEpisode, e);
            }
        });
    }

    private void onPaySuccess(EpisodeVideo lockedEpisode, EpisodeVideo unlockedEpisode) {
        L.d(this, "onPaySuccess", lockedEpisode, unlockedEpisode, unlockedEpisode.vid, unlockedEpisode.playAuthToken, unlockedEpisode.videoModel);
        Toast.makeText(requireActivity(), R.string.vevod_mini_drama_pay_success_start_playback, Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ACTION_DRAMA_EPISODE_PAY_DIALOG_EPISODE_UNLOCKED);
        intent.putExtra(EXTRA_EPISODE_VIDEO, unlockedEpisode);
        LocalBroadcastManager.getInstance(requireActivity()).sendBroadcast(intent);
    }

    private void onPayError(EpisodeVideo lockedEpisode, Exception e) {
        Toast.makeText(requireActivity(), getString(R.string.vevod_mini_drama_unlock_operation_error) + e.toString(), Toast.LENGTH_LONG).show();
        L.e(this, "onPayError", lockedEpisode, e.getMessage());
    }
}
