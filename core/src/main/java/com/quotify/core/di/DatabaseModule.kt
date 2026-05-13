package com.quotify.core.di

import android.content.Context
import androidx.room.Room
import com.quotify.core.data.localDatabase.QuotifyDAO
import com.quotify.core.data.localDatabase.QuotifyDatabase
import com.quotify.core.data.network.APIService
import com.quotify.core.data.paging.QuoteRemoteMediator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): QuotifyDatabase =
        Room
            .databaseBuilder(
                context,
                QuotifyDatabase::class.java,
                "quotify_database",
            ).build()

    @Provides
    @Singleton // Only one DAO instance as it's just a thin wrapper anyway
    fun providesQuotifyDao(database: QuotifyDatabase): QuotifyDAO = database.quotifyDAO()

    @Provides
    @Singleton
    fun providesRemoteMediator(
        apiService: APIService,
        database: QuotifyDatabase,
    ): QuoteRemoteMediator = QuoteRemoteMediator(apiService, database)
}
