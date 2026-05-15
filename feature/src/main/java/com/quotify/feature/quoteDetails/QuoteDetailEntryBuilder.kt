package com.quotify.feature.quoteDetails

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

fun EntryProviderScope<NavKey>.quoteDetailEntries() {
    entry<QuoteDetailNavKey> { key ->

        val viewModel: QuoteDetailViewModel = hiltViewModel()

//        Benefits of this:
//        1. Type Safety: You are using the QuoteDetailNavKey (a Kotlin data class) directly.
//        This is much safer than relying on a String key inside a SavedStateHandle map which can easily have typos.
//        2. Explicitness: It follows the "Single Source of Truth" principle. The Navigation Key is the source of the
//        quoteId. Passing it directly from the key to the ViewModel function is more transparent than hiding it inside
//        a SavedStateHandle.
//        3. Testability: You can now test QuoteDetailViewModel by simply calling viewModel.fetchQuoteDetails("id") in
//        a JUnit test, without having to mock a SavedStateHandle or set up complex CreationExtras.
//        Summary of the Flow
//            1. Navigation Key (Data) ->
//            2. EntryBuilder (Logic Glue/Router) ->
//            3. ViewModel.fetch (State Transition) ->
//            4.UseCase (Business Logic) ->
//            5.UI (Render)

        LaunchedEffect(key.quoteId) {
            viewModel.fetchQuoteDetails(key.quoteId)
        }

        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        QuoteDetailScreen(uiState = uiState, onFavoriteToggle = { quote -> viewModel.toggleFavorite(quote) })
    }
}
