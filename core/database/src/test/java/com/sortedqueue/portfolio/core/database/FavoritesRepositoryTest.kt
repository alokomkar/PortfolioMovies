package com.sortedqueue.portfolio.core.database

import com.sortedqueue.portfolio.core.model.MediaDetail
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import com.sortedqueue.portfolio.core.testing.FakeFavoritesDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FavoritesRepositoryTest {
    private val fakeDao = FakeFavoritesDao()
    private val repository = FavoritesRepository(fakeDao)

    @Test
    fun setFavorite_withSummary_addsAndRemovesFavorite() = runTest {
        val movie = MediaSummary(
            id = 11,
            type = MediaType.Movie,
            title = "Movie",
            overview = "Overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            releaseDate = "2026-01-01",
            voteAverage = 8.1
        )

        repository.setFavorite(movie, true)

        val favorites = repository.observeFavorites().first()
        assertEquals(1, favorites.size)
        assertEquals("Movie", favorites.first().title)
        assertTrue(favorites.first().isFavorite)
        assertTrue(repository.observeIsFavorite(11, MediaType.Movie).first())

        repository.setFavorite(movie, false)

        assertTrue(repository.observeFavorites().first().isEmpty())
        assertFalse(repository.observeIsFavorite(11, MediaType.Movie).first())
    }

    @Test
    fun setFavorite_withDetail_preservesDetailFields() = runTest {
        val detail = MediaDetail(
            id = 7,
            type = MediaType.Tv,
            title = "Show",
            overview = "A show",
            posterPath = "/show.jpg",
            backdropPath = "/show-backdrop.jpg",
            releaseDate = "2026-02-01",
            voteAverage = 7.2,
            runtimeLabel = "2 seasons",
            genres = listOf("Drama")
        )

        repository.setFavorite(detail, true)

        val favorite = repository.observeFavorites().first().single()
        assertEquals(MediaType.Tv, favorite.type)
        assertEquals("/show-backdrop.jpg", favorite.backdropPath)
        assertEquals(7.2, favorite.voteAverage ?: 0.0, 0.0)
    }
}
