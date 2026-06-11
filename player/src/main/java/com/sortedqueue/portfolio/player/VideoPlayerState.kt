package com.sortedqueue.portfolio.player

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.delay
import kotlinx.collections.immutable.ImmutableList

/**
 * State holder that encapsulates the playback operations and exposes them as observable Compose states.
 */
class VideoPlayerState(
    val player: ExoPlayer,
    val playlist: ImmutableList<VideoItem>
) {
    var currentItemIndex by mutableIntStateOf(0)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    var playbackState by mutableStateOf(Player.STATE_IDLE)
        private set

    var currentPosition by mutableLongStateOf(0L)
        internal set

    var duration by mutableLongStateOf(0L)
        internal set

    init {
        val mediaItems = playlist.map { video ->
            MediaItem.Builder()
                .setUri(video.url)
                .setMediaId(video.url)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(video.title)
                        .setSubtitle(video.subtitle)
                        .build()
                )
                .build()
        }
        player.setMediaItems(mediaItems)
        player.prepare()
        player.playWhenReady = true

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentItemIndex = player.currentMediaItemIndex
            }

            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }

            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                duration = player.duration.coerceAtLeast(0L)
            }
        })
    }

    fun play() {
        player.play()
    }

    fun pause() {
        player.pause()
    }

    fun stop() {
        player.stop()
        player.seekTo(currentItemIndex, 0L)
    }

    fun seekForward() {
        val target = (player.currentPosition + 10000L).coerceAtMost(player.duration)
        player.seekTo(target)
    }

    fun seekBackward() {
        val target = (player.currentPosition - 10000L).coerceAtLeast(0L)
        player.seekTo(target)
    }

    fun playNext() {
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        }
    }

    fun playPrevious() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        }
    }

    fun playAtIndex(index: Int) {
        if (index in playlist.indices) {
            player.seekTo(index, 0L)
            player.play()
        }
    }

    fun seekToPosition(positionMs: Long) {
        player.seekTo(positionMs)
    }
}

/**
 * Creates and remembers a [VideoPlayerState] instance, handling lifecycle release on dispose.
 */
@Composable
fun rememberVideoPlayerState(playlist: ImmutableList<VideoItem>): VideoPlayerState {
    val context = LocalContext.current
    val exoPlayer = remember(playlist) {
        ExoPlayer.Builder(context).build()
    }

    val state = remember(playlist, exoPlayer) {
        VideoPlayerState(exoPlayer, playlist)
    }

    // Keep track of current playing position via cooperative polling
    LaunchedEffect(state.isPlaying, playlist) {
        if (state.isPlaying) {
            while (true) {
                state.currentPosition = exoPlayer.currentPosition
                state.duration = exoPlayer.duration.coerceAtLeast(0L)
                delay(250)
            }
        } else {
            state.currentPosition = exoPlayer.currentPosition
            state.duration = exoPlayer.duration.coerceAtLeast(0L)
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    return state
}
