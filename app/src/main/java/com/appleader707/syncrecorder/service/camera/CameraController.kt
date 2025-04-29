package com.appleader707.syncrecorder.service.camera

import android.content.Context
import android.hardware.camera2.CaptureRequest
import android.util.Range
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.lifecycle.LifecycleOwner
import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.extension.await
import timber.log.Timber
import javax.inject.Inject

class CameraController @Inject constructor() {
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var videoCapture: VideoCapture<Recorder>? = null

    @OptIn(ExperimentalCamera2Interop::class)
    suspend fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        quality: Quality = Quality.HD,
        frameRate: Int = 30,
        autoFocus: Boolean = true,
        stabilization: Boolean = true,
    ) {
        cameraProvider = ProcessCameraProvider.getInstance(context).await()

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(quality))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        val previewBuilder = Preview.Builder().apply {
            val extender = Camera2Interop.Extender(this)


            val fpsRange = Range(frameRate, frameRate)
            try {
                extender.setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
            } catch (e: Exception) {
                Timber.tag(TAG).w("FPS $fpsRange not supported, falling back.")
            }

            extender.setCaptureRequestOption(
                CaptureRequest.CONTROL_AF_MODE,
                if (autoFocus)
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
                else
                    CaptureRequest.CONTROL_AF_MODE_OFF
            )

            if (stabilization) {
                extender.setCaptureRequestOption(
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,
                    CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON
                )
            }
        }

        preview = previewBuilder.build().also {
            it.surfaceProvider = surfaceProvider
        }

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider?.unbindAll()
            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
        } catch (exc: Exception) {
            Timber.tag(TAG).e(exc, "CameraX binding failed")
        }
    }

    fun getVideoCapture(): VideoCapture<Recorder>? = videoCapture
}
