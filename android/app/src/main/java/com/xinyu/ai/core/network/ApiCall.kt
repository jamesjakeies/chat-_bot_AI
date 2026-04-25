package com.xinyu.ai.core.network

import com.xinyu.ai.core.common.AppResult
import com.xinyu.ai.core.common.NetworkError
import java.io.IOException
import java.net.SocketTimeoutException
import retrofit2.HttpException

suspend fun <T> safeApiCall(
    block: suspend () -> ApiEnvelope<T>,
): AppResult<T> {
    return try {
        val response = block()
        val data = response.data
        if (response.success && data != null) {
            AppResult.Success(data)
        } else {
            AppResult.Error(
                NetworkError.Http(
                    code = 400,
                    message = response.message ?: "请求失败",
                ),
            )
        }
    } catch (error: HttpException) {
        AppResult.Error(
            if (error.code() == 401) {
                NetworkError.Unauthorized
            } else {
                NetworkError.Http(error.code(), error.extractReadableMessage())
            },
        )
    } catch (_: SocketTimeoutException) {
        AppResult.Error(NetworkError.Timeout)
    } catch (_: IOException) {
        AppResult.Error(NetworkError.Http(-1, "网络连接失败，请确认后端服务已经启动"))
    } catch (error: Throwable) {
        AppResult.Error(NetworkError.Unknown(error))
    }
}

private fun HttpException.extractReadableMessage(): String {
    val body = response()?.errorBody()?.string().orEmpty()
    val message = Regex("\"message\"\\s*:\\s*\"([^\"]+)\"")
        .find(body)
        ?.groupValues
        ?.getOrNull(1)
    return message ?: message()
}
