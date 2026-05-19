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
                    results = listOf(tvDto(id = 22, name = "Severance")),
                    total_pages = 1,
                    total_results = 1
                )
            )
        }
        val repository = FavoritesRepository(FakeFavoritesDao())
        val viewModel = TvViewModel(api, repository)

        val loadedShow = viewModel.uiState.value.shows.single()
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("Severance", loadedShow.title)
        assertFalse(loadedShow.isFavorite)

        viewModel.toggleFavorite(loadedShow)

        assertTrue(repository.observeIsFavorite(22, MediaType.Tv).first())
        assertTrue(viewModel.uiState.value.shows.single().isFavorite)

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
