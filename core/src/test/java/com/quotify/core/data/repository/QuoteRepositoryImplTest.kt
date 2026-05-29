package com.quotify.core.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.paging.testing.asSnapshot
import app.cash.turbine.test
import com.quotify.core.common.DomainResult
import com.quotify.core.data.localDatabase.QuoteEntity
import com.quotify.core.data.localDatabase.QuotifyDao
import com.quotify.core.data.paging.QuoteRemoteMediator
import com.quotify.core.domain.model.Quote
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class QuoteRepositoryImplTest {
    private val remoteMediator = mockk<QuoteRemoteMediator>(relaxed = true)
    private val dao = mockk<QuotifyDao>(relaxed = true)
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
    fun `getQuotesStream maps QuoteEntity rows from DAO to domain Quote in order`() =
        runTest {
            val entities =
                listOf(
                    QuoteEntity(
                        id = "1",
                        author = "Robert Frost",
                        quote = "Miles to go before I sleep",
                        favorite = false,
                    ),
                    QuoteEntity(id = "2", author = "Oscar Wilde", quote = "Be yourself", favorite = true),
                )
            coEvery { remoteMediator.initialize() } returns RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH
            coEvery {
                remoteMediator.load(any(), any())
            } returns RemoteMediator.MediatorResult.Success(endOfPaginationReached = true)
            every { dao.getQuotesPagingSource() } returns FakeQuotesPagingSource(entities)

            val snapshot = repository.getQuotesStream().asSnapshot()

            assertEquals(
                listOf(
                    Quote(id = "1", content = "Miles to go before I sleep", author = "Robert Frost", favorite = false),
                    Quote(id = "2", content = "Be yourself", author = "Oscar Wilde", favorite = true),
                ),
                snapshot,
            )
        }

    @Test
    fun `getQuotesStream emits empty list when DAO has no rows`() =
        runTest {
            coEvery { remoteMediator.initialize() } returns RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH
            coEvery {
                remoteMediator.load(any(), any())
            } returns RemoteMediator.MediatorResult.Success(endOfPaginationReached = true)
            every { dao.getQuotesPagingSource() } returns FakeQuotesPagingSource(emptyList())

            val snapshot = repository.getQuotesStream().asSnapshot()

            assertEquals(emptyList<Quote>(), snapshot)
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
    fun `getSingleQuoteStream filters out null emissions instead of emitting Failure`() =
        runTest {
            // Null arises transiently during REFRESH (clearNonFavorites window). We
            // intentionally swallow it so the UI doesn't flash Error → Success.
            every { dao.getQuoteById("missing") } returns flowOf(null)

            repository.getSingleQuoteStream("missing").test {
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
    fun `toggleFavoriteQuote delegates to DAO and returns Success`() =
        runTest {
            val result = repository.toggleFavoriteQuote(id = "1")

            assertTrue(result is DomainResult.Success)
            coVerify(exactly = 1) { dao.toggleFavorite("1") }
        }

    @Test
    fun `toggleFavoriteQuote wraps DAO exception in Failure`() =
        runTest {
            val boom = RuntimeException("db locked")
            coEvery { dao.toggleFavorite("1") } throws boom

            val result = repository.toggleFavoriteQuote(id = "1")

            assertTrue(result is DomainResult.Failure)
            assertSame(boom, (result as DomainResult.Failure).error)
        }

    // --- observeFavoriteQuotes ---

    @Test
    fun `observeFavoriteQuotes maps DAO rows to domain Quotes`() =
        runTest {
            val favorites =
                listOf(
                    entity.copy(id = "1", favorite = true),
                    entity.copy(id = "2", favorite = true),
                )
            every { dao.observeFavoriteQuotes() } returns flowOf(favorites)

            repository.observeFavoriteQuotes().test {
                val result = awaitItem()
                assertEquals(2, result.size)
                assertEquals(listOf("1", "2"), result.map { it.id })
                assertTrue(result.all { it.favorite })
                awaitComplete()
            }
        }

    @Test
    fun `observeFavoriteQuotes emits empty list when DAO has no favorites`() =
        runTest {
            every { dao.observeFavoriteQuotes() } returns flowOf(emptyList())

            repository.observeFavoriteQuotes().test {
                assertEquals(emptyList<Quote>(), awaitItem())
                awaitComplete()
            }
        }
}

/**
 * Deterministic PagingSource for repository tests: returns the given data as a single page
 * with no further keys. Used wherever the DAO's PagingSource needs to be controlled in a
 * unit test, since instantiating a real Room PagingSource would require Robolectric + an
 * in-memory database.
 */
private class FakeQuotesPagingSource(
    private val data: List<QuoteEntity>,
) : PagingSource<Int, QuoteEntity>() {
    override fun getRefreshKey(state: PagingState<Int, QuoteEntity>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, QuoteEntity> =
        LoadResult.Page(
            data = data,
            prevKey = null,
            nextKey = null,
        )
}
