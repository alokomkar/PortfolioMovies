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
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.delay
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

/**
 * State holder that encapsulates the playback operations and exposes them as observable Compose states.
 */
@OptIn(UnstableApi::class)
@Suppress("TooManyFunctions")
class VideoPlayerState(
    val player: ExoPlayer,
    initialPlaylist: ImmutableList<VideoItem>
) {
    var playlist by mutableStateOf(initialPlaylist)
        internal set

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

    private fun checkYoutubePlayback() {
        val currentVideo = playlist.getOrNull(currentItemIndex)
        val isYoutube = currentVideo?.url?.let {
            it.contains("youtube.com") || it.contains("youtu.be")
        } == true
        if (isYoutube && player.isPlaying) {
            player.pause()
        }
    }

    init {
        val mediaItems = playlist.map { video ->
            val isYoutube = video.url.contains("youtube.com") || video.url.contains("youtu.be")
            val playUrl = if (isYoutube) {
                "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            } else {
                video.url
            }
            MediaItem.Builder()
                .setUri(playUrl)
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
        checkYoutubePlayback()

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                currentItemIndex = player.currentMediaItemIndex
                checkYoutubePlayback()
            }

            override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                isPlaying = isPlayingChanged
            }

            override fun onPlaybackStateChanged(state: Int) {
                playbackState = state
                duration = player.duration.coerceAtLeast(0L)
            }

            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                android.util.Log.e("VideoPlayerState", "Playback error: ${error.message}", error)
                val currentMediaItem = player.currentMediaItem
                if (currentMediaItem != null) {
                    val currentUri = currentMediaItem.localConfiguration?.uri?.toString() ?: ""
                    // If the failing URI is a youtube stream, hot-swap with fallback stream
                    if (currentUri.contains("googlevideo.com") || currentUri.contains("piped")) {
                        val fallbackUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
                        val fallbackMediaItem = MediaItem.Builder()
                            .setUri(fallbackUri)
                            .setMediaId(fallbackUri)
                            .setMediaMetadata(currentMediaItem.mediaMetadata)
                            .build()
                        val currentIndex = player.currentMediaItemIndex
                        player.replaceMediaItem(currentIndex, fallbackMediaItem)
                        player.prepare()
                        player.play()
                    }
                }
            }
        })
    }

    fun play() {
        val currentVideo = playlist.getOrNull(currentItemIndex)
        val isYoutube = currentVideo?.url?.let {
            it.contains("youtube.com") || it.contains("youtu.be")
        } == true
        if (!isYoutube) {
            player.play()
        }
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
            val currentVideo = playlist.getOrNull(index)
            val isYoutube = currentVideo?.url?.let {
                it.contains("youtube.com") || it.contains("youtu.be")
            } == true
            if (!isYoutube) {
                player.play()
            } else {
                player.pause()
            }
        }
    }

    fun seekToPosition(positionMs: Long) {
        player.seekTo(positionMs)
    }

    fun handleYoutubeError() {
        if (currentItemIndex in playlist.indices) {
            val currentItem = playlist[currentItemIndex]
            val fallbackUri = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            val updatedItem = currentItem.copy(
                url = fallbackUri,
                title = "${currentItem.title} (Fallback Stream)"
            )
            val updatedList = playlist.toMutableList()
            updatedList[currentItemIndex] = updatedItem
            playlist = updatedList.toImmutableList()

            // Also replace the item in ExoPlayer so ExoPlayer plays it
            val mediaItem = MediaItem.Builder()
                .setUri(fallbackUri)
                .setMediaId(fallbackUri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(updatedItem.title)
                        .setSubtitle(updatedItem.subtitle)
                        .build()
                )
                .build()
            player.replaceMediaItem(currentItemIndex, mediaItem)
            player.prepare()
            player.play()
        }
    }
}

/**
 * Creates and remembers a [VideoPlayerState] instance, handling lifecycle release on dispose.
 */
@OptIn(UnstableApi::class)
@Composable
fun rememberVideoPlayerState(playlist: ImmutableList<VideoItem>): VideoPlayerState {
    val context = LocalContext.current
    val exoPlayer = remember(playlist) {
        val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .setAllowCrossProtocolRedirects(true)
        
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(httpDataSourceFactory)
            )
            .build()
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
