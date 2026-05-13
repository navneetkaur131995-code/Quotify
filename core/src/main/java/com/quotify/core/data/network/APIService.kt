package com.quotify.core.data.network

import com.quotify.core.data.model.QuoteAPIResponse
import com.quotify.core.data.model.QuotesListAPIResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @GET("quotes")
    suspend fun getQuotesList(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0,
    ): QuotesListAPIResponse
}
