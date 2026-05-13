package com.quotify.feature.home

import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.paging.compose.collectAsLazyPagingItems
import com.quotify.core.navigation.LocalNavigator
import com.quotify.feature.quoteDetails.QuoteDetailNavKey

/*
*  Extension function the app's entryProvider DSL calls.
*  Keeps HomeScreen internal and lets this module own how its keys map to content.
*
*  Navigation lives here, not inside HomeScreen.
*  HomeScreen receives a plain (String) -> Unit lambda — it never imports Navigator
*  or QuoteDetailNavKey, so it stays independently testable and previewable.
* */

fun EntryProviderScope<NavKey>.homeEntries() {
    entry<HomeNavKey.QuoteList> {
        val viewModel: HomeViewModel = hiltViewModel()

        val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()
        val pagingData = viewModel.pagingDataFlow.collectAsLazyPagingItems()

        // Read the navigator here in the entry builder, where composition locals
        // are available and navigation is an appropriate concern.
        val navigator = LocalNavigator.current

        HomeScreen(
            lazyPagingItems = pagingData,
            isOnline = isOnline,
            // The entry builder owns the routing decision: quoteId → QuoteDetailNavKey.
            // HomeScreen just calls onQuoteClick(id) — it doesn't know what happens next.
            onQuoteClick = { quoteId -> navigator.navigate(QuoteDetailNavKey(quoteId)) },
        )
    }
}
