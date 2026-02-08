package com.youtube.auto.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = (this as? Success)?.data

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw cause ?: IllegalStateException(message)
        is Loading -> throw IllegalStateException("Result is still loading")
    }

    fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, cause)
        is Loading -> Loading
    }
}
