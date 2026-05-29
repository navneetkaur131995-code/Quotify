package com.quotify.feature.home

import androidx.paging.PagingData
import app.cash.turbine.test
import com.quotify.core.domain.connectivity.NetworkMonitor
import com.quotify.core.domain.usecase.GetQuotesUseCase
import com.quotify.feature.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private val getQuotesUseCase = mockk<GetQuotesUseCase>()
    private val networkMonitor = mockk<NetworkMonitor>()

    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun `pagingDataFlow subscribes to the use case`() =
        runTest {
            // cachedIn keeps the upstream alive in viewModelScope, so we can't asSnapshot
            // it here without leaking a coroutine into the test scope. Verifying delegation
            // is sufficient as the use case's own snapshot test covers the data path
            // end-to-end.
            every { getQuotesUseCase() } returns flowOf(PagingData.empty())
            every { networkMonitor.isOnline } returns flowOf(false)

            HomeViewModel(getQuotesUseCase, networkMonitor)

            verify(exactly = 1) { getQuotesUseCase() }
        }

    @Test
    fun `isOnline starts null then reflects monitor`() =
        runTest {
            every { getQuotesUseCase() } returns flowOf(PagingData.empty())
            val onlineFlow = MutableStateFlow(false)
            every { networkMonitor.isOnline } returns onlineFlow

            val viewModel = HomeViewModel(getQuotesUseCase, networkMonitor)

            viewModel.isOnline.test {
                // null initial value — UI treats this as "no reading yet" and hides the banner.
                assertNull(awaitItem())

                assertEquals(false, awaitItem())

                onlineFlow.value = true
                assertEquals(true, awaitItem())

                onlineFlow.value = false
                assertEquals(false, awaitItem())

                cancelAndIgnoreRemainingEvents()
            }
        }
}
