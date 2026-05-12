package com.quotify.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.quotify.core.domain.NetworkMonitor
import com.quotify.core.domain.usecase.GetQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        getQuotesUseCase: GetQuotesUseCase,
        networkMonitor: NetworkMonitor,
    ) : ViewModel() {
        // Define the PagingData Flow
        // We use 'cachedIn' so the data survives configuration changes (like rotation).
        val pagingDataFlow = getQuotesUseCase().cachedIn(viewModelScope)

        val isOnline: StateFlow<Boolean> =
            networkMonitor.isOnline
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = true,
                )

        private companion object {
            // 5s tolerates rotation/back-stack churn without tearing down the callback.
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        }
    }
