package com.quotify.core.domain.repository

import androidx.paging.PagingData
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    // Paginated stream backed by Room as single source of truth.
    // The RemoteMediator keeps Room fresh; the UI observes Room only.
    // PagingData is an Android type, but pragmatic to surface it here at this project size.
    fun getQuotesStream(): Flow<PagingData<Quote>>

    // Hot Room-backed stream that re-emits when the row changes (e.g. on favorite toggle).
    // Emits Success when the row is present; never emits Failure for transient absence.
    fun getSingleQuoteStream(id: String): Flow<DomainResult<Quote>>

    // Atomic toggle — implementation flips the row's favorite flag in SQL, so callers
    // don't have to pass the current state and risk read/decide/write races.
    suspend fun toggleFavoriteQuote(id: String): DomainResult<Unit>

    // Reactive list of favorites so the favorites screen updates automatically when a
    // favorite is toggled from anywhere in the app.
    fun observeFavoriteQuotes(): Flow<List<Quote>>
}
