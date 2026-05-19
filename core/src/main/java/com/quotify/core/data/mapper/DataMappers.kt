package com.quotify.core.data.mapper

import com.quotify.core.data.localDatabase.QuoteEntity
import com.quotify.core.data.model.QuoteAPIResponse
import com.quotify.core.domain.model.Quote

fun QuoteAPIResponse.toEntity(): QuoteEntity =
    QuoteEntity(
        id = id.toString(),
        quote = quote,
        author = author,
    )

// Database Entity → Domain Model
fun QuoteEntity.toDomain(): Quote =
    Quote(
        id = id,
        content = quote,
        author = author,
        favorite = favorite,
    )
