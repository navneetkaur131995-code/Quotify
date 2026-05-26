package com.quotify.core.domain.usecase

import com.quotify.core.domain.repository.QuoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleFavoriteUseCaseTest {
    private val quoteRepository = mockk<QuoteRepository>(relaxed = true)
    private val useCase = ToggleFavoriteUseCase(quoteRepository)

    @Test
    fun `invoke forwards quoteId and isFavorite=true to repository unchanged`() =
        runTest {
            useCase(quoteId = "quote-abc-123", isFavorite = true)

            coVerify(exactly = 1) { quoteRepository.toggleFavoriteQuote("quote-abc-123", true) }
        }

    @Test
    fun `invoke forwards quoteId and isFavorite=false to repository unchanged`() =
        runTest {
            useCase(quoteId = "quote-abc-123", isFavorite = false)

            coVerify(exactly = 1) { quoteRepository.toggleFavoriteQuote("quote-abc-123", false) }
        }

    @Test(expected = IllegalStateException::class)
    fun `invoke propagates exceptions from the repository`() =
        runTest {
            coEvery {
                quoteRepository.toggleFavoriteQuote(any(), any())
            } throws IllegalStateException("db error")

            useCase(quoteId = "42", isFavorite = true)
        }
}
