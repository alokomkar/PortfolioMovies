package com.sortedqueue.portfolio.core.testing

import com.sortedqueue.portfolio.core.database.FavoriteEntity
import com.sortedqueue.portfolio.core.database.FavoritesDao
import com.sortedqueue.portfolio.core.model.MediaType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeFavoritesDao(
    initialFavorites: List<FavoriteEntity> = emptyList()
) : FavoritesDao {
    private val favorites = MutableStateFlow(initialFavorites)

    override fun observeFavorites(): Flow<List<FavoriteEntity>> = favorites

    override fun observeIsFavorite(mediaId: Int, mediaType: MediaType): Flow<Boolean> {
        return favorites.map { items ->
            items.any { it.mediaId == mediaId && it.mediaType == mediaType }
        }
    }

    override suspend fun getFavorite(mediaId: Int, mediaType: MediaType): FavoriteEntity? {
        return favorites.value.firstOrNull { it.mediaId == mediaId && it.mediaType == mediaType }
    }

    override suspend fun deleteFavorite(mediaId: Int, mediaType: MediaType) {
        favorites.value = favorites.value.filterNot { it.mediaId == mediaId && it.mediaType == mediaType }
    }

    override suspend fun upsertFavorite(favorite: FavoriteEntity) {
        favorites.value = favorites.value
            .filterNot { it.mediaId == favorite.mediaId && it.mediaType == favorite.mediaType } + favorite
    }

    override suspend fun deleteFavorite(favorite: FavoriteEntity) {
        deleteFavorite(favorite.mediaId, favorite.mediaType)
    }
}
