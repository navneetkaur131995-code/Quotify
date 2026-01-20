package com.example.quotify.core.domain.usecase

import com.example.quotify.core.common.Result
import com.example.quotify.core.domain.model.Quote
import com.example.quotify.core.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetQuotesUseCase @Inject constructor(
    private val quoteRepository: QuoteRepository
) {
    operator fun invoke(): Flow<Result<List<Quote>>> = flow {
        try {
            val quotes = quoteRepository.getQuotes()
            emit(Result.Success(quotes))
        } catch (e: Exception) {
            emit(Result.Failure(e))
        }
    }
}