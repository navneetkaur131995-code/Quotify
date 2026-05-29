package com.quotify.feature.favorites

import app.cash.turbine.test
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetFavoriteQuotesUseCase
import com.quotify.feature.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FavoritesViewModelTest {
    private val getFavoriteQuotesUseCase = mockk<GetFavoriteQuotesUseCase>()

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun `uiState starts in Loading`() =
        runTest {
            every { getFavoriteQuotesUseCase() } returns flowOf(emptyList())

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            assertEquals(FavoritesUiState.Loading, viewModel.uiState.value)
        }

    @Test
    fun `uiState emits Success with the list from the use case`() =
        runTest {
            val favorites =
                listOf(
                    Quote(id = "1", content = "a", author = "x", favorite = true),
                    Quote(id = "2", content = "b", author = "y", favorite = true),
                )
            every { getFavoriteQuotesUseCase() } returns flowOf(favorites)

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            viewModel.uiState.test {
                assertEquals(FavoritesUiState.Loading, awaitItem())
                assertEquals(FavoritesUiState.Success(favorites), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState emits Success with empty list when use case has no favorites`() =
        runTest {
            every { getFavoriteQuotesUseCase() } returns flowOf(emptyList())

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            viewModel.uiState.test {
                assertEquals(FavoritesUiState.Loading, awaitItem())
                assertEquals(FavoritesUiState.Success(emptyList()), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState emits Error when the upstream flow throws`() =
        runTest {
            every { getFavoriteQuotesUseCase() } returns
                flow {
                    throw RuntimeException("db broken")
                }

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            viewModel.uiState.test {
                assertEquals(FavoritesUiState.Loading, awaitItem())
                val state = awaitItem()
                assertTrue(state is FavoritesUiState.Error)
                assertEquals("db broken", (state as FavoritesUiState.Error).message)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState reflects later emissions from the use case (e g favorite toggle)`() =
        runTest {
            val source = MutableStateFlow<List<Quote>>(emptyList())
            every { getFavoriteQuotesUseCase() } returns source

            val viewModel = FavoritesViewModel(getFavoriteQuotesUseCase)

            viewModel.uiState.test {
                assertEquals(FavoritesUiState.Loading, awaitItem())
                assertEquals(FavoritesUiState.Success(emptyList()), awaitItem())

                val updated = listOf(Quote(id = "1", content = "a", author = "x", favorite = true))
                source.value = updated
                assertEquals(FavoritesUiState.Success(updated), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
