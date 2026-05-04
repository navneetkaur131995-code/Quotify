package com.example.quotify.app.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.quotify.core.navigation.Navigator
import com.quotify.core.navigation.QuotifyNavKey

/*
* The real Navigator. Wraps the mutable back stack owned by QuotifyNavHost.
* Intentionally not a @Singleton, it's scoped to the composable that creates it,
* because the backstack lives in composition.
*
* */

class AppNavigator(
    private val backStack: NavBackStack<NavKey>,
) : Navigator {
    override fun navigate(key: QuotifyNavKey) {
        backStack.add(key)
    }

    override fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    override fun popUpTo(
        key: QuotifyNavKey,
        inclusive: Boolean,
    ) {
        val index = backStack.indexOfLast { it == key }
        if (index == -1) return // key not found, no-op
        val dropFrom = if (inclusive) index else index + 1
        while (backStack.size > dropFrom) backStack.removeAt(backStack.lastIndex)
    }

    override fun resetTo(key: QuotifyNavKey) {
        backStack.clear()
        backStack.add(key)
    }
}
