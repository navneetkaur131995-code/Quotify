package com.example.quotify.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.quotify.core.navigation.Navigator
import com.quotify.core.navigation.QuotifyNavKey
import kotlin.reflect.KClass

/*
 * The real Navigator. Wraps the mutable back stack owned by QuotifyNavHost.
 * Intentionally not a @Singleton: it's scoped to the composable that creates it,
 * because the back stack lives in composition.
 */
class AppNavigator(
    private val backStack: NavBackStack<NavKey>,
) : Navigator {
    override fun navigate(key: QuotifyNavKey) {
        // Dedupe: a fast double-tap shouldn't push the same destination twice.
        // Equality is structural here, so it works for both data objects and data classes
        // with identical args.
        if (backStack.lastOrNull() == key) return
        backStack.add(key)
    }

    override fun goBack() {
        backStack.removeLastOrNull()
    }

    override fun popUpTo(
        key: QuotifyNavKey,
        inclusive: Boolean,
    ) {
        // Structural equality: matches a specific instance (e.g. QuoteDetailNavKey("42")).
        val index = backStack.indexOfLast { it == key }
        if (index == -1) return
        val dropFrom = if (inclusive) index else index + 1
        while (backStack.size > dropFrom) backStack.removeAt(backStack.lastIndex)
    }

    override fun popUpToRoute(
        route: KClass<out QuotifyNavKey>,
        inclusive: Boolean,
    ) {
        // Type match: pops back to "the detail screen" regardless of which quoteId it
        // currently displays.
        val index = backStack.indexOfLast { route.isInstance(it) }
        if (index == -1) return
        val dropFrom = if (inclusive) index else index + 1
        while (backStack.size > dropFrom) backStack.removeAt(backStack.lastIndex)
    }

    override fun resetTo(key: QuotifyNavKey) {
        backStack.clear()
        backStack.add(key)
    }

    override fun navigateToTab(key: QuotifyNavKey) {
        if (backStack.lastOrNull() == key) return
        val index = backStack.indexOfLast { it == key }
        if (index != -1) {
            while (backStack.size > index + 1) backStack.removeAt(backStack.lastIndex)
        } else {
            backStack.add(key)
        }
    }
}
