package com.example.quotify.core.domain.repository

import com.example.quotify.core.domain.model.Quote

interface QuoteRepository {
    suspend fun getQuotes(): List<Quote>
}