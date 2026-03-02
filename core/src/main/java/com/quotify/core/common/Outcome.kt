package com.quotify.core.common

sealed class Outcome<out T>{
    data class Success<out T>(val data: T): Outcome<T>()
    data class Failure(val throwable: Throwable): Outcome<Nothing>()
    object Loading: Outcome<Nothing>()
}