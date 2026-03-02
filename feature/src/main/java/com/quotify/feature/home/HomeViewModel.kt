package com.quotify.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.common.Outcome
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getQuotesUseCase: GetQuotesUseCase
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _quotes: MutableStateFlow<List<Quote>> = MutableStateFlow(emptyList())
    val quotes: Flow<List<Quote>> = _quotes

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    init {
        getQuotes()
    }

    private fun getQuotes() {
        _isLoading.value = true
        viewModelScope.launch {
            getQuotesUseCase().collect {
                when (it) {
                   is Outcome.Failure-> _errorMessage.value = it.throwable.message.toString()
                    is Outcome.Success -> _quotes.value = it.data
                    else -> _isLoading.value = true
                }
            }
        }
    }

}