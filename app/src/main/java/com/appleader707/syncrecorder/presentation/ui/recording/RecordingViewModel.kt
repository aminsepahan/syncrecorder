package com.appleader707.syncrecorder.presentation.ui.recording

import android.content.Context
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.appleader707.common.ui.base.BaseViewModel
import com.appleader707.common.ui.livedata.SingleLiveData
import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.convert.EmbedSubtitleIntoVideoUseCase
import com.appleader707.syncrecorder.business.usecase.setting.GetRecordingSettingsUseCase
import com.appleader707.syncrecorder.business.usecase.setting.SetRecordingSettingsUseCase
import com.appleader707.syncrecorder.service.camera.CameraService
import com.appleader707.syncrecorder.service.durationmillis.DurationMillisService
import com.appleader707.syncrecorder.service.sensor.SensorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 *
 * @see BaseViewModel
 */

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val getRecordingSettingsUseCase: GetRecordingSettingsUseCase,
    private val setRecordingSettingsUseCase: SetRecordingSettingsUseCase,
    private val embedSubtitleIntoVideoUseCase: EmbedSubtitleIntoVideoUseCase,
    private val durationMillisService: DurationMillisService,
    private val sensorService: SensorService,
    private val cameraService: CameraService,
) : BaseViewModel<RecordingViewEvent>() {

    private val _state = MutableStateFlow(RecordingViewState())
    val state: StateFlow<RecordingViewState> = _state.asStateFlow()

    val effect = SingleLiveData<RecordingViewEffect>()

    private var autoRestartJob: Job? = null

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
        viewModelScope.launch {
            _state.value.recordingStartNanos = System.nanoTime()
            cameraService.startRecording(
                context = context,
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = surfaceProvider,
                recordingCount = _state.value.recordingCount,
                finalizeVideo = { }
            )

            sensorService.startSensors(
                context,
                _state.value.recordingStartNanos,
                _state.value.recordingCount
            )

            durationMillisService.start(viewModelScope) { newDuration ->
                updateState { it.copy(durationMillis = newDuration) }
            }

            startAutoRestartLoop(context, lifecycleOwner, surfaceProvider)

            updateState { it.copy(isRecording = true, durationMillis = 0L) }

            effect.postValue(RecordingViewEffect.RecordingStarted)
        }
    }

    fun stopAll() {
        viewModelScope.launch {
            autoRestartJob?.cancel()
            autoRestartJob = null
            durationMillisService.stop()

            val currentCount = _state.value.recordingCount
            sensorService.stopSensors(currentCount)
            cameraService.stopRecordingAndWait()
            saveSegementEmbedVideo(currentCount)

            updateState { it.copy(isRecording = false) }
            effect.postValue(RecordingViewEffect.RecordingStopped)
        }
    }

    private fun saveSegementEmbedVideo(recordingCount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                embedSubtitleIntoVideoUseCase(
                    videoNameFile = "recorded_data_${recordingCount}.mp4",
                    subtitleNameFile = "sensor_data_${recordingCount}.srt",
                    outputNameFile = "output_with_subtitles_${recordingCount}.mp4"
                )
                Timber.tag(TAG).d("✅ Subtitles embedded successfully for segment $recordingCount")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "❌ Failed to embed subtitles for segment $recordingCount")
            }
        }
    }

    private fun startAutoRestartLoop(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: Preview.SurfaceProvider
    ) {
        autoRestartJob?.cancel()
        autoRestartJob = viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(5 * 1000L)

                val currentCount = _state.value.recordingCount

                sensorService.stopSensors(currentCount)
                cameraService.stopRecordingAndWait()
                saveSegementEmbedVideo(currentCount)

                updateState { it.copy(recordingCount = it.recordingCount + 1) }

                _state.value.recordingStartNanos = System.nanoTime()

                cameraService.startRecording(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    surfaceProvider = surfaceProvider,
                    recordingCount = _state.value.recordingCount,
                    finalizeVideo = { }
                )

                sensorService.startSensors(
                    context,
                    _state.value.recordingStartNanos,
                    _state.value.recordingCount
                )
            }
        }
    }

    private fun updateState(update: (RecordingViewState) -> RecordingViewState) {
        _state.update(update)
    }

    fun clearEffect() {
        effect.postValue(RecordingViewEffect.DoNothing)
    }
}