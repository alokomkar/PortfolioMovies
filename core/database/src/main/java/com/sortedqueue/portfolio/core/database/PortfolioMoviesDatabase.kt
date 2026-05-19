package com.sortedqueue.portfolio.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class PortfolioMoviesDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}
