package com.quotify.core.data.network

import com.quotify.core.data.model.QuotesListAPIResponse
import com.quotify.core.data.paging.PagingConstants
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("quotes")
    suspend fun getQuotesList(
        @Query("limit") limit: Int = PagingConstants.PAGE_SIZE,
        @Query("skip") skip: Int = 0,
    ): QuotesListAPIResponse
}
