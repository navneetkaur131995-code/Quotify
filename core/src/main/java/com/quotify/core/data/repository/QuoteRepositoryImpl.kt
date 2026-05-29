package com.quotify.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.quotify.core.common.DomainResult
import com.quotify.core.data.localDatabase.QuotifyDao
import com.quotify.core.data.mapper.toDomain
import com.quotify.core.data.paging.PagingConstants
import com.quotify.core.data.paging.QuoteRemoteMediator
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

class QuoteRepositoryImpl
    @Inject
    constructor(
        private val remoteMediator: QuoteRemoteMediator,
        private val quotifyDao: QuotifyDao,
    ) : QuoteRepository {
        @OptIn(ExperimentalPagingApi::class)
        override fun getQuotesStream(): Flow<PagingData<Quote>> =
            Pager(
                config =
                    PagingConfig(
                        pageSize = PagingConstants.PAGE_SIZE,
                        prefetchDistance = PagingConstants.PREFETCH_DISTANCE,
                        enablePlaceholders = false,
                    ),
                remoteMediator = remoteMediator,
                pagingSourceFactory = { quotifyDao.getQuotesPagingSource() },
            ).flow.map { pagingData -> pagingData.map { it.toDomain() } }

        // Filter out transient nulls: during REFRESH, clearNonFavorites can briefly leave the
        // row absent. Flipping the UI to Error in that window caused a visible flash. We
        // always navigate here from a list where the id was just present, so "row missing
        // forever" is unreachable in practice — the VM stays on Loading until a real value
        // arrives.
        override fun getSingleQuoteStream(id: String): Flow<DomainResult<Quote>> =
            quotifyDao
                .getQuoteById(id)
                .filterNotNull()
                .map { DomainResult.Success(it.toDomain()) }

        override suspend fun toggleFavoriteQuote(id: String): DomainResult<Unit> =
            try {
                quotifyDao.toggleFavorite(id)
                DomainResult.Success(Unit)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                DomainResult.Failure(e)
            }

        override fun observeFavoriteQuotes(): Flow<List<Quote>> =
            quotifyDao
                .observeFavoriteQuotes()
                .map { rows -> rows.map { it.toDomain() } }
    }
