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

import static com.bytedance.playerkit.player.source.MediaSource.mediaEquals;
import static com.bytedance.playerkit.utils.event.Dispatcher.EventListener;

import android.os.Build;
import android.os.Looper;
import android.view.Surface;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bytedance.playerkit.player.Player;
import com.bytedance.playerkit.player.playback.event.ActionPreparePlayback;
import com.bytedance.playerkit.player.playback.event.ActionStartPlayback;
import com.bytedance.playerkit.player.playback.event.ActionStopPlayback;
import com.bytedance.playerkit.player.playback.event.StateBindPlayer;
import com.bytedance.playerkit.player.playback.event.StateBindVideoView;
import com.bytedance.playerkit.player.playback.event.StateUnbindPlayer;
import com.bytedance.playerkit.player.playback.event.StateUnbindVideoView;
import com.bytedance.playerkit.player.source.MediaSource;
import com.bytedance.playerkit.player.source.Track;
import com.bytedance.playerkit.utils.Asserts;
import com.bytedance.playerkit.utils.L;
import com.bytedance.playerkit.utils.event.Dispatcher;
import com.bytedance.playerkit.utils.event.Event;

import java.lang.ref.WeakReference;

/**
 * Session controller of playback.
 *
 * <p> NOTE: All public method of {@code PlaybackController} must be called in <b>main thread</b>.
 * Otherwise, {@link IllegalThreadStateException} will be throw.
 *
 * <p>The main responsibility of PlaybackController is controlling playback pipeline.
 * <ol>
 *   <li>Hold {@link Player} and {@link VideoView} instance of current playback.</li>
 *   <li>Control the <b>Start</b> and <b>Stop</b> of the playback session.</li>
 *   <li>Set {@link VideoView}'s Surface instance to {@link Player} to render when Surface is ready</li>
 * </ol>
 *
 * <p>Calling {@link #bind(VideoView)} to bind the {@link VideoView} instance to PlaybackController.
 * Calling {@link #unbind()} to unbind the {@link VideoView} and {@link Player} instance.
 *
 * <p>{@link Player} instance will be bounded automatically when you calling
 * {@link #startPlayback()}. {@link Player} instance will be unbound and recycled when you calling
 * {@link #stopPlayback()}. A {@link PlayerPool} instance is required to fetch the player instance.
 * Using {@link PlayerPool#DEFAULT} if the PlayerPool is not passed by constructor.
 *
 * <p>You should always Calling {@link #startPlayback()} and {@link #stopPlayback()} to start or
 * stop a playback instead calling {@link Player#prepare(MediaSource)} + {@link Player#start()} to
 * start playback or {@link} calling {@link Player#stop()} or {@link Player#release()} to stop
 * playback.
 *
 * <p>PlaybackController only controls the <b>Start</b> and <b>Stop</b> of the playback session.
 * You should get the player instance {@link #player()} to control <b>Pause</b> <b>Seek</b> and
 * other behaviors or query player state during playback.
 *
 * <p> A simple demonstration of usage of PlayerKit.
 *
 * <pre>
 * {@code
 * public class MainActivity {
 *   VideoView videoView;
 *
 *   @Override
 *   public void onCreate() {
 *     // ...
 *     videoView = findViewById(R.id.videoView);
 *
 *     // bind layers
 *     VideoLayerHost layerHost = new VideoLayerHost(context);
 *     layerHost.addLayer(new CoverLayer());
 *     layerHost.addLayer(new LoadingLayer());
 *     layerHost.addLayer(new PauseLayer());
 *     layerHost.addLayer(new SimpleProgressBarLayer());
 *     layerHost.attachToVideoView(videoView);
 *
 *     // bind playback controller
 *     PlaybackController controller = new PlaybackController();
 *     controller.bind(videoView)
 *
 *     // select display mode
 *     videoView.setDisplayMode(DisplayModeHelper.DISPLAY_MODE_ASPECT_FIT);
 *     // select display view TextureView or SurfaceView
 *     videoView.selectDisplayView(DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW);
 *
 *     // bind media data source
 *     videoView.bindDataSource(createSource())
 *   }
 *
 *    // create a media source
 *   MediaSource createSource() {
 *     MediaSource mediaSource = new MediaSource(UUID.randomUUID().toString(), MediaSource.SOURCE_TYPE_URL);
 *     Track track0 = new Track();
 *     track0.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track0.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_480_1_5MG.mp4"); // 480x270
 *     track0.setQuality(new Quality(Quality.QUALITY_RES_240, "240P"));
 *
 *     Track track1 = new Track();
 *     track1.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track1.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_640_3MG.mp4"); // 640x360
 *     track1.setQuality(new Quality(Quality.QUALITY_RES_360, "360P"));
 *
 *     Track track2 = new Track();
 *     track2.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track2.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1280_10MG.mp4"); // 1280x720
 *     track2.setQuality(new Quality(Quality.QUALITY_RES_720, "720P"));
 *
 *     Track track3 = new Track();
 *     track3.setTrackType(Track.TRACK_TYPE_VIDEO);
 *     track3.setUrl("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_1920_18MG.mp4"); // 1920x1080
 *     track3.setQuality(new Quality(Quality.QUALITY_RES_1080, "1080P"));
 *
 *     // You can switch quality of current playback by calling {@link Player#selectTrack(int, Track)}.
 *     // You can only add one track, If you don't have multi quality of tracks.
 *     mediaSource.setTracks(Arrays.asList(track0, track1, track2, track3));
 *     return source;
 *   }
 *
 *   @Override
 *   public void onResume() {
 *     videoView.startPlayback();
 *   }
 *
 *   @Override
 *   public void onPause() {
 *     videoView.pausePlayback();
 *   }
 *
 *   @Override
 *   public void onDestroy() {
 *     videoView.stopPlayback();
 *   }
 * }
 * }
 * </pre>
 *
 * @see PlayerPool
 * @see Player
 * @see MediaSource
 * @see VideoView
 */
