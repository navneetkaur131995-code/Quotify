package com.quotify.core.domain.usecase

import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.repository.QuoteRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class GetQuotesUseCaseTest {
    private val quoteRepository = mockk<QuoteRepository>()
    private val useCase = GetQuotesUseCase(quoteRepository)

    @Test
    fun `invoke returns the flow produced by the repository`() {
        val expectedFlow: Flow<PagingData<Quote>> = flowOf(PagingData.empty())
        every { quoteRepository.getQuotesStream() } returns expectedFlow

        val actualFlow = useCase()

        assertSame(expectedFlow, actualFlow)
        verify(exactly = 1) { quoteRepository.getQuotesStream() }
    }

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
}
