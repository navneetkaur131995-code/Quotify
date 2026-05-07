package com.quotify.feature.quoteDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.common.Outcome
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class QuoteDetailUiState {
    data object Loading : QuoteDetailUiState()

    data class Success(
        val quote: Quote,
    ) : QuoteDetailUiState()

    data class Error(
        val message: String,
    ) : QuoteDetailUiState()
}

@HiltViewModel
class QuoteDetailViewModel
    @Inject
    constructor(
        private val getQuotesUseCase: GetQuotesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<QuoteDetailUiState>(QuoteDetailUiState.Loading)
        val uiState = _uiState.asStateFlow()

        fun fetchQuoteDetails(quoteId: String) {
            viewModelScope.launch {
                _uiState.value = QuoteDetailUiState.Loading

                when (val result = getQuotesUseCase.getSingleQuote(quoteId)) {
                    is Outcome.Success -> {
                        _uiState.value = QuoteDetailUiState.Success(result.data)
                    }

                    is Outcome.Failure -> {
                        _uiState.value = QuoteDetailUiState.Error("Something went wrong")
                    }

                    else -> {
                        _uiState.value = QuoteDetailUiState.Error("Unknown outcome")
                    }
                }
            }
        }
    }
