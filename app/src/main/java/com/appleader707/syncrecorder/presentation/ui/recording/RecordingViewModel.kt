package com.appleader707.syncrecorder.presentation.ui.recording

import android.content.Context
import android.os.SystemClock
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.appleader707.common.ui.base.BaseViewModel
import com.appleader707.common.ui.livedata.SingleLiveData
import com.appleader707.syncrecorder.business.usecase.setting.GetRecordingSettingsUseCase
import com.appleader707.syncrecorder.business.usecase.setting.SetRecordingSettingsUseCase
import com.appleader707.syncrecorder.domain.SaveTask
import com.appleader707.syncrecorder.service.camera.CameraService
import com.appleader707.syncrecorder.service.durationmillis.DurationMillisService
import com.appleader707.syncrecorder.service.save.SaveService
import com.appleader707.syncrecorder.service.sensor.SensorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *
 * @see BaseViewModel
 */

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val getRecordingSettingsUseCase: GetRecordingSettingsUseCase,
    private val setRecordingSettingsUseCase: SetRecordingSettingsUseCase,
    private val durationMillisService: DurationMillisService,
    private val sensorService: SensorService,
    private val cameraService: CameraService,
    private val saveService: SaveService,
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
            _state.value.recordingStartNanos = SystemClock.elapsedRealtimeNanos()
            val recordingSettings = getRecordingSettingsUseCase()
            cameraService.startRecording(
                context = context,
                lifecycleOwner = lifecycleOwner,
                surfaceProvider = surfaceProvider,
                recordingCount = _state.value.recordingCount,
                recordingSettings = recordingSettings,
                finalizeVideo = { }
            )

            sensorService.startSensors(
                recordingCount = _state.value.recordingCount,
                recordingStartNanos = _state.value.recordingStartNanos,
                imuFrequency = recordingSettings.getImuSensorDelay()
            )

            durationMillisService.start(viewModelScope) { newDuration ->
                updateState { it.copy(durationMillis = newDuration) }
            }

            saveService.startService(viewModelScope)

            startAutoRestartLoop(context, lifecycleOwner, surfaceProvider)

            updateState { it.copy(isRecording = true, durationMillis = 0L) }

            effect.postValue(RecordingViewEffect.RecordingStarted)
        }
    }

    fun stopAll() {
        viewModelScope.launch {
            val currentCount = _state.value.recordingCount

            sensorService.stopSensors(currentCount)
            cameraService.stopRecordingAndWait()

            saveService.addTask(
                SaveTask(
                    videoName = "recorded_data_${currentCount}.mp4",
                    outputName = "output_with_subtitles_${currentCount}.mp4",
                    recordingCount = currentCount
                )
            )

            autoRestartJob?.cancel()
            autoRestartJob = null
            durationMillisService.stop()

            updateState { it.copy(isRecording = false) }
            effect.postValue(RecordingViewEffect.RecordingStopped)
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
                delay(30 * 60 * 1000L)

                val currentCount = _state.value.recordingCount

                sensorService.stopSensors(currentCount)
                cameraService.stopRecordingAndWait()

                saveService.addTask(
                    SaveTask(
                        videoName = "recorded_data_${currentCount}.mp4",
                        outputName = "output_with_subtitles_${currentCount}.mp4",
                        recordingCount = currentCount
                    )
                )

                updateState { it.copy(recordingCount = it.recordingCount + 1) }
                _state.value.recordingStartNanos = System.nanoTime()

                val recordingSettings = getRecordingSettingsUseCase()
                cameraService.startRecording(
                    context = context,
                    lifecycleOwner = lifecycleOwner,
                    surfaceProvider = surfaceProvider,
                    recordingCount = _state.value.recordingCount,
                    recordingSettings = recordingSettings,
                    finalizeVideo = { }
                )

                sensorService.startSensors(
                    recordingCount = _state.value.recordingCount,
                    recordingStartNanos = _state.value.recordingStartNanos,
                    imuFrequency = recordingSettings.imuFrequency
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