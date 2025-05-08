package com.syn2core.syn2corecamera.presentation.ui.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.syn2core.syn2corecamera.R
import com.syn2core.syn2corecamera.core.HComponentActivity
import com.syn2core.syn2corecamera.extension.Helper
import com.syn2core.syn2corecamera.presentation.theme.Syn2CoreCameraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : HComponentActivity() {

    private var backPressed = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Syn2CoreCameraTheme {
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