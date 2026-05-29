package com.quotify.core.domain.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetQuotesUseCaseTest {
    private val quoteRepository = mockk<QuoteRepository>()
    private val useCase = GetQuotesUseCase(quoteRepository)

    @Test
    fun `invoke delegates to repository exactly once`() {
        every { quoteRepository.getQuotesStream() } returns flowOf(PagingData.empty())

        useCase()

        verify(exactly = 1) { quoteRepository.getQuotesStream() }
    }

    @Test
    fun `invoke does not call repository more than once per invocation`() {
        every { quoteRepository.getQuotesStream() } returns flowOf(PagingData.empty())

        useCase()
        useCase()

        // Each call to invoke() should trigger exactly one repository call.
        // Two invocations → two repository calls, not one cached or zero.
        verify(exactly = 2) { quoteRepository.getQuotesStream() }
    }

    // --- Data flow ---

    @Test
    fun `invoke emits the quotes provided by the repository`() =
        runTest {
            val quotes =
                listOf(
                    Quote(id = "1", content = "Stay hungry", author = "Jobs", favorite = false),
                    Quote(id = "2", content = "Be water", author = "Bruce Lee", favorite = true),
                )
            every { quoteRepository.getQuotesStream() } returns flowOf(PagingData.from(quotes))

            val snapshot = useCase().asSnapshot()

            assertEquals(quotes, snapshot)
        }

    @Test
    fun `invoke emits empty list when repository returns empty PagingData`() =
        runTest {
            every { quoteRepository.getQuotesStream() } returns flowOf(PagingData.empty())

            val snapshot = useCase().asSnapshot()

            assertEquals(emptyList<Quote>(), snapshot)
        }

    @Test
    fun `invoke preserves all quote fields through the pipeline`() =
        runTest {
            val quote =
                Quote(
                    id = "42",
                    content = "The only way out is through.",
                    author = "Robert Frost",
                    favorite = true,
                )
            every { quoteRepository.getQuotesStream() } returns flowOf(PagingData.from(listOf(quote)))

            val snapshot = useCase().asSnapshot()

            assertEquals(1, snapshot.size)
            val result = snapshot.first()
            assertEquals("42", result.id)
            assertEquals("The only way out is through.", result.content)
            assertEquals("Robert Frost", result.author)
            assertEquals(true, result.favorite)
        }
}
