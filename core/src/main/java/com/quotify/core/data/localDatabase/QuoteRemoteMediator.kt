package com.quotify.core.data.localDatabase

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quotify.core.data.mapper.toEntity
import com.quotify.core.data.network.APIService
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class QuoteRemoteMediator
    @Inject
    constructor(
        private val api: APIService,
        private val database: QuotifyDatabase,
    ) : RemoteMediator<Int, QuoteEntity>() {
        override suspend fun load(
            loadType: LoadType,
            state: PagingState<Int, QuoteEntity>,
        ): MediatorResult {
            return try {
                // Calculate how many items to skip
                val skip =
                    when (loadType) {
                        LoadType.REFRESH -> 0 // Start from the beginning

                        LoadType.PREPEND ->
                            // This API doesn't support loading items before the first one.
                            // So return success immediately so Paging stops trying.
                            return MediatorResult.Success(endOfPaginationReached = true)

                        LoadType.APPEND -> {
                            // FIXED: count total items loaded, not number of pages
                            // state.pages is a list of loaded pages.
                            // Each page has a .data list. We sum all their sizes.
                            // Example: 3 pages × 20 items = skip 60
                            state.pages.sumOf { it.data.size }
                        }
                    }

                // Fetch from the network
                val response =
                    api.getQuotesList(
                        limit = state.config.pageSize,
                        skip = skip,
                    )

                // Convert network DTOs directly to database entities
                // API → Entity
                val entities = response.quotes.map { it.toEntity() }

                // Write to Room atomically.
                // withTransaction means: if the insert fails halfway through,
                // the clearAll() is also rolled back. No partial state.
                database.withTransaction {
                    if (loadType == LoadType.REFRESH) {
                        // On refresh, wipe stale data first so old and new
                        // data never mix in the UI
                        database.quotifyDAO().clearAll()
                    }
                    database.quotifyDAO().insertAll(entities)
                }

                // If the API returned 0 items, we've reached the end of the list
                MediatorResult.Success(endOfPaginationReached = entities.isEmpty())
            } catch (e: Exception) {
                MediatorResult.Error(e)
            }
        }
    }
