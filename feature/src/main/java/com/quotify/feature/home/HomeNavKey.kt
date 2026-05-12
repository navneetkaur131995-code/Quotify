package com.quotify.feature.home

import com.quotify.core.navigation.QuotifyNavKey
import kotlinx.serialization.Serializable

/**
 * The closed set of destinations owned by the Home feature.
 *
 * Each nested type IS a destination (no envelope wrapper). Add new destinations as nested
 * `data object`s (no args) or `data class`es (with args) implementing this interface, then:
 *   1. Register the new leaf in `app/navigation/NavigationSerializers.kt`.
 *   2. Add a matching `entry<HomeNavKey.X>` block in `HomeEntryBuilder`.
 */
sealed interface HomeNavKey : QuotifyNavKey {
    @Serializable
    data object QuoteList : HomeNavKey
}
