package com.quotify.core.data.localDatabase

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QuotifyDAO {
    // The 'ORDER BY rowid ASC' preserves insertion order so the list
    // doesn't jump around when new pages are appended.
    @Query("SELECT * FROM quotes ORDER BY rowid ASC")
    fun getQuotesPagingSource(): PagingSource<Int, QuoteEntity>

    // Bulk insert. REPLACE means: if a row with the same primary key
    // already exists, overwrite it with fresh data. No duplicates.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

    // Wipes the whole table. Called during REFRESH so stale data
    // doesn't mix with fresh data from the network.
    @Query("DELETE FROM quotes")
    suspend fun clearAll()

    @Query("SELECT * FROM quotes WHERE id = :id")
    suspend fun getQuoteById(id: String): QuoteEntity?
}
