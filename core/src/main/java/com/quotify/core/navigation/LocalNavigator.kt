package com.quotify.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * CompositionLocal that screens use to reach the Navigator without pop drilling
 * or Hilt injection into composables. The app module provides a value at the root
 *
 * A CompositionLocal is a tool in Jetpack Compose that allows you to pass data through the UI tree implicitly,
 * without having to pass it as a parameter to every single function.
 *
 * • Provides: A way to share data globally within a specific part of the UI.
 * • Prevents: Passing the same parameters through dozens of functions.
 * • Usage: Great for "Infrastructure" (Navigation, Themes, Analytics, Context)
 * */

val LocalNavigator =
    staticCompositionLocalOf<Navigator> {
        error("LocalNavigator not provided. Wrap content in CompositionLocalProvider at the app root.")
    }
