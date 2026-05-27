package com.quotify.feature.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quotify.feature.R
import com.quotify.core.domain.model.Quote

private val FavoritesAnimations =
    slideInVertically(
        animationSpec =
            tween(
                durationMillis = 500,
                easing = LinearOutSlowInEasing,
            ),
    ) { -500 } + expandVertically() +
        fadeIn(
            animationSpec = tween(durationMillis = 500),
        )

@Composable
fun FavoritesScreen(
    favoritesUiState: FavoritesUiState,
    onFavoriteClick: (String) -> Unit,
) {
    when (favoritesUiState) {
        is FavoritesUiState.Loading ->
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

        is FavoritesUiState.Success ->
            if (favoritesUiState.quotes.isEmpty()) {
                EmptyFavoritesState()
            } else {
                FavoritesList(
                    items = favoritesUiState.quotes,
                    onFavoriteClick = onFavoriteClick,
                )
            }

        is FavoritesUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
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
}

@Composable
private fun FavoritesList(
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
            items = items,
            key = { quote -> quote.id },
        ) { quote ->
            val visibleState =
                remember(quote.id) {
                    MutableTransitionState(false).apply {
                        targetState = true
                    }
                }
            AnimatedVisibility(
                visibleState = visibleState,
                enter = FavoritesAnimations,
            ) {
                FavoritesListItem(quote = quote) {
                    onFavoriteClick(quote.id)
                }
            }
        }
    }
}

@Composable
private fun FavoritesListItem(
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
            text = stringResource(R.string.quote_attribution, quote.author),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun EmptyFavoritesState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.favorites_empty_message),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp),
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
