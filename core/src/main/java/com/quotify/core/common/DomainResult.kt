package com.quotify.core.common

/**
 * Two-case result for one-shot data-layer calls.
 *
 * Loading is intentionally NOT a member: a `suspend fun` returns exactly once when it
 * completes, so "in flight" cannot be a return value. Model in-flight state in the UI
 * layer (a sealed UiState) or as Flow emissions where streaming progress is meaningful.
 */
sealed interface DomainResult<out T> {
    data class Success<T>(
        val data: T,
    ) : DomainResult<T>

    data class Failure(
        val error: Throwable,
    ) : DomainResult<Nothing>
}
