package com.sortedqueue.portfolio

import android.app.Application
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.soloader.SoLoader

internal object AppDebugTools {
    fun initialize(application: Application) {
        SoLoader.init(application, false)

        if (FlipperUtils.shouldEnableFlipper(application)) {
            AndroidFlipperClient.getInstance(application).start()
        }
    }
}
