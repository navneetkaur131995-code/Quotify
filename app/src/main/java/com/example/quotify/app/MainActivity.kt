package com.example.quotify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.quotify.R
import com.example.quotify.app.navigation.AppNavigator
import com.example.quotify.app.navigation.QuotifyNavConfiguration
import com.example.quotify.app.navigation.QuotifyNavHost
import com.example.quotify.app.theme.QuotifyTheme
import com.quotify.core.navigation.LocalNavigator
import com.quotify.feature.home.HomeNavKeys
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val backStack =
                rememberNavBackStack(
                    configuration = QuotifyNavConfiguration,
                    HomeNavKeys.QuoteList, // start destination
                )
            val navigator = remember(backStack) { AppNavigator(backStack) }

            QuotifyTheme {
                CompositionLocalProvider(LocalNavigator provides navigator) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            TopAppBar(
                                title = { Text(text = "Quotify", color = Color.Black) },
                                colors =
                                    topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        titleContentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                navigationIcon = {
                                    val canGoBack = backStack.size > 1
                                    if (canGoBack) {
                                        IconButton(
                                            onClick = { navigator.goBack() },
                                            modifier = Modifier.padding(8.dp),
                                        ) {
                                            Icon(painter = painterResource(R.drawable.ic_back), "Back")
                                        }
                                    }
                                },
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                val currentKey = backStack.lastOrNull()
                                NavigationBarItem(
                                    selected = currentKey is HomeNavKeys.QuoteList,
                                    onClick = { navigator.goBack() },
                                    icon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_quotes_home),
                                            contentDescription = "Home",
                                        )
                                    },
                                    label = { Text("Home") },
                                )
                                NavigationBarItem(
                                    selected = currentKey is HomeNavKeys.Favorites,
                                    onClick = { navigator.navigate(HomeNavKeys.Favorites) },
                                    icon = {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_favorite),
                                            contentDescription = "Favorites",
                                        )
                                    },
                                    label = { Text("Favorites") },
                                )
                            }
                        },
                    ) { paddingValues ->
                        QuotifyNavHost(backStack = backStack, paddingValues = paddingValues)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun TopBarPreview() {
    TopAppBar(
        title = { Text(text = "Quotify") },
        colors =
            topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary,
            ),
        navigationIcon = {
            IconButton(
                onClick = {},
                modifier = Modifier.padding(16.dp),
            ) {
                Icon(painter = painterResource(R.drawable.ic_back), "Back")
            }
        },
    )
}
