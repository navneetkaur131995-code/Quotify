package com.quotify.feature.favorites

import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quotify.core.domain.model.Quote

@Composable
fun FavoritesScreen(
    favoritesUiState: FavoritesUiState,
    onFavoriteClick: (String) -> Unit,
) {
    when (favoritesUiState) {
        is FavoritesUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        is FavoritesUiState.Success ->
            FavoritesList(
                items = favoritesUiState.quotes,
                onFavoriteClick = onFavoriteClick,
            )
        is FavoritesUiState.Error -> {
            Text(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                text = favoritesUiState.message,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun FavoritesList(
    items: List<Quote>,
    onFavoriteClick: (String) -> Unit,
) {
    LazyColumn(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(8.dp),
    ) {
        items(
            count = items.size,
            key = { index -> items[index].id },
        ) { index ->
            FavoritesListItem(items[index]) {
                onFavoriteClick(items[index].id)
            }
        }
    }
}

@Composable
fun FavoritesListItem(
    quote: Quote,
    onFavoriteClick: () -> Unit,
) {
    ElevatedCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onFavoriteClick,
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
            text = "- ${quote.author}",
            textAlign = TextAlign.End,
        )
    }
}

@Preview
@Composable
private fun PreviewFavoritesScreen() {
    FavoritesScreen(
        favoritesUiState =
            FavoritesUiState.Success(
                quotes =
                    listOf(
                        Quote(
                            id = "1",
                            content = "Be yourself; everyone else is already taken.",
                            author = "Oscar Wilde",
                            favorite = true,
                        ),
                    ),
            ),
        onFavoriteClick = { },
    )
}
