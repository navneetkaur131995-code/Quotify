package com.example.quotify.core.network.model

data class QuotesResponseDto(
    val count: Int,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val lastItemIndex: Int,
    val results: List<QuoteDTO>
)