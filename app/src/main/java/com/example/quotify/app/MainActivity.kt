package com.example.quotify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.quotify.app.navigation.AppNavigator
import com.example.quotify.app.navigation.QuotifyNavConfiguration
import com.example.quotify.app.theme.QuotifyTheme
import com.quotify.feature.home.HomeNavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val backStack =
                rememberNavBackStack(
                    configuration = QuotifyNavConfiguration,
                    HomeNavKeys.QuoteList,
                )
            val navigator = remember(backStack) { AppNavigator(backStack) }

            QuotifyTheme {
                QuotifyAppShell(backStack = backStack, navigator = navigator)
            }
        }
    }
}
