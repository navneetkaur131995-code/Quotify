package com.quotify.core.navigation

import androidx.navigation3.runtime.NavKey

/**
 * Marker interface for all Quotify navigation keys.
 * NOT sealed — feature modules extend this across module boundaries.
 * Sealed-ness is preserved *within* each feature module via its own sealed interface.
 */

interface QuotifyNavKey : NavKey
