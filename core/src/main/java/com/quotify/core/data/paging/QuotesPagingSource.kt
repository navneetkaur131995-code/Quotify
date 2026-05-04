package com.quotify.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quotify.core.data.mapper.toDomain
import com.quotify.core.data.network.model.QuotesListAPIResponse
import com.quotify.core.domain.model.Quote

/*
 * QuotesPagingSource loads pages of quotes using skip/limit pagination.
 * Depends on a suspend lambda so the repository controls how data is fetched —
 * network-only today, network+cache tomorrow, no changes needed here.
 */
class QuotesPagingSource(
    private val fetchQuotes: suspend (skip: Int, limit: Int) -> QuotesListAPIResponse,
) : PagingSource<Int, Quote>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Quote> =
        try {
            val skip = params.key ?: 0
            val limit = params.loadSize

            val response = fetchQuotes(skip, limit)
            val quotes = response.quotes.map { it.toDomain() }

            LoadResult.Page(
                data = quotes,
                prevKey = if (skip == 0) null else (skip - limit).coerceAtLeast(0),
                nextKey = if (skip + limit >= response.total) null else skip + limit,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }

    /**
     * Returns the skip offset to reload from when refreshing, so the user
     * stays near their current scroll position rather than jumping to the top.
     */
    override fun getRefreshKey(state: PagingState<Int, Quote>): Int =
        state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(state.config.pageSize)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(state.config.pageSize)
        } ?: 0
}
