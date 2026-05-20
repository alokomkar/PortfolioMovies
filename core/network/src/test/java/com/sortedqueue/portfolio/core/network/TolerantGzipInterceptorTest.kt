package com.sortedqueue.portfolio.core.network

import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.GZIPOutputStream
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

class TolerantGzipInterceptorTest {
    @Test
    fun interceptor_requestsGzipAndReturnsDecompressedJson() {
        val chain = GzipRecordingChain(
            responseBody = gzip("""{"results":[]}""").toResponseBody("application/json".toMediaType()),
            headers = mapOf("Content-Encoding" to "gzip", "Content-Length" to "30")
        )

        val response = TolerantGzipInterceptor().intercept(chain)

        assertEquals("gzip", chain.proceededRequest?.header("Accept-Encoding"))
        assertEquals("""{"results":[]}""", response.body?.string())
        assertNull(response.header("Content-Encoding"))
        assertNull(response.header("Content-Length"))
    }

    @Test
    fun interceptor_leavesIdentityResponsesAlone() {
        val chain = GzipRecordingChain(
            responseBody = """{"results":[]}""".toResponseBody("application/json".toMediaType())
        )

        val response = TolerantGzipInterceptor().intercept(chain)

        assertEquals("gzip", chain.proceededRequest?.header("Accept-Encoding"))
        assertEquals("""{"results":[]}""", response.body?.string())
    }
}

private class GzipRecordingChain(
    private val responseBody: okhttp3.ResponseBody,
    private val headers: Map<String, String> = emptyMap()
) : Interceptor.Chain {
    private val initialRequest = Request.Builder()
        .url("https://api.themoviedb.org/3/movie/popular")
        .build()

    var proceededRequest: Request? = null
        private set

    override fun request(): Request = initialRequest

    override fun proceed(request: Request): Response {
        proceededRequest = request
        val responseBuilder = Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody)

        headers.forEach { (name, value) ->
            responseBuilder.header(name, value)
        }

        return responseBuilder.build()
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

private fun gzip(value: String): ByteArray {
    val output = ByteArrayOutputStream()
    GZIPOutputStream(output).use { gzipOutputStream ->
        gzipOutputStream.write(value.toByteArray())
    }
    return output.toByteArray()
}
