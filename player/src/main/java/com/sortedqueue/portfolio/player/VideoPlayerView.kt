package com.sortedqueue.portfolio.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

/**
 * Renders the ExoPlayer video output or the YouTube video player depending on the active media type.
 */
@Composable
fun VideoPlayerView(
    state: VideoPlayerState,
    modifier: Modifier = Modifier
) {
    val currentVideo = state.playlist.getOrNull(state.currentItemIndex)
    val youtubeId = currentVideo?.url?.let { extractYoutubeId(it) }
    val isYoutube = youtubeId != null

    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        // 1. Dynamic Video Renderers
        if (isYoutube && youtubeId != null) {
            // Render YouTube Player inside key to recreate and release cleanly on item change
            key(youtubeId) {
                YouTubePlayer(
                    videoId = youtubeId,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Render Media3 PlayerView (ExoPlayer) for direct stream playback
            AndroidView(
                factory = { context ->
                    PlayerView(context).apply {
                        useController = false // Disable default ExoPlayer controllers
                        player = state.player
                    }
                },
                update = { playerView ->
                    playerView.player = state.player
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Custom Overlay Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Title Bar (Always visible)
            Text(
                text = currentVideo?.title ?: "Playing Trailer",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Bottom controls & progress (Only visible for direct ExoPlayer streams)
            // For YouTube streams, Soffritti's player renders the official controls.
            if (!isYoutube) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.65f), MaterialTheme.shapes.medium)
                        .padding(12.dp)
                ) {
                    // Progress Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatTime(state.currentPosition),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )

                        Slider(
                            value = state.currentPosition.toFloat(),
                            onValueChange = { state.seekToPosition(it.toLong()) },
                            valueRange = 0f..state.duration.toFloat().coerceAtLeast(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Red,
                                activeTrackColor = Color.Red,
                                inactiveTrackColor = Color.Gray
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )

                        Text(
                            text = formatTime(state.duration),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Playback Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous
                        IconButton(
                            onClick = { state.playPrevious() },
                            enabled = state.currentItemIndex > 0
                        ) {
                            val color = if (state.currentItemIndex > 0) Color.White else Color.DarkGray
                            Text(
                                text = "⏮",
                                color = color,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }

                        // Rewind -10s
                        IconButton(onClick = { state.seekBackward() }) {
                            Text("⏪", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }

                        // Stop
                        IconButton(onClick = { state.stop() }) {
                            Text("⏹", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }

                        // Play/Pause Toggle
                        IconButton(
                            onClick = {
                                if (state.isPlaying) state.pause() else state.play()
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Red, CircleShape)
                        ) {
                            Text(
                                text = if (state.isPlaying) "⏸" else "▶",
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }

                        // Forward +10s
                        IconButton(onClick = { state.seekForward() }) {
                            Text("⏩", color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }

                        // Next
                        IconButton(
                            onClick = { state.playNext() },
                            enabled = state.currentItemIndex < state.playlist.lastIndex
                        ) {
                            val color = if (state.currentItemIndex < state.playlist.lastIndex) {
                                Color.White
                            } else {
                                Color.DarkGray
                            }
                            Text(
                                text = "⏭",
                                color = color,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Android Composable wrapping the Soffritti YouTube player view.
 */
@Composable
fun YouTubePlayer(
    videoId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView(ctx).apply {
                enableAutomaticInitialization = false
                initialize(object : com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer) {
                        youTubePlayer.loadVideo(videoId, 0f)
                    }
                })
                lifecycleOwner.lifecycle.addObserver(this)
            }
        },
        modifier = modifier,
        onRelease = { view ->
            lifecycleOwner.lifecycle.removeObserver(view)
            view.release()
        }
    )
}

/**
 * Helper function to parse/extract YouTube video ID from direct/short watch URLs.
 */
@Suppress("MagicNumber")
private fun extractYoutubeId(url: String): String? {
    return when {
        url.contains("v=") -> {
            url.substringAfter("v=").substringBefore("&")
        }
        url.contains("youtu.be/") -> {
            url.substringAfter("youtu.be/").substringBefore("?").substringBefore("/")
        }
        url.length == 11 -> {
            url
        }
        else -> null
    }
}

/**
 * Utility function to format timestamp in milliseconds to mm:ss format.
 */
private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
