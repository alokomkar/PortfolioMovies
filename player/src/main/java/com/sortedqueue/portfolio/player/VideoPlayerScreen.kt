package com.sortedqueue.portfolio.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Screen wrapper that manages player state and renders the player alongside the interactive playlist.
 */
@Composable
fun VideoPlayerScreen(
    playlist: List<VideoItem>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (playlist.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No videos in playlist.", color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onBack) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val playerState = rememberVideoPlayerState(playlist = playlist)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Close Player")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Trailer Playlist (${playlist.size})",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Video Player Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
            ) {
                VideoPlayerView(
                    state = playerState,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Playlist Header
            Text(
                text = "Up Next",
                style = MaterialTheme.typography.titleMedium,
                color = Color.LightGray,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Playlist Queue List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                itemsIndexed(playlist) { index, item ->
                    val isCurrent = index == playerState.currentItemIndex
                    val itemBgColor = if (isCurrent) Color.Red.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.4f)
                    val borderColor = if (isCurrent) Color.Red else Color.Transparent

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                            .clickable { playerState.playAtIndex(index) },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = itemBgColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Video Status Dot / Index
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(
                                        if (isCurrent) Color.Red else Color.LightGray.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isCurrent && playerState.isPlaying) "▶" else "${index + 1}",
                                    color = if (isCurrent) Color.White else Color.LightGray,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Title & Description
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    color = if (isCurrent) Color.Red else Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                item.subtitle?.let {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = it,
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple shape definition helper since CircleShape requires standard material shapes
private val CircleShape = RoundedCornerShape(50)
