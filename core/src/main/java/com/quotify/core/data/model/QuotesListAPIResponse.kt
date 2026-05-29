package com.quotify.core.data.model

data class QuotesListAPIResponse(
    val quotes: List<QuoteAPIResponse>,
    val total: Int,
    val skip: Int,
    val limit: Int,
)
