package com.example.quotify.core.domain.model

data class Quote(
    val id: String,
    val content: String,
    val author: String,
    val authorSlug: String,
    val length: Int,
    val tags: List<String>
)