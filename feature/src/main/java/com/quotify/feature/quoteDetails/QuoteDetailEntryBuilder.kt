package com.quotify.feature.quoteDetails

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.quotify.core.navigation.LocalNavigator

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
