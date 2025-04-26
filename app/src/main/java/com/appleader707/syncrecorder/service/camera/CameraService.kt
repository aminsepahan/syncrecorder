package com.appleader707.syncrecorder.service.camera

import android.content.Context
import androidx.camera.core.Preview
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.appleader707.syncrecorder.business.usecase.convert.ConvertJsonToSrtUseCase
import com.appleader707.syncrecorder.business.usecase.convert.EmbedSubtitleIntoVideoUseCase
import com.appleader707.syncrecorder.business.usecase.directory.GetSyncRecorderDirectoryUseCase
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class CameraService @Inject constructor(
    private val getSyncRecorderDirectoryUseCase: GetSyncRecorderDirectoryUseCase,
    private val convertJsonToSrtUseCase: ConvertJsonToSrtUseCase,
    private val embedSubtitleIntoVideoUseCase: EmbedSubtitleIntoVideoUseCase,
    private val cameraController: CameraController
) {
    private var recording: Recording? = null

    suspend fun startRecording(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider,
        recordingStartNanos: Long,
        onRecordingStarted: () -> Unit,
        onRecordingFinished: () -> Unit
    ) {
        val directoryRecord = getSyncRecorderDirectoryUseCase()
        val videoFile = File(directoryRecord, "recorded_data.mp4")

        cameraController.initializeCamera(context, lifecycleOwner, surfaceProvider)

        val outputOptions = FileOutputOptions.Builder(videoFile).build()

        val videoCapture = cameraController.getVideoCapture() ?: return

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                when (recordEvent) {
                    is VideoRecordEvent.Start -> {
                        Timber.tag("CameraService").d("Recording started")
                        onRecordingStarted()
                    }
                    is VideoRecordEvent.Finalize -> {
                        Timber.tag("CameraService").d("Recording finalized")
                        saveVideo(recordingStartNanos)
                        onRecordingFinished()
                    }
                }
            }
    }

    fun stopRecording() {
        recording?.stop()
        recording = null
    }

    private fun saveVideo(recordingStartNanos: Long) {
        convertJsonToSrtUseCase(
            sensorFileJsonName = "sensor_data_${recordingStartNanos}.jsonl",
            sensorFileSrtName = "sensor_data.srt"
        )
        embedSubtitleIntoVideoUseCase(
            videoNameFile = "recorder_data.mp4",
            subtitleNameFile = "sensor_data.srt",
            outputNameFile = "output_with_subtitles.mp4"
        )
    }
}
