package com.quotify.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.quotify.core.domain.connectivity.NetworkMonitor
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
        // cachedIn keeps PagingData alive across configuration changes (rotation, etc.).
        val pagingDataFlow = getQuotesUseCase().cachedIn(viewModelScope)

        // Nullable so the UI can distinguish "not yet known" from "definitely offline".
        // This avoids a brief offline-banner flash on cold start: the banner suppresses
        // until we have a real reading.
        val isOnline: StateFlow<Boolean?> =
            networkMonitor.isOnline
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = null,
                )

        private companion object {
            // 5s tolerates rotation/back-stack churn without tearing down the callback.
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        }
    }
