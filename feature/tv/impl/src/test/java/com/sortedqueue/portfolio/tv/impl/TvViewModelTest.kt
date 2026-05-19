package com.sortedqueue.portfolio.tv.impl

import com.sortedqueue.portfolio.core.database.FavoritesRepository
import com.sortedqueue.portfolio.core.model.MediaType
import com.sortedqueue.portfolio.core.network.TmdbGenreDto
import com.sortedqueue.portfolio.core.network.TmdbPagedResponse
import com.sortedqueue.portfolio.core.network.TmdbTvShowDetailDto
import com.sortedqueue.portfolio.core.network.TmdbTvShowDto
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

class TvViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun tvViewModel_loadsShowsAndTogglesFavorite() = runTest {
        val api = FakeTmdbApi().apply {
            popularTvShowsResult = Result.success(
                TmdbPagedResponse(
                    page = 1,
                    results = listOf(
                        tvDto(id = 22, name = "Severance"),
                        tvDto(id = 23, name = "Silo")
                    ),
                    total_pages = 1,
                    total_results = 2
                )
            )
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = TvViewModel(api, repository)

        val loadedShow = viewModel.uiState.value.shows.first { it.id == 22 }
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Severance", loadedShow.title)
        assertFalse(loadedShow.isFavorite)

        viewModel.toggleFavorite(loadedShow)

        assertTrue(repository.observeIsFavorite(22, MediaType.Tv).first())
        assertTrue(viewModel.uiState.value.shows.first { it.id == 22 }.isFavorite)
        assertFalse(viewModel.uiState.value.shows.first { it.id == 23 }.isFavorite)

    }

