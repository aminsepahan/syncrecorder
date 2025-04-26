package com.appleader707.syncrecorder.presentation.ui.recording

import android.content.Context
import androidx.camera.core.Preview
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import com.appleader707.common.ui.base.BaseViewModel
import com.appleader707.common.ui.livedata.SingleLiveData
import com.appleader707.syncrecorder.TAG
import com.appleader707.syncrecorder.business.usecase.setting.GetRecordingSettingsUseCase
import com.appleader707.syncrecorder.business.usecase.setting.SetRecordingSettingsUseCase
import com.appleader707.syncrecorder.service.camera.CameraService
import com.appleader707.syncrecorder.service.durationmillis.DurationMillisService
import com.appleader707.syncrecorder.service.sensor.SensorService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    private val durationMillisService: DurationMillisService,
    private val sensorService: SensorService,
    private val cameraService: CameraService
) : BaseViewModel<RecordingViewEvent>() {

    private val _state = MutableStateFlow(RecordingViewState())
    val state: StateFlow<RecordingViewState> = _state.asStateFlow()

    val effect = SingleLiveData<RecordingViewEffect>()

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
                recordingStartNanos = _state.value.recordingStartNanos,
                onRecordingStarted = {
                    Timber.tag(TAG).d("Recording Started Callback")
                },
                onRecordingFinished = {
                    Timber.tag(TAG).d("Recording Finished Callback")
                }
            )

            sensorService.startSensors(context, _state.value.recordingStartNanos)

            durationMillisService.start(viewModelScope) { newDuration ->
                updateState { it.copy(durationMillis = newDuration) }
            }

            updateState { it.copy(isRecording = true, durationMillis = 0L) }

            effect.postValue(RecordingViewEffect.RecordingStarted)
        }
    }

    fun stopAll() {
        viewModelScope.launch {
            cameraService.stopRecording()
            sensorService.stopSensors()
            durationMillisService.stop()
            updateState { it.copy(isRecording = false) }
            effect.postValue(RecordingViewEffect.RecordingStopped)
        }
    }

    private fun updateState(update: (RecordingViewState) -> RecordingViewState) {
        _state.update(update)
    }

    fun clearEffect() {
        effect.postValue(RecordingViewEffect.DoNothing)
    }
}