public class PlaybackController {

    private VideoView mVideoView;
    private Player mPlayer;

    private final PlayerPool mPlayerPool;
    private final Player.Factory mPlayerFactory;
    private final SurfaceListener mSurfaceListener;
    private final PlayerListener mPlayerListener;
    private final Dispatcher mDispatcher;

    private Runnable mStartOnReadyCommand;

    @MainThread
    public PlaybackController() {
        this(PlayerPool.DEFAULT, Player.Factory.Default.get());
    }

    @MainThread
    public PlaybackController(PlayerPool playerPool, Player.Factory playerFactory) {
        Asserts.checkMainThread();
        mPlayerPool = playerPool;
        mPlayerFactory = playerFactory;
        mSurfaceListener = new SurfaceListener(this);
        mPlayerListener = new PlayerListener(this);
        mDispatcher = new Dispatcher(Looper.getMainLooper());
    }

    @MainThread
    public final void addPlaybackListener(EventListener listener) {
        Asserts.checkMainThread();
        mDispatcher.addEventListener(listener);
    }

    @MainThread
    public final void removePlaybackListener(EventListener listener) {
        Asserts.checkMainThread();
        mDispatcher.removeEventListener(listener);
    }

    @MainThread
    public final void removeAllPlaybackListeners() {
        Asserts.checkMainThread();
        mDispatcher.removeAllEventListener();
    }

    @AnyThread
    public Player.Factory playerFactory() {
        return mPlayerFactory;
    }

    @MainThread
    @Nullable
    public VideoView videoView() {
        Asserts.checkMainThread();
        return mVideoView;
    }

    @MainThread
    @Nullable
    public Player player() {
        Asserts.checkMainThread();
        return mPlayer;
    }

    /**
     * bind/unbind videoView to playback.
     *
     * @param videoView bind videoView instance to PlaybackController. Passing null to unbind
     *                  the pre bound videoView.
     */
    @MainThread
    public void bind(@Nullable VideoView videoView) {
        Asserts.checkMainThread();
        L.d(this, "bind", mVideoView, videoView);
        if (mVideoView != null && mVideoView != videoView) {
            unbindVideoView();
        }
        bindVideoView(videoView);
    }

