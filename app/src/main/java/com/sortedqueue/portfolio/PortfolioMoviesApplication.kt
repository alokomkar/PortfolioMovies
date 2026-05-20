package com.sortedqueue.portfolio

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PortfolioMoviesApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppDebugTools.initialize(this)
    }
}
