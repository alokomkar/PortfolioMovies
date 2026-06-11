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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView

/**
 * Renders the ExoPlayer video output with custom control overlays.
 */
@Composable
fun VideoPlayerView(
    state: VideoPlayerState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Black)
    ) {
        // 1. AndroidView wrapping Media3 PlayerView
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

        // 2. Custom Overlay Controls
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Title
            val currentVideo = state.playlist.getOrNull(state.currentItemIndex)
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

            // Bottom controls & progress
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
                        Text("⏮", color = if (state.currentItemIndex > 0) Color.White else Color.DarkGray, style = MaterialTheme.typography.titleLarge)
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
                        Text("⏭", color = if (state.currentItemIndex < state.playlist.lastIndex) Color.White else Color.DarkGray, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
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
