package com.quotify.core.data.localDatabase

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey val id: String,
    val author: String,
    val quote: String,
    val favorite: Boolean = false,
)
