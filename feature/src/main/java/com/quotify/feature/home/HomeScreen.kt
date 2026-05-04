package com.quotify.feature.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey

@Composable
fun HomeScreen(
    paddingValues: PaddingValues,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()
    val isRefreshing = lazyPagingItems.loadState.refresh is LoadState.Loading

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { lazyPagingItems.refresh() },
        modifier =
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
    ) {
        val loadState = lazyPagingItems.loadState
        when (loadState.refresh) {
            is LoadState.Loading if lazyPagingItems.itemCount == 0 -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            is LoadState.Error if lazyPagingItems.itemCount == 0 -> {
                val error = (loadState.refresh as LoadState.Error).error.message
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.Center),
                    text = error ?: "Something went wrong!",
                    textAlign = TextAlign.Center,
                )
            }

            else ->
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
                        lazyPagingItems[index]?.let {
                            LazyListItem(it.content, it.author)
                        }
                    }
                }
        }
    }
}

@Composable
fun LazyListItem(
    quote: String,
    author: String,
) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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

// @Composable
// fun PullToRefreshBox(
//    isRefreshing: Boolean,
//    onRefresh: () -> Unit,
//    modifier: Modifier = Modifier,
//    state: PullToRefreshState = rememberPullToRefreshState(),
//    contentAlignment: Alignment = Alignment.TopStart,
//    indicator: @Composable BoxScope.() -> Unit = {
//        Indicator(
//            modifier = Modifier.align(Alignment.TopCenter),
//            state = state,
//            isRefreshing = isRefreshing
//        )
//    },
//    content: @Composable BoxScope.() -> Unit
// ) {
//    Box(
//        modifier.pullToRefresh(state = state, isRefreshing = isRefreshing, onRefresh = onRefresh),
//        contentAlignment = contentAlignment
//    ) {
//        content()
//        indicator()
//    }
// }

// @Preview
// @Composable
// fun HomeScreenPreview() {
//    HomeScreen(PaddingValues(32.dp))
// }
