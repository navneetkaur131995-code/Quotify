package com.quotify.feature.home

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quotify.core.navigation.LocalNavigator
import com.quotify.feature.quoteDetails.QuoteDetailNavKey

/*
*  Extension funcion the app's entryProvider DSL calls.
*  Keeps HomeScreen internal and lets this module own how its keys map to content.
* */

fun EntryProviderScope<NavKey>.homeEntries() {
    entry<HomeNavKey> {
        val navigator = LocalNavigator.current
        val viewModel: HomeViewModel = hiltViewModel()
        HomeScreen(
            viewModel = viewModel,
            onQuoteClick = { quoteId ->
                navigator.navigate(QuoteDetailNavKey(quoteId))
            },
        )
    }
}

fun EntryProviderScope<NavKey>.quoteDetailEntries() {
    entry<QuoteDetailNavKey> { key ->
        val navigator = LocalNavigator.current
        // SavedStateHandle receives the key's fields automatically when you see
        // rememberViewModelStoreNavEntryDecorator + Hilt's hiltViewModel().
        // @TODO: Tomorrow
//        val viewModel : QuoteDetailViewModel = hiltViewModel()
//        QuoteDetailScreen(
//            viewModel = viewModel,
//            onBack = navigator::goBack,
//        )
    }
}
