package com.example.quotify.core.domain.model

data class Quote(
    val id: String,
    val text: String,
    val authorName: String,
    val tags: List<String>
)