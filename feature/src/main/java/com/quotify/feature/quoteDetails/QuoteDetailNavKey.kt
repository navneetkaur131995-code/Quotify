package com.quotify.feature.quoteDetails

import com.quotify.core.navigation.QuotifyNavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface QuoteDetailNavKeys : QuotifyNavKey

/*
* Note: The key itself is the destination. 'quoteId' is a compile-time-typed field.
* No bundles, no NavType, no argument parsing.
* */
@Serializable
data class QuoteDetailNavKey(
    val quoteId: String,
) : QuoteDetailNavKeys
