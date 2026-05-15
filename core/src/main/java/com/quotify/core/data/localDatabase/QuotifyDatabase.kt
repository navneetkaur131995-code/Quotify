package com.quotify.core.data.localDatabase

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [QuoteEntity::class], version = 1)
abstract class QuotifyDatabase : RoomDatabase() {
    abstract fun quotifyDAO(): QuotifyDAO

    companion object {
        val MIGRATION_1_2 =
            object : Migration(1, 2) {
                override fun migrate(db: SupportSQLiteDatabase) {
                    db.execSQL("ALTER TABLE quotes ADD COLUMN favorite INTEGER NOT NULL DEFAULT 0")
                }
            }
    }
}
