package com.quotify.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetFavoriteQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface FavoritesUiState {
    data object Loading : FavoritesUiState

    data class Success(
        val quotes: List<Quote>,
    ) : FavoritesUiState

    data class Error(
        val message: String,
    ) : FavoritesUiState
}

@HiltViewModel
class FavoritesViewModel
    @Inject
    constructor(
        private val getFavoriteQuotesUseCase: GetFavoriteQuotesUseCase,
    ) : ViewModel() {
        private val _uiState: MutableStateFlow<FavoritesUiState> = MutableStateFlow(FavoritesUiState.Loading)
        val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

        fun getFavoriteQuotes() {
            _uiState.value = FavoritesUiState.Loading
            viewModelScope.launch {
                when (val result = getFavoriteQuotesUseCase()) {
                    is DomainResult.Success -> {
                        _uiState.value = FavoritesUiState.Success(result.data)
                    }

                    is DomainResult.Failure -> {
                        _uiState.value = FavoritesUiState.Error(result.error.message ?: "An unknown error occurred")
                    }
                }
            }
        }
    }
