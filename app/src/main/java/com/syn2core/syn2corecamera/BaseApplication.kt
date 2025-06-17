package com.syn2core.syn2corecamera

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

const val TAG = "SYN2CORE"

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        appContext = this
    }

    companion object {
        lateinit var appContext: Application private set
    }
}