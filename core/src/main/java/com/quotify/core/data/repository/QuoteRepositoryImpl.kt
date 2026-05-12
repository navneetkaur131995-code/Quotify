package com.quotify.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.quotify.core.common.Outcome
import com.quotify.core.data.localDatabase.QuoteRemoteMediator
import com.quotify.core.data.localDatabase.QuotifyDAO
import com.quotify.core.data.localDatabase.QuotifyDatabase
import com.quotify.core.data.mapper.toDomain
import com.quotify.core.data.network.APIService
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class QuoteRepositoryImpl
    @Inject
    constructor(
        private val apiService: APIService,
        private val database: QuotifyDatabase,
        private val quotifyDAO: QuotifyDAO,
    ) : QuoteRepository {
        @OptIn(ExperimentalPagingApi::class)
        override fun getPager(): Flow<PagingData<Quote>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = 20,
                        prefetchDistance = 5, // Load next page when 5 items from the end
                        enablePlaceholders = false,
                    ),
                // The RemoteMediator fetches from network → writes to Room
                remoteMediator = QuoteRemoteMediator(apiService, database),
                // The PagingSource reads from Room → drives the UI
                // .map converts QuoteEntity → Quote so the UI sees domain types
                pagingSourceFactory = {
                    // Can't convert directly here as the data type we're dealing with
                    // should be similar to Remote mediator's
                    quotifyDAO.pagingSource()
                },
            ).flow.map { pagingData ->
                // So map or convert the data here
                pagingData.map { quoteEntity -> quoteEntity.toDomain() }
            }

        override suspend fun getSingleQuote(id: String): Outcome<Quote> =
            try {
                val quoteEntity = quotifyDAO.getQuoteById(id)
                if (quoteEntity != null) {
                    Outcome.Success(quoteEntity.toDomain())
                } else {
                    Outcome.Failure(Exception("Quote with id $id not found in cache"))
                }
            } catch (e: Exception) {
                Outcome.Failure(e)
            }
    }
