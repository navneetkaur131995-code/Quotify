package com.quotify.feature.quoteDetails

import com.quotify.core.navigation.QuotifyNavKey
import kotlinx.serialization.Serializable

/**
 * The key itself is the destination. `quoteId` is a compile-time-typed field —
 * no bundles, no NavType, no argument parsing.
 *
 * If the QuoteDetail feature gains a second destination, promote this to a sealed-interface
 * grouping (mirroring `HomeNavKeys`) and nest each destination inside it.
 */
@Serializable
data class QuoteDetailNavKey(
    val quoteId: String,
) : QuotifyNavKey
