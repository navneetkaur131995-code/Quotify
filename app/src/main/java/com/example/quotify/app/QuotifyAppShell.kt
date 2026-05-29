package com.example.quotify.app

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.example.quotify.R
import com.example.quotify.app.navigation.QuotifyNavHost
import com.quotify.core.navigation.LocalNavigator
import com.quotify.core.navigation.Navigator
import com.quotify.core.navigation.QuotifyNavKey
import com.quotify.feature.home.HomeNavKeys

/*
 * App shell composable that sets up the top-level UI structure of the app, including the top app bar,:
 *   - The activity stays a one-screen wiring file.
 *   - Each piece (top bar, bottom bar, host) gets the smallest possible recomposition scope
 *     via derivedStateOf around back-stack reads.
 *   - Previews can render the shell without spinning up a Hilt entry point.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotifyAppShell(
    backStack: NavBackStack<NavKey>,
    navigator: Navigator,
) {
    val canGoBack by remember(backStack) { derivedStateOf { backStack.size > 1 } }
    val currentKey by remember(backStack) { derivedStateOf { backStack.lastOrNull() } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { QuotifyTopBar(canGoBack = canGoBack, onBack = navigator::goBack) },
        bottomBar = { QuotifyBottomBar(currentKey = currentKey, onTabClick = navigator::navigateToTab) },
    ) { paddingValues ->
        CompositionLocalProvider(LocalNavigator provides navigator) {
            QuotifyNavHost(backStack = backStack, paddingValues = paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuotifyTopBar(
    canGoBack: Boolean,
    onBack: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = stringResource(R.string.app_name)) },
        colors =
            topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        navigationIcon = {
            if (canGoBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = stringResource(R.string.nav_back),
                    )
                }
            }
        },
    )
}

@Composable
private fun QuotifyBottomBar(
    currentKey: NavKey?,
    onTabClick: (QuotifyNavKey) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = currentKey is HomeNavKeys.QuoteList,
            onClick = { onTabClick(HomeNavKeys.QuoteList) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_quotes_home),
                    contentDescription = stringResource(R.string.nav_home),
                )
            },
            label = { Text(stringResource(R.string.nav_home)) },
        )
        NavigationBarItem(
            selected = currentKey is HomeNavKeys.Favorites,
            onClick = { onTabClick(HomeNavKeys.Favorites) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_favorite),
                    contentDescription = stringResource(R.string.nav_favorites),
                )
            },
            label = { Text(stringResource(R.string.nav_favorites)) },
        )
    }
}
