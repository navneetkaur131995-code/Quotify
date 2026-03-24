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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(paddingValues: PaddingValues, viewModel: HomeViewModel = hiltViewModel()) {

    val quotes = viewModel.quotes.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val errorMessage = viewModel.errorMessage.collectAsState().value

    Column(modifier = Modifier.padding(paddingValues)) {
        if (isLoading) {
            CircularProgressIndicator()
        } else if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage)
        } else if (quotes.isNotEmpty())
            LazyColumn(modifier = Modifier.padding(8.dp)) {
                items(
                    count = 5,
                ) {
                    LazyListItem("Miles to go before I sleep", author = "Who")
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