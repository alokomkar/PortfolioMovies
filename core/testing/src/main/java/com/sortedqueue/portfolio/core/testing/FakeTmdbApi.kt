package com.sortedqueue.portfolio.core.testing

import com.sortedqueue.portfolio.core.network.TmdbApi
import com.sortedqueue.portfolio.core.network.TmdbMovieDetailDto
import com.sortedqueue.portfolio.core.network.TmdbMovieDto
import com.sortedqueue.portfolio.core.network.TmdbPagedResponse
import com.sortedqueue.portfolio.core.network.TmdbTvShowDetailDto
import com.sortedqueue.portfolio.core.network.TmdbTvShowDto

class FakeTmdbApi : TmdbApi {
    var popularMoviesResult: Result<TmdbPagedResponse<TmdbMovieDto>> = Result.success(
        TmdbPagedResponse(page = 1, results = emptyList(), total_pages = 1, total_results = 0)
    )
    var popularTvShowsResult: Result<TmdbPagedResponse<TmdbTvShowDto>> = Result.success(
        TmdbPagedResponse(page = 1, results = emptyList(), total_pages = 1, total_results = 0)
    )
    var movieDetailsResult: Result<TmdbMovieDetailDto> = Result.failure(IllegalStateException("No movie detail configured"))
    var tvShowDetailsResult: Result<TmdbTvShowDetailDto> = Result.failure(IllegalStateException("No TV detail configured"))

    override suspend fun popularMovies(apiKey: String): TmdbPagedResponse<TmdbMovieDto> {
        return popularMoviesResult.getOrThrow()
    }

    override suspend fun popularTvShows(apiKey: String): TmdbPagedResponse<TmdbTvShowDto> {
        return popularTvShowsResult.getOrThrow()
    }

    override suspend fun movieDetails(movieId: Int, apiKey: String): TmdbMovieDetailDto {
        return movieDetailsResult.getOrThrow()
    }

    override suspend fun tvShowDetails(tvShowId: Int, apiKey: String): TmdbTvShowDetailDto {
        return tvShowDetailsResult.getOrThrow()
    }
}
