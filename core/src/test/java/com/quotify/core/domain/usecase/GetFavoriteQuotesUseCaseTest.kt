package com.quotify.core.domain.usecase

import app.cash.turbine.test
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFavoriteQuotesUseCaseTest {
    private val quoteRepository = mockk<QuoteRepository>()
    private val useCase = GetFavoriteQuotesUseCase(quoteRepository)

    @Test
    fun `invoke returns the reactive flow from the repository`() =
        runTest {
            val favorites =
                listOf(
                    Quote(id = "1", content = "A", author = "X", favorite = true),
                    Quote(id = "2", content = "B", author = "Y", favorite = true),
                )
            every { quoteRepository.observeFavoriteQuotes() } returns flowOf(favorites)

            useCase().test {
                assertEquals(favorites, awaitItem())
                awaitComplete()
            }
            verify(exactly = 1) { quoteRepository.observeFavoriteQuotes() }
        }

    @Test
    fun `invoke emits an empty list when the repository has no favorites`() =
        runTest {
            every { quoteRepository.observeFavoriteQuotes() } returns flowOf(emptyList())

            useCase().test {
                assertEquals(emptyList<Quote>(), awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun `invoke re-emits when the repository emits updates`() =
        runTest {
            val initial = listOf(Quote(id = "1", content = "A", author = "X", favorite = true))
            val updated =
                initial +
                    Quote(id = "2", content = "B", author = "Y", favorite = true)
            every { quoteRepository.observeFavoriteQuotes() } returns flowOf(initial, updated)

            useCase().test {
                assertEquals(initial, awaitItem())
                assertEquals(updated, awaitItem())
                awaitComplete()
            }
        }
}
