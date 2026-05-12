package com.quotify.feature.home

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/*
*  Extension function the app's entryProvider DSL calls.
*  Keeps HomeScreen internal and lets this module own how its keys map to content.
* */

fun EntryProviderScope<NavKey>.homeEntries() {
    entry<HomeNavKey.QuoteList> {
        val viewModel: HomeViewModel = hiltViewModel()
        HomeScreen(
            viewModel = viewModel,
        )
    }
}
