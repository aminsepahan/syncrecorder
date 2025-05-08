package com.syn2core.syn2corecamera.presentation.components

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@Suppress("DEPRECATION")
@Composable
fun CameraView(
    modifier: Modifier = Modifier,
    resolution: Pair<Int, Int>,
    onSurfaceReady: (Surface) -> Unit
) {
    AndroidView(
        factory = { context ->
            val textureView = TextureView(context)

            textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                    configureTransform(textureView, width, height, resolution)
                    onSurfaceReady(Surface(surface))
                }

                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
            }

            textureView.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            textureView
        },
        modifier = modifier
    )
}

private fun configureTransform(view: TextureView, viewWidth: Int, viewHeight: Int, resolution: Pair<Int, Int>) {
    val matrix = Matrix()

    val (previewWidth, previewHeight) = resolution

    matrix.postRotate(270f, viewWidth / 2f, viewHeight / 2f)

    val scaleX = viewHeight.toFloat() / previewHeight.toFloat()
    val scaleY = viewWidth.toFloat() / previewWidth.toFloat()

    val scale = maxOf(scaleX, scaleY)

    matrix.postScale(scale, scale, viewWidth / 2f, viewHeight / 2f)

    view.setTransform(matrix)
}