package com.example.quotify.core.data.mapper

import com.example.quotify.core.domain.model.Quote
import com.example.quotify.core.network.model.QuoteDTO

fun QuoteDTO.toDomain(): Quote {
    return Quote(id = id, text = content, authorName = author, tags = tags)
}
