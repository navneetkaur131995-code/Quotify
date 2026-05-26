package com.quotify.feature.home

import androidx.paging.PagingData
import app.cash.turbine.test
import com.quotify.core.domain.connectivity.NetworkMonitor
import com.quotify.core.domain.usecase.GetQuotesUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val getQuotesUseCase = mockk<GetQuotesUseCase>()
    private val networkMonitor = mockk<NetworkMonitor>()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `pagingDataFlow subscribes to the use case`() =
        runTest {
            // cachedIn keeps the upstream alive in viewModelScope, so we can't asSnapshot()
            // it here without leaking a coroutine into the test scope. Verifying delegation
            // is sufficient — the use case's own snapshot test (GetQuotesUseCaseTest) covers
            // the data path end-to-end.
            every { getQuotesUseCase() } returns flowOf(PagingData.empty())
            every { networkMonitor.isOnline } returns flowOf(false)

            HomeViewModel(getQuotesUseCase, networkMonitor)

            verify(exactly = 1) { getQuotesUseCase() }
        }

    @Test
    fun `pagingDataFlow delegates to use case exactly once`() =
        runTest {
            every { getQuotesUseCase() } returns flowOf(PagingData.empty())
            every { networkMonitor.isOnline } returns flowOf(false)

            HomeViewModel(getQuotesUseCase, networkMonitor)

            verify(exactly = 1) { getQuotesUseCase() }
        }

    @Test
    fun `isOnline starts with false then reflects monitor`() =
        runTest(UnconfinedTestDispatcher()) {
            every { getQuotesUseCase() } returns flowOf(PagingData.empty())
            val onlineFlow = MutableStateFlow(false)
            every { networkMonitor.isOnline } returns onlineFlow

            val viewModel = HomeViewModel(getQuotesUseCase, networkMonitor)

            viewModel.isOnline.test {
                // Initial value as documented in the ViewModel.
                assertEquals(false, awaitItem())

                onlineFlow.value = true
                assertEquals(true, awaitItem())

                onlineFlow.value = false
                assertEquals(false, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
