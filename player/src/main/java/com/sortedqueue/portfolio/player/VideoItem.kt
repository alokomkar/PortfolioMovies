package com.sortedqueue.portfolio.player

/**
 * Representation of a playable video file in the player library.
 * Designed to be generic and independent of application domain models.
 */
data class VideoItem(
    val url: String,
    val title: String,
    val subtitle: String? = null
)
