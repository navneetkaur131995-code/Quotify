package com.quotify.core.data.network.model

data class QuotesListAPIResponse(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val lastItemIndex: Int,
    val results: List<QuoteAPIResponse>
)