package com.syn2core.syn2corecamera.service.camera

import android.view.Surface
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.service.sensor.SensorService
import kotlinx.coroutines.CompletableDeferred
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraService @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    private val camera2Recorder: Camera2Recorder,
    private val sensorService: SensorService
) {
    private var finalizeDeferred: CompletableDeferred<Unit>? = null

    fun startPreview(surface: Surface) {
        Timber.d("🔍 Starting camera preview")
        camera2Recorder.startPreview(surface)
    }

    suspend fun startRecordingAndSensors(
        surface: Surface,
        recordingCount: Int,
        recordingSettings: RecordingSettings,
        finalizeVideo: () -> Unit
    ) {
        Timber.d("🎬 Starting recording with count $recordingCount")
        val directory = getSyn2CoreCameraDirectoryUseCase()
        val videoFile = File(directory, "recorded_data_${recordingCount}.mp4")

        finalizeDeferred = CompletableDeferred()

        camera2Recorder.startRecording(
            surface = surface,
            outputFile = videoFile,
            settings = recordingSettings,
            onStartTimestamp = {
                Timber.d("⏱️ Sensor recording started")
                sensorService.startSensors(
                    imuFrequency = recordingSettings.getImuSensorDelay()
                )
            },
            onFinalize = {
                Timber.d("📦 Finalizing recording")
                finalizeVideo()
                finalizeDeferred?.complete(Unit)
            }
        )
    }

    suspend fun stopRecordingAndWait() {
        Timber.d("🛑 Stopping recording...")
        camera2Recorder.stopRecording()
        finalizeDeferred?.await()
        Timber.d("✅ Recording stopped and finalized")
    }
}
