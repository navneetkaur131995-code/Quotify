package com.quotify.core.data.network

import com.quotify.core.data.network.model.QuotesListAPIResponse
import retrofit2.http.GET

interface APIService{

    @GET("quotes")
    suspend fun getQuotesList() : QuotesListAPIResponse
}