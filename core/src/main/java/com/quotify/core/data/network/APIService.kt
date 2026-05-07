package com.quotify.core.data.network

import com.quotify.core.data.network.model.QuoteAPIResponse
import com.quotify.core.data.network.model.QuotesListAPIResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {
    @GET("quotes")
    suspend fun getQuotesList(
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0,
    ): QuotesListAPIResponse

    @GET("quotes/{id}")
    suspend fun getSingleQuote(
        @Path("id") id: String = "0",
    ): QuoteAPIResponse
}
