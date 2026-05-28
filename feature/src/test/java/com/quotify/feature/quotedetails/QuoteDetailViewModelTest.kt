package com.quotify.feature.quotedetails

import app.cash.turbine.test
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.usecase.GetQuoteDetailUseCase
import com.quotify.core.domain.usecase.ToggleFavoriteUseCase
import com.quotify.feature.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteDetailViewModelTest {
    private val getQuoteDetailUseCase = mockk<GetQuoteDetailUseCase>()
    private val toggleFavoriteUseCase = mockk<ToggleFavoriteUseCase>(relaxed = true)

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun `uiState starts at Loading before any id is set`() =
        runTest {
            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)

            viewModel.uiState.test {
                assertEquals(QuoteDetailUiState.Loading, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState transitions to Success when use case returns a quote`() =
        runTest {
            val quote = Quote(id = "1", content = "Hi", author = "X", favorite = false)
            every { getQuoteDetailUseCase("1") } returns flowOf(DomainResult.Success(quote))

            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)

            viewModel.uiState.test {
                assertEquals(QuoteDetailUiState.Loading, awaitItem())
                viewModel.setQuoteId("1")
                assertEquals(QuoteDetailUiState.Success(quote), awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState transitions to Error with use case failure message`() =
        runTest {
            every { getQuoteDetailUseCase("missing") } returns
                flowOf(
                    DomainResult.Failure(Exception("not cached")),
                )

            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)

            viewModel.uiState.test {
                assertEquals(QuoteDetailUiState.Loading, awaitItem())
                viewModel.setQuoteId("missing")
                val item = awaitItem()
                assertTrue(item is QuoteDetailUiState.Error)
                assertEquals("not cached", (item as QuoteDetailUiState.Error).message)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState Error falls back to default message when exception has no message`() =
        runTest {
            every { getQuoteDetailUseCase("x") } returns
                flowOf(
                    DomainResult.Failure(Exception()),
                )

            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)

            viewModel.uiState.test {
                assertEquals(QuoteDetailUiState.Loading, awaitItem())
                viewModel.setQuoteId("x")
                val item = awaitItem()
                assertTrue(item is QuoteDetailUiState.Error)
                assertEquals("Something went wrong", (item as QuoteDetailUiState.Error).message)
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `uiState reflects later emissions from the use case (e g favorite toggle)`() =
        runTest {
            val source = MutableSharedFlow<DomainResult<Quote>>(replay = 1)
            every { getQuoteDetailUseCase("1") } returns source

            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)

            viewModel.uiState.test {
                assertEquals(QuoteDetailUiState.Loading, awaitItem())
                viewModel.setQuoteId("1")

                val q1 = Quote(id = "1", content = "Hi", author = "X", favorite = false)
                source.emit(DomainResult.Success(q1))
                assertEquals(QuoteDetailUiState.Success(q1), awaitItem())

                val q2 = q1.copy(favorite = true)
                source.emit(DomainResult.Success(q2))
                assertEquals(QuoteDetailUiState.Success(q2), awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `setQuoteId is a no-op when the same id is set twice`() =
        runTest {
            every { getQuoteDetailUseCase("1") } returns
                flowOf(
                    DomainResult.Success(Quote(id = "1", content = "a", author = "b", favorite = false)),
                )

            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)

            viewModel.uiState.test {
                assertEquals(QuoteDetailUiState.Loading, awaitItem())
                viewModel.setQuoteId("1")
                // Awaiting Success forces flatMapLatest to actually run with the new id —
                // without this the StandardTestDispatcher never drives the emission and
                // the verify below would see zero invocations.
                awaitItem()
                viewModel.setQuoteId("1")
                viewModel.setQuoteId("1")
                cancelAndIgnoreRemainingEvents()
            }

            // Use case invoked exactly once because the id never actually changes.
            verify(exactly = 1) { getQuoteDetailUseCase("1") }
        }

    @Test
    fun `toggleFavorite delegates to ToggleFavoriteUseCase with quote id`() =
        runTest {
            coEvery { toggleFavoriteUseCase("42") } returns DomainResult.Success(Unit)
            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)
            val quote = Quote(id = "42", content = "c", author = "a", favorite = true)

            viewModel.toggleFavorite(quote)
            // Drive the viewModelScope.launch on the test dispatcher so the use case
            // actually gets invoked before we verify.
            advanceUntilIdle()

            coVerify(exactly = 1) { toggleFavoriteUseCase("42") }
        }

    @Test
    fun `toggleFavorite emits ShowError effect on Failure`() =
        runTest {
            coEvery { toggleFavoriteUseCase("42") } returns
                DomainResult.Failure(RuntimeException("db locked"))
            val viewModel = QuoteDetailViewModel(getQuoteDetailUseCase, toggleFavoriteUseCase)
            val quote = Quote(id = "42", content = "c", author = "a", favorite = true)

            viewModel.effects.test {
                viewModel.toggleFavorite(quote)
                val effect = awaitItem()
                assertTrue(effect is QuoteDetailEffect.ShowError)
                assertEquals("db locked", (effect as QuoteDetailEffect.ShowError).message)
                cancelAndIgnoreRemainingEvents()
            }
        }
}