    /**
     * Unbind pre bound {@link Player} and {@link VideoView} instance. This is useful when your
     * PlaybackController instance is static. Calling this method unbind VideoView and Player to
     * avoid memory leak.
     *
     * <p>If there is a {@link Player} instance. The instance will be unbound.
     * {@link StateUnbindPlayer} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)}.
     *
     * <p>If there is a {@link VideoView} instance. The instance will be unbound.
     * {@link StateUnbindVideoView} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)}
     */
    @MainThread
    public void unbind() {
        L.d(this, "unbind", mVideoView, mPlayer);
        Asserts.checkMainThread();
        mStartOnReadyCommand = null;
        unbindPlayer(false);
        unbindVideoView();
    }

    @MainThread
    public void unbindPlayer() {
        L.d(this, "unbindPlayer");
        Asserts.checkMainThread();
        mStartOnReadyCommand = null;
        unbindPlayer(false);
    }

    private void bindPlayer(Player newPlayer) {
        if (mPlayer == null && newPlayer != null && !newPlayer.isReleased()) {
            L.d(this, "bindPlayer", mPlayer, newPlayer);
            mPlayer = newPlayer;
            mPlayer.addPlayerListener(mPlayerListener);
            mDispatcher.obtain(StateBindPlayer.class, this).init(newPlayer).dispatch();
        }
    }

    private void unbindPlayer(boolean recycle) {
        if (mPlayer != null) {
            L.d(this, "unbindPlayer", mPlayer, recycle);
            if (recycle) {
                mPlayer.setSurface(null);
                mPlayerPool.recycle(mPlayer);
            }
            mPlayer.removePlayerListener(mPlayerListener);
            final Player toUnbind = mPlayer;
            mPlayer = null;
            mDispatcher.obtain(StateUnbindPlayer.class, this).init(toUnbind).dispatch();
        }
    }

    private void bindVideoView(VideoView newVideoView) {
        if (mVideoView == null && newVideoView != null) {
            L.d(this, "bindVideoView", newVideoView);
            mVideoView = newVideoView;
            mVideoView.addVideoViewListener(mSurfaceListener);
            mVideoView.bindController(this);
            mDispatcher.obtain(StateBindVideoView.class, this).init(newVideoView).dispatch();
        }
    }

    private void unbindVideoView() {
        if (mVideoView != null) {
            L.d(this, "unbindVideoView", mVideoView);
            mVideoView.removeVideoViewListener(mSurfaceListener);
            VideoView toUnbind = mVideoView;
            mVideoView = null;
            toUnbind.unbindController(this);
            mDispatcher.obtain(StateUnbindVideoView.class, this).init(toUnbind).dispatch();
        }
    }

    public final void preparePlayback() {
        startPlayback(false);
    }

    /**
     * Starts playback session.
     * <p> Make sure {@link #bind(VideoView)} and {@link VideoView#bindDataSource(MediaSource)} is
     * called before calling {@code startPlayback()}. {@code startPlayback()} will take no effect
     * if {@link VideoView} and {@link MediaSource} haven't been bound.
     *
     * <p>If there is no {@link Player} instance is bound. A {@link Player} instance will be bound
     * automatically after calling.
     *
     * <p> If there is a {@link Player} instance has been bound already.
     * <ul>
     *   <li>If {@link Player#getDataSource()} and {@link VideoView#getDataSource()} is not
     *       the same media.The {@link Player} instance will be recycled by {@link PlayerPool}</li>
     *   <li>If {@link Player#getDataSource()} and {@link VideoView#getDataSource()} is same media.
     *      {@link Player} instance will be reused.
     *   </li>
     * </ul>
     *
     * <p> Anyway, a {@link Player} instance will be bound to {@link PlaybackController}
     * automatically after calling this method.
     *
     * <p> If {@link VideoView}'s Surface is not ready when calling this method
     * playback will be automatically started when Surface is ready.
     *
     * <p> Calling {@link #stopPlayback()} to stop playback anytime you want.
     *
     * <p>{@link ActionStartPlayback} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)} after calling.
     */
    @MainThread
    public final void startPlayback() {
        startPlayback(true);
    }

