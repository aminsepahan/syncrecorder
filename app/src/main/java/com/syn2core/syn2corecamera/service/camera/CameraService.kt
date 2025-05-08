package com.syn2core.syn2corecamera.service.camera

import android.content.Context
import android.view.Surface
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.service.sensor.SensorService
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CameraService @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    private val camera2Recorder: Camera2Recorder,
    private val sensorService: SensorService
) {
    private var finalizeDeferred: CompletableDeferred<Unit>? = null

    suspend fun startRecordingAndSensors(
        context: Context,
        surface: Surface,
        recordingCount: Int,
        recordingSettings: RecordingSettings,
        finalizeVideo: () -> Unit
    ) {
        val directory = getSyn2CoreCameraDirectoryUseCase()
        val videoFile = File(directory, "recorded_data_${recordingCount}.mp4")

        finalizeDeferred = CompletableDeferred()

        val quality = recordingSettings.getQuality()
        val frameRate = recordingSettings.frameRate
        val autoFocus = recordingSettings.autoFocus
        val stabilization = recordingSettings.stabilization

        camera2Recorder.startRecording(
            surface = surface,
            outputFile = videoFile,
            settings = recordingSettings,
            onStartTimestamp = { timestamp ->
                Timber.tag(TAG).d("🎥 Recording start timestamp: $timestamp")
                sensorService.startSensors(
                    startTimeStamp = timestamp,
                    imuFrequency = recordingSettings.getImuSensorDelay()
                )
            },
            onFinalize = {
                Timber.tag(TAG).d("✅ Finalize callback called.")
                finalizeVideo()
                finalizeDeferred?.complete(Unit)
            }
        )
    }

    suspend fun stopRecordingAndWait() {
        Timber.tag(TAG).d("⏹️ Stopping recording...")
        camera2Recorder.stopRecording()
        finalizeDeferred?.await()
        Timber.tag(TAG).d("✅ Recording stopped and finalized.")
    }
}
