package com.quotify.feature.quotedetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quotify.feature.R
import com.quotify.core.domain.model.Quote

@Composable
fun QuoteDetailScreen(
    uiState: QuoteDetailUiState,
    onFavoriteToggle: (Quote) -> Unit,
) {
    when (uiState) {
        is QuoteDetailUiState.Loading ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

        is QuoteDetailUiState.Success -> {
            val data = (uiState).quote
            QuoteDetail(data) { onFavoriteToggle(uiState.quote) }
        }

        is QuoteDetailUiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = (uiState).message)
            }
        }
    }
}

@Composable
fun QuoteDetail(
    data: Quote,
    onFavoriteClick: () -> Unit,
) {
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
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
        ) {
            val favoriteIcon = if (data.favorite) R.drawable.ic_favorite_selected else R.drawable.ic_favorite
            val tint =
                if (data.favorite) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            IconButton(
                onClick = { onFavoriteClick() },
            ) {
                Icon(
                    painter = painterResource(favoriteIcon),
                    contentDescription = "Toggle Favorite",
                    tint = tint,
                )
            }
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                text = "— ${data.author}",
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Preview
@Composable
private fun PreviewQuotesScreen() {
    QuoteDetailScreen(
        uiState =
            QuoteDetailUiState.Success(
                Quote(
                    id = "1",
                    content = "The only way to do great work is to love what you do.",
                    author = "Steve Jobs",
                    favorite = true,
                ),
            ),
        onFavoriteToggle = { },
    )
}
