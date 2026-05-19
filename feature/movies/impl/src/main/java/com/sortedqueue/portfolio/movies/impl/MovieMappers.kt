package com.sortedqueue.portfolio.movies.impl

import com.sortedqueue.portfolio.core.model.MediaDetail
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import com.sortedqueue.portfolio.core.network.TmdbMovieDetailDto
import com.sortedqueue.portfolio.core.network.TmdbMovieDto

internal fun TmdbMovieDto.toMediaSummary(): MediaSummary {
    return MediaSummary(
        id = id,
        type = MediaType.Movie,
        title = title.orEmpty().ifBlank { "Untitled movie" },
        overview = overview.orEmpty().ifBlank { "No overview available." },
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average
    )
}

internal fun TmdbMovieDetailDto.toMediaDetail(): MediaDetail {
    return MediaDetail(
        id = id,
        type = MediaType.Movie,
        title = title.orEmpty().ifBlank { "Untitled movie" },
        overview = overview.orEmpty().ifBlank { "No overview available." },
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = release_date,
        voteAverage = vote_average,
        runtimeLabel = runtime?.let { "$it min" },
        genres = genres.orEmpty().map { it.name }
    )
}
