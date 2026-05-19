package com.sortedqueue.portfolio.movies.impl

import com.sortedqueue.portfolio.core.database.FavoritesRepository
import com.sortedqueue.portfolio.core.model.MediaType
import com.sortedqueue.portfolio.core.network.TmdbGenreDto
import com.sortedqueue.portfolio.core.network.TmdbMovieDetailDto
import com.sortedqueue.portfolio.core.network.TmdbMovieDto
import com.sortedqueue.portfolio.core.network.TmdbPagedResponse
import com.sortedqueue.portfolio.core.testing.FakeFavoritesDao
import com.sortedqueue.portfolio.core.testing.FakeTmdbApi
import com.sortedqueue.portfolio.core.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MoviesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun moviesViewModel_loadsMoviesAndTogglesFavorite() = runTest {
        val api = FakeTmdbApi().apply {
            popularMoviesResult = Result.success(
                TmdbPagedResponse(
                    page = 1,
                    results = listOf(movieDto(id = 10, title = "Heat")),
                    total_pages = 1,
                    total_results = 1
                )
            )
        }
        val favoritesDao = FakeFavoritesDao()
        val repository = FavoritesRepository(favoritesDao)
        val viewModel = MoviesViewModel(api, repository)

        val loadedMovie = viewModel.uiState.value.movies.single()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Heat", loadedMovie.title)
        assertFalse(loadedMovie.isFavorite)

        viewModel.toggleFavorite(loadedMovie)

        assertTrue(repository.observeIsFavorite(10, MediaType.Movie).first())
        assertTrue(viewModel.uiState.value.movies.single().isFavorite)

    }

    @Test
    fun movieDetailViewModel_loadsDetailAndTracksFavoriteState() = runTest {
        val api = FakeTmdbApi().apply {
            movieDetailsResult = Result.success(
                TmdbMovieDetailDto(
                    id = 42,
                    title = "Arrival",
                    overview = "First contact.",
                    poster_path = "/arrival.jpg",
                    backdrop_path = "/arrival-backdrop.jpg",
                    release_date = "2016-11-11",
                    vote_average = 7.6,
                    runtime = 116,
                    genres = listOf(TmdbGenreDto(id = 1, name = "Science Fiction"))
                )
            )
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = MovieDetailViewModel(api, repository)

        viewModel.loadMovie(42)

        val detail = viewModel.uiState.value.detail
        assertEquals("Arrival", detail?.title)
        assertEquals("116 min", detail?.runtimeLabel)
        assertEquals(listOf("Science Fiction"), detail?.genres)
        assertFalse(detail?.isFavorite ?: true)

        viewModel.toggleFavorite()

        assertTrue(repository.observeIsFavorite(42, MediaType.Movie).first())
        assertTrue(viewModel.uiState.value.detail?.isFavorite ?: false)

    }

    @Test
    fun moviesViewModel_exposesErrorStateWhenApiFails() = runTest {
        val api = FakeTmdbApi().apply {
            popularMoviesResult = Result.failure(IllegalStateException("network down"))
        }
        val viewModel = MoviesViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("network down", viewModel.uiState.value.errorMessage)

    }
}

private fun movieDto(id: Int, title: String): TmdbMovieDto {
    return TmdbMovieDto(
        id = id,
        title = title,
        overview = "Overview",
        poster_path = "/poster.jpg",
        backdrop_path = "/backdrop.jpg",
        release_date = "2026-01-01",
        vote_average = 8.0
    )
}