    @Test
    fun tvViewModel_unfavoritesAlreadyFavoriteShow() = runTest {
        val api = FakeTmdbApi().apply {
            popularTvShowsResult = Result.success(
                TmdbPagedResponse(
                    page = 1,
                    results = listOf(tvDto(id = 22, name = "Severance")),
                    total_pages = 1,
                    total_results = 1
                )
            )
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = TvViewModel(api, repository)

        val show = viewModel.uiState.value.shows.single()
        viewModel.toggleFavorite(show)
        assertTrue(repository.observeIsFavorite(22, MediaType.Tv).first())

        viewModel.toggleFavorite(viewModel.uiState.value.shows.single())

        assertFalse(repository.observeIsFavorite(22, MediaType.Tv).first())
        assertFalse(viewModel.uiState.value.shows.single().isFavorite)
    }

    @Test
    fun tvDetailViewModel_loadsDetailAndTracksFavoriteState() = runTest {
        val api = FakeTmdbApi().apply {
            tvShowDetailsResult = Result.success(
                TmdbTvShowDetailDto(
                    id = 31,
                    name = "Slow Horses",
                    overview = "Spies.",
                    poster_path = "/slow.jpg",
                    backdrop_path = "/slow-backdrop.jpg",
                    first_air_date = "2022-04-01",
                    vote_average = 8.2,
                    number_of_seasons = 4,
                    genres = listOf(TmdbGenreDto(id = 2, name = "Drama"))
                )
            )
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = TvDetailViewModel(api, repository)

        viewModel.loadShow(31)

        val detail = viewModel.uiState.value.detail
        assertEquals("Slow Horses", detail?.title)
        assertEquals("4 seasons", detail?.runtimeLabel)
        assertEquals(listOf("Drama"), detail?.genres)

        viewModel.toggleFavorite()

        assertTrue(repository.observeIsFavorite(31, MediaType.Tv).first())
        assertTrue(viewModel.uiState.value.detail?.isFavorite ?: false)

    }

    @Test
    fun tvDetailViewModel_unfavoritesAlreadyFavoriteShow() = runTest {
        val api = FakeTmdbApi().apply {
            tvShowDetailsResult = Result.success(tvDetailDto(id = 31, name = "Slow Horses"))
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        repository.setFavorite(tvDetailDto(id = 31, name = "Slow Horses").toMediaDetail(), true)
        val viewModel = TvDetailViewModel(api, repository)

        viewModel.loadShow(31)

        assertTrue(viewModel.uiState.value.detail?.isFavorite ?: false)

        viewModel.toggleFavorite()

        assertFalse(repository.observeIsFavorite(31, MediaType.Tv).first())
        assertFalse(viewModel.uiState.value.detail?.isFavorite ?: true)
    }

    @Test
    fun tvDetailViewModel_skipsReloadingSameShowId() = runTest {
        val api = FakeTmdbApi().apply {
            tvShowDetailsResult = Result.success(tvDetailDto(id = 31, name = "Slow Horses"))
        }
        val viewModel = TvDetailViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        viewModel.loadShow(31)
        api.tvShowDetailsResult = Result.success(tvDetailDto(id = 32, name = "Changed"))
        viewModel.loadShow(31)

        assertEquals(31, viewModel.uiState.value.detail?.id)
        assertEquals("Slow Horses", viewModel.uiState.value.detail?.title)
    }

    @Test
    fun tvViewModel_exposesErrorStateWhenApiFails() = runTest {
        val api = FakeTmdbApi().apply {
            popularTvShowsResult = Result.failure(IllegalStateException("network down"))
        }
        val viewModel = TvViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("network down", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun tvViewModel_usesFallbackErrorMessageWhenThrowableHasNoMessage() = runTest {
        val api = FakeTmdbApi().apply {
            popularTvShowsResult = Result.failure(Throwable())
        }
        val viewModel = TvViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Unable to load TV shows", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun tvDetailViewModel_exposesErrorStateWhenApiFails() = runTest {
        val api = FakeTmdbApi().apply {
            tvShowDetailsResult = Result.failure(IllegalStateException("detail unavailable"))
        }
        val viewModel = TvDetailViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        viewModel.loadShow(31)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("detail unavailable", viewModel.uiState.value.errorMessage)
        assertEquals(null, viewModel.uiState.value.detail)
    }

    @Test
    fun tvDetailViewModel_usesFallbackErrorMessageWhenThrowableHasNoMessage() = runTest {
        val api = FakeTmdbApi().apply {
            tvShowDetailsResult = Result.failure(Throwable())
        }
        val viewModel = TvDetailViewModel(api, FavoritesRepository(FakeFavoritesDao()))

        viewModel.loadShow(31)

        assertEquals("Unable to load TV details", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun tvDetailViewModel_ignoresFavoriteToggleWhenDetailIsNotLoaded() = runTest {
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = TvDetailViewModel(FakeTmdbApi(), repository)

        viewModel.toggleFavorite()

        assertTrue(repository.observeFavorites().first().isEmpty())
    }

    @Test
    fun tvMappers_useFallbackTextAndSingularSeasonLabel() {
        val summary = tvDto(id = 1, name = "").copy(overview = "")
            .toMediaSummary()
        val detail = TmdbTvShowDetailDto(
            id = 2,
            name = null,
            overview = null,
            poster_path = null,
            backdrop_path = null,
            first_air_date = null,
            vote_average = null,
            number_of_seasons = 1,
            genres = null
        ).toMediaDetail()

        assertEquals("Untitled TV show", summary.title)
        assertEquals("No overview available.", summary.overview)
        assertEquals(MediaType.Tv, summary.type)
        assertEquals("Untitled TV show", detail.title)
        assertEquals("No overview available.", detail.overview)
        assertEquals("1 season", detail.runtimeLabel)
        assertTrue(detail.genres.isEmpty())
    }

    @Test
    fun tvMappers_preserveNonBlankFieldsAndNullOverviewFallbacks() {
        val summary = TmdbTvShowDto(
            id = 3,
            name = "Foundation",
            overview = null,
            poster_path = null,
            backdrop_path = null,
            first_air_date = null,
            vote_average = null
        ).toMediaSummary()
        val detail = tvDetailDto(id = 4, name = "The Bear")
        val noSeasonDetail = tvDetailDto(id = 5, name = "No Season").copy(number_of_seasons = null)
            .toMediaDetail()

        assertEquals("Foundation", summary.title)
        assertEquals("No overview available.", summary.overview)
        assertEquals("The Bear", detail.toMediaDetail().title)
        assertEquals("4 seasons", detail.toMediaDetail().runtimeLabel)
        assertEquals(listOf("Drama"), detail.toMediaDetail().genres)
        assertEquals(null, noSeasonDetail.runtimeLabel)
    }
}

private fun tvDto(id: Int, name: String): TmdbTvShowDto {
    return TmdbTvShowDto(
        id = id,
        name = name,
        overview = "Overview",
        poster_path = "/poster.jpg",
        backdrop_path = "/backdrop.jpg",
        first_air_date = "2026-01-01",
        vote_average = 8.0
    )
}

private fun tvDetailDto(id: Int, name: String): TmdbTvShowDetailDto {
    return TmdbTvShowDetailDto(
        id = id,
        name = name,
        overview = "Overview",
        poster_path = "/poster.jpg",
        backdrop_path = "/backdrop.jpg",
        first_air_date = "2026-01-01",
        vote_average = 8.0,
        number_of_seasons = 4,
        genres = listOf(TmdbGenreDto(id = 1, name = "Drama"))
    )
}
