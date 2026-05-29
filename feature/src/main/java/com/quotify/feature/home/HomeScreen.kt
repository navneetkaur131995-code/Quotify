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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.example.quotify.feature.R
import com.quotify.core.domain.model.Quote

@Composable
fun HomeScreen(
    lazyPagingItems: LazyPagingItems<Quote>,
    isOnline: Boolean?,
    onQuoteClick: (String) -> Unit,
) {
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    val mediatorRefreshFailed =
        lazyPagingItems.loadState.mediator?.refresh is LoadState.Error
    val hasCachedItems = lazyPagingItems.itemCount > 0

    // Banner: only show when we KNOW the device is offline or remote refresh failed,
    // AND there's cached data to label as stale. Null isOnline (initial reading) is
    // treated as "no reason to show the banner yet" — prevents a cold-start flash.
    val showCachedBanner =
        shouldShowCachedBanner(
            isOnline = isOnline,
            hasCachedItems = hasCachedItems,
            mediatorRefreshFailed = mediatorRefreshFailed,
        )

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
                is LoadState.Loading if !hasCachedItems ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                is LoadState.Error if !hasCachedItems ->
                    Text(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.Center),
                        text = refresh.error.message ?: stringResource(R.string.error_generic),
                        textAlign = TextAlign.Center,
                    )

                else ->
                    QuotesList(
                        lazyPagingItems = lazyPagingItems,
                        onQuoteClick = onQuoteClick,
                    )
            }
        }
    }
}

@Composable
private fun QuotesList(
    lazyPagingItems: LazyPagingItems<Quote>,
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
                    quote = quote,
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
            text = stringResource(R.string.offline_banner_message),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun LazyListItem(
    quote: Quote,
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
            text = quote.content,
            textAlign = TextAlign.Start,
        )
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .padding(bottom = 4.dp),
            text = stringResource(R.string.quote_attribution, quote.author),
            textAlign = TextAlign.End,
        )
    }
}

// Lives at file scope rather than in the VM because it needs the paging loadState,
// which is only resolvable from the LazyPagingItems holder in the Compose layer.
private fun shouldShowCachedBanner(
    isOnline: Boolean?,
    hasCachedItems: Boolean,
    mediatorRefreshFailed: Boolean,
): Boolean = hasCachedItems && (isOnline == false || mediatorRefreshFailed)
