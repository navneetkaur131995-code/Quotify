package com.quotify.core.domain.usecase

import com.quotify.core.common.DomainResult
import com.quotify.core.domain.repository.QuoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ToggleFavoriteUseCaseTest {
    private val quoteRepository = mockk<QuoteRepository>()
    private val useCase = ToggleFavoriteUseCase(quoteRepository)

    @Test
    fun `invoke forwards quoteId to repository and returns the result`() =
        runTest {
            coEvery { quoteRepository.toggleFavoriteQuote("42") } returns DomainResult.Success(Unit)

            val result = useCase("42")

            assertTrue(result is DomainResult.Success)
            coVerify(exactly = 1) { quoteRepository.toggleFavoriteQuote("42") }
        }

    @Test
    fun `invoke propagates a Failure from the repository`() =
        runTest {
            val error = RuntimeException("db locked")
            coEvery { quoteRepository.toggleFavoriteQuote("42") } returns DomainResult.Failure(error)

            val result = useCase("42")

            assertTrue(result is DomainResult.Failure)
            assertSame(error, (result as DomainResult.Failure).error)
        }
}
