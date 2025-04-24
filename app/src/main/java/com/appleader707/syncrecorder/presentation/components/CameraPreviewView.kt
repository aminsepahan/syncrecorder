package com.appleader707.syncrecorder.presentation.components

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.appleader707.syncrecorder.TAG
import timber.log.Timber

@Composable
fun CameraPreviewView(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { previewView },
        modifier = modifier,
        update = { view ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = view.surfaceProvider
                }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (e: Exception) {
                    Timber.tag(TAG).e(e, "Use case binding failed")
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}
