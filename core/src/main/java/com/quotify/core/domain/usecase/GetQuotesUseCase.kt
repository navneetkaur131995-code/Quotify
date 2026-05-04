package com.quotify.core.domain.usecase

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetQuotesUseCase
    @Inject
    constructor(
        private val quoteRepository: QuoteRepository,
    ) {
        operator fun invoke(): Flow<PagingData<Quote>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = 20,
                        prefetchDistance = 2,
                        enablePlaceholders = false,
                    ),
                pagingSourceFactory = { quoteRepository.getQuotesPagingSource() },
            ).flow
    }
