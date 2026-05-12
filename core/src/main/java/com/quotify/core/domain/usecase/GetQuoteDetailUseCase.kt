package com.quotify.core.domain.usecase

import com.quotify.core.common.Outcome
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import javax.inject.Inject

class GetQuoteDetailUseCase
    @Inject
    constructor(
        private val quoteRepository: QuoteRepository,
    ) {
        suspend operator fun invoke(quoteId: String): Outcome<Quote> = quoteRepository.getSingleQuote(quoteId)
    }
