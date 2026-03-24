package com.quotify.core.domain.usecase

import com.quotify.core.common.Outcome
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetQuotesUseCase @Inject constructor(
    private val quoteRepository: QuoteRepository
) {

    operator fun invoke(): Flow<Outcome<List<Quote>>> = flow {
        try {
            val quotes = quoteRepository.getQuotes()
            emit(Outcome.Success(quotes))
        } catch (e: Exception) {
            emit(Outcome.Failure(e))
        }
    }
}