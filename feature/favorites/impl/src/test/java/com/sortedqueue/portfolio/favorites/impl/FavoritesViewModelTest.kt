package com.sortedqueue.portfolio.favorites.impl

import com.sortedqueue.portfolio.core.database.FavoritesRepository
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import com.sortedqueue.portfolio.core.testing.FakeFavoritesDao
import com.sortedqueue.portfolio.core.testing.MainDispatcherRule
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun favoritesViewModel_observesAndRemovesFavorites() = runTest {
        val repository = FavoritesRepository(FakeFavoritesDao())
        val favorite = MediaSummary(
            id = 5,
            type = MediaType.Movie,
            title = "Favorite Movie",
            overview = "Overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2026-01-01",
            voteAverage = 8.8
        )
        repository.setFavorite(favorite, true)
        val viewModel = FavoritesViewModel(repository)
        val collectionJob = backgroundScope.launch {
            viewModel.favorites.collect {}
        }
        advanceUntilIdle()

        assertEquals(1, viewModel.favorites.value.size)
        assertEquals("Favorite Movie", viewModel.favorites.value.single().title)

        viewModel.removeFavorite(viewModel.favorites.value.single())
        advanceUntilIdle()

        assertTrue(viewModel.favorites.value.isEmpty())

        collectionJob.cancel()
    }
}
