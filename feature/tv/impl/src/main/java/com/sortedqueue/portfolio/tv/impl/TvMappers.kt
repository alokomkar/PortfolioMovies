package com.sortedqueue.portfolio.tv.impl

import com.sortedqueue.portfolio.core.model.MediaDetail
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import com.sortedqueue.portfolio.core.network.TmdbTvShowDetailDto
import com.sortedqueue.portfolio.core.network.TmdbTvShowDto

internal fun TmdbTvShowDto.toMediaSummary(): MediaSummary {
    return MediaSummary(
        id = id,
        type = MediaType.Tv,
        title = name.orEmpty().ifBlank { "Untitled TV show" },
        overview = overview.orEmpty().ifBlank { "No overview available." },
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = first_air_date,
        voteAverage = vote_average
    )
}

internal fun TmdbTvShowDetailDto.toMediaDetail(): MediaDetail {
    return MediaDetail(
        id = id,
        type = MediaType.Tv,
        title = name.orEmpty().ifBlank { "Untitled TV show" },
        overview = overview.orEmpty().ifBlank { "No overview available." },
        posterPath = poster_path,
        backdropPath = backdrop_path,
        releaseDate = first_air_date,
        voteAverage = vote_average,
        runtimeLabel = number_of_seasons?.let { seasons -> if (seasons == 1) "1 season" else "$seasons seasons" },
        genres = genres.orEmpty().map { it.name }
    )
}
