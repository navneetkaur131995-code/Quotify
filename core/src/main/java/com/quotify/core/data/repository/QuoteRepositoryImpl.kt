package com.quotify.core.data.repository

import com.quotify.core.data.mapper.toDomain
import com.quotify.core.data.network.APIService
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import jakarta.inject.Inject

class QuoteRepositoryImpl @Inject constructor(
    private val apiService: APIService
) : QuoteRepository {

    override suspend fun getQuotes(): List<Quote> {
        val response = apiService.getQuotesList()
        val quotes = response.results.map {
            it.toDomain()
        }
        return quotes
    }
}