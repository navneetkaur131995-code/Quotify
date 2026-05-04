package com.quotify.core.data.mapper

import com.quotify.core.data.network.model.QuoteAPIResponse
import com.quotify.core.data.network.model.QuotesListAPIResponse
import com.quotify.core.domain.model.Quote
import com.quotify.core.domain.model.QuotesList

fun QuotesListAPIResponse.toDomain(): QuotesList =
    QuotesList(
        results = quotes.map { it.toDomain() },
    )

fun QuoteAPIResponse.toDomain(): Quote =
    Quote(
        id = id.toString(),
        content = quote,
        author = author,
    )
