package com.quotify.feature.quotedetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetQuoteDetailUseCase
import com.quotify.core.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
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

// One-shot effects (snackbars, toasts, navigation). Modeled as a Channel so events
// aren't lost across configuration changes and aren't re-delivered on re-collection.
sealed interface QuoteDetailEffect {
    data class ShowError(
        val message: String,
    ) : QuoteDetailEffect
}

@HiltViewModel
class QuoteDetailViewModel
    @Inject
    constructor(
        private val getQuoteDetailUseCase: GetQuoteDetailUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    ) : ViewModel() {
        // Navigation3 constructs the ViewModel before the entry builder runs, so we can't
        // receive the quoteId as a constructor argument. A nullable MutableStateFlow acts
        // as the bridge: starts null, set exactly once when the entry builder fires its
        // LaunchedEffect, and the reactive pipeline below reacts.
        private val quoteIdFlow = MutableStateFlow<String?>(null)

        // filterNotNull → flatMapLatest → stateIn:
        // - filterNotNull skips the initial null so the use case isn't called with an empty id.
        // - flatMapLatest cancels any prior inner Flow when the id changes, guaranteeing
        //   at most one active Room collection — structurally, not via manual guards.
        // - stateIn(WhileSubscribed 5s) keeps the Room query alive across rotation/back-stack
        //   churn without re-issuing the query.
        @OptIn(ExperimentalCoroutinesApi::class)
        val uiState: StateFlow<QuoteDetailUiState> =
            quoteIdFlow
                .filterNotNull()
                .flatMapLatest { id -> getQuoteDetailUseCase(id) }
                .map { result ->
                    when (result) {
                        is DomainResult.Success -> QuoteDetailUiState.Success(result.data)
                        is DomainResult.Failure ->
                            QuoteDetailUiState.Error(result.error.message ?: "Something went wrong")
                    }
                }.stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
                    initialValue = QuoteDetailUiState.Loading,
                )

        // Channel (not SharedFlow) because effects are point-in-time events: each one is
        // consumed exactly once. A SharedFlow would re-deliver to late subscribers.
        private val _effects = Channel<QuoteDetailEffect>(Channel.BUFFERED)
        val effects: Flow<QuoteDetailEffect> = _effects.receiveAsFlow()

        fun setQuoteId(quoteId: String) {
            // No-op when unchanged so flatMapLatest doesn't restart Room on re-entry.
            if (quoteIdFlow.value == quoteId) return
            quoteIdFlow.value = quoteId
        }

        fun toggleFavorite(quote: Quote) {
            viewModelScope.launch {
                when (val result = toggleFavoriteUseCase(quote.id)) {
                    is DomainResult.Success -> Unit // Room flow re-emits with the new favorite state.
                    is DomainResult.Failure ->
                        _effects.send(
                            QuoteDetailEffect.ShowError(
                                result.error.message ?: "Failed to update favorite",
                            ),
                        )
                }
            }
        }

        private companion object {
            const val SUBSCRIPTION_TIMEOUT_MS = 5_000L
        }
    }
