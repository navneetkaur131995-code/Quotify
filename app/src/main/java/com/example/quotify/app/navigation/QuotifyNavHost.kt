package com.example.quotify.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.quotify.core.navigation.LocalNavigator
import com.quotify.feature.home.HomeNavKey
import com.quotify.feature.home.HomeNavKeys
import com.quotify.feature.home.homeEntries
import com.quotify.feature.quoteDetails.quoteDetailEntries

@Composable
fun QuotifyNavHost(paddingValues: PaddingValues) {
    // 1. Create the backstack with the start destination and polymorphic config.
    val backStack =
        rememberNavBackStack(
            configuration = QuotifyNavConfiguration,
            HomeNavKey(HomeNavKeys.QuoteList), // start destination
        )

    // 2. Create the Navigator, keyed to the backstack so it survives recomposition.
    val navigator = remember(backStack) { AppNavigator(backStack) }

    // 3. Provide the Navigator to all descendants via CompositionLocal
    CompositionLocalProvider(LocalNavigator provides navigator) {
        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators =
                listOf(
                    // Add the default decorators for managing scenes and saving state
                    rememberSaveableStateHolderNavEntryDecorator(),
                    // Then add the view model store decorator
                    // rememberViewModelStoreNavEntryDecorator()
                ),
            entryProvider =
                entryProvider {
                    homeEntries()
                    quoteDetailEntries()
                },
        )
    }
}
