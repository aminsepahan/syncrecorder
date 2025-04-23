package com.appleader707.syncrecorder.presentation.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.appleader707.syncrecorder.core.HComponentActivity
import com.appleader707.syncrecorder.extension.Helper
import com.appleader707.syncrecorder.presentation.theme.SyncRecorderTheme
import dagger.hilt.android.AndroidEntryPoint
import com.appleader707.syncrecorder.R

@AndroidEntryPoint
class MainActivity : HComponentActivity() {

    private var backPressed = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SyncRecorderTheme {
                MainScreen(finish = finish)
            }
        }
    }

    private val finish: () -> Unit = {
        if (backPressed + 2000 > System.currentTimeMillis()) {
            finishAndRemoveTask()
        } else {
            Helper.showMessage(getString(R.string.back_exit_app))
        }
        backPressed = System.currentTimeMillis()
    }
}