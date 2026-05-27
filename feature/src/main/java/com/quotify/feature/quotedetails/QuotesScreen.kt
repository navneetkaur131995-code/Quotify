package com.quotify.feature.quotedetails

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
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
    val visibleState =
        remember(data.id) {
            MutableTransitionState(false).apply {
                targetState = true
            }
        }
    AnimatedVisibility(
        visibleState = visibleState,
        enter = QuoteDetailAnimations.enterTransition,
        exit = QuoteDetailAnimations.exitTransition,
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
                val tint =
                    if (data.favorite) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                IconButton(
                    onClick = { onFavoriteClick() },
                ) {
                    val image = AnimatedImageVector.animatedVectorResource(R.drawable.avd_favorite)
                    Icon(
                        painter = rememberAnimatedVectorPainter(image, data.favorite),
                        contentDescription = stringResource(R.string.cd_toggle_favorite),
                        tint = tint,
                    )
                }
                Text(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                    text = stringResource(R.string.quote_attribution, data.author),
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

private object QuoteDetailAnimations {
    val enterTransition =
        slideInHorizontally(
            animationSpec =
                tween(
                    durationMillis = 500,
                    easing = LinearOutSlowInEasing,
                ),
        ) { -500 } + expandHorizontally() +
            fadeIn(
                animationSpec = tween(durationMillis = 500),
            )
    val exitTransition = slideOutHorizontally() + shrinkHorizontally() + fadeOut()
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
        onFavoriteToggle = {},
    )
}
