package com.sortedqueue.portfolio.core.database.di

import android.content.Context
import androidx.room.Room
import com.sortedqueue.portfolio.core.database.FavoritesDao
import com.sortedqueue.portfolio.core.database.PortfolioMoviesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): PortfolioMoviesDatabase {
        return Room.databaseBuilder(
            context,
            PortfolioMoviesDatabase::class.java,
            "portfolio-movies.db"
        ).build()
    }

    @Provides
    fun provideFavoritesDao(database: PortfolioMoviesDatabase): FavoritesDao {
        return database.favoritesDao()
    }
}
