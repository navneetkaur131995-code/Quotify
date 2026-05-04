package com.quotify.feature.home

import com.quotify.core.navigation.QuotifyNavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeNavKeys : QuotifyNavKey {
    @Serializable
    data object QuoteList : HomeNavKeys
}

@Serializable
data class HomeNavKey(
    val key: HomeNavKeys,
) : HomeNavKeys
