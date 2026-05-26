package com.quotify.feature.favorites

import app.cash.turbine.test
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetFavoriteQuotesUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val getFavoriteQuotesUseCase = mockk<GetFavoriteQuotesUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `uiState starts in Loading`() =
        runTest {
            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            assertEquals(FavoritesUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun `getFavoriteQuotes emits Success with the list from the use case`() =
        runTest {
            val favorites =
                listOf(
                    Quote(id = "1", content = "a", author = "x", favorite = true),
                    Quote(id = "2", content = "b", author = "y", favorite = true),
                )
            coEvery { getFavoriteQuotesUseCase() } returns DomainResult.Success(favorites)

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            viewModel.uiState.test {
                // Initial Loading from constructor.
                assertEquals(FavoritesUiState.Loading, awaitItem())

                viewModel.getFavoriteQuotes()

                // Reaches Success — on UnconfinedTestDispatcher the intermediate Loading set
                // by getFavoriteQuotes() collapses with the existing Loading, so only the
                // final Success transition shows up as a new emission.
                assertEquals(FavoritesUiState.Success(favorites), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
            coVerify(exactly = 1) { getFavoriteQuotesUseCase() }
        }

    @Test
    fun `getFavoriteQuotes emits Error with message from the failure`() =
        runTest {
            coEvery { getFavoriteQuotesUseCase() } returns DomainResult.Failure(Exception("db broken"))

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)
            viewModel.getFavoriteQuotes()

            val state = viewModel.uiState.value
            assertTrue(state is FavoritesUiState.Error)
            assertEquals("db broken", (state as FavoritesUiState.Error).message)
        }

    @Test
    fun `getFavoriteQuotes Error falls back to default when failure has no message`() =
        runTest {
            coEvery { getFavoriteQuotesUseCase() } returns DomainResult.Failure(Exception())

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)
            viewModel.getFavoriteQuotes()

            val state = viewModel.uiState.value
            assertTrue(state is FavoritesUiState.Error)
            assertEquals("An unknown error occurred", (state as FavoritesUiState.Error).message)
        }

    @Test
    fun `getFavoriteQuotes can be called multiple times to refresh`() =
        runTest {
            coEvery { getFavoriteQuotesUseCase() } returnsMany
                listOf(
                    DomainResult.Success(emptyList()),
                    DomainResult.Success(
                        listOf(Quote(id = "1", content = "a", author = "x", favorite = true)),
                    ),
                )

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            viewModel.getFavoriteQuotes()
            assertEquals(FavoritesUiState.Success(emptyList()), viewModel.uiState.value)

            viewModel.getFavoriteQuotes()
            assertEquals(
                FavoritesUiState.Success(
                    listOf(Quote(id = "1", content = "a", author = "x", favorite = true)),
                ),
                viewModel.uiState.value,
            )

            coVerify(exactly = 2) { getFavoriteQuotesUseCase() }
        }
}
