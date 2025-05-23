package com.syn2core.syn2corecamera.service.camera

import android.view.Surface
import com.syn2core.syn2corecamera.business.usecase.directory.GetVideoFileUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.domain.SaveTask
import com.syn2core.syn2corecamera.service.save.SaveService
import com.syn2core.syn2corecamera.service.sensor.SensorService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraService @Inject constructor(
    private val getVideoFileUseCase: GetVideoFileUseCase,
    val camera2Recorder: Camera2Recorder,
    private val sensorService: SensorService,
    private val saveService: SaveService
) {
    private var currentVideoFile: File? = null
    private var segmentCount = 0
    private var recordingSettings: RecordingSettings? = null

    val pendingSaveTasks: StateFlow<Int> get() = saveService.queueSize

    fun startPreview(surface: Surface) {
        camera2Recorder.startPreview(surface)
    }

    suspend fun startRecordingAndSensors(
        surface: Surface,
        recordingSettings: RecordingSettings,
    ): String {
        this.recordingSettings = recordingSettings
        saveService.startService(CoroutineScope(Dispatchers.IO))
        return startNewSegment(surface, recordingSettings)
    }

    suspend fun stopRecordingAndSensors() {
        val stoppedFile = camera2Recorder.stopRecording()
        sensorService.stopSensors()
        camera2Recorder.finalizeDeferred?.await()

        stoppedFile?.name?.let { originalName ->
            saveService.addTask(SaveTask(videoName = originalName))
        }
    }

    private suspend fun startNewSegment(
        surface: Surface,
        recordingSettings: RecordingSettings,
    ): String {
        segmentCount++
        currentVideoFile = getVideoFileUseCase(segmentCount)

        camera2Recorder.startRecording(
            surface = surface,
            outputFile = currentVideoFile!!,
            settings = recordingSettings,
            onStartSensor = {
                sensorService.startSensors(
                    imuFrequency = recordingSettings.getImuSensorDelay(),
                    segmentNumber = segmentCount
                )
            },
        )

        return currentVideoFile!!.name
    }

    suspend fun switchToNewSegment(
        surface: Surface,
    ): String {
        val stoppedFile = camera2Recorder.stopRecording()

        sensorService.stopSensors()

        camera2Recorder.finalizeDeferred?.await()

        val fileName = stoppedFile?.name
        val recordingSettings = recordingSettings!!

        if (fileName != null) {
            saveService.addTask(
                SaveTask(videoName = fileName)
            )
        }

        return startNewSegment(surface, recordingSettings)
    }
}