    @MainThread
    private void startPlayback(boolean startWhenPrepared) {
        Asserts.checkMainThread();

        final VideoView attachedView = mVideoView;

        if (attachedView == null) {
            L.e(this, startWhenPrepared ? "startPlayback" : "preparePlayback",
                    "VideoView not bind!");
            return;
        }

        final MediaSource viewSource = attachedView.getDataSource();
        if (viewSource == null) {
            L.e(this, startWhenPrepared ? "startPlayback" : "preparePlayback",
                    "Data source not bind to VideoView yet!");
            return;
        }

        L.d(this, startWhenPrepared ? "startPlayback" : "preparePlayback");

        attachedView.setReuseSurface(true);

        if (startWhenPrepared) {
            mDispatcher.obtain(ActionStartPlayback.class, this).dispatch();
        } else {
            mDispatcher.obtain(ActionPreparePlayback.class, this).dispatch();
        }

        if (mPlayer != null) {
            if (mPlayer.isReleased() || mPlayer.isError()) {
                unbindPlayer(true);
            } else if (!mPlayer.isIDLE()
                    && !mediaEquals(mPlayer.getDataSource(), viewSource)) {
                unbindPlayer(true);
            }
        }

        final Player attachedPlayer;
        if (mPlayer == null) {
            attachedPlayer = mPlayerPool.acquire(viewSource, mPlayerFactory);
            bindPlayer(attachedPlayer);
            // new player，media source is not bind yet
        } else {
            attachedPlayer = mPlayer;
            // reuse player, same media source
        }

        Asserts.checkNotNull(attachedPlayer);

        if (isReady(attachedPlayer, attachedView)) {
            startPlayback(startWhenPrepared, attachedPlayer, attachedView);
            L.d(this, startWhenPrepared ? "startPlayback" : "preparePlayback", "end");
        } else {
            Surface surface = attachedView.getSurface();
            L.d(this, startWhenPrepared ? "startPlayback" : "preparePlayback",
                    "but resource not ready",
                    attachedPlayer, // player not bind
                    attachedView, // view not bind
                    surface, // surface not ready
                    viewSource // data source not bind
            );
            mStartOnReadyCommand = () -> {
                if (isReady(attachedPlayer, attachedView)) {
                    startPlayback(startWhenPrepared, attachedPlayer, attachedView);
                    L.d(PlaybackController.this, startWhenPrepared ? "startPlayback" : "preparePlayback", "end");
                }
            };
        }
    }

    private boolean isReady(Player player, VideoView videoView) {
        return player != null
                && videoView != null
                && videoView.getSurface() != null
                && videoView.getSurface().isValid()
                && videoView.getDataSource() != null;
    }

    private void startPlayback(boolean startWhenPrepared, @NonNull Player player, @NonNull VideoView videoView) {
        mStartOnReadyCommand = null;

        final MediaSource viewSource = videoView.getDataSource();
        final Surface surface = videoView.getSurface();

        L.d(this, startWhenPrepared ? "startPlayback" : "preparePlayback",
                "resource is ready",
                player,
                videoView,
                surface,
                viewSource
        );

        if (viewSource == null) return;
        if (surface == null) return;

        // 1. update surface
        player.setSurface(surface);

        @Player.PlayerState final int playerState = player.getState();

        // 2. start play
        switch (playerState) {
            case Player.STATE_IDLE: {
                if (startWhenPrepared) {
                    player.setStartWhenPrepared(true);
                }
                player.prepare(viewSource);
                break;
            }
            case Player.STATE_PREPARING: {
                if (!player.isStartWhenPrepared()) {
                    if (startWhenPrepared) {
                        player.setStartWhenPrepared(true);
                    }
                } else {
                    L.d(this, startWhenPrepared ? "startPlayback" : "preparePlayback",
                            "player is preparing, will be started automatically when prepared");
                }
                break;
            }
            case Player.STATE_STARTED:
            case Player.STATE_PREPARED:
            case Player.STATE_PAUSED:
            case Player.STATE_COMPLETED: {
                L.d(this, startWhenPrepared ? "startPlayback" : "preparePlayback",
                        "already " + Player.mapState(playerState) + "! nop~");
                if (startWhenPrepared) {
                    player.start();
                }
                break;
            }
            case Player.STATE_ERROR:
            case Player.STATE_STOPPED:
            case Player.STATE_RELEASED:
            default:
                throw new IllegalStateException(mPlayer + " state is illegal. " + mPlayer.dump());
        }
    }

    @MainThread
    public void pausePlayback() {
        Asserts.checkMainThread();

        final Player player = mPlayer;
        if (player != null) {
            L.d(this, "pausePlayback");

            if (player.isInPlaybackState()) {
                player.pause();
            } else if (player.isPreparing()) {
                player.setStartWhenPrepared(false);
            }
        }
    }

