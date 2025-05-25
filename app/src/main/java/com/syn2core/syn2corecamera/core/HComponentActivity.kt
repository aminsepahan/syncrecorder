package com.syn2core.syn2corecamera.core

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class HComponentActivity : ComponentActivity() {

    companion object {
        lateinit var currentActivity: HComponentActivity
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        currentActivity = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentActivity = this
    }

    override fun onStart() {
        super.onStart()
        currentActivity = this
    }

    override fun onResume() {
        super.onResume()
        currentActivity = this
    }
}