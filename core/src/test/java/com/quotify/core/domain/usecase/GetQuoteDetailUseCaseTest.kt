package com.quotify.core.domain.usecase

import app.cash.turbine.test
import com.quotify.core.common.DomainResult
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
import org.junit.Assert.assertTrue
import org.junit.Test

class GetQuoteDetailUseCaseTest {
    private val quoteRepository = mockk<QuoteRepository>()
    private val useCase = GetQuoteDetailUseCase(quoteRepository)

    @Test
    fun `invoke returns the flow produced by the repository`() {
        val expected: Flow<DomainResult<Quote>> = flowOf()
        every { quoteRepository.getSingleQuoteStream("1") } returns expected

        val actual = useCase("1")

        assertSame(expected, actual)
        verify(exactly = 1) { quoteRepository.getSingleQuoteStream("1") }
    }

    @Test
    fun `invoke forwards the quoteId to the repository`() {
        every { quoteRepository.getSingleQuoteStream(any()) } returns flowOf()

        useCase("quote-99")

        verify(exactly = 1) { quoteRepository.getSingleQuoteStream("quote-99") }
    }

    @Test
    fun `invoke emits Success values from repository stream`() =
        runTest {
            val quote = Quote(id = "1", content = "Hi", author = "Me", favorite = false)
            every { quoteRepository.getSingleQuoteStream("1") } returns
                flowOf(
                    DomainResult.Success(quote),
                )

            useCase("1").test {
                val result = awaitItem()
                assertTrue(result is DomainResult.Success)
                assertEquals(quote, (result as DomainResult.Success).data)
                awaitComplete()
            }
        }

    @Test
    fun `invoke emits Failure values from repository stream`() =
        runTest {
            val error = Exception("not cached")
            every { quoteRepository.getSingleQuoteStream("1") } returns
                flowOf(
                    DomainResult.Failure(error),
                )

            useCase("1").test {
                val result = awaitItem()
                assertTrue(result is DomainResult.Failure)
                assertSame(error, (result as DomainResult.Failure).error)
                awaitComplete()
            }
        }
}
