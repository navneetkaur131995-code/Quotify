package com.quotify.core.di

import com.quotify.core.data.repository.QuoteRepositoryImpl
import com.quotify.core.domain.repository.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindsQuoteRepository(quoteRepositoryImpl: QuoteRepositoryImpl): QuoteRepository
}
