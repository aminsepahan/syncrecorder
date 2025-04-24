package com.appleader707.syncrecorder.presentation.ui.recording

import com.appleader707.common.ui.base.BaseViewEvent
import com.appleader707.syncrecorder.presentation.components.settings_recording.RecordingSettingsState

/**
 *
 * @see BaseViewEvent
 */

sealed class RecordingViewEvent : BaseViewEvent {
    object ToggleRecording : RecordingViewEvent()
    object ShowSettings : RecordingViewEvent()
    object HideSettings : RecordingViewEvent()
    data class SaveSettings(val settings: RecordingSettingsState) : RecordingViewEvent()
}