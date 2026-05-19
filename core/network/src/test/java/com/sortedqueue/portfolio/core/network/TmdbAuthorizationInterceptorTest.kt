package com.sortedqueue.portfolio.core.network

import java.util.concurrent.TimeUnit
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TmdbAuthorizationInterceptorTest {
    @Test
    fun interceptor_addsBearerAuthorizationHeaderWhenTokenExists() {
        val chain = RecordingChain()
        val interceptor = TmdbAuthorizationInterceptor("token-123")

        interceptor.intercept(chain)

        assertEquals("Bearer token-123", chain.proceededRequest?.header("Authorization"))
    }

    @Test
    fun interceptor_doesNotAddAuthorizationHeaderWhenTokenIsBlank() {
        val chain = RecordingChain()
        val interceptor = TmdbAuthorizationInterceptor("")

        interceptor.intercept(chain)

        assertNull(chain.proceededRequest?.header("Authorization"))
    }
}

private class RecordingChain : Interceptor.Chain {
    private val initialRequest = Request.Builder()
        .url("https://api.themoviedb.org/3/movie/11")
        .build()

    var proceededRequest: Request? = null
        private set

    override fun request(): Request = initialRequest

    override fun proceed(request: Request): Response {
        proceededRequest = request
        return Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body("{}".toResponseBody("application/json".toMediaType()))
            .build()
    }

    override fun call(): Call {
        throw UnsupportedOperationException()
    }

    override fun connection(): Connection? = null

    override fun connectTimeoutMillis(): Int = 0

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

    override fun readTimeoutMillis(): Int = 0

    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this

    override fun writeTimeoutMillis(): Int = 0

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain = this
}
