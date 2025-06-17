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
        videoDirectory: String,
    ): File {
        segmentCount = 0
        this.recordingSettings = recordingSettings
        return startNewSegment(
            surface = surface,
            recordingSettings = recordingSettings,
            videoDirectory = videoDirectory
        )
    }

    suspend fun stopRecordingAndSensors() {
        camera2Recorder.stopRecording()
        sensorService.stopSensors()
    }

    private suspend fun startNewSegment(
        surface: Surface,
        recordingSettings: RecordingSettings,
        videoDirectory: String,
    ): File {
        segmentCount++
        currentVideoFile = getVideoFileUseCase(
            segmentCount = segmentCount,
            videoDirectory = videoDirectory
        )

        camera2Recorder.startRecording(
            surface = surface,
            outputFile = currentVideoFile!!,
            settings = recordingSettings,
            onStartSensor = {
                sensorService.startSensors(
                    imuFrequency = recordingSettings.getImuSensorDelay(),
                    currentVideoFile = currentVideoFile!!
                )
            }
        )

        return currentVideoFile!!
    }

    suspend fun switchToNewSegment(
        surface: Surface,
        videoDirectory: String,
    ): Int {
        sensorService.stopSensors()
        camera2Recorder.stopRecording()
        startNewSegment(
            surface = surface,
            recordingSettings = recordingSettings!!,
            videoDirectory = videoDirectory
        )
        return segmentCount
    }

    fun stopCamera() {
        camera2Recorder.stopCamera()
    }

    suspend fun startStreaming(
        previewSurface: Surface,
        webRtcSurface: Surface,
        settings: RecordingSettings
    ) {

        camera2Recorder.startStreaming(
            previewSurface = previewSurface,
            webRtcSurface = webRtcSurface,
            settings = settings,
        )
    }

    suspend fun stopStreaming() {
        camera2Recorder.stopStreaming()

        sensorService.stopSensors()
    }
}