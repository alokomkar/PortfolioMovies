package com.sortedqueue.portfolio.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.sortedqueue.portfolio.core.model.MediaType
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites ORDER BY addedAtMillis DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId AND mediaType = :mediaType)")
    fun observeIsFavorite(mediaId: Int, mediaType: MediaType): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE mediaId = :mediaId AND mediaType = :mediaType LIMIT 1")
    suspend fun getFavorite(mediaId: Int, mediaType: MediaType): FavoriteEntity?

    @Query("DELETE FROM favorites WHERE mediaId = :mediaId AND mediaType = :mediaType")
    suspend fun deleteFavorite(mediaId: Int, mediaType: MediaType)

    @Upsert
    suspend fun upsertFavorite(favorite: FavoriteEntity)

    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
}
