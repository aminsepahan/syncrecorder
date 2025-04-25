package com.appleader707.syncrecorder.presentation.ui.recording

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.appleader707.common.ui.base.BaseViewModel
import com.appleader707.common.ui.livedata.SingleLiveData
import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.GetRecordingSettingsUseCase
import com.appleader707.syncrecorder.business.usecase.SetRecordingSettingsUseCase
import com.appleader707.syncrecorder.domain.SensorSnapshot
import com.appleader707.syncrecorder.extension.Helper
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.BufferedWriter
import java.io.File
import javax.inject.Inject

/**
 *
 * @see BaseViewModel
 */

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val getRecordingSettingsUseCase: GetRecordingSettingsUseCase,
    private val setRecordingSettingsUseCase: SetRecordingSettingsUseCase,
) : BaseViewModel<RecordingViewEvent>() {

    private val _state = MutableStateFlow(RecordingViewState())
    val state: StateFlow<RecordingViewState> = _state.asStateFlow()

    val effect = SingleLiveData<RecordingViewEffect>()

    private var recordingJob: Job? = null

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var sensorManager: SensorManager
    private val sensorData = mutableListOf<SensorSnapshot>()
    private var sensorWriter: BufferedWriter? = null

    private var recordedFile: File? = null
    fun getRecordedFile() = recordedFile

    private var recordingStartNanos: Long = 0
    fun getRecordingStartNanos() = recordingStartNanos
    fun getElapsedMillis(sensorNanos: Long): Long {
        return (sensorNanos - recordingStartNanos) / 1_000_000
    }

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val snapshot = SensorSnapshot(
                type = event.sensor.type,
                timestampNanos = (event.timestamp - recordingStartNanos),
                values = event.values.toList()
            )
            sensorData.add(snapshot)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun processEvent(event: RecordingViewEvent) {
        when (event) {
            is RecordingViewEvent.ToggleRecording -> {
                val isRecording = _state.value.isRecording
                if (isRecording) {
                    stopAll()
                } else {
                    startAll(event.context, event.lifecycleOwner, event.surfaceProvider)
                }
            }

            RecordingViewEvent.ShowSettings -> {
                updateState { it.copy(settingsDialogVisible = true) }
            }

            RecordingViewEvent.HideSettings -> {
                updateState { it.copy(settingsDialogVisible = false) }
            }

            is RecordingViewEvent.SaveSettings -> {
                viewModelScope.launch {
                    setRecordingSettingsUseCase(event.settings)
                    updateState {
                        it.copy(
                            settingsState = event.settings,
                            settingsDialogVisible = false
                        )
                    }
                }
            }

            RecordingViewEvent.LoadSettings -> {
                viewModelScope.launch {
                    val settingsState = getRecordingSettingsUseCase()
                    updateState { it.copy(settingsState = settingsState) }
                }
            }
        }
    }

    fun startAll(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        recordingStartNanos = System.nanoTime()
        startRecording(context, lifecycleOwner, surfaceProvider)
        startSensors(context)
        startDurationMillis()
        updateState { it.copy(isRecording = true, durationMillis = 0L) }
        effect.postValue(RecordingViewEffect.RecordingStarted)
    }

    fun stopAll() {
        stopRecording()
        stopSensors()
        stopDurationMillis()
        updateState { it.copy(isRecording = false) }
        effect.postValue(RecordingViewEffect.RecordingStopped)
    }

    private fun saveVideo() {
        val sensorFileJson = File(
            Helper.getSyncRecorderDir(),
            "sensor_data_${getRecordingStartNanos()}.jsonl"
        )
        val metadataFile = File(Helper.getSyncRecorderDir(), "sensor_data.srt")
        convertJsonToSRT(sensorFileJson, metadataFile)
        embedSubtitleIntoVideo()
    }

    private fun embedSubtitleIntoVideo() {
        val videoFile = File(Helper.getSyncRecorderDir(), "recorded_data.mp4")
        val subtitleFile = File(Helper.getSyncRecorderDir(), "sensor_data.srt")
        val outputFile = File(Helper.getSyncRecorderDir(), "output_with_subtitles.mp4")

        val cmd = listOf(
            "-y",
            "-i", videoFile.absolutePath,
            "-vf", "subtitles=${subtitleFile.absolutePath}",  // Burn subtitles
            "-c:a", "copy",
            outputFile.absolutePath
        )

        FFmpegKit.executeAsync(cmd.joinToString(" ")) { session ->
            val returnCode = session.returnCode
            if (ReturnCode.isSuccess(returnCode)) {
                Timber.tag(TAG).d("✅ Subtitle embedded successfully into video.")
            } else {
                Timber.tag(TAG).e("❌ FFmpeg failed: ${session.failStackTrace}")
            }
        }
    }

    fun convertJsonToSRT(jsonFile: File, outputSrt: File) {
        val json = jsonFile.readText()
        val data = Gson().fromJson(json, Array<SensorSnapshot>::class.java)

        if (data.isEmpty()) return

        val baseTime = data.first().timestampNanos / 1_000_000  // اولین timestamp

        outputSrt.bufferedWriter().use { out ->
            var index = 1
            var previousTime = 0L

            data.forEach { snapshot ->
                val currentTime = (snapshot.timestampNanos / 1_000_000) - baseTime
                val values = snapshot.values.joinToString(",") { "%.5f".format(it) }
                val type = when (snapshot.type) {
                    Sensor.TYPE_ACCELEROMETER -> "ACC"
                    Sensor.TYPE_GYROSCOPE -> "GYRO"
                    Sensor.TYPE_MAGNETIC_FIELD -> "MAG"
                    else -> "SENSOR_${snapshot.type}"
                }

                out.write("$index\n")
                out.write("${formatTime(previousTime)} --> ${formatTime(currentTime)}\n")
                out.write("$type: $values\n\n")

                previousTime = currentTime
                index++
            }
        }
    }

    fun formatTime(ms: Long): String {
        val hours = ms / 3600000
        val minutes = (ms % 3600000) / 60000
        val seconds = (ms % 60000) / 1000
        val millis = ms % 1000
        return "%02d:%02d:%02d,%03d".format(hours, minutes, seconds, millis)
    }

    /*fun convertJsonToFFMetadata(jsonFile: File, outputMetadata: File) {
        val json = jsonFile.readText()
        val data = Gson().fromJson(json, Array<SensorSnapshot>::class.java)

        outputMetadata.bufferedWriter().use { out ->
            out.write(";FFMETADATA1\n")

            out.write("[metadata]\n")
            out.write("title=Recorded Sensor Video\n")
            out.write("description=Video enriched with synchronized sensor data\n")
            out.write("encoder=SyncRecorder Android App\n")
            out.write("comment=Includes accelerometer and gyroscope data per frame\n\n")

            out.write("[sensor_data]\n")
            out.write("timebase=1/1000\n")

            data.forEach { snapshot ->
                val timeMs = snapshot.timestampNanos / 1_000_000
                val values = snapshot.values.joinToString(",") { "%.5f".format(it) }
                val type = when (snapshot.type) {
                    Sensor.TYPE_ACCELEROMETER -> "ACC"
                    Sensor.TYPE_GYROSCOPE -> "GYRO"
                    Sensor.TYPE_MAGNETIC_FIELD -> "MAG"
                    else -> "SENSOR_${snapshot.type}"
                }
                out.write("$timeMs=$type:$values\n")
            }
        }
    }*/

    private fun startRecording(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        val directoryRecord = Helper.getSyncRecorderDir()

        val videoFile = File(directoryRecord, "recorded_data.mp4")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HD))
                .build()

            videoCapture = VideoCapture.withOutput(recorder)

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = surfaceProvider
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture!!
                )

                recordedFile = videoFile
                val outputOptions = FileOutputOptions.Builder(videoFile).build()

                recording = videoCapture?.output
                    ?.prepareRecording(context, outputOptions)
                    ?.withAudioEnabled()
                    ?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                        when (recordEvent) {
                            is VideoRecordEvent.Start -> {
                                Timber.tag(TAG).d("Recoding Started")
                            }

                            is VideoRecordEvent.Finalize -> {
                                Timber.tag(TAG)
                                    .d("Recording File: ${recordEvent.outputResults.outputUri}")
                                saveVideo()
                            }
                        }
                    }

            } catch (exc: Exception) {
                Timber.tag(TAG).e(exc, "CameraX Error binding use cases")
            }

        }, ContextCompat.getMainExecutor(context))
    }

    private fun stopRecording() {
        recording?.stop()
        recording = null
    }

    fun startSensors(context: Context) {
        val sensorFile = File(
            Helper.getSyncRecorderDir(),
            "sensor_data_${getRecordingStartNanos()}.jsonl"
        )
        sensorWriter = sensorFile.bufferedWriter()

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        val sensors = listOf(
            Sensor.TYPE_ACCELEROMETER,
            Sensor.TYPE_GYROSCOPE,
            Sensor.TYPE_MAGNETIC_FIELD
        )

        sensors.forEach { type ->
            val sensor = sensorManager.getDefaultSensor(type)
            sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopSensors() {
        sensorManager.unregisterListener(sensorListener)
        convertDataSensorToJson()
    }

    private fun convertDataSensorToJson() {
        val json = Gson().toJson(sensorData)
        sensorWriter?.write(json)
        sensorWriter?.flush()
        sensorWriter?.close()
        sensorWriter = null
    }

    private fun startDurationMillis() {
        recordingJob = viewModelScope.launch {
            tickerFlow().collect {
                updateState {
                    val newDuration = it.durationMillis + 1000
                    it.copy(durationMillis = newDuration)
                }
            }
        }
    }

    private fun stopDurationMillis() {
        recordingJob?.cancel()
        recordingJob = null
    }

    private fun tickerFlow(): Flow<Unit> = flow {
        while (currentCoroutineContext().isActive) {
            emit(Unit)
            delay(1000L)
        }
    }

    private fun updateState(update: (RecordingViewState) -> RecordingViewState) {
        _state.update(update)
    }

    fun clearEffect() {
        effect.postValue(RecordingViewEffect.DoNothing)
    }
}