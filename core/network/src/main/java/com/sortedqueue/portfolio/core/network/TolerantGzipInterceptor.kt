package com.sortedqueue.portfolio.core.network

import java.util.Locale
import java.util.zip.GZIPInputStream
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class TolerantGzipInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
            .newBuilder()
            .header("Accept-Encoding", "gzip")
            .build()

        val response = chain.proceed(request)
        val body = response.body ?: return response

        if (!response.isGzipEncoded()) {
            return response
        }

        val decompressedBytes = GZIPInputStream(body.byteStream()).use { gzipInputStream ->
            gzipInputStream.readBytes()
        }

        return response.newBuilder()
            .removeHeader("Content-Encoding")
            .removeHeader("Content-Length")
            .body(decompressedBytes.toResponseBody(body.contentType()))
            .build()
    }
}

private fun Response.isGzipEncoded(): Boolean {
    return header("Content-Encoding")
        ?.lowercase(Locale.US)
        ?.contains("gzip") == true
}
