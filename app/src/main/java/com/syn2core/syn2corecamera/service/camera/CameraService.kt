package com.syn2core.syn2corecamera.service.camera

import android.view.Surface
import com.syn2core.syn2corecamera.business.usecase.directory.GetSyn2CoreCameraDirectoryUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedDateUseCase
import com.syn2core.syn2corecamera.business.usecase.time.GetFormattedTimeUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.domain.SaveTask
import com.syn2core.syn2corecamera.service.save.SaveService
import com.syn2core.syn2corecamera.service.sensor.SensorService
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraService @Inject constructor(
    private val getSyn2CoreCameraDirectoryUseCase: GetSyn2CoreCameraDirectoryUseCase,
    private val getFormattedDateUseCase: GetFormattedDateUseCase,
    private val getFormattedTimeUseCase: GetFormattedTimeUseCase,
    val camera2Recorder: Camera2Recorder,
    private val sensorService: SensorService,
    private val saveService: SaveService
) {
    private var currentVideoFile: File? = null
    private var segmentCount = 0
    private var recordingSettings: RecordingSettings? = null

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
        this.recordingSettings = recordingSettings
        segmentCount = 0
        return startNewSegment(surface, recordingCount, recordingSettings, finalizeVideo)
    }

    fun stopAndGetCurrentVideoFile(): File? {
        return camera2Recorder.stopRecording()
    }

    private suspend fun startNewSegment(
        surface: Surface,
        recordingCount: Int,
        recordingSettings: RecordingSettings,
        finalizeVideo: () -> Unit
    ): String {
        val directory = getSyn2CoreCameraDirectoryUseCase()
        val date = getFormattedDateUseCase()
        val time = getFormattedTimeUseCase()
        val fileName = "s2c_${recordingCount}_${segmentCount}_${date}_${time}.mp4"
        val videoFile = File(directory, fileName)
        currentVideoFile = videoFile

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
            }
        )

        segmentCount++
        return fileName
    }

    suspend fun switchToNewSegment(
        surface: Surface,
        recordingCount: Int,
        finalizeVideo: () -> Unit
    ): String {
        val stoppedFile = camera2Recorder.stopRecording()

        camera2Recorder.finalizeDeferred?.await()

        val fileName = stoppedFile?.name
        val recordingSettings = recordingSettings!!

        if (fileName != null) {
            val embeddedName = fileName.replace("s2c_", "s2c_embedded_")
            saveService.addTask(
                SaveTask(
                    videoName = fileName,
                    outputName = embeddedName,
                    recordingCount = recordingCount
                )
            )
        }

        return startNewSegment(surface, recordingCount, recordingSettings, finalizeVideo)
    }
}