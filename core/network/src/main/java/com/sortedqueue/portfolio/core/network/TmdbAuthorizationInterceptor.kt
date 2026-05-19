package com.sortedqueue.portfolio.core.network

import okhttp3.Interceptor
import okhttp3.Response

class TmdbAuthorizationInterceptor(
    private val accessToken: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (accessToken.isBlank()) {
            chain.request()
        } else {
            chain.request()
                .newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }

        return chain.proceed(request)
    }
}
