package com.quotify.core.data.network.model

data class QuotesListAPIResponse(
    val quotes: List<QuoteAPIResponse>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)
