package com.example.quotify.core.network

import com.example.quotify.core.network.model.QuotesListAPIResponse
import retrofit2.http.GET

interface APIService{

    @GET("quotes")
    suspend fun getQuotesList() : QuotesListAPIResponse
}