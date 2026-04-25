package com.xinyu.ai.core.common

import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkErrorTest {
    @Test
    fun `asMessage returns readable Chinese messages`() {
        assertEquals("登录状态已失效，请重新登录。", NetworkError.Unauthorized.asMessage())
        assertEquals("请求超时了，请稍后再试。", NetworkError.Timeout.asMessage())
        assertEquals("额度已用完", NetworkError.Http(403, "额度已用完").asMessage())
        assertEquals("请求失败，请稍后再试。", NetworkError.Http(500, null).asMessage())
        assertEquals("发生了未知错误，请稍后再试。", NetworkError.Unknown().asMessage())
    }
}
