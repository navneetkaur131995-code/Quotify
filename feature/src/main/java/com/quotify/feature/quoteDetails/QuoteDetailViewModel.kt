package com.quotify.feature.quoteDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetQuoteDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface QuoteDetailUiState {
    data object Loading : QuoteDetailUiState

    data class Success(
        val quote: Quote,
    ) : QuoteDetailUiState

    data class Error(
        val message: String,
    ) : QuoteDetailUiState
}

@HiltViewModel
class QuoteDetailViewModel
    @Inject
    constructor(
        private val getQuoteDetailUseCase: GetQuoteDetailUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<QuoteDetailUiState>(QuoteDetailUiState.Loading)
        val uiState: StateFlow<QuoteDetailUiState> = _uiState

        fun fetchQuoteDetails(quoteId: String) {
            if (_uiState.value is QuoteDetailUiState.Success) return
            _uiState.value = QuoteDetailUiState.Loading

            viewModelScope.launch {
                getQuoteDetailUseCase(quoteId).collect { result ->
                    _uiState.value =
                        when (result) {
                            is DomainResult.Success -> QuoteDetailUiState.Success(result.data)
                            is DomainResult.Failure ->
                                QuoteDetailUiState.Error(result.error.message ?: "Something went wrong")
                        }
                }
            }
        }

        fun toggleFavorite(quote: Quote) {
            viewModelScope.launch {
                getQuoteDetailUseCase.toggleFavorites(quote.id, quote.favorite)
            }
        }
    }
