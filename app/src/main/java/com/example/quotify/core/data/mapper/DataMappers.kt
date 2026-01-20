package com.example.quotify.core.data.mapper

import com.example.quotify.core.domain.model.Quote
import com.example.quotify.core.domain.model.QuotesList
import com.example.quotify.core.network.model.QuoteAPIResponse
import com.example.quotify.core.network.model.QuotesListAPIResponse

fun QuotesListAPIResponse.toDomain(): QuotesList {
    return QuotesList(
        count = count,
        totalCount = totalCount,
        page = page,
        totalPages = totalPages,
        lastItemIndex = lastItemIndex,
        results = results.map { it.toDomain() }

    )
}

fun QuoteAPIResponse.toDomain(): Quote {
    return Quote(
        id = id,
        content = content,
        author = author,
        authorSlug = authorSlug,
        length = length,
        tags = tags
    )
}
