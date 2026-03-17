package com.quotify.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.quotify.core.domain.usecase.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getQuotesUseCase: GetQuotesUseCase
) : ViewModel() {

    // Define the PagingData Flow
    // We use 'cachedIn' so the data survives configuration changes (like rotation).
   val pagingDataFlow = getQuotesUseCase().cachedIn(viewModelScope)
}