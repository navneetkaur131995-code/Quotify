package com.quotify.feature.quoteDetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.quotify.core.domain.model.Quote

@Composable
fun QuoteDetailScreen(viewModel: QuoteDetailViewModel) {
    val state by viewModel.uiState.collectAsState()

    when (state) {
        is QuoteDetailUiState.Loading -> CircularProgressIndicator()

        is QuoteDetailUiState.Success -> {
            val data = (state as QuoteDetailUiState.Success).quote
            QuoteDetail(data)
        }

        is QuoteDetailUiState.Error -> {
            Text(text = (state as QuoteDetailUiState.Error).message)
        }
    }
}

@Composable
fun QuoteDetail(data: Quote) {
    Column(modifier = Modifier.fillMaxSize()) {
        ElevatedCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
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
                text = data.content,
                textAlign = TextAlign.Start,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 8.dp),
                text = "- ${data.author}",
                textAlign = TextAlign.End,
                fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            )
        }
    }
}

// @Preview
// @Composable
// fun PreviewQuotesScreen(){
//   QuoteDetailScreen()
// }
