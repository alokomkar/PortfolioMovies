package com.sortedqueue.portfolio.core.network.debug

import android.content.Context
import okhttp3.OkHttpClient

object NetworkDebugTools {
    fun addDebugInterceptors(
        builder: OkHttpClient.Builder,
        context: Context
    ): OkHttpClient.Builder = builder
}
