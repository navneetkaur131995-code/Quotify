package com.quotify.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun HomeScreen(paddingValues: PaddingValues, viewModel: HomeViewModel = hiltViewModel()) {

    val lazyPagingItems = viewModel.pagingDataFlow.collectAsLazyPagingItems()

    Column(modifier = Modifier.padding(paddingValues)) {
        // The `CombinedLoadStates` object provides info on the load states of the paging source
        // implementation & also for the RemoteMediator (e.g. Local Database) impl., if one exists
        // Different combinedLoadStates - refresh, prepend, append
        // Different load states - Loading, Error and NotLoading states

        val loadState = lazyPagingItems.loadState
        when (loadState.refresh) {
            is LoadState.Loading -> {
                CircularProgressIndicator()
            }

            is LoadState.Error -> {
                val error = (loadState.refresh as LoadState.Error).error.message
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 4.dp)
                        .padding(horizontal = 8.dp),
                    text = error ?: "Something went wrong!",
                    textAlign = TextAlign.Start,
                )
            }

            else -> {
                // Use the Paging items extension
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    items(
                        lazyPagingItems.itemCount,
                        key = { index ->
                            val item = lazyPagingItems[index]
                            "${item?.id ?: ""}_$index"
                        }) { index ->
                        val item = lazyPagingItems[index]
                        if (item != null) {
                            LazyListItem(
                                quote = item.content,
                                author = item.author
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LazyListItem(quote: String, author: String) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp)
                .padding(horizontal = 8.dp),
            text = quote,
            textAlign = TextAlign.Start,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .padding(bottom = 4.dp),
            text = "-$author",
            textAlign = TextAlign.End
        )
    }
}

//@Preview
//@Composable
//fun HomeScreenPreview() {
//    HomeScreen(PaddingValues(32.dp))
//}