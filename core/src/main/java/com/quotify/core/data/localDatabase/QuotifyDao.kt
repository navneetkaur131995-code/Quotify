package com.quotify.core.data.localDatabase

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuotifyDao {
    // Preserves insertion order so appended pages don't reorder visible rows.
    @Query("SELECT * FROM quotes ORDER BY rowid ASC")
    fun getQuotesPagingSource(): PagingSource<Int, QuoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quotes: List<QuoteEntity>)

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

    // Atomic SQL toggle — no read/decide/write race in app code, no parameter ambiguity.
    @Query("UPDATE quotes SET favorite = CASE WHEN favorite = 1 THEN 0 ELSE 1 END WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    // Reactive stream so the favorites screen updates when toggled from elsewhere.
    @Query("SELECT * FROM quotes WHERE favorite = 1 ORDER BY rowid ASC")
    fun observeFavoriteQuotes(): Flow<List<QuoteEntity>>
}
