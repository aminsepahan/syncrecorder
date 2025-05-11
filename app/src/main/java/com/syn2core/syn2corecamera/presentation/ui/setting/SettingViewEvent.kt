package com.syn2core.syn2corecamera.presentation.ui.setting

import com.syn2core.common.ui.base.BaseViewEvent
import com.syn2core.syn2corecamera.domain.RecordingSettings

/**
 *
 * @see BaseViewEvent
 */

sealed class SettingViewEvent : BaseViewEvent {
    data object GoBackToRecordingPage: SettingViewEvent()
    data object LoadData: SettingViewEvent()
    data class Save(val settings: RecordingSettings): SettingViewEvent()
}