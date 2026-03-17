package com.quotify.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quotify.core.data.mapper.toDomain
import com.quotify.core.data.network.APIService
import com.quotify.core.domain.model.Quote
import javax.inject.Inject

/*
* QuotesPagingSource loads items for paging. The [Int] is the paging key or query that is used to
* fetch the data, and the [QuoteAPIResponse] specifies the type of data that will be loaded.
* */
class QuotesPagingSource @Inject constructor(
    private val apiService: APIService
) : PagingSource<Int, Quote>() {

    /**
     * Loads a specific page of data based on the given key.
     *
     * The Paging library calls this method when it needs to load more data.
     * This implementation fetches data from the repository using the `query`
     * and the `key` (current page number).
     *
     * @param params Contains information about the requested load size and key.
     * @return A LoadResult object containing either the data or an error.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Quote> {
        return try {
            // Determine the next page to load. Defaults to 1 if the key is null.
            val nextPage = params.key ?: 1

            // Fetch the data from repository
            val quotesListAPIResponse = apiService.getQuotesList(pages = nextPage)

            // Convert & return the successfully loaded data as a LoadResult.Page
            if (quotesListAPIResponse.results.isNotEmpty()) {
                val quotesList = quotesListAPIResponse.results.map { it.toDomain() }
                LoadResult.Page(
                    data = quotesList,
                    prevKey = if (nextPage == 1) null else nextPage - 1,
                    nextKey = nextPage.plus(1)
                )
            } else
                LoadResult.Error(Exception("No data found"))

        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    /**
     * Returns the key for the next page to be loaded when refreshing.
     *
     * This method is used by the Paging library to determine the starting point
     * for loading data when the user performs a refresh action (e.g., swipe-to-refresh).
     *
     * Its goal is to ensure the user stays at the same scroll position even after the data is reloaded.
     * Without this logic, every time you refreshed, it would jump back to the very top (Page 1).
     * With this, the Paging library reloads the page the user is currently seeing, providing a
     * seamless "stay in place" experience.
     *
     * @param state The current state of the Paging system.
     * @return The page key to refresh from or null if no valid refresh key exists.
     */
    override fun getRefreshKey(state: PagingState<Int, Quote>): Int {
        // Try to find the page key of the closest page to the anchor position.
        // The anchor position is the most recently accessed index.
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey are null -> anchorPage is the
        //    initial page, so return null.
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        } ?: 1
    }
}