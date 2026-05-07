package com.quotify.feature.quoteDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.common.Outcome
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetSingleQuoteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface QuoteDetailUiState {
    data object Loading : QuoteDetailUiState
    data class Success(val quote: Quote, ) : QuoteDetailUiState
    data class Error(val message: String, ) : QuoteDetailUiState
}

@HiltViewModel
class QuoteDetailViewModel
    @Inject
    constructor(
        private val getSingleQuoteUseCase: GetSingleQuoteUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<QuoteDetailUiState>(QuoteDetailUiState.Loading)
        val uiState = _uiState.asStateFlow()

        fun fetchQuoteDetails(quoteId: String) {
            if (_uiState.value is QuoteDetailUiState.Success) return

            viewModelScope.launch {
                when (val result = getSingleQuoteUseCase(quoteId)) {
                    is Outcome.Success -> {
                        _uiState.value = QuoteDetailUiState.Success(result.data)
                    }

                    is Outcome.Failure -> {
                        _uiState.value = QuoteDetailUiState.Error(result.throwable.message ?: "Something went wrong")
                    }

                    else -> {
                        _uiState.value = QuoteDetailUiState.Error("Unknown outcome")
                    }
                }
            }
        }
    }
