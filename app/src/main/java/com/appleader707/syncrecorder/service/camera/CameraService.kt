package com.appleader707.syncrecorder.service.camera

import android.content.Context
import androidx.camera.core.Preview
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CameraService @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val cameraController: CameraController
) {
    private var recording: Recording? = null

    suspend fun startRecording(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        recordingCount: Int,
        finalizeVideo: () -> Unit
    ) {
        val directoryRecord = getSyncRecorderDirectoryUseCase()
        val videoFile = File(directoryRecord, "recorded_data_${recordingCount}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        cameraController.initializeCamera(context, lifecycleOwner, surfaceProvider)

        val videoCapture = cameraController.getVideoCapture() ?: return

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Timber.tag("CameraService").d("Recording started")
                    }

                    is VideoRecordEvent.Finalize -> {
                        Timber.tag("CameraService").d("Recording finalized")
                        finalizeVideo()
                    }
                }
            }
    }

    fun stopRecording() {
        recording?.stop()
        recording = null
    }
}
