package com.quotify.core.domain.usecase

import com.quotify.core.common.Outcome
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import javax.inject.Inject

/* TODO: Revisit, review pt.4, if really necessary: UseCase kept here to keep the separation of layers */
class GetSingleQuoteUseCase
@Inject
constructor(
    private val quoteRepository: QuoteRepository,
) {
    suspend operator fun invoke(quoteId: String): Outcome<Quote> =
        when (val result = quoteRepository.getSingleQuote(quoteId)) {
            is Outcome.Success -> result
            else -> {
                Outcome.Failure(Exception(result.toString()))
            }
        }
}
