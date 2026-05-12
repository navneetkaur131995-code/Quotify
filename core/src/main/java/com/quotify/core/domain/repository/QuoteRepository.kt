package com.quotify.core.domain.repository

import androidx.paging.PagingSource
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote

interface QuoteRepository {
    fun getQuotesPagingSource(): PagingSource<Int, Quote> // acceptable pragmatic tradeoff

    suspend fun getSingleQuote(id: String): DomainResult<Quote>
}
