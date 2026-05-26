package com.quotify.core.domain.usecase

import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFavoriteQuotesUseCaseTest {
    private val quoteRepository = mockk<QuoteRepository>()
    private val useCase = GetFavoriteQuotesUseCase(quoteRepository)

    @Test
    fun `invoke returns Success list from repository`() =
        runTest {
            val favorites =
                listOf(
                    Quote(id = "1", content = "A", author = "X", favorite = true),
                    Quote(id = "2", content = "B", author = "Y", favorite = true),
                )
            coEvery { quoteRepository.getFavoriteQuotes() } returns DomainResult.Success(favorites)

            val result = useCase()

            assertTrue(result is DomainResult.Success)
            assertEquals(favorites, (result as DomainResult.Success).data)
            coVerify(exactly = 1) { quoteRepository.getFavoriteQuotes() }
        }

    @Test
    fun `invoke returns Success with empty list when no favorites`() =
        runTest {
            coEvery { quoteRepository.getFavoriteQuotes() } returns DomainResult.Success(emptyList())

            val result = useCase()

            assertTrue(result is DomainResult.Success)
            assertEquals(emptyList<Quote>(), (result as DomainResult.Success).data)
        }

    @Test
    fun `invoke returns Failure when repository fails`() =
        runTest {
            val error = RuntimeException("db unreachable")
            coEvery { quoteRepository.getFavoriteQuotes() } returns DomainResult.Failure(error)

            val result = useCase()

            assertTrue(result is DomainResult.Failure)
            assertSame(error, (result as DomainResult.Failure).error)
        }
}
