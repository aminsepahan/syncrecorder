package com.syn2core.syn2corecamera.presentation.ui.setting

import androidx.lifecycle.viewModelScope
import com.syn2core.common.ui.base.BaseViewModel
import com.syn2core.common.ui.livedata.SingleLiveData
import com.syn2core.syn2corecamera.business.usecase.setting.GetRecordingSettingsUseCase
import com.syn2core.syn2corecamera.business.usecase.setting.SetRecordingSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 *
 * @see BaseViewModel
 */

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val getRecordingSettingsUseCase: GetRecordingSettingsUseCase,
    private val setRecordingSettingsUseCase: SetRecordingSettingsUseCase
) : BaseViewModel<SettingViewEvent>() {

    private val _state = MutableStateFlow(SettingViewState())
    val state: StateFlow<SettingViewState> = _state.asStateFlow()

    val effect = SingleLiveData<SettingViewEffect>()

    override fun processEvent(event: SettingViewEvent) {
        when (event) {
            SettingViewEvent.LoadData -> {
                viewModelScope.launch {
                    val settingsState = getRecordingSettingsUseCase()
                    updateState { it.copy(settingsState = settingsState) }
                }
            }

            is SettingViewEvent.Save -> {
                viewModelScope.launch {
                    setRecordingSettingsUseCase(event.settings)
                    updateState {
                        it.copy(
                            settingsState = event.settings,
                        )
                    }
                    effect.postValue(SettingViewEffect.GoBackRecordingPage)
                }
            }

            SettingViewEvent.GoBackToRecordingPage -> {
                effect.postValue(SettingViewEffect.GoBackRecordingPage)
            }

        }
    }

    private fun updateState(update: (SettingViewState) -> SettingViewState) {
        _state.update(update)
    }

    fun clearEffect() {
        effect.postValue(SettingViewEffect.DoNothing)
    }
}