package com.syn2core.syn2corecamera.service.camera

import android.view.Surface
import com.syn2core.syn2corecamera.business.usecase.directory.GetVideoFileUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.service.sensor.SensorService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CameraService @Inject constructor(
    private val getVideoFileUseCase: GetVideoFileUseCase,
    val camera2Recorder: Camera2Recorder,
    private val sensorService: SensorService,
) {
    private var currentVideoFile: File? = null
    private var segmentCount = 0
    private var recordingSettings: RecordingSettings? = null


    fun startPreview(surface: Surface) {
        camera2Recorder.startPreview(surface)
    }

    suspend fun startRecordingAndSensors(
        surface: Surface,
        recordingSettings: RecordingSettings,
    ) {
        segmentCount = 0
        this.recordingSettings = recordingSettings
        startNewSegment(surface, recordingSettings)
    }

    suspend fun stopRecordingAndSensors() {
        camera2Recorder.stopRecording()
        sensorService.stopSensors()
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
                    segmentNumber = segmentCount,
                    currentVideoFile = currentVideoFile!!
                )
            },
            segmentCount = segmentCount
        )

        return currentVideoFile!!.name
    }

    suspend fun switchToNewSegment(
        surface: Surface,
    ): Int {
        sensorService.stopSensors()
        camera2Recorder.stopRecording()
        startNewSegment(surface, recordingSettings!!)
        return segmentCount
    }
}