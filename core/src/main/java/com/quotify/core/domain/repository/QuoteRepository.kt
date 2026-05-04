package com.quotify.core.domain.repository

import androidx.paging.PagingSource
import com.quotify.core.domain.model.Quote

interface QuoteRepository {
    fun getQuotesPagingSource(): PagingSource<Int, Quote>
}
