package com.example.quotify.core.network.model

import com.google.gson.annotations.SerializedName

/*
// A unique id for this author
_id: string
// A brief, one paragraph bio of the author. Source: wiki API
bio: string
// A one-line description of the author. Typically it is the person's primary
// occupation or what they are know for.
description: string
// The link to the author's wikipedia page or official website
link: string
// The authors full name
name: string
// A slug is a URL-friendly ID derived from the authors name. It can be used as
slug: string
// The number of quotes by this author
quoteCount: string
*/
data class QuoteAPIResponse(
    @SerializedName("_id")
    val id: String,

    val content: String,
    val author: String,
    val authorSlug: String,
    val length: Int,
    val tags: List<String>
)