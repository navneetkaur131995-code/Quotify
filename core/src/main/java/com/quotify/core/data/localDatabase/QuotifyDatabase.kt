package com.quotify.core.data.localDatabase

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [QuoteEntity::class], version = 1)
abstract class QuotifyDatabase : RoomDatabase() {
    abstract fun quotifyDao(): QuotifyDao
}
