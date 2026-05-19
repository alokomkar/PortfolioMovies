package com.sortedqueue.portfolio.core.model

enum class MediaType {
    Movie,
    Tv
}

data class MediaSummary(
    val id: Int,
    val type: MediaType,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val isFavorite: Boolean = false
)

data class MediaDetail(
    val id: Int,
    val type: MediaType,
    val title: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String?,
    val voteAverage: Double?,
    val runtimeLabel: String?,
    val genres: List<String>,
    val isFavorite: Boolean = false
)

const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
