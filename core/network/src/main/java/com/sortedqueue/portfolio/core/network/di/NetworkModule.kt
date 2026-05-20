package com.sortedqueue.portfolio.core.network.di

import android.content.Context
import com.sortedqueue.portfolio.core.network.BuildConfig
import com.sortedqueue.portfolio.core.network.debug.NetworkDebugTools
import com.sortedqueue.portfolio.core.network.TmdbApi
import com.sortedqueue.portfolio.core.network.TmdbAuthorizationInterceptor
import com.sortedqueue.portfolio.core.network.TolerantGzipInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(TmdbAuthorizationInterceptor(BuildConfig.TMDB_ACCESS_TOKEN))

        NetworkDebugTools.addDebugInterceptors(builder, context)

        return builder
            .addInterceptor(loggingInterceptor)
            .addInterceptor(TolerantGzipInterceptor())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.TMDB_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideTmdbApi(retrofit: Retrofit): TmdbApi {
        return retrofit.create(TmdbApi::class.java)
    }
}
