package com.sortedqueue.portfolio.core.network

import retrofit2.http.GET
import retrofit2.http.Path

interface TmdbApi {
    @GET("movie/popular")
    suspend fun popularMovies(): TmdbPagedResponse<TmdbMovieDto>

    @GET("tv/popular")
    suspend fun popularTvShows(): TmdbPagedResponse<TmdbTvShowDto>

    @GET("movie/{movie_id}")
    suspend fun movieDetails(
        @Path("movie_id") movieId: Int
    ): TmdbMovieDetailDto

    @GET("tv/{series_id}")
    suspend fun tvShowDetails(
        @Path("series_id") tvShowId: Int
    ): TmdbTvShowDetailDto

    @GET("movie/{movie_id}/videos")
    suspend fun movieVideos(
        @Path("movie_id") movieId: Int
    ): TmdbVideosResponse

    @GET("tv/{series_id}/videos")
    suspend fun tvShowVideos(
        @Path("series_id") tvShowId: Int
    ): TmdbVideosResponse
}

data class TmdbPagedResponse<T>(
    val page: Int,
    val results: List<T>,
    val total_pages: Int,
    val total_results: Int
)

data class TmdbMovieDto(
    val id: Int,
    val title: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double?
)

data class TmdbTvShowDto(
    val id: Int,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val first_air_date: String?,
    val vote_average: Double?
)

data class TmdbGenreDto(
    val id: Int,
    val name: String
)

data class TmdbMovieDetailDto(
    val id: Int,
    val title: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val release_date: String?,
    val vote_average: Double?,
    val runtime: Int?,
    val genres: List<TmdbGenreDto>?
)

data class TmdbTvShowDetailDto(
    val id: Int,
    val name: String?,
    val overview: String?,
    val poster_path: String?,
    val backdrop_path: String?,
    val first_air_date: String?,
    val vote_average: Double?,
    val number_of_seasons: Int?,
    val genres: List<TmdbGenreDto>?
)

data class TmdbVideosResponse(
    @com.google.gson.annotations.SerializedName("id") val id: Int,
    @com.google.gson.annotations.SerializedName("results") val results: List<TmdbVideoDto>
)

data class TmdbVideoDto(
    @com.google.gson.annotations.SerializedName("id") val id: String,
    @com.google.gson.annotations.SerializedName("key") val key: String,
    @com.google.gson.annotations.SerializedName("name") val name: String?,
    @com.google.gson.annotations.SerializedName("site") val site: String?,
    @com.google.gson.annotations.SerializedName("size") val size: Int?,
    @com.google.gson.annotations.SerializedName("type") val type: String?,
    @com.google.gson.annotations.SerializedName("official") val official: Boolean?
)

