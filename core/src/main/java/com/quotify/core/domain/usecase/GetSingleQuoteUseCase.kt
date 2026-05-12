package com.quotify.core.domain.usecase

import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import javax.inject.Inject

/**
 * Passthrough today. Kept as a seam for future business logic (e.g. tracking analytics,
 * combining with a favorites repository, applying user-specific filters). If no logic
 * lands here over time, will consider deleting it and injecting `QuoteRepository` directly
 * into the ViewModel as Android's official guidance discourages empty use cases.
 */
class GetSingleQuoteUseCase
    @Inject
    constructor(
        private val quoteRepository: QuoteRepository,
    ) {
        suspend operator fun invoke(quoteId: String): DomainResult<Quote> = quoteRepository.getSingleQuote(quoteId)
    }
