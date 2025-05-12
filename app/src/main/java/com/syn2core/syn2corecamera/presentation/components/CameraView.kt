package com.syn2core.syn2corecamera.presentation.components

import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    resolution: Pair<Int, Int>,
    onSurfaceReady: (Surface) -> Unit
) {
    AndroidView(
        factory = { context ->
            val surfaceView = SurfaceView(context)
            val (width, height) = resolution

            surfaceView.layoutParams = ViewGroup.LayoutParams(width, height)

            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    onSurfaceReady(holder.surface)
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {}

                override fun surfaceDestroyed(holder: SurfaceHolder) {}
            })
            surfaceView
        },
        modifier = modifier
    )
}