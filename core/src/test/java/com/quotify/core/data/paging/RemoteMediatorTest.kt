package com.quotify.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quotify.core.data.localDatabase.QuoteEntity
import com.quotify.core.data.localDatabase.QuotifyDAO
import com.quotify.core.data.localDatabase.QuotifyDatabase
import com.quotify.core.data.model.QuoteAPIResponse
import com.quotify.core.data.model.QuotesListAPIResponse
import com.quotify.core.data.network.APIService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Notes on scope:
 *
 * The mediator's `database.withTransaction { ... }` block is a Room top-level suspend
 * extension. Reliably executing that block from a JVM unit test would require Robolectric
 * + an in-memory Room database, which is overkill for what is essentially a pass-through
 * to clearAll/insertAll. Instead, we cover:
 *
 *   - The branches that complete BEFORE the transaction (PREPEND short-circuit, network
 *     errors,  API skip-calculation for REFRESH and APPEND).
 *   - Verifying which API method is called and with what args — the contract of the
 *     mediator from the network's perspective.
 *
 * The "wipe-on-refresh + insertAll" Room interaction is left to instrumented tests, since
 * it depends on Room behavior we cannot meaningfully unit-test in isolation.
 */
@OptIn(ExperimentalPagingApi::class)
class RemoteMediatorTest {
    private val api = mockk<APIService>()
    private val dao = mockk<QuotifyDAO>(relaxed = true)
    private val database = mockk<QuotifyDatabase>(relaxed = true)
    private lateinit var mediator: QuoteRemoteMediator

    private val pagingConfig = PagingConfig(pageSize = 20, prefetchDistance = 5, enablePlaceholders = false)

    @Before
    fun setUp() {
        every { database.quotifyDAO() } returns dao
        // Static mock so the real Room implementation isn't invoked. Tests that depend on
        // the block actually running its body are excluded from this suite (see file-level
        // note above).
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { database.withTransaction(any<suspend () -> Any?>()) } returns Unit
        mediator = QuoteRemoteMediator(api, database)
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    private fun emptyPagingState(): PagingState<Int, QuoteEntity> =
        PagingState(
            pages = emptyList(),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0,
        )

    private fun pagingStateWithItems(itemCount: Int): PagingState<Int, QuoteEntity> {
        val data = (1..itemCount).map { QuoteEntity(id = "$it", author = "a$it", quote = "q$it") }
        val page =
            PagingSource.LoadResult.Page<Int, QuoteEntity>(
                data = data,
                prevKey = null,
                nextKey = null,
            )
        return PagingState(
            pages = listOf(page),
            anchorPosition = null,
            config = pagingConfig,
            leadingPlaceholderCount = 0,
        )
    }

    private fun apiResponse(
        count: Int,
        skip: Int = 0,
    ): QuotesListAPIResponse =
        QuotesListAPIResponse(
            quotes = (1..count).map { QuoteAPIResponse(id = it, quote = "q$it", author = "a$it") },
            total = 100,
            skip = skip,
            limit = 20,
        )

    @Test
    fun `PREPEND returns endOfPaginationReached without calling API`() =
        runTest {
            val result = mediator.load(LoadType.PREPEND, emptyPagingState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
            coVerify(exactly = 0) { api.getQuotesList(any(), any()) }
        }

    @Test
    fun `REFRESH fetches with skip zero`() =
        runTest {
            coEvery { api.getQuotesList(limit = 20, skip = 0) } returns apiResponse(count = 3)

            mediator.load(LoadType.REFRESH, emptyPagingState())

            // Verifies the contract from the network's perspective: refresh always starts at 0.
            coVerify(exactly = 1) { api.getQuotesList(limit = 20, skip = 0) }
        }

    @Test
    fun `APPEND fetches with skip equal to the total items already loaded`() =
        runTest {
            coEvery { api.getQuotesList(limit = 20, skip = 40) } returns apiResponse(count = 20, skip = 40)

            val state = pagingStateWithItems(itemCount = 40)
            mediator.load(LoadType.APPEND, state)

            // APPEND skip == state.pages.sumOf { it.data.size }
            coVerify(exactly = 1) { api.getQuotesList(limit = 20, skip = 40) }
        }

    @Test
    fun `APPEND with multiple loaded pages sums all page sizes for skip`() =
        runTest {
            // Two pages of 20 items each — total 40 → next skip should be 40, not 2 (pages).
            val page1 =
                PagingSource.LoadResult.Page<Int, QuoteEntity>(
                    data = (1..20).map { QuoteEntity(id = "$it", author = "a", quote = "q") },
                    prevKey = null,
                    nextKey = null,
                )
            val page2 =
                PagingSource.LoadResult.Page<Int, QuoteEntity>(
                    data = (21..40).map { QuoteEntity(id = "$it", author = "a", quote = "q") },
                    prevKey = null,
                    nextKey = null,
                )
            val state =
                PagingState(
                    pages = listOf(page1, page2),
                    anchorPosition = null,
                    config = pagingConfig,
                    leadingPlaceholderCount = 0,
                )
            coEvery { api.getQuotesList(limit = 20, skip = 40) } returns apiResponse(count = 20, skip = 40)

            mediator.load(LoadType.APPEND, state)

            coVerify(exactly = 1) { api.getQuotesList(limit = 20, skip = 40) }
        }

    @Test
    fun `network exception is wrapped in MediatorResult Error`() =
        runTest {
            val boom = RuntimeException("network down")
            coEvery { api.getQuotesList(any(), any()) } throws boom

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            assertTrue(result is RemoteMediator.MediatorResult.Error)
            assertSame(boom, (result as RemoteMediator.MediatorResult.Error).throwable)
            // No DB work attempted when the network call itself fails.
            coVerify(exactly = 0) { dao.clearAll() }
            coVerify(exactly = 0) { dao.insertAll(any()) }
        }

    @Test
    fun `mediator can be instantiated with API and database`() {
        assertNotNull(mediator)
    }
}
