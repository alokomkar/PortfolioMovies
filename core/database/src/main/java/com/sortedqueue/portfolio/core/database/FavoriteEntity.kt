package com.sortedqueue.portfolio.core.database

import androidx.room.Entity
import com.sortedqueue.portfolio.core.model.MediaType

@Entity(tableName = "favorites", primaryKeys = ["mediaId", "mediaType"])
data class FavoriteEntity(
    val mediaId: Int,
    val mediaType: MediaType,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val addedAtMillis: Long
)
