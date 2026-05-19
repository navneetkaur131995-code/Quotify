package com.quotify.core.domain.usecase

import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import javax.inject.Inject

class GetFavoriteQuotesUseCase
    @Inject
    constructor(
        private val quoteRepository: QuoteRepository,
    ) {
        suspend operator fun invoke(): DomainResult<List<Quote>> = quoteRepository.getFavoriteQuotes()
    }
