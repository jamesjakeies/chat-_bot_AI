package com.xinyu.ai.core.common

sealed interface NetworkError {
    data object Unauthorized : NetworkError

    data object Timeout : NetworkError

    data class Http(val code: Int, val message: String?) : NetworkError

    data class Unknown(val cause: Throwable? = null) : NetworkError
}

fun NetworkError.asMessage(): String {
    return when (this) {
        NetworkError.Unauthorized -> "登录状态已失效，请重新登录。"
        NetworkError.Timeout -> "请求超时了，请稍后再试。"
        is NetworkError.Http -> message ?: "请求失败，请稍后再试。"
        is NetworkError.Unknown -> cause?.message ?: "发生了未知错误，请稍后再试。"
    }
}
