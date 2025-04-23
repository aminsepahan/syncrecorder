package com.appleader707.syncrecorder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

const val TAG = "SYNCRECORDER"

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}