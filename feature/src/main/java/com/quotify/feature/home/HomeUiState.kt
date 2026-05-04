package com.quotify.feature.home

import com.quotify.core.domain.model.Quote

data class HomeUiState(
    val isLoading: Boolean = false,
    val success: List<Quote> = emptyList(),
    val error: String? = null,
)
