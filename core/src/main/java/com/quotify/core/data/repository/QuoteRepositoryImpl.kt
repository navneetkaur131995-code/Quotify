package com.quotify.core.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quotify.core.data.network.APIService
import com.quotify.core.data.paging.QuotesPagingSource
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class QuoteRepositoryImpl @Inject constructor(
    private val apiService: APIService
) : QuoteRepository {

    override fun getQuotes(): Flow<PagingData<Quote>> = Pager(
        config = PagingConfig(
            pageSize = 20,
            // Prefetch distance which defines how far from the edge of loaded content an access
            // must be to trigger further loading.
            prefetchDistance = 2,
            enablePlaceholders = false
        ),
        pagingSourceFactory = {
            QuotesPagingSource(apiService)
        }).flow
}