package com.example.quotify.core.data.repository

import com.example.quotify.core.common.Result
import com.example.quotify.core.data.mapper.toDomain
import com.example.quotify.core.domain.model.Quote
import com.example.quotify.core.domain.repository.QuoteRepository
import com.example.quotify.core.network.APIService
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class QuoteRepositoryImpl @Inject constructor(
    private val apiService: APIService
) : QuoteRepository {

    override fun getQuotes(): Flow<Result<List<Quote>>> {
        return flow {
            emit(Result.Loading)

            try {
                val response = apiService.getQuotesList()
                val quotes = response.results.map {
                    it.toDomain()
                }
                emit(Result.Success(quotes))
            } catch (e: Exception) {
                emit(Result.Failure(e))
            }
        }
    }
}