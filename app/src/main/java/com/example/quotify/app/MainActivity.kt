package com.example.quotify.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.rememberNavBackStack
import com.example.quotify.R
import com.example.quotify.app.navigation.AppNavigator
import com.example.quotify.app.navigation.QuotifyNavConfiguration
import com.example.quotify.app.navigation.QuotifyNavHost
import com.example.quotify.app.theme.QuotifyTheme
import com.quotify.core.navigation.LocalNavigator
import com.quotify.feature.home.HomeNavKey
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
                    HomeNavKey.QuoteList, // start destination
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
                            BottomAppBar(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.primary,
                            ) {
                                BottomBarActions()
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

@Composable
fun BottomBarActions() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        BottomBarButtons(title = "Home", image = R.drawable.ic_quotes_home, action = {})
        BottomBarButtons(title = "Favorites", image = R.drawable.ic_favorite, action = {})
    }
}

@Composable
fun BottomBarButtons(
    title: String,
    image: Int,
    action: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .clickable(onClick = action),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Icon(painter = painterResource(image), title)
        Text(text = title, fontSize = 12.sp)
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
