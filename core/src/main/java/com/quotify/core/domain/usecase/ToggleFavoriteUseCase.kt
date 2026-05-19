package com.quotify.core.domain.usecase

import com.quotify.core.domain.repository.QuoteRepository
import javax.inject.Inject

class ToggleFavoriteUseCase
    @Inject
    constructor(
        private val quoteRepository: QuoteRepository,
    ) {
        suspend operator fun invoke(
            quoteId: String,
            isFavorite: Boolean,
        ) {
            quoteRepository.toggleFavoriteQuote(quoteId, isFavorite)
        }
    }
