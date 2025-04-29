package com.appleader707.syncrecorder.service.camera

import android.content.Context
import androidx.camera.core.Preview
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import com.appleader707.syncrecorder.domain.RecordingSettings
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CameraService @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val cameraController: CameraController
) {
    private var recording: Recording? = null
    private var onFinalizeCallback: (() -> Unit)? = null
    private var finalizeDeferred: CompletableDeferred<Unit>? = null

    suspend fun startRecording(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        recordingCount: Int,
        recordingSettings: RecordingSettings,
        finalizeVideo: () -> Unit
    ) {
        val quality = recordingSettings.getQuality()
        val frameRate = recordingSettings.frameRate
        val autoFocus = recordingSettings.autoFocus
        val stabilization = recordingSettings.stabilization

        val directoryRecord = getSyncRecorderDirectoryUseCase()
        val videoFile = File(directoryRecord, "recorded_data_${recordingCount}.mp4")
        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        cameraController.initializeCamera(
            context = context,
            lifecycleOwner = lifecycleOwner,
            surfaceProvider = surfaceProvider,
            quality = quality,
            frameRate = frameRate,
            autoFocus = autoFocus,
            stabilization = stabilization,
        )

        val videoCapture = cameraController.getVideoCapture() ?: return

        finalizeDeferred = CompletableDeferred()
        onFinalizeCallback = {
            Timber.tag(TAG).d("✅ Finalize callback called.")
            finalizeVideo()
            finalizeDeferred?.complete(Unit)
        }

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Timber.tag(TAG).d("Recording started")
                    }

                    is VideoRecordEvent.Finalize -> {
                        Timber.tag(TAG).d("Recording finalized")
                        onFinalizeCallback?.invoke()
                    }
                }
            }
    }

    suspend fun stopRecordingAndWait() {
        Timber.tag(TAG).d("⏹️ Stopping recording...")
        recording?.stop()
        recording = null
        finalizeDeferred?.await()
        Timber.tag(TAG).d("✅ Recording stopped and finalized.")
    }
}
