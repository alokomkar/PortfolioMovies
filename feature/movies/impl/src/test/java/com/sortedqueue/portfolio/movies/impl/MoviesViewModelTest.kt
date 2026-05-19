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
                    results = listOf(
                        movieDto(id = 10, title = "Heat"),
                        movieDto(id = 11, title = "Collateral")
                    ),
                    total_pages = 1,
                    total_results = 2
                )
            )
        }
        val favoritesDao = FakeFavoritesDao()
        val repository = FavoritesRepository(favoritesDao)
        val viewModel = MoviesViewModel(api, repository)

        val loadedMovie = viewModel.uiState.value.movies.first { it.id == 10 }
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Heat", loadedMovie.title)
        assertFalse(loadedMovie.isFavorite)

        viewModel.toggleFavorite(loadedMovie)

        assertTrue(repository.observeIsFavorite(10, MediaType.Movie).first())
        assertTrue(viewModel.uiState.value.movies.first { it.id == 10 }.isFavorite)
        assertFalse(viewModel.uiState.value.movies.first { it.id == 11 }.isFavorite)

    }

    @Test
    fun moviesViewModel_unfavoritesAlreadyFavoriteMovie() = runTest {
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
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = MoviesViewModel(api, repository)

        val movie = viewModel.uiState.value.movies.single()
        viewModel.toggleFavorite(movie)
        assertTrue(repository.observeIsFavorite(10, MediaType.Movie).first())

        viewModel.toggleFavorite(viewModel.uiState.value.movies.single())

        assertFalse(repository.observeIsFavorite(10, MediaType.Movie).first())
        assertFalse(viewModel.uiState.value.movies.single().isFavorite)
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
    fun movieDetailViewModel_unfavoritesAlreadyFavoriteMovie() = runTest {
        val api = FakeTmdbApi().apply {
            movieDetailsResult = Result.success(movieDetailDto(id = 42, title = "Arrival"))
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        repository.setFavorite(movieDetailDto(id = 42, title = "Arrival").toMediaDetail(), true)
        val viewModel = MovieDetailViewModel(api, repository)

        viewModel.loadMovie(42)

        assertTrue(viewModel.uiState.value.detail?.isFavorite ?: false)

        viewModel.toggleFavorite()

        assertFalse(repository.observeIsFavorite(42, MediaType.Movie).first())
        assertFalse(viewModel.uiState.value.detail?.isFavorite ?: true)
    }

    @Test
    fun movieDetailViewModel_skipsReloadingSameMovieId() = runTest {
        val api = FakeTmdbApi().apply {
            movieDetailsResult = Result.success(movieDetailDto(id = 42, title = "Arrival"))
        }
        val viewModel = MovieDetailViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        viewModel.loadMovie(42)
        api.movieDetailsResult = Result.success(movieDetailDto(id = 43, title = "Changed"))
        viewModel.loadMovie(42)

        assertEquals(42, viewModel.uiState.value.detail?.id)
        assertEquals("Arrival", viewModel.uiState.value.detail?.title)
    }

    @Test
    fun movieDetailViewModel_exposesErrorStateWhenApiFails() = runTest {
        val api = FakeTmdbApi().apply {
            movieDetailsResult = Result.failure(IllegalStateException("detail unavailable"))
        }
        val viewModel = MovieDetailViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        viewModel.loadMovie(42)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("detail unavailable", viewModel.uiState.value.errorMessage)
        assertEquals(null, viewModel.uiState.value.detail)
    }

    @Test
    fun movieDetailViewModel_usesFallbackErrorMessageWhenThrowableHasNoMessage() = runTest {
        val api = FakeTmdbApi().apply {
            movieDetailsResult = Result.failure(Throwable())
        }
        val viewModel = MovieDetailViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        viewModel.loadMovie(42)

        assertEquals("Unable to load movie details", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun movieDetailViewModel_ignoresFavoriteToggleWhenDetailIsNotLoaded() = runTest {
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = MovieDetailViewModel(FakeTmdbApi(), repository)

        viewModel.toggleFavorite()

        assertTrue(repository.observeFavorites().first().isEmpty())
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

    @Test
    fun moviesViewModel_usesFallbackErrorMessageWhenThrowableHasNoMessage() = runTest {
        val api = FakeTmdbApi().apply {
            popularMoviesResult = Result.failure(Throwable())
        }
        val viewModel = MoviesViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Unable to load movies", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun movieMappers_useFallbackTextForBlankFields() {
        val summary = movieDto(id = 1, title = "").copy(overview = "")
            .toMediaSummary()
        val detail = TmdbMovieDetailDto(
            id = 2,
            title = null,
            overview = null,
            poster_path = null,
            backdrop_path = null,
            release_date = null,
            vote_average = null,
            runtime = null,
            genres = null
        ).toMediaDetail()

        assertEquals("Untitled movie", summary.title)
        assertEquals("No overview available.", summary.overview)
        assertEquals(MediaType.Movie, summary.type)
        assertEquals("Untitled movie", detail.title)
        assertEquals("No overview available.", detail.overview)
        assertEquals(null, detail.runtimeLabel)
        assertTrue(detail.genres.isEmpty())
    }

    @Test
    fun movieMappers_preserveNonBlankFieldsAndNullOverviewFallbacks() {
        val summary = TmdbMovieDto(
            id = 3,
            title = "The Insider",
            overview = null,
            poster_path = null,
            backdrop_path = null,
            release_date = null,
            vote_average = null
        ).toMediaSummary()
        val detail = movieDetailDto(id = 4, title = "Ali")

        assertEquals("The Insider", summary.title)
        assertEquals("No overview available.", summary.overview)
        assertEquals("Ali", detail.toMediaDetail().title)
        assertEquals("116 min", detail.toMediaDetail().runtimeLabel)
        assertEquals(listOf("Drama"), detail.toMediaDetail().genres)
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

private fun movieDetailDto(id: Int, title: String): TmdbMovieDetailDto {
    return TmdbMovieDetailDto(
        id = id,
        title = title,
        overview = "Overview",
        poster_path = "/poster.jpg",
        backdrop_path = "/backdrop.jpg",
        release_date = "2026-01-01",
        vote_average = 8.0,
        runtime = 116,
        genres = listOf(TmdbGenreDto(id = 1, name = "Drama"))
    )
}
