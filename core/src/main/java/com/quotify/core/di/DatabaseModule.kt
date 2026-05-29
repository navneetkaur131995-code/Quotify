package com.quotify.core.di

import android.content.Context
import androidx.room.Room
import com.quotify.core.data.localDatabase.QuotifyDao
import com.quotify.core.data.localDatabase.QuotifyDatabase
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

    // Room caches the DAO on the database instance, so no @Singleton needed.
    @Provides
    fun providesQuotifyDao(database: QuotifyDatabase): QuotifyDao = database.quotifyDao()
}
