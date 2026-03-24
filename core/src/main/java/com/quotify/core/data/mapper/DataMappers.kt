package com.quotify.core.data.mapper

import com.quotify.core.data.network.model.QuoteAPIResponse
import com.quotify.core.data.network.model.QuotesListAPIResponse
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.model.QuotesList

fun QuotesListAPIResponse.toDomain(): QuotesList {
    return QuotesList(
        results = results.map { it.toDomain() }

    )
}

fun QuoteAPIResponse.toDomain(): Quote {
    return Quote(
        id = id,
        content = content,
        author = author
    )
}
