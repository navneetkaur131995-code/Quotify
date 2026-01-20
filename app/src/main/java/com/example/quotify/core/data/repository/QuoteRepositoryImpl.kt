package com.example.quotify.core.data.repository

import com.example.quotify.core.data.mapper.toDomain
import com.example.quotify.core.domain.model.Quote
import com.example.quotify.core.domain.repository.QuoteRepository
import com.example.quotify.core.network.APIService
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