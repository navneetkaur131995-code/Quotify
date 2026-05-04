package com.example.quotify.app.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.quotify.feature.home.HomeNavKey
import com.quotify.feature.quoteDetails.QuoteDetailNavKey
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

// Register all NavKey serializers here. This is required for the NavController to be able to serialize and deserialize NavKeys.
// Note: If you forget to register a NavKey serializer, you'll get a runtime exception when navigating to that destination.

val QuotifyNavConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(HomeNavKey::class, HomeNavKey.serializer())
                    subclass(QuoteDetailNavKey::class, QuoteDetailNavKey.serializer())
                }
            }
    }
