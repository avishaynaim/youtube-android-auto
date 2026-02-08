package com.youtube.auto.player

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.youtube.auto.data.model.Video
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _currentVideo = MutableStateFlow<Video?>(null)
    val currentVideo: StateFlow<Video?> = _currentVideo.asStateFlow()

    fun connect() {
        if (controllerFuture != null) return

        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            controller = controllerFuture?.let {
                if (it.isDone && !it.isCancelled) it.get() else null
            }
            controller?.addListener(playerListener)
        }, MoreExecutors.directExecutor())
    }

    fun playVideo(video: Video, streamUrl: String) {
        _currentVideo.value = video
        val mediaItem = MediaItem.Builder()
            .setMediaId(video.id)
            .setUri(streamUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(video.title)
                    .setArtist(video.channelTitle)
                    .setDescription(video.description)
                    .build()
            )
            .build()

        controller?.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }
    }

    fun play() { controller?.play() }
    fun pause() { controller?.pause() }
    fun seekTo(positionMs: Long) { controller?.seekTo(positionMs) }
    fun seekForward() { controller?.seekForward() }
    fun seekBack() { controller?.seekBack() }

    fun isPlaying(): Boolean = controller?.isPlaying == true

    fun getCurrentPosition(): Long = controller?.currentPosition ?: 0
    fun getDuration(): Long {
        val duration = controller?.duration ?: androidx.media3.common.C.TIME_UNSET
        return if (duration == androidx.media3.common.C.TIME_UNSET) 0 else duration
    }

    fun release() {
        controller?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controllerFuture = null
        controller = null
    }

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
        }

        override fun onPlaybackStateChanged(state: Int) {
            _playerState.value = _playerState.value.copy(
                playbackState = state,
                isBuffering = state == Player.STATE_BUFFERING
            )
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            _playerState.value = _playerState.value.copy(
                currentMediaId = mediaItem?.mediaId
            )
        }
    }
}

data class PlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val playbackState: Int = Player.STATE_IDLE,
    val currentMediaId: String? = null
)
