package com.quotify.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.quotify.core.navigation.LocalNavigator
import com.quotify.feature.quoteDetails.QuoteDetailNavKey

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    val isOnline by viewModel.isOnline.collectAsStateWithLifecycle()

    val navigator = LocalNavigator.current

    val mediatorRefreshFailed =
        lazyPagingItems.loadState.mediator?.refresh is LoadState.Error
    val hasCachedItems = lazyPagingItems.itemCount > 0

    // Banner shows when the user is looking at stale local data:
    //  - device is offline, OR
    //  - device is online but the remote refresh failed (server down, 5xx, etc.)
    // ...and only when there's actually data on screen to label as "cached".
    val showCachedBanner = hasCachedItems && (!isOnline || mediatorRefreshFailed)

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = showCachedBanner) {
            OfflineBanner()
        }

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { lazyPagingItems.refresh() },
            modifier = Modifier.fillMaxSize(),
            state = rememberPullToRefreshState(),
        ) {
            when (val refresh = lazyPagingItems.loadState.refresh) {
                is LoadState.Loading if !hasCachedItems -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is LoadState.Error if !hasCachedItems -> {
                    Text(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.Center),
                        text = refresh.error.message ?: "Something went wrong!",
                        textAlign = TextAlign.Center,
                    )
                }

                else ->
                    QuotesList(
                        lazyPagingItems = lazyPagingItems,
                        onQuoteClick = { id -> navigator.navigate(QuoteDetailNavKey(id)) },
                    )
            }
        }
    }
}

@Composable
private fun QuotesList(
    lazyPagingItems: androidx.paging.compose.LazyPagingItems<com.quotify.core.domain.model.Quote>,
    onQuoteClick: (String) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(8.dp),
    ) {
        items(
            count = lazyPagingItems.itemCount,
            key = lazyPagingItems.itemKey { it.id },
        ) { index ->
            lazyPagingItems[index]?.let { quote ->
                LazyListItem(
                    quote = quote.quote,
                    author = quote.author,
                    onQuoteClick = { onQuoteClick(quote.id) },
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        tonalElevation = 2.dp,
    ) {
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            text = "You're offline. Showing cached quotes.",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun LazyListItem(
    quote: String,
    author: String,
    onQuoteClick: () -> Unit,
) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onQuoteClick,
    ) {
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp)
                    .padding(horizontal = 8.dp),
            text = quote,
            textAlign = TextAlign.Start,
        )
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp),
            text = "-$author",
            textAlign = TextAlign.End,
        )
    }
}
