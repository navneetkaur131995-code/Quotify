package com.quotify.core.data.network

import com.quotify.core.data.network.model.QuotesListAPIResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface APIService{

    @GET("quotes")
    suspend fun getQuotesList(@Query("page") pages: Int = 1) : QuotesListAPIResponse
}