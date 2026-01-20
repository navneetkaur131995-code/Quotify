package com.example.quotify.core.domain.repository

import com.example.quotify.core.common.Result
import com.example.quotify.core.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getQuotes(): Flow<Result<List<Quote>>>
}