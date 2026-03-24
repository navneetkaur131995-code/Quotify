package com.quotify.core.domain.repository

import com.quotify.core.domain.model.Quote

interface QuoteRepository {
    suspend fun getQuotes(): List<Quote>
}