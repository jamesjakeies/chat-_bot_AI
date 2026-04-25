package com.xinyu.ai.core.common

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>

    data class Error(val error: NetworkError) : AppResult<Nothing>
}
