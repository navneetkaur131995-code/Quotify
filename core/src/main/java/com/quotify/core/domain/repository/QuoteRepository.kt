package com.quotify.core.domain.repository

import androidx.paging.PagingData
import com.quotify.core.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getQuotes(): Flow<PagingData<Quote>>
}