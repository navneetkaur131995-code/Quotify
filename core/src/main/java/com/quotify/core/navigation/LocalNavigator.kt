package com.quotify.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal that screens use to reach the Navigator without pop drilling
 * or Hilt injection into composables. The app module provides a value at the root
 * */

val LocalNavigator =
    staticCompositionLocalOf<Navigator> {
        error("LocalNavigator not provided. Wrap content in CompositionLocalProvider at the app root.")
    }
