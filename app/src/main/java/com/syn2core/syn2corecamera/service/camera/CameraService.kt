package com.syn2core.syn2corecamera.service.camera

import android.view.Surface
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedDateUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedTimeUseCase
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
    private val getFormattedDateUseCase: GetFormattedDateUseCase,
    private val getFormattedTimeUseCase: GetFormattedTimeUseCase,
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
    ): String {
        val directory = getSyn2CoreCameraDirectoryUseCase()

        val date = getFormattedDateUseCase()
        val time = getFormattedTimeUseCase()
        val fileName = "s2c_${recordingCount}_${date}_${time}.mp4"
        val videoFile = File(directory, fileName)

        finalizeDeferred = CompletableDeferred()

        camera2Recorder.startRecording(
            surface = surface,
            outputFile = videoFile,
            settings = recordingSettings,
            onStartTimestamp = {
                sensorService.startSensors(
                    imuFrequency = recordingSettings.getImuSensorDelay()
                )
            },
            onFinalize = {
                finalizeVideo()
                finalizeDeferred?.complete(Unit)
            }
        )

        return fileName
    }

    suspend fun stopRecordingAndWait() {
        camera2Recorder.stopRecording()
        finalizeDeferred?.await()
    }
}
