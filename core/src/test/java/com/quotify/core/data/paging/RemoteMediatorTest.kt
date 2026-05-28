package com.quotify.core.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.quotify.core.data.localDatabase.QuoteEntity
import com.quotify.core.data.localDatabase.QuotifyDao
import com.quotify.core.data.localDatabase.QuotifyDatabase
import com.quotify.core.data.model.QuoteAPIResponse
import com.quotify.core.data.model.QuotesListAPIResponse
import com.quotify.core.data.network.ApiService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
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
 * to clearNonFavorites/insertAll. Instead, we cover:
 *
 *   - Branches that complete BEFORE the transaction (PREPEND short-circuit, network
 *     errors, API skip-calculation, REFRESH-uses-initialLoadSize-vs-APPEND-uses-pageSize).
 *   - Verifying which API method is called and with what args — the contract from the
 *     network's perspective.
 *
 * The "wipe-on-refresh + insertAll + restore favorites" Room interaction is left to
 * instrumented tests, since it depends on Room behavior we cannot meaningfully unit-test
 * in isolation.
 */
@OptIn(ExperimentalPagingApi::class)
class RemoteMediatorTest {
    private val api = mockk<ApiService>()
    private val dao = mockk<QuotifyDao>(relaxed = true)
    private val database = mockk<QuotifyDatabase>(relaxed = true)
    private lateinit var mediator: QuoteRemoteMediator

    private val pagingConfig =
        PagingConfig(
            pageSize = PagingConstants.PAGE_SIZE,
            prefetchDistance = PagingConstants.PREFETCH_DISTANCE,
            enablePlaceholders = false,
        )
    private val initialLoadSize = pagingConfig.initialLoadSize

    @Before
    fun setUp() {
        every { database.quotifyDao() } returns dao
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
            limit = PagingConstants.PAGE_SIZE,
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
    fun `REFRESH fetches with initialLoadSize and skip zero`() =
        runTest {
            coEvery { api.getQuotesList(limit = initialLoadSize, skip = 0) } returns apiResponse(count = 3)

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertFalse((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)

            // initialLoadSize avoids the three-round-trips problem on cold start.
            coVerify(exactly = 1) { api.getQuotesList(limit = initialLoadSize, skip = 0) }
        }

    @Test
    fun `APPEND fetches with pageSize and skip equal to total items already loaded`() =
        runTest {
            coEvery {
                api.getQuotesList(limit = PagingConstants.PAGE_SIZE, skip = 40)
            } returns apiResponse(count = PagingConstants.PAGE_SIZE, skip = 40)

            val state = pagingStateWithItems(itemCount = 40)
            mediator.load(LoadType.APPEND, state)

            // APPEND skip == state.pages.sumOf { it.data.size }; APPEND uses normal pageSize.
            coVerify(exactly = 1) { api.getQuotesList(limit = PagingConstants.PAGE_SIZE, skip = 40) }
        }

    @Test
    fun `APPEND with multiple loaded pages sums all page sizes for skip`() =
        runTest {
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
            coEvery {
                api.getQuotesList(limit = PagingConstants.PAGE_SIZE, skip = 40)
            } returns apiResponse(count = PagingConstants.PAGE_SIZE, skip = 40)

            mediator.load(LoadType.APPEND, state)

            coVerify(exactly = 1) { api.getQuotesList(limit = PagingConstants.PAGE_SIZE, skip = 40) }
        }

    @Test
    fun `REFRESH returns endOfPaginationReached when API returns empty list`() =
        runTest {
            coEvery { api.getQuotesList(limit = initialLoadSize, skip = 0) } returns
                QuotesListAPIResponse(quotes = emptyList(), total = 0, skip = 0, limit = PagingConstants.PAGE_SIZE)

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            assertTrue(result is RemoteMediator.MediatorResult.Success)
            assertTrue((result as RemoteMediator.MediatorResult.Success).endOfPaginationReached)
        }

    @Test
    fun `network exception is wrapped in MediatorResult Error`() =
        runTest {
            val boom = RuntimeException("network down")
            coEvery { api.getQuotesList(any(), any()) } throws boom

            val result = mediator.load(LoadType.REFRESH, emptyPagingState())

            assertTrue(result is RemoteMediator.MediatorResult.Error)
            assertSame(boom, (result as RemoteMediator.MediatorResult.Error).throwable)
        }
}
