package com.quotify.feature.quoteDetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetQuoteDetailUseCase
import com.quotify.core.domain.usecase.ToggleFavoriteUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

@HiltViewModel
class QuoteDetailViewModel
    @Inject
    constructor(
        private val getQuoteDetailUseCase: GetQuoteDetailUseCase,
        private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    ) : ViewModel() {
        // WHY MutableStateFlow<String?> instead of a plain String parameter?
        //
        // Navigation3 constructs the ViewModel before the entry builder runs, so there
        // is no constructor slot to pass the quoteId at creation time. We need a way to
        // feed the ID into the reactive pipeline *after* the ViewModel exists.
        // A MutableStateFlow<String?> acts as that bridge: it starts null (no ID yet),
        // and the entry builder emits the real ID exactly once via setQuoteId().
        // The rest of the pipeline reacts to that emission automatically.
        private val quoteIdFlow = MutableStateFlow<String?>(null)

        // WHY this pipeline instead of viewModelScope.launch { flow.collect { ... } }?
        //
        // The old approach — launch { getQuoteDetailUseCase(id).collect { _uiState.value = ... } }
        // — had a subtle but serious bug: if fetchQuoteDetails() was called a second time
        // (e.g. after an error, or if the entry builder recomposed), a *second* coroutine
        // was launched while the first was still collecting the infinite Room Flow.
        // Two coroutines now race to write _uiState, producing unpredictable UI updates.
        //
        // This pipeline fixes that with three operators working together:
        //
        // 1. filterNotNull()
        //    Skips the initial null value so the use case is never called with an empty ID.
        //    The UI stays on Loading until a real ID arrives.
        //
        // 2. flatMapLatest { id -> getQuoteDetailUseCase(id) }
        //    For each new ID emitted by quoteIdFlow, this cancels the previous inner Flow
        //    and starts a new one. Since setQuoteId() is a no-op when the ID hasn't changed,
        //    in practice only one inner Flow ever runs at a time — but flatMapLatest gives
        //    us that guarantee structurally, not by relying on a manual guard.
        //
        // 3. stateIn(WhileSubscribed(5_000L), initialValue = Loading)
        //    Converts the cold pipeline into a hot StateFlow that the UI can collect.
        //    - initialValue = Loading means the UI always has a valid state to render,
        //      even before the first Room emission arrives.
        //    - WhileSubscribed(5_000L) keeps the upstream Flow (and the Room query) alive
        //      for 5 seconds after the last subscriber disappears. This tolerates screen
        //      rotation and back-stack transitions without tearing down and restarting the
        //      database query — the user never sees a loading flash on return.
        //    - When all subscribers are gone for longer than 5 seconds (e.g. the user
        //      navigated away permanently), the coroutine is cancelled and Room stops
        //      watching the row, freeing resources.
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
                    started = SharingStarted.WhileSubscribed(5_000L),
                    initialValue = QuoteDetailUiState.Loading,
                )

        fun setQuoteId(quoteId: String) {
            // No-op if the ID is already set to the same value.
            // Prevents flatMapLatest from cancelling and restarting the Room query
            // on every recomposition of the entry builder.
            if (quoteIdFlow.value == quoteId) return
            quoteIdFlow.value = quoteId
        }

        fun toggleFavorite(quote: Quote) {
            viewModelScope.launch {
                toggleFavoriteUseCase(quote.id, quote.favorite)
            }
        }
    }
