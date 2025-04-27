package com.appleader707.syncrecorder.service.camera

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.lifecycle.LifecycleOwner
import com.appleader707.syncrecorder.extension.await
import timber.log.Timber
import javax.inject.Inject

class CameraController @Inject constructor() {
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    suspend fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        quality: Quality = Quality.HD,
    ) {
        cameraProvider = ProcessCameraProvider.getInstance(context).await()

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(quality))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        preview = Preview.Builder().build().also {
            it.surfaceProvider = surfaceProvider
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture,
            )
        } catch (exc: Exception) {
            Timber.tag("CameraController").e(exc, "CameraX binding failed")
        }
    }

    fun getVideoCapture(): VideoCapture<Recorder>? = videoCapture
}
