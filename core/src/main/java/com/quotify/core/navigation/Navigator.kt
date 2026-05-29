package com.quotify.core.navigation

import kotlin.reflect.KClass

/**
 *  Abstraction over the app's navigation state. Feature modules depend on this,
 *  not on the SnapshotStateList or AppNavigator implementation.
 */
interface Navigator {
    // Push a new destination onto the back stack. No-op if the same destination is already
    // on top — protects against double-tap navigation bugs.
    fun navigate(key: QuotifyNavKey)

    // Pop the top destination. Safe to call on the last entry (no-op).
    fun goBack()

    // Pop until the given INSTANCE is on top. Structural equality, so `popUpTo(QuoteDetail("a"))`
    // won't match `QuoteDetail("b")`. Use popUpToRoute for "pop back to the detail screen
    // regardless of args".
    fun popUpTo(
        key: QuotifyNavKey,
        inclusive: Boolean = false,
    )

    // Pop until the most recent entry of the given TYPE is on top — args-agnostic.
    fun popUpToRoute(
        route: KClass<out QuotifyNavKey>,
        inclusive: Boolean = false,
    )

    // Replace the entire stack with a single destination.
    fun resetTo(key: QuotifyNavKey)

    // Tab switch: no-op if already there, pop back if in stack, push if absent.
    fun navigateToTab(key: QuotifyNavKey)
}
