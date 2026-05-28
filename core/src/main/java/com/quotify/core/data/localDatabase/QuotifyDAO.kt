package com.quotify.core.data.localDatabase

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

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

    // We need to wipe the whole table. Called during REFRESH
    // so stale data doesn't mix with fresh data from the network.

    // Wipe only non-favorite rows on REFRESH so user favorites survive.
    @Query("DELETE FROM quotes WHERE favorite = 0")
    suspend fun clearNonFavorites()

    // Snapshot of favorite ids taken before insert so we can re-apply after REPLACE.
    @Query("SELECT id FROM quotes WHERE favorite = 1")
    suspend fun getFavoriteIds(): List<String>

    // Re-apply favorite=1 for the ids that had it before insertAll(REPLACE) clobbered them.
    @Query("UPDATE quotes SET favorite = 1 WHERE id IN (:ids)")
    suspend fun markFavorites(ids: List<String>)


    @Query("SELECT * FROM quotes WHERE id = :id")
    fun getQuoteById(id: String): Flow<QuoteEntity?>

    @Query("UPDATE quotes SET favorite = 1 WHERE id = :id")
    suspend fun addToFavorites(id: String)

    @Query("UPDATE quotes SET favorite = 0 WHERE id = :id")
    suspend fun removeFromFavorites(id: String)

    @Query("SELECT * FROM quotes WHERE favorite = 1")
    suspend fun getFavoriteQuotes(): List<QuoteEntity>
}
