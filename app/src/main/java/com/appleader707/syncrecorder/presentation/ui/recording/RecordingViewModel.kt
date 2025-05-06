package com.appleader707.syncrecorder.presentation.ui.recording

import android.content.Context
import android.view.Surface
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

    private var cameraSurface: Surface? = null

    fun updateSurface(surface: Surface) { cameraSurface = surface }
    fun getSurface(): Surface? = cameraSurface

    private var autoRestartJob: Job? = null

    override fun processEvent(event: RecordingViewEvent) {
        when (event) {
            is RecordingViewEvent.ToggleRecording -> {
                val isRecording = _state.value.isRecording
                if (isRecording) {
                    stopAll()
                } else {
                    startAll(event.context)
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

            RecordingViewEvent.NavigateToShowByChart -> {
                effect.postValue(RecordingViewEffect.NavigateToShowByChart)
            }
        }
    }

    fun startAll(
        context: Context,
    ) {
        viewModelScope.launch {
            cameraSurface?.let { surface ->
                val recordingSettings = getRecordingSettingsUseCase()

                cameraService.startRecordingAndSensors(
                    context = context,
                    surface = surface,
                    recordingCount = _state.value.recordingCount,
                    recordingSettings = recordingSettings,
                    finalizeVideo = { }
                )

                durationMillisService.start(viewModelScope) { newDuration ->
                    updateState { it.copy(durationMillis = newDuration) }
                }

                saveService.startService(viewModelScope)

                startAutoRestartLoop(context)

                updateState { it.copy(isRecording = true, durationMillis = 0L) }

                effect.postValue(RecordingViewEffect.RecordingStarted)
            }
        }
    }

    fun stopAll() {
        viewModelScope.launch {
            val currentCount = _state.value.recordingCount

            cameraService.stopRecordingAndWait()
            sensorService.stopSensors(currentCount)

            saveService.addTask(
                SaveTask(
                    videoName = "recorded_data_${currentCount}.mp4",
                    outputName = "output_with_subtitle_${currentCount}.mp4",
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
    ) {
        cameraSurface?.let { surface ->
            autoRestartJob?.cancel()
            autoRestartJob = viewModelScope.launch {
                while (currentCoroutineContext().isActive) {
                    delay(30 * 60 * 1000L)

                    val currentCount = _state.value.recordingCount

                    cameraService.stopRecordingAndWait()
                    sensorService.stopSensors(currentCount)

                    saveService.addTask(
                        SaveTask(
                            videoName = "recorded_data_${currentCount}.mp4",
                            outputName = "output_with_subtitle_${currentCount}.mp4",
                            recordingCount = currentCount
                        )
                    )

                    updateState { it.copy(recordingCount = it.recordingCount + 1) }

                    val recordingSettings = getRecordingSettingsUseCase()
                    cameraService.startRecordingAndSensors(
                        context = context,
                        surface = surface,
                        recordingCount = _state.value.recordingCount,
                        recordingSettings = recordingSettings,
                        finalizeVideo = { }
                    )
                }
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