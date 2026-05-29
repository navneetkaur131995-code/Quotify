package com.quotify.feature.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetFavoriteQuotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
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
        getFavoriteQuotesUseCase: GetFavoriteQuotesUseCase,
    ) : ViewModel() {
        // Reactive: the favorites screen updates automatically whenever a favorite is
        // toggled elsewhere in the app (no manual refresh function needed).
        val uiState: StateFlow<FavoritesUiState> =
            getFavoriteQuotesUseCase()
                .map<List<Quote>, FavoritesUiState> { FavoritesUiState.Success(it) }
                .onStart { emit(FavoritesUiState.Loading) }
                .catch { e -> emit(FavoritesUiState.Error(e.message ?: "Failed to load favorites")) }
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = FavoritesUiState.Loading,
                )

        private companion object {
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        }
    }
