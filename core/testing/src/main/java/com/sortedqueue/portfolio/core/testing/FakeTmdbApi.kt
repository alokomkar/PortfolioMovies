package com.sortedqueue.portfolio.core.testing

import com.sortedqueue.portfolio.core.network.TmdbApi
import com.sortedqueue.portfolio.core.network.TmdbMovieDetailDto
import com.sortedqueue.portfolio.core.network.TmdbMovieDto
import com.sortedqueue.portfolio.core.network.TmdbPagedResponse
import com.sortedqueue.portfolio.core.network.TmdbTvShowDetailDto
import com.sortedqueue.portfolio.core.network.TmdbTvShowDto
import com.sortedqueue.portfolio.core.network.TmdbVideosResponse

class FakeTmdbApi : TmdbApi {
    var popularMoviesResult: Result<TmdbPagedResponse<TmdbMovieDto>> = Result.success(
        TmdbPagedResponse(page = 1, results = emptyList(), total_pages = 1, total_results = 0)
    )
    var popularTvShowsResult: Result<TmdbPagedResponse<TmdbTvShowDto>> = Result.success(
        TmdbPagedResponse(page = 1, results = emptyList(), total_pages = 1, total_results = 0)
    )
    var movieDetailsResult: Result<TmdbMovieDetailDto> = Result.failure(IllegalStateException("No movie detail configured"))
    var tvShowDetailsResult: Result<TmdbTvShowDetailDto> = Result.failure(IllegalStateException("No TV detail configured"))
    var movieVideosResult: Result<TmdbVideosResponse> = Result.success(TmdbVideosResponse(id = 0, results = emptyList()))
    var tvShowVideosResult: Result<TmdbVideosResponse> = Result.success(TmdbVideosResponse(id = 0, results = emptyList()))

    override suspend fun popularMovies(): TmdbPagedResponse<TmdbMovieDto> {
        return popularMoviesResult.getOrThrow()
    }

    override suspend fun popularTvShows(): TmdbPagedResponse<TmdbTvShowDto> {
        return popularTvShowsResult.getOrThrow()
    }

    override suspend fun movieDetails(movieId: Int): TmdbMovieDetailDto {
        return movieDetailsResult.getOrThrow()
    }

    override suspend fun tvShowDetails(tvShowId: Int): TmdbTvShowDetailDto {
        return tvShowDetailsResult.getOrThrow()
    }

    override suspend fun movieVideos(movieId: Int): TmdbVideosResponse {
        return movieVideosResult.getOrThrow()
    }

    override suspend fun tvShowVideos(tvShowId: Int): TmdbVideosResponse {
        return tvShowVideosResult.getOrThrow()
    }
}
