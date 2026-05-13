package com.quotify.core.domain.repository

import androidx.paging.PagingData
import com.quotify.core.common.DomainResult
import com.quotify.core.domain.model.Quote
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    // Returns a Flow of paginated quotes backed by Room.
    // The Flow emits a new PagingData whenever Room's data changes.
    // The RemoteMediator keeps Room fresh from the network.

    //    Why Flow<PagingData<Quote>> as it is Paging 3? Yes it is still Android library & not be in domain layer
    //    But it's the right abstraction level as
    //    the interface expresses "give me a stream of paged quotes" without dictating how paging works internally.
    //    A pragmatic tradeoff & a reasonable position for this project size
    fun getQuotesStream(): Flow<PagingData<Quote>>

    // Reads a single quote from Room by ID.
    // Returns DomainResult.Success if found, DomainResult.Failure if not cached.
    suspend fun getSingleQuote(id: String): DomainResult<Quote>
}
