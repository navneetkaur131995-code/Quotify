package com.quotify.feature.home

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.quotify.core.navigation.LocalNavigator
import com.quotify.feature.favorites.FavoritesScreen
import com.quotify.feature.favorites.FavoritesViewModel
import com.quotify.feature.quotedetails.QuoteDetailNavKey

/*
 * Routing layer: NavKey → screen wiring. Screens themselves never import the Navigator
 * or other features' NavKeys — they receive plain lambdas, which keeps them previewable
 * and unit-testable in isolation.
 */
fun EntryProviderScope<NavKey>.homeEntries() {
    entry<HomeNavKeys.QuoteList> {
        val viewModel: HomeViewModel = hiltViewModel()

        val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
        val pagingData = viewModel.pagingDataFlow.collectAsLazyPagingItems()

        val navigator = LocalNavigator.current

        HomeScreen(
            lazyPagingItems = pagingData,
            isOnline = isOnline,
            onQuoteClick = { quoteId -> navigator.navigate(QuoteDetailNavKey(quoteId)) },
        )
    }

    entry<HomeNavKeys.Favorites> {
        // Favorites are observed reactively in the ViewModel via observeFavoriteQuotes(),
        // so no manual refresh trigger is needed here.
        val viewModel: FavoritesViewModel = hiltViewModel()
        val navigator = LocalNavigator.current
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()

        FavoritesScreen(uiState) { quoteId -> navigator.navigate(QuoteDetailNavKey(quoteId)) }
    }
}
