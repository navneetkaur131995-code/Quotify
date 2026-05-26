package com.quotify.core.data.repository

import app.cash.turbine.test
import com.quotify.core.common.DomainResult
import com.quotify.core.data.localDatabase.QuoteEntity
import com.quotify.core.data.localDatabase.QuotifyDAO
import com.quotify.core.data.paging.QuoteRemoteMediator
import com.quotify.core.domain.model.Quote
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuoteRepositoryImplTest {
    private val remoteMediator = mockk<QuoteRemoteMediator>(relaxed = true)
    private val dao = mockk<QuotifyDAO>(relaxed = true)
    private val repository = QuoteRepositoryImpl(remoteMediator, dao)

    private val entity =
        QuoteEntity(
            id = "1",
            author = "Robert Frost",
            quote = "Miles to go before I sleep",
            favorite = false,
        )
    private val domain =
        Quote(
            id = "1",
            content = "Miles to go before I sleep",
            author = "Robert Frost",
            favorite = false,
        )

    // --- getQuotesStream ---

    @Test
    fun `getQuotesStream returns a non-null paging flow`() {
        val flow = repository.getQuotesStream()
        assertNotNull(flow)
    }

    // --- getSingleQuoteStream ---

    @Test
    fun `getSingleQuoteStream emits Success when DAO returns an entity`() =
        runTest {
            every { dao.getQuoteById("1") } returns flowOf(entity)

            repository.getSingleQuoteStream("1").test {
                val result = awaitItem()
                assertTrue(result is DomainResult.Success)
                assertEquals(domain, (result as DomainResult.Success).data)
                awaitComplete()
            }
        }

    @Test
    fun `getSingleQuoteStream emits Failure when DAO returns null`() =
        runTest {
            every { dao.getQuoteById("missing") } returns flowOf(null)

            repository.getSingleQuoteStream("missing").test {
                val result = awaitItem()
                assertTrue(result is DomainResult.Failure)
                assertTrue(
                    (result as DomainResult.Failure).error.message!!.contains("missing"),
                )
                awaitComplete()
            }
        }

    @Test
    fun `getSingleQuoteStream re-emits when DAO emits updates`() =
        runTest {
            every { dao.getQuoteById("1") } returns
                flowOf(
                    entity,
                    entity.copy(favorite = true),
                )

            repository.getSingleQuoteStream("1").test {
                val first = awaitItem() as DomainResult.Success
                assertEquals(false, first.data.favorite)

                val second = awaitItem() as DomainResult.Success
                assertEquals(true, second.data.favorite)

                awaitComplete()
            }
        }

    // --- toggleFavoriteQuote ---

    @Test
    fun `toggleFavoriteQuote removes from favorites when currently favorited`() =
        runTest {
            coEvery { dao.removeFromFavorites("1") } returns Unit

            repository.toggleFavoriteQuote(id = "1", isFavorite = true)

            coVerify(exactly = 1) { dao.removeFromFavorites("1") }
            coVerify(exactly = 0) { dao.addToFavorites(any()) }
        }

    @Test
    fun `toggleFavoriteQuote adds to favorites when not currently favorited`() =
        runTest {
            coEvery { dao.addToFavorites("1") } returns Unit

            repository.toggleFavoriteQuote(id = "1", isFavorite = false)

            coVerify(exactly = 1) { dao.addToFavorites("1") }
            coVerify(exactly = 0) { dao.removeFromFavorites(any()) }
        }

    // --- getFavoriteQuotes ---

    @Test
    fun `getFavoriteQuotes returns Success with mapped domain list`() =
        runTest {
            val favorites =
                listOf(
                    entity.copy(id = "1", favorite = true),
                    entity.copy(id = "2", favorite = true),
                )
            coEvery { dao.getFavoriteQuotes() } returns favorites

            val result = repository.getFavoriteQuotes()

            assertTrue(result is DomainResult.Success)
            val data = (result as DomainResult.Success).data
            assertEquals(2, data.size)
            assertEquals(listOf("1", "2"), data.map { it.id })
            assertTrue(data.all { it.favorite })
        }

    @Test
    fun `getFavoriteQuotes returns Success with empty list when DAO returns empty`() =
        runTest {
            coEvery { dao.getFavoriteQuotes() } returns emptyList()

            val result = repository.getFavoriteQuotes()

            assertTrue(result is DomainResult.Success)
            assertEquals(emptyList<Quote>(), (result as DomainResult.Success).data)
        }

    @Test
    fun `getFavoriteQuotes wraps DAO exception in Failure`() =
        runTest {
            val boom = RuntimeException("db down")
            coEvery { dao.getFavoriteQuotes() } throws boom

            val result = repository.getFavoriteQuotes()

            assertTrue(result is DomainResult.Failure)
            assertEquals(boom, (result as DomainResult.Failure).error)
        }
}
