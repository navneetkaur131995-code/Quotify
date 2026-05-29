package com.quotify.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quotify.core.data.localDatabase.QuoteEntity
import com.quotify.core.data.localDatabase.QuotifyDatabase
import com.quotify.core.data.mapper.toEntity
import com.quotify.core.data.network.ApiService
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalPagingApi::class)
class QuoteRemoteMediator
    @Inject
    constructor(
        private val api: ApiService,
        private val database: QuotifyDatabase,
    ) : RemoteMediator<Int, QuoteEntity>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, QuoteEntity>,
        ): MediatorResult {
            return try {
                val skip =
                    when (loadType) {
                        LoadType.REFRESH -> 0

                        LoadType.PREPEND ->
                            // API doesn't support loading items before the first one.
                            return MediatorResult.Success(endOfPaginationReached = true)

                        LoadType.APPEND ->
                            // Count loaded items, not pages — pages can have varying sizes.
                            state.pages.sumOf { it.data.size }
                    }

                // Honor Paging's `initialLoadSize` (default 3 × pageSize) to avoid three
                // round-trips on cold start. APPEND uses the normal pageSize.
                val limit =
                    if (loadType == LoadType.REFRESH) state.config.initialLoadSize else state.config.pageSize

                val response = api.getQuotesList(limit = limit, skip = skip)
                val entities = response.quotes.map { it.toEntity() }

                database.withTransaction {
                    val dao = database.quotifyDao()
                    if (loadType == LoadType.REFRESH) {
                        // Snapshot favorites BEFORE the wipe so the REPLACE on insertAll
                        // doesn't lose them when ids overlap.
                        val preserved = dao.getFavoriteIds()
                        dao.clearNonFavorites()
                        dao.insertAll(entities)
                        if (preserved.isNotEmpty()) dao.markFavorites(preserved)
                    } else {
                        // APPEND: brand-new ids as favorites aren't at risk
                        dao.insertAll(entities)
                    }
                }

                MediatorResult.Success(endOfPaginationReached = entities.isEmpty())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                MediatorResult.Error(e)
            }
        }
    }
