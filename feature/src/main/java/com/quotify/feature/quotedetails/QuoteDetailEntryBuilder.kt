package com.quotify.feature.quotedetails

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/*
 * Entry builder is the "router/glue" layer:
 *   NavKey (data) → entry builder (decode + wire) → ViewModel (state) → screen (render).
 *
 * Two side effects to be careful about here:
 *   1. setQuoteId MUST go through a LaunchedEffect as mutating ViewModel state directly in
 *      composition is a Compose antipattern even when the VM guards against re-entry.
 *   2. Effect collection (snackbars) MUST go through a LaunchedEffect keyed on the VM so
 *      it survives recomposition but stops when the entry leaves the back stack.
 */
fun EntryProviderScope<NavKey>.quoteDetailEntries() {
    entry<QuoteDetailNavKey> { key ->
        val viewModel: QuoteDetailViewModel = hiltViewModel()

        LaunchedEffect(key.quoteId) { viewModel.setQuoteId(key.quoteId) }

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val snackBarHostState = remember { SnackbarHostState() }

        LaunchedEffect(viewModel) {
            viewModel.effects.collect { effect ->
                when (effect) {
                    is QuoteDetailEffect.ShowError -> snackBarHostState.showSnackbar(effect.message)
                }
            }
        }

        QuoteDetailScreen(
            uiState = uiState,
            snackbarHostState = snackBarHostState,
            onFavoriteToggle = viewModel::toggleFavorite,
        )
    }
}
