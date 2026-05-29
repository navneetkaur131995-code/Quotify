package com.quotify.core.domain.usecase

import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFavoriteQuotesUseCase
    @Inject
    constructor(
        private val quoteRepository: QuoteRepository,
    ) {
        operator fun invoke(): Flow<List<Quote>> = quoteRepository.observeFavoriteQuotes()
    }
