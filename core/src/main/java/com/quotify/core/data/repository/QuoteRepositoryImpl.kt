package com.quotify.core.data.repository

import androidx.paging.PagingSource
import com.quotify.core.common.DomainResult
import com.quotify.core.data.mapper.toDomain
import com.quotify.core.data.network.APIService
import com.quotify.core.data.paging.QuotesPagingSource
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import javax.inject.Inject

class QuoteRepositoryImpl
    @Inject
    constructor(
        private val apiService: APIService,
    ) : QuoteRepository {
        override fun getQuotesPagingSource(): PagingSource<Int, Quote> =
            QuotesPagingSource(
                fetchQuotes = { skip, limit -> apiService.getQuotesList(limit = limit, skip = skip) },
            )

        override suspend fun getSingleQuote(id: String): DomainResult<Quote> =
            try {
                val result = apiService.getSingleQuote(id)
                DomainResult.Success(result.toDomain())
            } catch (e: Exception) {
                DomainResult.Failure(e)
            }
    }
