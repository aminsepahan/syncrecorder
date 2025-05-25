package com.syn2core.syn2corecamera.presentation.ui.recording

import android.view.Surface
import androidx.lifecycle.viewModelScope
import com.syn2core.common.ui.base.BaseViewModel
import com.syn2core.common.ui.livedata.SingleLiveData
import com.syn2core.syn2corecamera.TAG
import com.syn2core.syn2corecamera.business.usecase.setting.GetRecordingSettingsUseCase
import com.syn2core.syn2corecamera.domain.RecordingSettings
import com.syn2core.syn2corecamera.service.camera.CameraService
import com.syn2core.syn2corecamera.service.durationmillis.DurationMillisService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
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
    private val cameraService: CameraService,
) : BaseViewModel<RecordingViewEvent>() {

    private val _state = MutableStateFlow(RecordingViewState())
    val state: StateFlow<RecordingViewState> = _state.asStateFlow()

    val effect = SingleLiveData<RecordingViewEffect>()

    private var cameraSurface: Surface? = null
    private var autoRestartJob: Job? = null
    private var segmentCount: Int = 0


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
        }
    }

    fun startAll() {
        viewModelScope.launch {
            val surface = cameraSurface
            if (surface == null) {
                Timber.tag(TAG).w("🚫 Cannot start recording: Surface is null")
                return@launch
            }

            val settings = getRecordingSettingsUseCase()

            cameraService.startRecordingAndSensors(
                surface = surface,
                recordingSettings = settings,
            )

            durationMillisService.start(viewModelScope) { newDuration ->
                updateState {
                    it.copy(
                        durationMillis = newDuration,
                    )
                }
            }

            startAutoRestartLoop(surface, settings)

            updateState { it.copy(isRecording = true, durationMillis = 0L) }
            effect.postValue(RecordingViewEffect.RecordingStarted)
        }
    }

    private fun stopAll() {
        viewModelScope.launch(Dispatchers.IO) {
            autoRestartJob?.cancel()
            autoRestartJob = null
            durationMillisService.stop()

            cameraService.stopRecordingAndSensors()

            updateState {
                it.copy(
                    isRecording = false,
                    isSaving = false,
                    durationMillis = 0
                )
            }

            effect.postValue(RecordingViewEffect.RecordingStopped)
        }
    }

    private fun startAutoRestartLoop(surface: Surface, settings: RecordingSettings) {
        autoRestartJob?.cancel()
        autoRestartJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay((settings.autoStopMinutes * 60 - 1) * 1000L)
                segmentCount = cameraService.switchToNewSegment(surface = surface)
                updateState { it.copy(segmentCount = segmentCount) }
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