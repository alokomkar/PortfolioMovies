package com.sortedqueue.portfolio.core.database

import com.sortedqueue.portfolio.core.model.MediaDetail
import com.sortedqueue.portfolio.core.model.MediaSummary
import com.sortedqueue.portfolio.core.model.MediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesRepository @Inject constructor(
    private val favoritesDao: FavoritesDao
) {
    fun observeFavorites(): Flow<List<MediaSummary>> {
        return favoritesDao.observeFavorites().map { favorites ->
            favorites.map { it.toMediaSummary() }
        }
    }

    fun observeIsFavorite(mediaId: Int, mediaType: MediaType): Flow<Boolean> {
        return favoritesDao.observeIsFavorite(mediaId, mediaType)
    }

    suspend fun setFavorite(media: MediaSummary, favorite: Boolean) {
        if (favorite) {
            favoritesDao.upsertFavorite(media.toFavoriteEntity())
        } else {
            favoritesDao.deleteFavorite(media.id, media.type)
        }
    }

    suspend fun setFavorite(media: MediaDetail, favorite: Boolean) {
        if (favorite) {
            favoritesDao.upsertFavorite(media.toFavoriteEntity())
        } else {
            favoritesDao.deleteFavorite(media.id, media.type)
        }
    }
}

private fun FavoriteEntity.toMediaSummary(): MediaSummary {
    return MediaSummary(
        id = mediaId,
        type = mediaType,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        isFavorite = true
    )
}

private fun MediaSummary.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        mediaId = id,
        mediaType = type,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        addedAtMillis = System.currentTimeMillis()
    )
}

private fun MediaDetail.toFavoriteEntity(): FavoriteEntity {
    return FavoriteEntity(
        mediaId = id,
        mediaType = type,
        title = title,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDate = releaseDate,
        voteAverage = voteAverage,
        addedAtMillis = System.currentTimeMillis()
    )
}
