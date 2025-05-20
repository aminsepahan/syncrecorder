package com.syn2core.syn2corecamera.presentation.ui.recording

import android.view.Surface
import androidx.lifecycle.viewModelScope
import com.syn2core.common.ui.base.BaseViewModel
import com.syn2core.common.ui.livedata.SingleLiveData
import com.syn2core.syn2corecamera.business.usecase.setting.GetRecordingSettingsUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.domain.SaveTask
import com.syn2core.syn2corecamera.service.camera.CameraService
import com.syn2core.syn2corecamera.service.durationmillis.DurationMillisService
import com.syn2core.syn2corecamera.service.save.SaveService
import com.syn2core.syn2corecamera.service.sensor.SensorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
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
    private val durationMillisService: DurationMillisService,
    private val sensorService: SensorService,
    private val cameraService: CameraService,
    private val saveService: SaveService,
) : BaseViewModel<RecordingViewEvent>() {

    private val _state = MutableStateFlow(RecordingViewState())
    val state: StateFlow<RecordingViewState> = _state.asStateFlow()

    val effect = SingleLiveData<RecordingViewEffect>()

    private var cameraSurface: Surface? = null
    private var autoRestartJob: Job? = null

    private var recordingVideoName: String = ""

    fun updateSurface(surface: Surface) {
        cameraSurface = surface
        cameraService.startPreview(surface)
    }

    fun getSurface(): Surface? = cameraSurface

    override fun processEvent(event: RecordingViewEvent) {
        when (event) {
            is RecordingViewEvent.ToggleRecording -> {
                if (_state.value.isRecording) stopAll()
                else startAll()
            }

            RecordingViewEvent.LoadSettings -> {
                viewModelScope.launch {
                    val settingsState = getRecordingSettingsUseCase()
                    updateState { it.copy(settingsState = settingsState) }
                }
            }

            RecordingViewEvent.NavigateToSettings -> {
                if (!_state.value.isRecording) {
                    effect.postValue(RecordingViewEffect.NavigateToSetting)
                }
            }

            RecordingViewEvent.NavigateToShowByChart -> {
                if (!_state.value.isRecording) {
                    effect.postValue(RecordingViewEffect.NavigateToShowByChart)
                }
            }
        }
    }

    fun startAll() {
        viewModelScope.launch {
            val surface = cameraSurface
            if (surface == null) {
                Timber.w("🚫 Cannot start recording: Surface is null")
                return@launch
            }

            val settings = getRecordingSettingsUseCase()

            val videoFileName = cameraService.startRecordingAndSensors(
                surface = surface,
                recordingCount = _state.value.recordingCount,
                recordingSettings = settings,
                finalizeVideo = {}
            )
            recordingVideoName = videoFileName

            durationMillisService.start(viewModelScope) { newDuration ->
                updateState { it.copy(durationMillis = newDuration) }
            }

            saveService.startService(viewModelScope)
            startAutoRestartLoop(surface, settings)

            updateState { it.copy(isRecording = true, durationMillis = 0L) }
            effect.postValue(RecordingViewEffect.RecordingStarted)
        }
    }

    private fun stopAll() {
        viewModelScope.launch {
            autoRestartJob?.cancel()
            autoRestartJob = null
            durationMillisService.stop()

            updateState { it.copy(isSaving = true) }

            val currentCount = _state.value.recordingCount

            cameraService.stopRecordingAndWait()
            sensorService.stopSensors(currentCount)

            val originalName = recordingVideoName
            val embeddedName = originalName.replace("s2c_", "s2c_embedded_")
            saveService.addTask(
                SaveTask(
                    videoName = originalName,
                    outputName = embeddedName,
                    recordingCount = currentCount
                ),
                onDone = { updateState { it.copy(isSaving = false) } }
            )

            updateState {
                it.copy(isRecording = false, recordingCount = it.recordingCount + 1)
            }

            effect.postValue(RecordingViewEffect.RecordingStopped)
        }
    }

    private fun startAutoRestartLoop(surface: Surface, settings: RecordingSettings) {
        autoRestartJob?.cancel()

        cameraSurface?.let {
            autoRestartJob = viewModelScope.launch {
                while (isActive) {
                    delay(settings.autoStopMinutes * 60 * 1000L)

                    val count = _state.value.recordingCount

                    cameraService.stopRecordingAndWait()
                    sensorService.stopSensors(count)

                    val originalName = recordingVideoName
                    val embeddedName = originalName.replace("s2c_", "s2c_embedded_")
                    saveService.addTask(
                        SaveTask(
                            videoName = originalName,
                            outputName = embeddedName,
                            recordingCount = count
                        ),
                        onDone = { updateState { it.copy(isSaving = false) } }
                    )

                    updateState { it.copy(recordingCount = it.recordingCount + 1) }

                    val newSettings = getRecordingSettingsUseCase()

                    cameraService.startRecordingAndSensors(
                        surface = surface,
                        recordingCount = _state.value.recordingCount,
                        recordingSettings = newSettings,
                        finalizeVideo = {}
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