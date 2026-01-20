package com.example.quotify.core.network

import com.example.quotify.core.network.model.QuotesResponseDto
import retrofit2.http.GET

interface APIService{

    @GET("quotes")
    suspend fun getQuotesList() : QuotesResponseDto
}