package com.quotify.feature.quoteDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotify.core.domain.model.Quote

@Composable
fun QuoteDetailScreen(uiState: QuoteDetailUiState) {
    when (uiState) {
        is QuoteDetailUiState.Loading ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        is QuoteDetailUiState.Success -> {
            val data = (uiState).quote
            QuoteDetail(data)
        }

        is QuoteDetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = (uiState).message)
            }
        }
    }
}

@Composable
fun QuoteDetail(data: Quote) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
                    .padding(horizontal = 16.dp),
            text = data.content,
            textAlign = TextAlign.Start,
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
            text = "— ${data.author}",
            textAlign = TextAlign.End,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

// @Preview
// @Composable
// fun PreviewQuotesScreen(){
//   QuoteDetailScreen()
// }
