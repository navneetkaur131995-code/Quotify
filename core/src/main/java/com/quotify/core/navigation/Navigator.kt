package com.quotify.core.navigation

/**
 *  Abstraction over the app's navigation state. Feature modules depend on this,
 *  not on SnapshotStateList or AppNavigator.
 */

interface Navigator {
    // Push a new destination onto the back stack.
    fun navigate(key: QuotifyNavKey)

    // Pop the destination. Safe to call on the last entry (no-op)
    fun goBack()

    // Pop until [key] is on top. No-op if [key] isn't  on the stack
    fun popUpTo(
        key: QuotifyNavKey,
        inclusive: Boolean = false,
    )

    // Replace the entire stack with a single destination (e.g. for tab roots)
    fun resetTo(key: QuotifyNavKey)
}