    /**
     * Stops playback session.
     * <p> {@link Player} instance will be unbind and released if there is one.
     *
     * <p>{@link ActionStopPlayback} will be emitted by {@link EventListener} added by
     * {@link #addPlaybackListener(EventListener)} after calling.
     */
    @MainThread
    public void stopPlayback() {
        Asserts.checkMainThread();

        final VideoView attachedView = mVideoView;
        final Player attachedPlayer = mPlayer;
        final MediaSource attachedSource = attachedSource(attachedView, attachedPlayer);

        if (attachedView != null) {
            attachedView.setReuseSurface(false);
        }

        if (mStartOnReadyCommand != null // startPlayback but surface not ready
                || attachedPlayer != null
        ) {
            L.d(this, "stopPlayback");

            mDispatcher.obtain(ActionStopPlayback.class, this).dispatch();

            mStartOnReadyCommand = null;
            unbindPlayer(true);

            L.d(this, "stopPlayback", "end");
        }
    }

    @Nullable
    private MediaSource attachedSource(VideoView attachedView, Player attachedPlayer) {
        MediaSource mediaSource = null;
        if (attachedPlayer != null) {
            mediaSource = attachedPlayer.getDataSource();
        }
        if (mediaSource == null && attachedView != null) {
            mediaSource = attachedView.getDataSource();
        }
        return mediaSource;
    }

    static final class SurfaceListener extends VideoView.VideoViewListener.Adapter {

        final WeakReference<PlaybackController> controllerRef;

        SurfaceListener(PlaybackController controller) {
            controllerRef = new WeakReference<>(controller);
        }

        @Override
        public void onSurfaceAvailable(Surface surface, int width, int height) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            L.d(controller, "onSurfaceAvailable", surface, width, height);

            if (controller.mStartOnReadyCommand != null) {
                controller.mStartOnReadyCommand.run();
            } else {
                final Player player = controller.player();
                if (player != null) {
                    player.setSurface(surface);
                }
            }
        }

        @Override
        public void onSurfaceSizeChanged(Surface surface, int width, int height) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            L.d(controller, "onSurfaceSizeChanged", surface, width, height);
        }

        @Override
        public void onSurfaceUpdated(Surface surface) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            //L.v(controller, "onSurfaceUpdated", surface);
        }

        @Override
        public void onSurfaceDestroy(Surface surface) {
            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            L.d(controller, "onSurfaceDestroy", surface);

            final VideoView videoView = controller.videoView();
            if (videoView == null) return;

            final Player player = controller.player();
            if (player == null) return;

            final int type = videoView.getDisplayViewType();

            switch (type) {
                case DisplayView.DISPLAY_VIEW_TYPE_TEXTURE_VIEW:
                    if (!videoView.isReuseSurface()) {
                        if (player.getSurface() == surface) {
                            player.setSurface(null);
                        }
                    }
                    return;
                case DisplayView.DISPLAY_VIEW_TYPE_SURFACE_VIEW:
                    if (player.getSurface() == surface) {
                        player.setSurface(null);
                    }
                    return;
                default:
                    throw new IllegalArgumentException("unsupported displayViewType: " + type);
            }
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {

            final PlaybackController controller = controllerRef.get();
            if (controller == null) return;

            L.d(controller, "onWindowFocusChanged", hasWindowFocus);

            // Fix video frame is cleared by system after unlock screen.
            if (hasWindowFocus) {
                Player player = controller.player();
                if (player != null && player.isInPlaybackState() && player.getSurface() != null) {
                    player.setSurface(player.getSurface());
                }
            }
        }
    }

    private static final class PlayerListener implements EventListener {

        private final WeakReference<PlaybackController> controllerRef;

        PlayerListener(PlaybackController controller) {
            controllerRef = new WeakReference<>(controller);
        }

        @Override
        public void onEvent(Event event) {
            final PlaybackController controller = controllerRef.get();
            if (controller != null) {
                final Dispatcher dispatcher = controller.mDispatcher;
                if (dispatcher != null) {
                    dispatcher.dispatchEvent(event);
                }
            }
        }
    }
}
