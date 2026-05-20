package com.sortedqueue.portfolio.core.network.debug

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import okhttp3.OkHttpClient

object NetworkDebugTools {
    fun addDebugInterceptors(
        builder: OkHttpClient.Builder,
        context: Context
    ): OkHttpClient.Builder {
        val chuckerInterceptor = ChuckerInterceptor.Builder(context)
            .redactHeaders("Authorization")
            .build()

        return builder.addInterceptor(chuckerInterceptor)
    }
}
