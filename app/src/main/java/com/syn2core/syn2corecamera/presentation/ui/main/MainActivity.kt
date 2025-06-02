package com.syn2core.syn2corecamera.presentation.ui.main

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.syn2core.syn2corecamera.core.HComponentActivity
import com.syn2core.syn2corecamera.presentation.theme.Syn2CoreCameraTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : HComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContent {
            Syn2CoreCameraTheme {
                MainScreen(finish = finish)
            }
        }
    }

    private val finish: () -> Unit = {
        finishAffinity()
    }
}